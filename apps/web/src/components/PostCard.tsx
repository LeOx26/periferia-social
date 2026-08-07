import type { PostView } from '@periferia/core'

interface Props {
  post: PostView
  onToggleLike(post: PostView): void
}

const formatDate = (iso: string) =>
  new Intl.DateTimeFormat('es', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(iso))

export default function PostCard({ post, onToggleLike }: Props) {
  return (
    <article className="post card">
      <header className="post__header">
        <span className="avatar" aria-hidden="true">
          {post.authorAlias.slice(0, 2).toUpperCase()}
        </span>
        <div className="post__meta">
          <span className="post__alias">@{post.authorAlias}</span>
          {post.isOwn && <span className="badge">tú</span>}
          <time className="post__date" dateTime={post.createdAt}>
            {formatDate(post.createdAt)}
          </time>
        </div>
      </header>

      <p className="post__message">{post.message}</p>

      <footer className="post__footer">
        <button
          type="button"
          className={`like ${post.likedByMe ? 'like--on' : ''}`}
          onClick={() => onToggleLike(post)}
          // El backend responde 409 al auto-like: deshabilitarlo evita provocar
          // un error que ya sabemos que va a ocurrir.
          disabled={post.isOwn}
          title={post.isOwn ? 'No puedes dar like a tu propia publicación' : undefined}
        >
          <span aria-hidden="true">{post.likedByMe ? '♥' : '♡'}</span>
          <span className="like__count">{post.likeCount}</span>
        </button>
      </footer>
    </article>
  )
}
