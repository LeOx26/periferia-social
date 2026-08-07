export { configureCore, getConfig, type CoreConfig, type CoreStorage } from './config.js'

export { ApiError } from './api/httpClient.js'
export { login, fetchProfile } from './api/authApi.js'
export { fetchFeed, createPost, likePost, unlikePost } from './api/postsApi.js'
export { createRealtimeClient, type RealtimeStatus } from './api/realtimeClient.js'

export type {
  FeedPage,
  LikeEvent,
  LikeResponse,
  LoginResult,
  PostView,
  Profile,
  UserSummary,
} from './domain/types.js'
export { messageSchema } from './domain/schemas.js'

export { useAuthStore, type AuthStatus } from './store/authStore.js'

export { queryKeys } from './hooks/queryKeys.js'
export { applyLikeEvent, applyOptimisticLike } from './hooks/feedCache.js'
export { useLogin } from './hooks/useLogin.js'
export { useProfile } from './hooks/useProfile.js'
export { usePosts } from './hooks/usePosts.js'
export { useCreatePost } from './hooks/useCreatePost.js'
export { useLikePost } from './hooks/useLikePost.js'
export { useRealtimeLikes } from './hooks/useRealtimeLikes.js'
