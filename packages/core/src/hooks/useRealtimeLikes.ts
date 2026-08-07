import { useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { createRealtimeClient, type RealtimeStatus } from '../api/realtimeClient'
import type { FeedPage } from '../domain/types'
import { useAuthStore } from '../store/authStore'
import { applyLikeEvent } from './feedCache'
import { queryKeys } from './queryKeys'

/**
 * Conecta al WebSocket de likes y escribe el contador autoritativo en el cache
 * de TanStack Query. Convive con el update optimista de useLikePost porque el
 * evento del servidor siempre gana.
 *
 * Devuelve el estado de la conexión para que la interfaz pueda mostrarlo.
 */
export function useRealtimeLikes(page = 0, size = 20): RealtimeStatus {
  const token = useAuthStore((state) => state.token)
  const queryClient = useQueryClient()
  const [status, setStatus] = useState<RealtimeStatus>('idle')

  useEffect(() => {
    if (!token) return

    const key = queryKeys.feed(page, size)
    const client = createRealtimeClient({
      getToken: () => token,
      onStatusChange: setStatus,
      onEvent: (event) => {
        queryClient.setQueryData<FeedPage | undefined>(key, (current) =>
          applyLikeEvent(current, event),
        )
      },
    })

    client.connect()
    // Imprescindible: sin esta limpieza, cada montaje deja un socket vivo
    // reconectándose, y en React 19 con StrictMode se monta dos veces en desarrollo.
    return () => client.disconnect()
  }, [token, page, size, queryClient])

  return status
}
