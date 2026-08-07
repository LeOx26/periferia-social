export const queryKeys = {
  profile: ['profile'] as const,
  feed: (page: number, size: number) => ['feed', page, size] as const,
}
