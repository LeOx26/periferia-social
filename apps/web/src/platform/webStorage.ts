import type { CoreStorage } from '@periferia/core'

/**
 * Única pieza específica de navegador de toda la aplicación web. El móvil aporta
 * la suya con expo-secure-store y el resto del código es idéntico.
 */
export const webStorage: CoreStorage = {
  async getItem(key) {
    return localStorage.getItem(key)
  },
  async setItem(key, value) {
    localStorage.setItem(key, value)
  },
  async removeItem(key) {
    localStorage.removeItem(key)
  },
}
