import { useLikePost, usePosts, useRealtimeLikes, type PostView } from '@periferia/core'
import Composer from '../components/Composer'
import PostCard from '../components/PostCard'
import TopBar from '../components/TopBar'

export default function FeedScreen() {
  // Tres hooks del core. La app móvil usa exactamente estos mismos.
  const feed = usePosts()
  const like = useLikePost()
  const realtimeStatus = useRealtimeLikes()

  return (
    <div className="layout">
      <TopBar realtimeStatus={realtimeStatus} />

      <main className="feed">
        <Composer />

        {feed.isPending && <p className="muted">Cargando publicaciones…</p>}
        {feed.isError && <p className="alert">No se pudo cargar el feed.</p>}

        {feed.data?.content.map((post: PostView) => (
          <PostCard
            key={post.id}
            post={post}
            onToggleLike={(target) => like.mutate({ postId: target.id, liked: !target.likedByMe })}
          />
        ))}

        {feed.data?.content.length === 0 && (
          <p className="muted">Todavía no hay publicaciones. Sé el primero.</p>
        )}
      </main>
    </div>
  )
}
