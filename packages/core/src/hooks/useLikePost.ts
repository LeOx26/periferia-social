import { useMutation, useQueryClient } from '@tanstack/react-query'
import { likePost, unlikePost } from '../api/postsApi'
import type { FeedPage } from '../domain/types'
import { useAuthStore } from '../store/authStore'
import { applyOptimisticLike } from './feedCache'
import { queryKeys } from './queryKeys'

export function useLikePost(page = 0, size = 20) {
  const token = useAuthStore((state) => state.token)
  const queryClient = useQueryClient()
  const key = queryKeys.feed(page, size)

  return useMutation({
    mutationFn: ({ postId, liked }: { postId: string; liked: boolean }) =>
      liked ? likePost(token!, postId) : unlikePost(token!, postId),

    async onMutate({ postId, liked }) {
      // Cancelar primero: una petición en vuelo podría sobrescribir el cambio
      // optimista al resolverse justo después.
      await queryClient.cancelQueries({ queryKey: key })

      const previous = queryClient.getQueryData<FeedPage>(key)
      queryClient.setQueryData<FeedPage | undefined>(key, (current) =>
        applyOptimisticLike(current, postId, liked),
      )
      return { previous }
    },

    onError(_error, _variables, context) {
      // El backend devuelve 409 en auto-like y doble like: se deshace el cambio
      // para que la interfaz no mienta.
      if (context?.previous) queryClient.setQueryData(key, context.previous)
    },

    onSettled() {
      queryClient.invalidateQueries({ queryKey: key })
    },
  })
}
