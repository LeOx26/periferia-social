# Sirve la app React Native compilada a navegador con react-native-web.
#
# NO es la app nativa: es la MISMA capa de vista (View, Text, Pressable,
# FlatList, StyleSheet) traducida a DOM, consumiendo el mismo packages/core.
# Existe para que se pueda revisar la interfaz móvil y verificar la reutilización
# del core sin instalar Xcode ni el SDK de Android.
#
# El contexto de build es la raíz del monorepo, porque depende del workspace.

# --- build ---
FROM node:22-alpine AS build
WORKDIR /repo

RUN corepack enable

COPY pnpm-workspace.yaml pnpm-lock.yaml .npmrc ./
COPY packages/core/package.json packages/core/
COPY apps/mobile/package.json apps/mobile/
RUN pnpm install --frozen-lockfile --filter @periferia/core --filter mobile

COPY packages/core packages/core
COPY apps/mobile apps/mobile

RUN pnpm --filter @periferia/core build \
 && cd apps/mobile \
 && npx expo export --platform web --output-dir dist

# --- runtime ---
FROM nginx:alpine
COPY apps/mobile/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /repo/apps/mobile/dist /usr/share/nginx/html
EXPOSE 80
