# Prueba de extremo a extremo en navegador

Recorre la aplicación real con Chrome: login, feed, publicar, like, y la
propagación del like **desde la app móvil hacia la web por WebSocket**.

Existe porque tres fallos de este proyecto eran invisibles fuera de un navegador:

1. **CORS ausente.** La política la aplica el navegador, no el servidor: la API
   devolvía `200` y todas las verificaciones por `curl` pasaban, mientras la web
   habría fallado en silencio al no poder leer ninguna respuesta.
2. **Dos copias de React** en el bundle (el core declara React como
   `peerDependency` pero tiene la suya para sus tests). Síntoma: pantalla en
   blanco y `Cannot read properties of null (reading 'useCallback')`.
3. **Dos copias de `@tanstack/react-query`**, que son dos contextos distintos, de
   modo que el `QueryClientProvider` resultaba invisible para los hooks del core.

Ninguno lo detectaban los 85 tests de backend y core.

## Ejecutar

```bash
docker compose up -d --build     # los siete contenedores
pnpm add -D puppeteer-core       # usa el Chrome ya instalado, no descarga nada
node e2e/browser-e2e.mjs
```

Nota: el sondeo va por temporizador (`polling: 300`) y no por
`requestAnimationFrame`, que es el valor por defecto de Puppeteer, **porque rAF
no se ejecuta en pestañas de segundo plano** y el test observa dos pestañas a la
vez. Con el valor por defecto, la comprobación del tiempo real falla aunque la
aplicación funcione.
