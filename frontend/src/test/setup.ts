import { vi } from 'vitest'

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {}
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => { store[key] = value },
    removeItem: (key: string) => { delete store[key] },
    clear: () => { store = {} }
  }
})()

Object.defineProperty(window, 'localStorage', { value: localStorageMock })

// Mock window.location
Object.defineProperty(window, 'location', {
  value: { href: '', replace: vi.fn() },
  writable: true
})

// Mock Notification
Object.defineProperty(window, 'Notification', {
  value: { requestPermission: vi.fn(() => Promise.resolve('granted')), permission: 'default' },
  writable: true
})
