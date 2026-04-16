import CryptoJS from 'crypto-js'

const SECRET_KEY = 'wujie-im-secret-key'

export function encrypt(text: string): string {
  return CryptoJS.AES.encrypt(text, SECRET_KEY).toString()
}

export function decrypt(cipherText: string): string {
  const bytes = CryptoJS.AES.decrypt(cipherText, SECRET_KEY)
  return bytes.toString(CryptoJS.enc.Utf8)
}

export function md5(text: string): string {
  return CryptoJS.MD5(text).toString()
}
