import type { CoreStorage } from '@periferia/core'
import * as SecureStore from 'expo-secure-store'
import { Platform } from 'react-native'

// SecureStore de iOS no admite puntos en la clave; se normaliza aquí para que el
// core pueda seguir usando su nombre canónico.
const safeKey = (key: string) => key.replace(/\./g, '_')

/**
 * Única pieza específica de móvil de toda la aplicación.
 *
 * En dispositivo guarda el token en el llavero del sistema (Keychain en iOS,
 * Keystore en Android), no en texto plano. En Expo Web no existe SecureStore, así
 * que se cae a localStorage, que es la misma garantía que ofrece la web.
 */
export const secureStorage: CoreStorage = {
  async getItem(key) {
    if (Platform.OS === 'web') return localStorage.getItem(key)
    return SecureStore.getItemAsync(safeKey(key))
  },
  async setItem(key, value) {
    if (Platform.OS === 'web') {
      localStorage.setItem(key, value)
      return
    }
    await SecureStore.setItemAsync(safeKey(key), value)
  },
  async removeItem(key) {
    if (Platform.OS === 'web') {
      localStorage.removeItem(key)
      return
    }
    await SecureStore.deleteItemAsync(safeKey(key))
  },
}
