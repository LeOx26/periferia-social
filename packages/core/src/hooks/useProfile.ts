import { useQuery } from '@tanstack/react-query'
import { fetchProfile } from '../api/authApi'
import { useAuthStore } from '../store/authStore'
import { queryKeys } from './queryKeys'

export function useProfile() {
  const token = useAuthStore((state) => state.token)

  return useQuery({
    queryKey: queryKeys.profile,
    queryFn: () => fetchProfile(token!),
    // Sin token no se lanza la petición: evita un 401 garantizado al arrancar.
    enabled: Boolean(token),
  })
}
