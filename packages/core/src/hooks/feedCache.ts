import type { FeedPage, LikeEvent } from '../domain/types'

/**
 * Aplica el contador autoritativo que llega por WebSocket.
 *
 * NO toca `likedByMe`: el evento es global y solo informa de cuántos likes tiene
 * la publicación, no de quién los dio. Tocarlo encendería el corazón del usuario
 * equivocado cada vez que otra persona diera like.
 */
export function applyLikeEvent(
  page: FeedPage | undefined,
  event: LikeEvent,
): FeedPage | undefined {
  if (!page) return undefined

  return {
    ...page,
    content: page.content.map((post) =>
      post.id === event.postId ? { ...post, likeCount: event.likeCount } : post,
    ),
  }
}

/**
 * Cambio inmediato en la interfaz, antes de que el servidor responda. Si la
 * petición falla, useLikePost restaura la página anterior.
 */
export function applyOptimisticLike(
  page: FeedPage | undefined,
  postId: string,
  liked: boolean,
): FeedPage | undefined {
  if (!page) return undefined

  return {
    ...page,
    content: page.content.map((post) =>
      post.id === postId
        ? {
            ...post,
            likedByMe: liked,
            likeCount: liked ? post.likeCount + 1 : Math.max(0, post.likeCount - 1),
          }
        : post,
    ),
  }
}
