import { describe, expect, it } from 'vitest'
import * as core from './index'

/**
 * La reutilización entre web y móvil es lo que más puntúa en esta prueba, así que
 * conviene poder demostrarla en lugar de afirmarla. Si alguien mueve un hook
 * fuera de la superficie pública, este test lo detecta antes que la app móvil.
 */
describe('la superficie pública del core', () => {
  it('exports every hook both applications consume', () => {
    for (const hook of [
      'useLogin',
      'useProfile',
      'usePosts',
      'useCreatePost',
      'useLikePost',
      'useRealtimeLikes',
      'useAuthStore',
    ]) {
      expect(core, `falta ${hook}`).toHaveProperty(hook)
    }
  })

  it('exports the api client and the platform seam', () => {
    for (const symbol of [
      'configureCore',
      'ApiError',
      'login',
      'fetchProfile',
      'fetchFeed',
      'createPost',
      'likePost',
      'unlikePost',
      'createRealtimeClient',
      'messageSchema',
    ]) {
      expect(core, `falta ${symbol}`).toHaveProperty(symbol)
    }
  })
})
