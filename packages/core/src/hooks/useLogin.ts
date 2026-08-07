import { useMutation, useQueryClient } from '@tanstack/react-query'
import { login } from '../api/authApi'
import { useAuthStore } from '../store/authStore'

export function useLogin() {
  const signIn = useAuthStore((state) => state.signIn)
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ username, password }: { username: string; password: string }) =>
      login(username, password),
    onSuccess: async (result) => {
      await signIn(result)
      // El cache de la sesión anterior no vale para la nueva: `isOwn` y
      // `likedByMe` dependen de quién consulta.
      await queryClient.invalidateQueries()
    },
  })
}
