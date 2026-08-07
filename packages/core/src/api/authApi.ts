import { getConfig } from '../config'
import { loginResultSchema, profileSchema } from '../domain/schemas'
import type { LoginResult, Profile } from '../domain/types'
import { apiFetch } from './httpClient'

export function login(username: string, password: string): Promise<LoginResult> {
  return apiFetch(getConfig().authBaseUrl, '/api/auth/login', {
    method: 'POST',
    body: { username, password },
    schema: loginResultSchema,
  })
}

export function fetchProfile(token: string): Promise<Profile> {
  return apiFetch(getConfig().authBaseUrl, '/api/users/me', {
    token,
    schema: profileSchema,
  })
}
