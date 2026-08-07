# El contexto de build es la raíz del monorepo, porque la web depende del
# workspace @periferia/core y necesita compilarlo antes que a sí misma.

# --- build ---
FROM node:22-alpine AS build
WORKDIR /repo

RUN corepack enable

# Copiar primero los manifiestos aprovecha la caché de capas: las dependencias
# solo se reinstalan si cambia un package.json o el lockfile.
COPY pnpm-workspace.yaml pnpm-lock.yaml .npmrc ./
COPY packages/core/package.json packages/core/
COPY apps/web/package.json apps/web/
RUN pnpm install --frozen-lockfile --filter @periferia/core --filter web

COPY packages/core packages/core
COPY apps/web apps/web

# Vite compila las variables VITE_* dentro del bundle, así que se fijan aquí.
# Apuntan al host porque quien abre el navegador es la máquina del evaluador,
# no el contenedor: dentro de la red de Docker estos nombres no resolverían.
ARG VITE_AUTH_URL=http://localhost:8081
ARG VITE_SOCIAL_URL=http://localhost:8082
ARG VITE_WS_URL=ws://localhost:8082
ENV VITE_AUTH_URL=$VITE_AUTH_URL \
    VITE_SOCIAL_URL=$VITE_SOCIAL_URL \
    VITE_WS_URL=$VITE_WS_URL

RUN pnpm --filter @periferia/core build && pnpm --filter web build

# --- runtime ---
FROM nginx:alpine
COPY apps/web/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /repo/apps/web/dist /usr/share/nginx/html
EXPOSE 80
