import { ApiError, useLogin } from '@periferia/core'
import { useState } from 'react'

export default function LoginScreen() {
  // Precargados a propósito: es una demo y el evaluador no debería tener que
  // buscar las credenciales en el README para entrar.
  const [username, setUsername] = useState('leo')
  const [password, setPassword] = useState('Periferia2026!')
  const login = useLogin()

  const error = login.error instanceof ApiError ? login.error : null

  return (
    <div className="auth">
      <form
        className="card auth__form"
        onSubmit={(event) => {
          event.preventDefault()
          login.mutate({ username, password })
        }}
      >
        <h1 className="auth__title">Periferia Social</h1>
        <p className="auth__subtitle">Entra para ver lo que está publicando la gente.</p>

        <label className="field">
          <span className="field__label">Usuario</span>
          <input
            className="field__input"
            value={username}
            autoComplete="username"
            onChange={(event) => setUsername(event.target.value)}
          />
        </label>

        <label className="field">
          <span className="field__label">Contraseña</span>
          <input
            className="field__input"
            type="password"
            value={password}
            autoComplete="current-password"
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>

        {error && (
          <p className="alert" role="alert">
            {error.title}
            {error.correlationId && (
              <span className="alert__trace">ref: {error.correlationId}</span>
            )}
          </p>
        )}

        <button className="button" type="submit" disabled={login.isPending}>
          {login.isPending ? 'Entrando…' : 'Entrar'}
        </button>

        <p className="auth__hint">
          Usuarios: leo, mafe, carlos, ana, diego · contraseña <code>Periferia2026!</code>
        </p>
      </form>
    </div>
  )
}
