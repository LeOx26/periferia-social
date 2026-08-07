import type { PostView } from '@periferia/core'
import { Pressable, StyleSheet, Text, View } from 'react-native'

interface Props {
  post: PostView
  onToggleLike(post: PostView): void
}

const formatDate = (iso: string) =>
  new Intl.DateTimeFormat('es', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(iso))

export default function PostCard({ post, onToggleLike }: Props) {
  return (
    <View style={styles.card}>
      <View style={styles.header}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{post.authorAlias.slice(0, 2).toUpperCase()}</Text>
        </View>
        <Text style={styles.alias}>@{post.authorAlias}</Text>
        {post.isOwn && <Text style={styles.badge}>tú</Text>}
        <Text style={styles.date}>{formatDate(post.createdAt)}</Text>
      </View>

      <Text style={styles.message}>{post.message}</Text>

      <Pressable
        style={styles.like}
        onPress={() => onToggleLike(post)}
        // El backend responde 409 al auto-like; no tiene sentido provocarlo.
        disabled={post.isOwn}
      >
        <Text
          style={[styles.heart, post.likedByMe && styles.heartOn, post.isOwn && styles.disabled]}
        >
          {post.likedByMe ? '♥' : '♡'}
        </Text>
        <Text style={[styles.count, post.isOwn && styles.disabled]}>{post.likeCount}</Text>
      </Pressable>
    </View>
  )
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#181b22',
    borderRadius: 14,
    borderColor: '#272b35',
    borderWidth: 1,
    padding: 16,
    marginBottom: 12,
    gap: 10,
  },
  header: { flexDirection: 'row', alignItems: 'center', gap: 8, flexWrap: 'wrap' },
  avatar: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#2a3142',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: { color: '#a5b4fc', fontSize: 12, fontWeight: '700' },
  alias: { color: '#e5e7eb', fontWeight: '600' },
  badge: {
    color: '#0f1115',
    backgroundColor: '#7c9cff',
    fontSize: 11,
    fontWeight: '700',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 6,
    overflow: 'hidden',
  },
  date: { color: '#6b7280', fontSize: 11 },
  message: { color: '#d1d5db', fontSize: 15, lineHeight: 21 },
  like: { flexDirection: 'row', alignItems: 'center', gap: 6, alignSelf: 'flex-start' },
  heart: { color: '#6b7280', fontSize: 20 },
  heartOn: { color: '#f87171' },
  count: { color: '#9ca3af', fontSize: 14 },
  disabled: { opacity: 0.4 },
})
