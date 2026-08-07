import { getConfig } from '../config.js'
import { loginResultSchema, profileSchema } from '../domain/schemas.js'
import type { LoginResult, Profile } from '../domain/types.js'
import { apiFetch } from './httpClient.js'

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
