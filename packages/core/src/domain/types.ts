export interface UserSummary {
  id: string
  alias: string
  firstName: string
  lastName: string
}

export interface LoginResult {
  accessToken: string
  expiresIn: number
  user: UserSummary
}

export interface Profile {
  id: string
  username: string
  firstName: string
  lastName: string
  /** ISO 8601 sin hora: "1993-04-12". */
  birthDate: string
  alias: string
}

export interface PostView {
  id: string
  message: string
  createdAt: string
  authorId: string
  authorAlias: string
  likeCount: number
  /** Si el usuario que consulta ya dio like. */
  likedByMe: boolean
  /** Si la publicación es del propio usuario: no puede darse like a sí mismo. */
  isOwn: boolean
}

export interface FeedPage {
  content: PostView[]
  page: number
  size: number
  totalElements: number
}

export interface LikeResponse {
  postId: string
  likeCount: number
  likedByMe: boolean
}

/**
 * Mensaje que difunde social-service por /ws/likes.
 *
 * Solo informa de CUÁNTOS likes tiene la publicación, no de quién los dio: es un
 * evento global para todos los clientes conectados.
 */
export interface LikeEvent {
  type: 'LIKE_UPDATED'
  postId: string
  likeCount: number
}
