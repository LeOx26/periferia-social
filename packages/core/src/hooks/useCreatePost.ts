import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createPost } from '../api/postsApi.js'
import { useAuthStore } from '../store/authStore.js'
import { queryKeys } from './queryKeys.js'

export function useCreatePost(page = 0, size = 20) {
  const token = useAuthStore((state) => state.token)
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (message: string) => createPost(token!, message),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.feed(page, size) }),
  })
}
