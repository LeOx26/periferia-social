export { configureCore, getConfig, type CoreConfig, type CoreStorage } from './config'

export { ApiError } from './api/httpClient'
export { login, fetchProfile } from './api/authApi'
export { fetchFeed, createPost, likePost, unlikePost } from './api/postsApi'
export { createRealtimeClient, type RealtimeStatus } from './api/realtimeClient'

export type {
  FeedPage,
  LikeEvent,
  LikeResponse,
  LoginResult,
  PostView,
  Profile,
  UserSummary,
} from './domain/types'
export { messageSchema } from './domain/schemas'

export { useAuthStore, type AuthStatus } from './store/authStore'

export { queryKeys } from './hooks/queryKeys'
export { applyLikeEvent, applyOptimisticLike } from './hooks/feedCache'
export { useLogin } from './hooks/useLogin'
export { useProfile } from './hooks/useProfile'
export { usePosts } from './hooks/usePosts'
export { useCreatePost } from './hooks/useCreatePost'
export { useLikePost } from './hooks/useLikePost'
export { useRealtimeLikes } from './hooks/useRealtimeLikes'
