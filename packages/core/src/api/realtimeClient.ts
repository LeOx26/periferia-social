import { getConfig } from '../config'
import { likeEventSchema } from '../domain/schemas'
import type { LikeEvent } from '../domain/types'

export type RealtimeStatus = 'idle' | 'connecting' | 'open' | 'closed'

export interface RealtimeClientOptions {
  getToken(): string | null
  onEvent(event: LikeEvent): void
  onStatusChange?(status: RealtimeStatus): void
}

export interface RealtimeClient {
  connect(): void
  disconnect(): void
}

const FIRST_RETRY_MS = 1_000
const MAX_RETRY_MS = 30_000

/**
 * WebSocket nativo, sin STOMP ni SockJS. El mismo código funciona en navegador y
 * en React Native, que es justamente lo que permite compartir este fichero.
 */
export function createRealtimeClient(options: RealtimeClientOptions): RealtimeClient {
  let socket: WebSocket | null = null
  let retryDelay = FIRST_RETRY_MS
  let retryTimer: ReturnType<typeof setTimeout> | null = null
  let stopped = false

  const setStatus = (status: RealtimeStatus) => options.onStatusChange?.(status)

  function open() {
    const token = options.getToken()
    // Sin token no hay nada que escuchar: el handshake sería rechazado igualmente.
    if (!token) return

    setStatus('connecting')
    // El token va en el query param porque el WebSocket del navegador no admite
    // cabeceras en el handshake. El trade-off está documentado en el README.
    socket = new WebSocket(`${getConfig().wsBaseUrl}/ws/likes?token=${token}`)

    socket.onopen = () => {
      retryDelay = FIRST_RETRY_MS
      setStatus('open')
    }

    socket.onmessage = (event) => {
      try {
        const parsed = likeEventSchema.safeParse(JSON.parse(String(event.data)))
        if (parsed.success) options.onEvent(parsed.data)
      } catch {
        // Un mensaje mal formado no puede tumbar la conexión ni la interfaz.
      }
    }

    socket.onclose = () => {
      setStatus('closed')
      scheduleRetry()
    }

    socket.onerror = () => {
      socket?.close()
    }
  }

  function scheduleRetry() {
    // Tras un disconnect() explícito no se reintenta. Sin esta guarda, desmontar
    // el componente dejaría sockets zombis reconectándose para siempre.
    if (stopped) return

    retryTimer = setTimeout(() => {
      retryDelay = Math.min(retryDelay * 2, MAX_RETRY_MS)
      open()
    }, retryDelay)
  }

  return {
    connect() {
      stopped = false
      open()
    },
    disconnect() {
      stopped = true
      if (retryTimer) clearTimeout(retryTimer)
      retryTimer = null
      socket?.close()
      socket = null
      setStatus('idle')
    },
  }
}
