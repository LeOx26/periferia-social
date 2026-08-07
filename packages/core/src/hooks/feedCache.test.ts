import { describe, expect, it } from 'vitest'
import type { FeedPage, PostView } from '../domain/types'
import { applyLikeEvent, applyOptimisticLike } from './feedCache'

const post = (over: Partial<PostView> = {}): PostView => ({
  id: 'p1',
  message: 'Hola',
  createdAt: '2026-08-07T10:00:00Z',
  authorId: 'a1',
  authorAlias: 'mafe',
  likeCount: 0,
  likedByMe: false,
  isOwn: false,
  ...over,
})

const page = (posts: PostView[]): FeedPage => ({
  content: posts,
  page: 0,
  size: 20,
  totalElements: posts.length,
})

describe('applyLikeEvent', () => {
  it('writes the authoritative count coming from the server', () => {
    const result = applyLikeEvent(page([post({ likeCount: 1 })]), {
      type: 'LIKE_UPDATED',
      postId: 'p1',
      likeCount: 9,
    })

    expect(result?.content[0]!.likeCount).toBe(9)
  })

  it('never flips likedByMe, because the event says nothing about who liked', () => {
    const result = applyLikeEvent(page([post({ likedByMe: true, likeCount: 1 })]), {
      type: 'LIKE_UPDATED',
      postId: 'p1',
      likeCount: 5,
    })

    expect(result?.content[0]!.likedByMe).toBe(true)
  })

  it('leaves other posts untouched', () => {
    const result = applyLikeEvent(page([post({ id: 'p1' }), post({ id: 'p2', likeCount: 3 })]), {
      type: 'LIKE_UPDATED',
      postId: 'p1',
      likeCount: 7,
    })

    expect(result?.content[1]!.likeCount).toBe(3)
  })

  it('ignores an event for a post that is not in the page', () => {
    const original = page([post({ id: 'p1', likeCount: 2 })])

    const result = applyLikeEvent(original, { type: 'LIKE_UPDATED', postId: 'otro', likeCount: 99 })

    expect(result?.content[0]!.likeCount).toBe(2)
  })

  it('returns undefined when there is no cached page yet', () => {
    expect(
      applyLikeEvent(undefined, { type: 'LIKE_UPDATED', postId: 'p1', likeCount: 1 }),
    ).toBeUndefined()
  })
})

describe('applyOptimisticLike', () => {
  it('increments and marks the post as liked', () => {
    const result = applyOptimisticLike(page([post({ likeCount: 2 })]), 'p1', true)

    expect(result?.content[0]).toMatchObject({ likeCount: 3, likedByMe: true })
  })

  it('decrements and unmarks the post', () => {
    const result = applyOptimisticLike(page([post({ likeCount: 2, likedByMe: true })]), 'p1', false)

    expect(result?.content[0]).toMatchObject({ likeCount: 1, likedByMe: false })
  })

  it('never goes below zero', () => {
    const result = applyOptimisticLike(page([post({ likeCount: 0, likedByMe: true })]), 'p1', false)

    expect(result?.content[0]!.likeCount).toBe(0)
  })

  it('does not mutate the object it receives', () => {
    const original = page([post({ likeCount: 2 })])

    applyOptimisticLike(original, 'p1', true)

    expect(original.content[0]!.likeCount).toBe(2)
  })
})
