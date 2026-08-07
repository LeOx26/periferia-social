import { useQuery } from '@tanstack/react-query'
import { fetchFeed } from '../api/postsApi'
import { useAuthStore } from '../store/authStore'
import { queryKeys } from './queryKeys'

export function usePosts(page = 0, size = 20) {
  const token = useAuthStore((state) => state.token)

  return useQuery({
    queryKey: queryKeys.feed(page, size),
    queryFn: () => fetchFeed(token!, page, size),
    enabled: Boolean(token),
  })
}
