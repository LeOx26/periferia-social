import { beforeEach, describe, expect, it } from 'vitest'
import { configureCore, resetConfig, type CoreStorage } from '../config'
import { useAuthStore } from './authStore'

function memoryStorage(
  initial: Record<string, string> = {},
): CoreStorage & { data: Record<string, string> } {
  const data = { ...initial }
  return {
    data,
    getItem: async (key) => data[key] ?? null,
    setItem: async (key, value) => {
      data[key] = value
    },
    removeItem: async (key) => {
      delete data[key]
    },
  }
}

const session = {
  accessToken: 'un.token.jwt',
  expiresIn: 3600,
  user: { id: 'u1', alias: 'leo', firstName: 'Leonel', lastName: 'Benítez' },
}

function setUp(storage: CoreStorage) {
  resetConfig()
  configureCore({
    authBaseUrl: 'http://localhost:8081',
    socialBaseUrl: 'http://localhost:8082',
    wsBaseUrl: 'ws://localhost:8082',
    storage,
  })
  useAuthStore.setState({ token: null, user: null, status: 'unknown' })
}

describe('useAuthStore', () => {
  beforeEach(() => setUp(memoryStorage()))

  it('starts in an unknown state so the UI can show a splash instead of the login screen', () => {
    expect(useAuthStore.getState().status).toBe('unknown')
  })

  it('persists the session on sign in', async () => {
    const storage = memoryStorage()
    setUp(storage)

    await useAuthStore.getState().signIn(session)

    expect(useAuthStore.getState().token).toBe('un.token.jwt')
    expect(useAuthStore.getState().status).toBe('authenticated')
    expect(storage.data['periferia.session']).toContain('un.token.jwt')
  })

  it('restores a persisted session on start up', async () => {
    const storage = memoryStorage({
      'periferia.session': JSON.stringify({ token: 'guardado', user: session.user }),
    })
    setUp(storage)

    await useAuthStore.getState().restore()

    expect(useAuthStore.getState().token).toBe('guardado')
    expect(useAuthStore.getState().status).toBe('authenticated')
  })

  it('ends up anonymous when there is nothing stored', async () => {
    await useAuthStore.getState().restore()

    expect(useAuthStore.getState().status).toBe('anonymous')
    expect(useAuthStore.getState().token).toBeNull()
  })

  it('ends up anonymous when the stored session is corrupted instead of crashing', async () => {
    const storage = memoryStorage({ 'periferia.session': 'esto-no-es-json' })
    setUp(storage)

    await useAuthStore.getState().restore()

    expect(useAuthStore.getState().status).toBe('anonymous')
  })

  it('clears both memory and storage on sign out', async () => {
    const storage = memoryStorage()
    setUp(storage)
    await useAuthStore.getState().signIn(session)

    await useAuthStore.getState().signOut()

    expect(useAuthStore.getState().token).toBeNull()
    expect(useAuthStore.getState().status).toBe('anonymous')
    expect(storage.data['periferia.session']).toBeUndefined()
  })
})
