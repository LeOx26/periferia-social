import { ApiError, messageSchema, useCreatePost } from '@periferia/core'
import { useState } from 'react'

const MAX = 280

export default function Composer() {
  const [message, setMessage] = useState('')
  const createPost = useCreatePost()

  // Mismo esquema Zod que usa la app móvil: la validación no se escribe dos veces.
  const validation = messageSchema.safeParse(message)
  const remaining = MAX - message.length
  const error = createPost.error instanceof ApiError ? createPost.error : null

  return (
    <form
      className="composer card"
      onSubmit={(event) => {
        event.preventDefault()
        if (!validation.success) return
        createPost.mutate(validation.data, { onSuccess: () => setMessage('') })
      }}
    >
      <textarea
        className="composer__input"
        placeholder="¿Qué estás pensando?"
        value={message}
        maxLength={MAX}
        rows={3}
        onChange={(event) => setMessage(event.target.value)}
      />

      {error && <p className="alert">{error.title}</p>}

      <div className="composer__footer">
        <span className={`counter ${remaining < 20 ? 'counter--warn' : ''}`}>{remaining}</span>
        <button
          className="button"
          type="submit"
          disabled={!validation.success || createPost.isPending}
        >
          {createPost.isPending ? 'Publicando…' : 'Publicar'}
        </button>
      </div>
    </form>
  )
}
