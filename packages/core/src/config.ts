/**
 * Adaptador de almacenamiento persistente.
 *
 * Es la ÚNICA abstracción que necesita el core para funcionar en dos plataformas:
 * la web inyecta localStorage y el móvil expo-secure-store. Es asíncrono porque
 * el llavero del sistema en iOS y Android lo es; en web se resuelve al instante.
 */
export interface CoreStorage {
  getItem(key: string): Promise<string | null>
  setItem(key: string, value: string): Promise<void>
  removeItem(key: string): Promise<void>
}

export interface CoreConfig {
  /** auth-service. En producción ambos servicios estarían tras un API gateway. */
  authBaseUrl: string
  /** social-service. */
  socialBaseUrl: string
  /** Base del WebSocket de likes, normalmente el mismo host que socialBaseUrl. */
  wsBaseUrl: string
  storage: CoreStorage
}

let config: CoreConfig | null = null

const withoutTrailingSlash = (url: string) => url.replace(/\/+$/, '')

/**
 * Única costura entre plataformas de toda la aplicación. Se llama una sola vez
 * al arrancar cada app; a partir de ahí, web y móvil ejecutan el mismo código.
 */
export function configureCore(next: CoreConfig): void {
  config = {
    ...next,
    authBaseUrl: withoutTrailingSlash(next.authBaseUrl),
    socialBaseUrl: withoutTrailingSlash(next.socialBaseUrl),
    wsBaseUrl: withoutTrailingSlash(next.wsBaseUrl),
  }
}

export function getConfig(): CoreConfig {
  if (!config) {
    throw new Error(
      'El core no está configurado. Llama a configureCore({ authBaseUrl, socialBaseUrl, wsBaseUrl, storage }) al arrancar la aplicación.',
    )
  }
  return config
}

/** Solo para tests. */
export function resetConfig(): void {
  config = null
}
