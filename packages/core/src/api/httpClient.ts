import type { ZodType } from 'zod'

/**
 * Único tipo de error que sale del core. Quien llama nunca tiene que distinguir
 * entre un TypeError de red, un SyntaxError de parseo y un error de la API.
 */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly title: string,
    readonly detail?: string,
    readonly correlationId?: string,
    readonly problemType?: string,
  ) {
    super(title)
    this.name = 'ApiError'
  }
}

export interface ApiFetchOptions<T> {
  method?: 'GET' | 'POST' | 'DELETE'
  body?: unknown
  token?: string | null
  schema?: ZodType<T>
}

const newCorrelationId = () =>
  globalThis.crypto?.randomUUID?.() ?? `cli-${Date.now()}-${Math.random().toString(16).slice(2)}`

export async function apiFetch<T = unknown>(
  baseUrl: string,
  path: string,
  options: ApiFetchOptions<T> = {},
): Promise<T> {
  const { method = 'GET', body, token, schema } = options

  const headers: Record<string, string> = {
    Accept: 'application/json',
    // El backend registra este mismo id en sus logs JSON: un fallo que reporte
    // el usuario se puede rastrear hasta la línea exacta del servicio.
    'X-Correlation-Id': newCorrelationId(),
  }
  if (token) headers.Authorization = `Bearer ${token}`
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  let response: Response
  try {
    response = await fetch(`${baseUrl}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    // fetch solo rechaza por fallo de red, nunca por un código de error HTTP.
    throw new ApiError(
      0,
      'No hay conexión con el servidor',
      'Comprueba que el backend esté levantado.',
    )
  }

  const raw = await response.text()
  let parsed: unknown
  if (raw) {
    try {
      parsed = JSON.parse(raw)
    } catch {
      // Un 502 de un proxy devuelve HTML, no JSON. No puede reventar aquí.
      parsed = undefined
    }
  }

  if (!response.ok) {
    const problem = (parsed ?? {}) as Record<string, unknown>
    throw new ApiError(
      response.status,
      typeof problem.title === 'string' ? problem.title : `Error ${response.status}`,
      typeof problem.detail === 'string' ? problem.detail : undefined,
      typeof problem.correlationId === 'string' ? problem.correlationId : undefined,
      typeof problem.type === 'string' ? problem.type : undefined,
    )
  }

  if (!schema) return parsed as T

  const validation = schema.safeParse(parsed)
  if (!validation.success) {
    throw new ApiError(
      response.status,
      'El servidor devolvió una respuesta inesperada',
      validation.error.issues
        .map((issue) => `${issue.path.join('.')}: ${issue.message}`)
        .join('; '),
    )
  }
  return validation.data
}
