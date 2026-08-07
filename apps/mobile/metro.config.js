const { getDefaultConfig } = require('expo/metro-config')
const path = require('node:path')

const projectRoot = __dirname
const workspaceRoot = path.resolve(projectRoot, '../..')

const config = getDefaultConfig(projectRoot)

// Metro solo vigila la carpeta del proyecto por defecto. Sin esto no ve
// packages/core y falla al resolverlo dentro del monorepo.
config.watchFolders = [workspaceRoot]
config.resolver.nodeModulesPaths = [
  path.resolve(projectRoot, 'node_modules'),
  path.resolve(workspaceRoot, 'node_modules'),
]

/**
 * Fuerza que React y react-query se resuelvan SIEMPRE desde esta app, nunca desde
 * la copia que `packages/core` tiene para sus propios tests.
 *
 * Dos copias de React dejan el dispatcher de hooks a null («Cannot read
 * properties of null (reading 'useCallback')») y dos copias de react-query crean
 * dos contextos, de modo que el QueryClientProvider se vuelve invisible para los
 * hooks del core. Hoy funcionaría igual sin esto porque las versiones coinciden,
 * pero eso es una coincidencia, no una garantía.
 */
config.resolver.extraNodeModules = {
  react: path.resolve(projectRoot, 'node_modules/react'),
  'react-dom': path.resolve(projectRoot, 'node_modules/react-dom'),
  '@tanstack/react-query': path.resolve(projectRoot, 'node_modules/@tanstack/react-query'),
}

module.exports = config
