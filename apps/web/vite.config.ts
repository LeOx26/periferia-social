import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  resolve: {
    /**
     * Imprescindible en un monorepo con un paquete compartido que declara React
     * como peerDependency.
     *
     * Sin esto, `@periferia/core` resuelve su propia copia de React (la que tiene
     * para sus tests) mientras la app usa la suya, y acaban DOS Reacts en el
     * bundle. El síntoma es una pantalla en blanco con
     * "Cannot read properties of null (reading 'useCallback')": el segundo React
     * no comparte el dispatcher de hooks del primero.
     *
     * `@tanstack/react-query` sufre lo mismo por otra vía: usa un contexto de
     * React, así que dos copias significan dos contextos distintos y el
     * `QueryClientProvider` de la app resulta invisible para los hooks del core
     * ("No QueryClient set, use QueryClientProvider to set one").
     *
     * `dedupe` obliga a resolver siempre la copia de esta aplicación.
     */
    dedupe: ['react', 'react-dom', '@tanstack/react-query'],
  },
})
