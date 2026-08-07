import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    // 'node' a propósito: si algún test del core necesitara jsdom, sería señal
    // de que el core ha empezado a depender del DOM y ya no sirve para móvil.
    environment: 'node',
    include: ['src/**/*.test.ts', 'src/**/*.test.tsx'],
  },
})
