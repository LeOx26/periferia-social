import { useAuthStore, type RealtimeStatus } from '@periferia/core'
import { Link, useLocation } from 'react-router'

export default function TopBar({ realtimeStatus }: { realtimeStatus?: RealtimeStatus }) {
  const user = useAuthStore((state) => state.user)
  const signOut = useAuthStore((state) => state.signOut)
  const location = useLocation()

  const onProfile = location.pathname === '/perfil'

  return (
    <header className="topbar">
      <span className="topbar__brand">Periferia Social</span>

      {/* No es decoración: hace visible que el WebSocket está vivo, que es
          justo lo que hay que poder enseñar en la demo. */}
      {realtimeStatus && (
        <span
          className={`pill pill--${realtimeStatus}`}
          title="Estado de la conexión de likes en tiempo real"
        >
          {realtimeStatus === 'open' ? 'en vivo' : 'reconectando'}
        </span>
      )}

      <nav className="topbar__nav">
        <Link className="topbar__link" to={onProfile ? '/' : '/perfil'}>
          {onProfile ? 'Feed' : `@${user?.alias}`}
        </Link>
        <button className="button button--ghost" type="button" onClick={() => void signOut()}>
          Salir
        </button>
      </nav>
    </header>
  )
}
