import { useAuthStore, useLikePost, usePosts, useRealtimeLikes } from '@periferia/core'
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import PostCard from '../components/PostCard'

export default function FeedScreen() {
  // Exactamente los mismos tres hooks que FeedScreen.tsx de la web.
  // Ni una línea de lógica de negocio se repite aquí.
  const feed = usePosts()
  const like = useLikePost()
  const realtimeStatus = useRealtimeLikes()

  const user = useAuthStore((state) => state.user)
  const signOut = useAuthStore((state) => state.signOut)

  return (
    <SafeAreaView style={styles.screen} edges={['top']}>
      <View style={styles.topbar}>
        <Text style={styles.brand}>Periferia</Text>

        <View style={[styles.pill, realtimeStatus === 'open' && styles.pillLive]}>
          <Text style={[styles.pillText, realtimeStatus === 'open' && styles.pillTextLive]}>
            {realtimeStatus === 'open' ? 'en vivo' : 'reconectando'}
          </Text>
        </View>

        <Pressable onPress={() => void signOut()}>
          <Text style={styles.signOut}>@{user?.alias} · salir</Text>
        </Pressable>
      </View>

      <FlatList
        data={feed.data?.content ?? []}
        keyExtractor={(post) => post.id}
        contentContainerStyle={styles.list}
        refreshing={feed.isFetching}
        onRefresh={() => void feed.refetch()}
        ListEmptyComponent={
          <Text style={styles.empty}>
            {feed.isPending ? 'Cargando…' : 'No hay publicaciones todavía.'}
          </Text>
        }
        renderItem={({ item }) => (
          <PostCard
            post={item}
            onToggleLike={(target) => like.mutate({ postId: target.id, liked: !target.likedByMe })}
          />
        )}
      />
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: '#0f1115' },
  topbar: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomColor: '#272b35',
    borderBottomWidth: 1,
  },
  brand: { color: '#f3f4f6', fontWeight: '700', fontSize: 17, flex: 1 },
  pill: {
    backgroundColor: '#272b35',
    borderRadius: 999,
    paddingHorizontal: 9,
    paddingVertical: 3,
  },
  pillLive: { backgroundColor: 'rgba(74, 222, 128, 0.15)' },
  pillText: { color: '#9ca3af', fontSize: 11 },
  pillTextLive: { color: '#4ade80' },
  signOut: { color: '#9ca3af', fontSize: 13 },
  list: { padding: 16 },
  empty: { color: '#6b7280', textAlign: 'center', marginTop: 40 },
})
