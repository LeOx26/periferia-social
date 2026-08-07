import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { configureCore, resetConfig } from '../config'
import { createRealtimeClient } from './realtimeClient'

class FakeWebSocket {
  static instances: FakeWebSocket[] = []
  onopen: (() => void) | null = null
  onclose: (() => void) | null = null
  onerror: (() => void) | null = null
  onmessage: ((e: { data: string }) => void) | null = null
  closed = false

  constructor(readonly url: string) {
    FakeWebSocket.instances.push(this)
  }

  close() {
    this.closed = true
    this.onclose?.()
  }
}

const storage = {
  getItem: async () => null,
  setItem: async () => {},
  removeItem: async () => {},
}

beforeEach(() => {
  vi.useFakeTimers()
  FakeWebSocket.instances = []
  vi.stubGlobal('WebSocket', FakeWebSocket)
  resetConfig()
  configureCore({
    authBaseUrl: 'http://localhost:8081',
    socialBaseUrl: 'http://localhost:8082',
    wsBaseUrl: 'ws://localhost:8082',
    storage,
  })
})

afterEach(() => {
  vi.useRealTimers()
  vi.unstubAllGlobals()
})

describe('createRealtimeClient', () => {
  it('puts the token in the query string because browsers cannot set handshake headers', () => {
    createRealtimeClient({ getToken: () => 'mi.token', onEvent: () => {} }).connect()

    expect(FakeWebSocket.instances[0]!.url).toBe('ws://localhost:8082/ws/likes?token=mi.token')
  })

  it('does not connect at all when there is no token', () => {
    createRealtimeClient({ getToken: () => null, onEvent: () => {} }).connect()

    expect(FakeWebSocket.instances).toHaveLength(0)
  })

  it('delivers a valid like event to the listener', () => {
    const onEvent = vi.fn()
    createRealtimeClient({ getToken: () => 't', onEvent }).connect()

    FakeWebSocket.instances[0]!.onmessage?.({
      data: JSON.stringify({ type: 'LIKE_UPDATED', postId: 'p1', likeCount: 4 }),
    })

    expect(onEvent).toHaveBeenCalledWith({ type: 'LIKE_UPDATED', postId: 'p1', likeCount: 4 })
  })

  it('ignores malformed messages instead of crashing the listener', () => {
    const onEvent = vi.fn()
    createRealtimeClient({ getToken: () => 't', onEvent }).connect()

    FakeWebSocket.instances[0]!.onmessage?.({ data: 'esto no es json' })
    FakeWebSocket.instances[0]!.onmessage?.({ data: JSON.stringify({ type: 'OTRA_COSA' }) })

    expect(onEvent).not.toHaveBeenCalled()
  })

  it('reconnects after an unexpected close', () => {
    createRealtimeClient({ getToken: () => 't', onEvent: () => {} }).connect()

    FakeWebSocket.instances[0]!.onclose?.()
    vi.advanceTimersByTime(1000)

    expect(FakeWebSocket.instances).toHaveLength(2)
  })

  it('backs off exponentially instead of hammering the server', () => {
    createRealtimeClient({ getToken: () => 't', onEvent: () => {} }).connect()

    FakeWebSocket.instances[0]!.onclose?.()
    vi.advanceTimersByTime(1000)
    expect(FakeWebSocket.instances).toHaveLength(2)

    FakeWebSocket.instances[1]!.onclose?.()
    vi.advanceTimersByTime(1000)
    expect(FakeWebSocket.instances).toHaveLength(2)

    vi.advanceTimersByTime(1000)
    expect(FakeWebSocket.instances).toHaveLength(3)
  })

  it('does not reconnect after an explicit disconnect', () => {
    const client = createRealtimeClient({ getToken: () => 't', onEvent: () => {} })
    client.connect()

    client.disconnect()
    vi.advanceTimersByTime(30_000)

    expect(FakeWebSocket.instances).toHaveLength(1)
  })
})
