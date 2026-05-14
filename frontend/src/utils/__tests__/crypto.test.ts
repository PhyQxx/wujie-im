import { describe, it, expect } from 'vitest'
import { encrypt, decrypt } from '../crypto'

describe('crypto utils', () => {
  it('encrypt should return different string from input', () => {
    const plainText = 'Hello, World!'
    const encrypted = encrypt(plainText)

    expect(encrypted).toBeTruthy()
    expect(encrypted).not.toBe(plainText)
  })

  it('decrypt should return original text', () => {
    const plainText = 'Hello, World!'
    const encrypted = encrypt(plainText)

    const decrypted = decrypt(encrypted)

    expect(decrypted).toBe(plainText)
  })

  it('encrypt and decrypt should work with Chinese text', () => {
    const plainText = '你好，世界！'
    const encrypted = encrypt(plainText)

    const decrypted = decrypt(encrypted)

    expect(decrypted).toBe(plainText)
  })

  it('encrypt and decrypt should work with JSON string', () => {
    const plainText = '{"userId":123,"username":"test"}'
    const encrypted = encrypt(plainText)

    const decrypted = decrypt(encrypted)

    expect(decrypted).toBe(plainText)
  })

  it('encrypt should work with empty string', () => {
    const plainText = ''
    const encrypted = encrypt(plainText)

    expect(encrypted).toBeTruthy()
    expect(encrypted.length).toBeGreaterThan(0)
  })
})
