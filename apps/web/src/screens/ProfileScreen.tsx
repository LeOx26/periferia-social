import { useProfile } from '@periferia/core'
import TopBar from '../components/TopBar'

const formatBirthDate = (iso: string) =>
  new Intl.DateTimeFormat('es', { dateStyle: 'long' }).format(new Date(`${iso}T00:00:00`))

export default function ProfileScreen() {
  const profile = useProfile()

  return (
    <div className="layout">
      <TopBar />

      <main className="feed">
        {profile.isPending && <p className="muted">Cargando perfil…</p>}
        {profile.isError && <p className="alert">No se pudo cargar el perfil.</p>}

        {profile.data && (
          <section className="card profile">
            <span className="avatar avatar--lg" aria-hidden="true">
              {profile.data.alias.slice(0, 2).toUpperCase()}
            </span>

            <h1 className="profile__name">
              {profile.data.firstName} {profile.data.lastName}
            </h1>
            <p className="profile__alias">@{profile.data.alias}</p>

            <dl className="profile__grid">
              <dt>Usuario</dt>
              <dd>{profile.data.username}</dd>
              <dt>Fecha de nacimiento</dt>
              <dd>{formatBirthDate(profile.data.birthDate)}</dd>
            </dl>
          </section>
        )}
      </main>
    </div>
  )
}
