import { configureCore } from '@periferia/core'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router'
import App from './App'
import { webStorage } from './platform/webStorage'
import './styles.css'

// Única costura de plataforma: a partir de aquí, el código es el mismo que en móvil.
configureCore({
  authBaseUrl: import.meta.env.VITE_AUTH_URL,
  socialBaseUrl: import.meta.env.VITE_SOCIAL_URL,
  wsBaseUrl: import.meta.env.VITE_WS_URL,
  storage: webStorage,
})

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // El WebSocket ya trae los cambios de likes, así que refrescar al volver a
      // la pestaña sobra, y además enmascararía si el tiempo real deja de funcionar.
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
)
