# Periferia Social

Red social mínima —autenticación, feed, publicaciones, perfil y likes en tiempo real— construida con dos microservicios Spring Boot, una web en React y una app móvil en React Native que comparte el 100 % de la lógica con la web.

El documento de arquitectura completo, con las decisiones y sus alternativas descartadas, está en [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md) y en [`docs/Periferia-Social-Arquitectura.pdf`](docs/Periferia-Social-Arquitectura.pdf).

> **La respuesta a «¿lo habrías resuelto con microservicios?»** está en [Microservicios frente a monolito modular](#microservicios-frente-a-monolito-modular). Es la sección más importante de este documento.

---

## Dónde está cada requisito

| Requisito del enunciado | Dónde está | Cómo comprobarlo |
|---|---|---|
| Login con JWT (POST) | `backend/auth-service` → `AuthController` | http://localhost:8081/docs |
| Listar publicaciones (GET) | `backend/social-service` → `PostController` | http://localhost:8082/docs |
| Crear publicación (POST) | `PostController.create` | Composer de la web |
| Envío de like (POST) | `LikeController` | Botón ♡ del feed |
| Ver perfil (GET) | `auth-service` → `UserController.me` | Pantalla «@usuario» |
| Likes en tiempo real (WebSocket) | `LikeWebSocketHandler` | Dos ventanas del navegador |
| Microservicios | `auth-service` + `social-service` | `docker compose ps` |
| Seeder al iniciar | Migraciones Flyway `V2__*.sql` | 5 usuarios y 5 publicaciones |
| Dockerfile por servicio | `backend/*/Dockerfile` | Multi-stage, usuario sin privilegios |
| PostgreSQL con ORM | JPA/Hibernate, esquema por Flyway | `docker exec periferia-postgres psql -U periferia -l` |
| Procedimientos almacenados | **Omitidos, justificado** | [Ver razón](#sin-procedimientos-almacenados-en-plpgsql) |
| Pantallas web (login, perfil, feed) | `apps/web/src/screens/` | http://localhost:5173 |
| TypeScript | Todo el frontend | `pnpm --filter web build` |
| Estado agnóstico de plataforma | Zustand + TanStack Query en `packages/core` | Consumido por web y móvil |
| App móvil que reutiliza la lógica | `apps/mobile` | [Reutilización medida](#reutilización-entre-web-y-móvil-medida) |
| Paquete `core` compartido | `packages/core` | 711 líneas, 35 tests |
| Swagger en `/docs` | Ambos servicios | :8081/docs y :8082/docs |
| Pruebas unitarias e integración | 82 tests | [Pruebas](#pruebas) |
| Manejo de errores | RFC 7807 Problem Details | Provoca un 409 dando like a lo tuyo |
| Logs y auditoría | JSON con `correlationId` | `docker compose logs social-service` |
| Observabilidad | Actuator + Prometheus + Grafana | http://localhost:3000 |
| DDD, reglas en el dominio | `Post.java`, `PostTest.java` | 11 reglas en 42 ms |
| Script de BD con usuarios | `V2__seed_users.sql` | UUID fijos, hash BCrypt |
| Trade-offs documentados | [Decisiones y trade-offs](#decisiones-y-trade-offs) | Este documento |

---

## Si solo tienes diez minutos

1. `docker compose up -d --build` — un solo comando, sin instalar nada más. Espera a que `docker compose ps` muestre los siete contenedores.
2. Entra en http://localhost:5173. Las credenciales vienen precargadas: pulsa Entrar, publica algo y da un like.
3. Abre **http://localhost:5174** al lado: es la app móvil. Entra con `mafe`. **Da like en una pestaña y mira el contador saltar en la otra**, sin recargar. Eso es el WebSocket y el `core` compartido funcionando a la vez.
4. Mira el contrato de la API en http://localhost:8082/docs y el dashboard en http://localhost:3000.
5. Lee [Microservicios frente a monolito modular](#microservicios-frente-a-monolito-modular) y [Reutilización medida](#reutilización-entre-web-y-móvil-medida).

Lo que mejor resume el trabajo son esos dos apartados y el archivo [`packages/core/src/hooks/feedCache.ts`](packages/core/src/hooks/feedCache.ts), donde vive la reconciliación entre el update optimista y el evento del WebSocket.

---

## Arranque

**Para ver la aplicación funcionando solo hace falta Docker.** Los microservicios y la web se compilan dentro de contenedores con builds multi-stage: no necesitas Java, ni Maven, ni Node instalados en tu máquina.

Node 20+ y pnpm solo hacen falta para desarrollar o para arrancar la app móvil (verificado con Node 26 y pnpm 11). Para el simulador de iOS, Xcode; la app móvil también se abre en el navegador sin él.

### Todo en un comando

```bash
docker compose up -d --build
```

Levanta siete contenedores y deja **todo usable sin instalar nada más** — ni Node, ni pnpm, ni Java, ni Xcode:

| | URL | Qué es |
|---|---|---|
| Web | http://localhost:5173 | La aplicación React |
| **App móvil** | **http://localhost:5174** | La app React Native renderizada en navegador |
| Swagger auth | http://localhost:8081/docs | |
| Swagger social | http://localhost:8082/docs | |
| Prometheus | http://localhost:9090 | |
| Grafana (sin login) | http://localhost:3000 | Dashboard con 7 paneles |

> **Sobre `:5174`.** Es la app de `apps/mobile` compilada con `react-native-web`: **la misma capa de vista** (`View`, `Text`, `Pressable`, `FlatList`, `StyleSheet`) traducida a DOM, consumiendo el mismo `packages/core`. Está para que puedas revisar la interfaz móvil y comprobar la reutilización **sin compilar nada ni abrir un simulador**.
>
> **No es la app nativa.** Para verla como tal hace falta el simulador (abajo). El simulador de iOS no se puede contenedorizar: solo corre en macOS bajo Xcode.

### Desarrollo, o para ver la app nativa de verdad

```bash
pnpm install

pnpm web                         # web con recarga en caliente, :5173
pnpm mobile                      # simulador de iOS: la app nativa
pnpm mobile:web                  # la app móvil en el navegador, sin Xcode
```

Los tres compilan `packages/core` antes de arrancar, así que no hay que recordar el orden.

### Credenciales

| Usuario | Contraseña |
|---|---|
| `leo`, `mafe`, `carlos`, `ana`, `diego` | `Periferia2026!` |

### URLs

| Servicio | URL |
|---|---|
| Web | http://localhost:5173 |
| Swagger — auth-service | http://localhost:8081/docs |
| Swagger — social-service | http://localhost:8082/docs |
| Prometheus | http://localhost:9090 |
| Grafana (sin login) | http://localhost:3000 |

### Cómo ver los likes en tiempo real

Abre la web en dos ventanas (una en incógnito) con usuarios distintos, o la web con `leo` y el simulador con `mafe`. Da like en una y **el contador cambia en la otra sin recargar**. El indicador «en vivo» de la barra superior muestra el estado del WebSocket.

---

## Arquitectura

```
┌──────────────┐   ┌──────────────┐
│ apps/web     │   │ apps/mobile  │   ambos consumen packages/core
│ React + Vite │   │ Expo / RN    │
└──────┬───────┘   └──────┬───────┘
       │   HTTP + WebSocket │
       ├────────────────────┴────────────┐
       ▼                                 ▼
┌────────────────────┐        ┌──────────────────────────┐
│ auth-service :8081 │        │ social-service :8082     │
│ login JWT, perfil  │        │ posts, likes, /ws/likes  │
└─────────┬──────────┘        └────────────┬─────────────┘
          ▼                                ▼
    ┌───────────┐                    ┌────────────┐
    │  authdb   │                    │  socialdb  │
    └───────────┘                    └────────────┘

  prometheus:9090 ──scrape──> /actuator/prometheus
  grafana:3000    ──> dashboard provisionado
```

**Los dos servicios nunca se llaman entre sí.** `social-service` verifica los JWT en local con el secreto compartido y obtiene el id y el alias del autor de los propios claims.

---

## Decisiones y trade-offs

### Microservicios frente a monolito modular

El enunciado exige microservicios y así está resuelto, con dos servicios que corresponden a dos *bounded contexts* reales: identidad y contenido social.

**Con libertad de elección, este dominio se resolvería mejor con un monolito modular.** Las razones son concretas, no ideológicas:

- Los dos contextos comparten el mismo ciclo de vida y el mismo equipo. Separarlos añade coste operativo sin desacoplar nada que necesitara desacoplarse.
- La ausencia de transacciones distribuidas obliga a coordinar a mano los datos de siembra entre bases (ver más abajo), un problema que un monolito no tiene.
- El volumen esperado no justifica escalar los servicios por separado, que es el argumento principal a favor de los microservicios.

Un monolito modular con las mismas fronteras internas —paquetes `auth` y `feed`, sin dependencias cruzadas— daría el mismo aislamiento conceptual, permitiría extraer un servicio el día que hiciera falta, y evitaría todo el coste de red y despliegue. Los microservicios son la respuesta correcta cuando los contextos tienen **ritmos de cambio o de escalado distintos**, y aquí no los tienen.

### Sin procedimientos almacenados en PL/pgSQL

El enunciado los marca opcionales y pide justificar la decisión. **No se han usado**, y las reglas de negocio viven en el agregado `Post`:

- El mensaje no puede estar vacío ni superar los 280 caracteres.
- No puedes dar like a tu propia publicación.
- No puedes dar like dos veces.
- Retirar un like inexistente es un no-op, no un error.

Un dominio rico se testea sin infraestructura: las once reglas se verifican en **42 milisegundos**, sin Spring, sin base de datos y sin contenedores. Compruébalo:

```bash
cd backend/social-service && ./mvnw test -Dtest=PostTest
# Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.042 s
```

La misma lógica en PL/pgSQL exigiría levantar Postgres y cargar el procedimiento para probar cada caso, quedaría fuera del control de versiones habitual y no sería refactorizable con las herramientas del lenguaje.

La restricción `PRIMARY KEY (post_id, user_id)` sí existe en la base, pero como **red de seguridad ante concurrencia**, no como sede de la regla. Hay un test de integración que la comprueba insertando por SQL directo, sin pasar por el dominio.

### Bases separadas y ausencia de claves foráneas

Cada servicio es dueño de su base (`authdb` y `socialdb`) dentro de un único contenedor Postgres. No hay clave foránea entre `posts.author_id` y `users.id`: pertenecen a contextos distintos.

**Consecuencia asumida:** el alias del autor va denormalizado en `posts.author_alias`, copiado del claim del JWT al publicar. El feed no necesita ninguna consulta fuera de su propia base.

**El punto débil, dicho sin adornos:** los cinco usuarios de prueba se siembran con UUID fijos declarados a mano en las migraciones de **ambas** bases. Es la única forma honesta de sembrar dos bases independientes, y no escala. En un sistema real, `auth-service` publicaría un evento `UserRegistered` que `social-service` consumiría para construir su propia proyección de autores.

### JWT HS256 verificado en local

`social-service` no llama a `auth-service` en ninguna petición: verifica la firma en memoria con el secreto compartido y lee `sub` y `alias` de los claims.

**Lo que se gana:** cero latencia de red por petición, y el feed sigue funcionando aunque `auth-service` esté caído.

**Lo que se pierde:** no se puede revocar un token antes de que expire. Se mitiga con expiración corta (60 minutos en desarrollo) y se resolvería con refresh tokens.

**La evolución correcta es RS256 con JWKS:** `auth-service` firmaría con una clave privada que nunca sale de él y `social-service` solo necesitaría la pública, descargable de un endpoint estándar. Desaparece el secreto compartido y la rotación se vuelve automática. Es el modelo de Auth0, Keycloak y Cognito. No se implementó por tiempo.

**Para comprobar que no hay comunicación entre servicios**, apaga `auth-service` y usa un token ya emitido:

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"leo","password":"Periferia2026!"}' | python3 -c "import json,sys; print(json.load(sys.stdin)['accessToken'])")

docker compose stop auth-service

# El feed sigue funcionando con auth-service caído
curl -s "http://localhost:8082/api/posts" -H "Authorization: Bearer $TOKEN"

docker compose start auth-service
```

Y para comprobar que un token falsificado se rechaza sin consultar a nadie, basta con alterar un carácter de la firma: `social-service` responde `401`.

### Token en el query param del WebSocket

El objeto `WebSocket` del navegador no permite enviar cabeceras en el handshake, así que el token viaja como parámetro de consulta y se valida en un `HandshakeInterceptor` propio.

**El riesgo:** el JWT puede acabar en los logs de acceso del servidor o en el historial del navegador. **La solución en producción:** un ticket de un solo uso y vida corta, emitido por un endpoint HTTP y canjeado en el handshake.

### WebSocket nativo, sin STOMP ni SockJS

El payload es un único tipo de evento (`{type, postId, likeCount}`). STOMP añadiría enrutado por destinos y negociación de suscripciones que aquí no hacen falta, y SockJS da problemas en React Native, donde no existen los transportes de respaldo que asume.

Al usar el `WebSocket` nativo, **el mismo cliente funciona sin cambios en navegador y en móvil**, que es justo lo que permite compartirlo.

### Código duplicado entre servicios en vez de librería compartida

`CorrelationIdFilter`, `ApiExceptionHandler` y la verificación del JWT están duplicados en los dos servicios. Es deliberado: una librería común entre microservicios los ata a **desplegarse en lockstep**, que es precisamente lo que se intenta evitar al separarlos. Son unas cuarenta líneas por servicio a cambio de que cada uno pueda evolucionar su contrato de errores por su cuenta.

### El feed muestra todas las publicaciones

El enunciado dice «listar publicaciones de los demás usuarios». La lectura literal sería ocultar las propias, pero entonces publicas algo y desaparece de la pantalla, lo cual es confuso y perjudica la demostración.

**Se muestran todas, marcando las propias con `isOwn`**, y el espíritu del requisito se hace cumplir donde de verdad importa: no puedes dar like a tu propia publicación, ni en el cliente (el botón está deshabilitado) ni en el servidor (responde `409`).

### Un solo repositorio

El enunciado se contradice: en el objetivo menciona «repositorio(s) separados backend/frontend» y en los entregables «repositorio en GitHub con backend y frontend». Se ha elegido un monorepo porque es lo único coherente con exigir un `packages/core` compartido entre web y móvil: con repos separados haría falta publicar el paquete en un registro para cada cambio.

### Decisiones menores

- **Hoja de estilos propia en la web** en lugar de Tailwind o una librería de componentes. Son tres pantallas; una dependencia menos que justificar y que mantener.
- **Un condicional en vez de react-navigation en el móvil.** Con dos pantallas y una sola transición (autenticado / anónimo), instalar un enrutador sería añadir peso sin resolver nada. Con una tercera pantalla, cambiaría.
- **Sin Lombok en el backend.** Es un procesador de anotaciones que rompe con frecuencia al cambiar de versión de JDK. Los `record` de Java cubren los DTOs y las entidades son pocas.
- **Java 21 LTS** pese a que Spring Boot 4.1 admite hasta Java 26: el ecosistema de pruebas (ByteBuddy, Mockito) suele ir por detrás con JDKs recientes.

---

## Reutilización entre web y móvil, medida

| | Líneas |
|---|---|
| `packages/core` — compartido íntegro | **711** |
| `apps/web` — solo capa de vista | 344 |
| `apps/mobile` — solo capa de vista | 314 |

Lo único que se duplica por diseño son los adaptadores de almacenamiento: 17 líneas en la web (`localStorage`) y 35 en el móvil (Keychain/Keystore, con respaldo para Expo Web).

Las cifras salen de:

```bash
find packages/core/src -name '*.ts' ! -name '*.test.ts' -exec cat {} + | wc -l
find apps/web/src    -name '*.tsx' -exec cat {} + | wc -l
find apps/mobile/src apps/mobile/App.tsx -name '*.tsx' -exec cat {} + | wc -l
```

**Fuera de `packages/core` no hay una sola llamada a `fetch`, ni un `new WebSocket`, ni una línea de lógica de negocio.** Verificable en un comando:

```bash
grep -rnE "fetch\(|new WebSocket" apps/web/src apps/mobile/src apps/mobile/App.tsx
# sin resultados
```

Ambas apps consumen los mismos hooks:

```tsx
const feed = usePosts()
const like = useLikePost()
const realtimeStatus = useRealtimeLikes()
```

Ese bloque es idéntico en [`apps/web/src/screens/FeedScreen.tsx`](apps/web/src/screens/FeedScreen.tsx) y en [`apps/mobile/src/screens/FeedScreen.tsx`](apps/mobile/src/screens/FeedScreen.tsx). Lo que cambia es que uno pinta `<article>` y el otro `<View>`.

Conviene abrir los dos ficheros en paralelo: es la forma más rápida de comprobar que la reutilización es real y no una afirmación del README.

La costura entre plataformas es **una sola función**:

```ts
configureCore({ authBaseUrl, socialBaseUrl, wsBaseUrl, storage })
```

---

## Pruebas

```bash
# Backend — 47 tests. No requieren el docker-compose levantado:
# Testcontainers arranca su propio Postgres efímero.
cd backend/auth-service   && ./mvnw test
cd backend/social-service && ./mvnw test

# Core compartido — 35 tests
pnpm test
```

| Nivel | Qué cubre |
|---|---|
| Dominio (JUnit) | Las reglas de negocio, sin Spring ni base de datos. 42 ms. |
| Controladores (MockMvc) | Contratos, códigos de estado, formato Problem Details |
| Integración (Testcontainers) | El recorrido completo contra un Postgres real |
| Core (Vitest) | Cliente HTTP, store de sesión, reconexión del WebSocket y reconciliación de cache |

Las funciones de reconciliación (`applyLikeEvent`, `applyOptimisticLike`) se extrajeron a **funciones puras** para poder probar el update optimista con rollback y la llegada del evento en tiempo real sin montar React ni TanStack Query.

Un detalle sutil que fija un test: `applyLikeEvent` **nunca modifica `likedByMe`**. El evento del WebSocket es global y solo informa de cuántos likes tiene la publicación, no de quién los dio; tocarlo encendería el corazón del usuario equivocado.

---

## Observabilidad

- **Actuator** con `/actuator/health` (incluye conectividad a la base) y `/actuator/prometheus`.
- **Métricas de negocio** además de las de plataforma: `periferia_posts_published_total`, `periferia_likes_total`, `periferia_websocket_sessions_active`.
- **Logs JSON estructurados** con `correlationId` propagado desde el cliente. El front envía la cabecera `X-Correlation-Id`, el backend la registra en cada línea de log y la devuelve dentro del cuerpo de error: **un fallo reportado por el usuario se rastrea hasta la línea exacta del servicio**.
- **Grafana** con un dashboard provisionado por fichero: peticiones por segundo, latencia p95, likes por minuto y sesiones WebSocket activas.
- **Healthchecks reales** en el `docker-compose`, con `depends_on: condition: service_healthy`. Ningún `sleep` arbitrario.

---

## Qué haría con más tiempo

Por orden de valor:

1. **RS256 con JWKS** en lugar del secreto compartido HS256.
2. **Evento `UserRegistered`** para que `social-service` construya su proyección de autores, eliminando los UUID coordinados a mano.
3. **Refresh tokens** con rotación, para poder acortar la vida del token de acceso a minutos.
4. **Likes con arquitectura orientada a eventos**: hoy el contador se actualiza en la misma transacción; con volumen alto conviene un flujo asíncrono con consistencia eventual.
5. **API gateway** que oculte a los clientes que hay dos servicios detrás.
6. **Tracing distribuido** con OpenTelemetry, cerrando el círculo que ya abre el `correlationId`.
7. **Rate limiting** en login y en creación de publicaciones.
8. **Paginación infinita** en el feed; hoy solo se sirve la primera página.
9. **CI/CD** que ejecute las tres suites y construya las imágenes.

---

## Fuera de alcance, declarado

Registro de usuarios, seguir/dejar de seguir, comentarios, imágenes, notificaciones, edición y borrado de publicaciones, y despliegue en la nube. Se excluyen deliberadamente para dedicar el tiempo a la calidad de lo que sí está.
