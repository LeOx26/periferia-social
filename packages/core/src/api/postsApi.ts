import { getConfig } from '../config.js'
import { feedPageSchema, likeResponseSchema, postViewSchema } from '../domain/schemas.js'
import type { FeedPage, LikeResponse, PostView } from '../domain/types.js'
import { apiFetch } from './httpClient.js'

export function fetchFeed(token: string, page = 0, size = 20): Promise<FeedPage> {
  return apiFetch(getConfig().socialBaseUrl, `/api/posts?page=${page}&size=${size}`, {
    token,
    schema: feedPageSchema,
  })
}

/** El autor y la fecha los fija el servidor a partir del JWT: aquí solo va el mensaje. */
export function createPost(token: string, message: string): Promise<PostView> {
  return apiFetch(getConfig().socialBaseUrl, '/api/posts', {
    method: 'POST',
    body: { message },
    token,
    schema: postViewSchema,
  })
}

export function likePost(token: string, postId: string): Promise<LikeResponse> {
  return apiFetch(getConfig().socialBaseUrl, `/api/posts/${postId}/likes`, {
    method: 'POST',
    token,
    schema: likeResponseSchema,
  })
}

export function unlikePost(token: string, postId: string): Promise<LikeResponse> {
  return apiFetch(getConfig().socialBaseUrl, `/api/posts/${postId}/likes`, {
    method: 'DELETE',
    token,
    schema: likeResponseSchema,
  })
}
