import { useAuthStore } from '@periferia/core'
import { useEffect } from 'react'
import { Navigate, Route, Routes } from 'react-router'
import FeedScreen from './screens/FeedScreen'
import LoginScreen from './screens/LoginScreen'
import ProfileScreen from './screens/ProfileScreen'

export default function App() {
  const status = useAuthStore((state) => state.status)
  const restore = useAuthStore((state) => state.restore)

  useEffect(() => {
    void restore()
  }, [restore])

  // Mientras no se sabe si hay sesión guardada no se enseña el login: si no,
  // al recargar se vería un parpadeo de "login → feed".
  if (status === 'unknown') {
    return <div className="splash">Cargando…</div>
  }

  if (status === 'anonymous') {
    return (
      <Routes>
        <Route path="/login" element={<LoginScreen />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    )
  }

  return (
    <Routes>
      <Route path="/" element={<FeedScreen />} />
      <Route path="/perfil" element={<ProfileScreen />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
