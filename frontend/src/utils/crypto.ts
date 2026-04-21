import CryptoJS from 'crypto-js'

// 固定密钥（需要与后端一致）
const SECRET_KEY = 'wujie-im-aes-256-key-2024-change-in-production'
// 固定 IV（简化版，生产环境应随机生成并传输）
const IV = '1234567890123456'

/**
 * AES-CBC 加密
 */
export function encrypt(text: string): string {
  const key = CryptoJS.enc.Utf8.parse(SECRET_KEY.substring(0, 32))
  const iv = CryptoJS.enc.Utf8.parse(IV)

  const encrypted = CryptoJS.AES.encrypt(text, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7
  })

  // 直接取 ciphertext 并转为 Base64，避免 OpenSSL 格式混入 Salt 元数据
  return CryptoJS.enc.Base64.stringify(encrypted.ciphertext)
}

/**
 * AES-CBC 解密
 */
export function decrypt(cipherText: string): string {
  console.log('[Decrypt] 密文前50字符:', cipherText.substring(0, 50))
  const key = CryptoJS.enc.Utf8.parse(SECRET_KEY.substring(0, 32))
  const iv = CryptoJS.enc.Utf8.parse(IV)

  const decrypted = CryptoJS.AES.decrypt(cipherText, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7
  })

  // 使用 stringify 代替 toString
  const result = CryptoJS.enc.Utf8.stringify(decrypted)
  console.log('[Decrypt] 解密结果:', result)
  if (!result) {
    throw new Error('AES解密结果为空')
  }
  return result
}

export function md5(text: string): string {
  return CryptoJS.MD5(text).toString()
}
