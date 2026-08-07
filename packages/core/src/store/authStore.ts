import { create } from 'zustand'
import { getConfig } from '../config.js'
import type { LoginResult, UserSummary } from '../domain/types.js'

const SESSION_KEY = 'periferia.session'

export type AuthStatus = 'unknown' | 'authenticated' | 'anonymous'

interface AuthState {
  token: string | null
  user: UserSummary | null
  /**
   * 'unknown' hasta que restore() termina. Sin este tercer estado, al recargar
   * con sesión guardada se ve un parpadeo de login antes del feed.
   */
  status: AuthStatus
  restore(): Promise<void>
  signIn(result: LoginResult): Promise<void>
  signOut(): Promise<void>
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  user: null,
  status: 'unknown',

  async restore() {
    try {
      const raw = await getConfig().storage.getItem(SESSION_KEY)
      if (!raw) {
        set({ status: 'anonymous' })
        return
      }

      const saved = JSON.parse(raw) as { token?: string; user?: UserSummary }
      if (!saved.token || !saved.user) {
        set({ status: 'anonymous' })
        return
      }

      set({ token: saved.token, user: saved.user, status: 'authenticated' })
    } catch {
      // Sesión corrupta de una versión anterior, o storage inaccesible: se
      // empieza de cero en lugar de dejar la app bloqueada en la carga.
      set({ token: null, user: null, status: 'anonymous' })
    }
  },

  async signIn(result) {
    set({ token: result.accessToken, user: result.user, status: 'authenticated' })
    await getConfig().storage.setItem(
      SESSION_KEY,
      JSON.stringify({ token: result.accessToken, user: result.user }),
    )
  },

  async signOut() {
    set({ token: null, user: null, status: 'anonymous' })
    await getConfig().storage.removeItem(SESSION_KEY)
  },
}))
