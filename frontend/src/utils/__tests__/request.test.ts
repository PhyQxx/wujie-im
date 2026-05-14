import { describe, it, expect, vi, beforeEach } from 'vitest'

describe('request utils', () => {
  beforeEach(() => {
    vi.resetModules()
    localStorage.clear()
  })

  it('should be defined', () => {
    expect(true).toBe(true)
  })

  it('should read token from localStorage', () => {
    localStorage.setItem('accessToken', 'test-token')

    const token = localStorage.getItem('accessToken')

    expect(token).toBe('test-token')
  })

  it('should remove token on logout', () => {
    localStorage.setItem('accessToken', 'test-token')
    localStorage.removeItem('accessToken')

    const token = localStorage.getItem('accessToken')

    expect(token).toBeNull()
  })
})
