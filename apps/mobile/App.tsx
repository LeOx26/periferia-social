import { configureCore, useAuthStore } from '@periferia/core'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { StatusBar } from 'expo-status-bar'
import { useEffect } from 'react'
import { ActivityIndicator, StyleSheet, View } from 'react-native'
import { SafeAreaProvider } from 'react-native-safe-area-context'
import { secureStorage } from './src/platform/secureStorage'
import FeedScreen from './src/screens/FeedScreen'
import LoginScreen from './src/screens/LoginScreen'

// El simulador de iOS comparte la red del Mac, así que `localhost` apunta al
// docker-compose sin configuración extra. En un dispositivo físico habría que
// poner la IP de la red local; en el emulador de Android, 10.0.2.2.
const HOST = 'localhost'

// Misma llamada que en la web, con otro adaptador de almacenamiento.
// A partir de aquí, el código es idéntico.
configureCore({
  authBaseUrl: `http://${HOST}:8081`,
  socialBaseUrl: `http://${HOST}:8082`,
  wsBaseUrl: `ws://${HOST}:8082`,
  storage: secureStorage,
})

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1 } },
})

function Root() {
  const status = useAuthStore((state) => state.status)
  const restore = useAuthStore((state) => state.restore)

  useEffect(() => {
    void restore()
  }, [restore])

  if (status === 'unknown') {
    return (
      <View style={styles.splash}>
        <ActivityIndicator size="large" color="#7c9cff" />
      </View>
    )
  }

  // Con dos pantallas y una sola transición, un condicional es más honesto que
  // instalar react-navigation. Documentado como trade-off en el README.
  return status === 'authenticated' ? <FeedScreen /> : <LoginScreen />
}

export default function App() {
  return (
    <SafeAreaProvider>
      <QueryClientProvider client={queryClient}>
        <StatusBar style="light" />
        <Root />
      </QueryClientProvider>
    </SafeAreaProvider>
  )
}

const styles = StyleSheet.create({
  splash: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#0f1115',
  },
})
