import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createPost } from '../api/postsApi'
import { useAuthStore } from '../store/authStore'
import { queryKeys } from './queryKeys'

export function useCreatePost(page = 0, size = 20) {
  const token = useAuthStore((state) => state.token)
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (message: string) => createPost(token!, message),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.feed(page, size) }),
  })
}
