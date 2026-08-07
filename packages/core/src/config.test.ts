import { beforeEach, describe, expect, it } from 'vitest'
import { configureCore, getConfig, resetConfig } from './config'

const storage = {
  getItem: async () => null,
  setItem: async () => {},
  removeItem: async () => {},
}

describe('configureCore', () => {
  beforeEach(() => resetConfig())

  it('throws a helpful error when the core was never configured', () => {
    expect(() => getConfig()).toThrow(/configureCore/)
  })

  it('exposes the configuration once set', () => {
    configureCore({
      authBaseUrl: 'http://localhost:8081',
      socialBaseUrl: 'http://localhost:8082',
      wsBaseUrl: 'ws://localhost:8082',
      storage,
    })

    expect(getConfig().authBaseUrl).toBe('http://localhost:8081')
    expect(getConfig().wsBaseUrl).toBe('ws://localhost:8082')
  })

  it('strips trailing slashes so URL building never doubles them', () => {
    configureCore({
      authBaseUrl: 'http://localhost:8081/',
      socialBaseUrl: 'http://localhost:8082/',
      wsBaseUrl: 'ws://localhost:8082/',
      storage,
    })

    expect(getConfig().authBaseUrl).toBe('http://localhost:8081')
    expect(getConfig().socialBaseUrl).toBe('http://localhost:8082')
  })
})
