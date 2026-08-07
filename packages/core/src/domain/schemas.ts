import { z } from 'zod'

/**
 * Validación en el borde. Un cambio de contrato del backend falla aquí, con un
 * mensaje que dice exactamente qué campo cambió, en lugar de aparecer como
 * `undefined` a mitad de un componente tres pantallas después.
 */

export const userSummarySchema = z.object({
  id: z.string(),
  alias: z.string(),
  firstName: z.string(),
  lastName: z.string(),
})

export const loginResultSchema = z.object({
  accessToken: z.string(),
  expiresIn: z.number(),
  user: userSummarySchema,
})

export const profileSchema = z.object({
  id: z.string(),
  username: z.string(),
  firstName: z.string(),
  lastName: z.string(),
  birthDate: z.string(),
  alias: z.string(),
})

export const postViewSchema = z.object({
  id: z.string(),
  message: z.string(),
  createdAt: z.string(),
  authorId: z.string(),
  authorAlias: z.string(),
  likeCount: z.number(),
  likedByMe: z.boolean(),
  isOwn: z.boolean(),
})

export const feedPageSchema = z.object({
  content: z.array(postViewSchema),
  page: z.number(),
  size: z.number(),
  totalElements: z.number(),
})

export const likeResponseSchema = z.object({
  postId: z.string(),
  likeCount: z.number(),
  likedByMe: z.boolean(),
})

export const likeEventSchema = z.object({
  type: z.literal('LIKE_UPDATED'),
  postId: z.string(),
  likeCount: z.number(),
})

/**
 * Mismas reglas que el agregado Post del backend. Se valida en cliente para dar
 * respuesta inmediata, pero el servidor sigue siendo la autoridad: esta copia es
 * para la experiencia de usuario, no para la seguridad.
 */
export const messageSchema = z
  .string()
  .trim()
  .min(1, 'El mensaje no puede estar vacío')
  .max(280, 'El mensaje no puede superar los 280 caracteres')
