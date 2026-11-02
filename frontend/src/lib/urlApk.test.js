import { describe, expect, it } from 'vitest'
import { urlApkSegura } from './urlApk'

describe('urlApkSegura', () => {
  it.each([
    '/apk/ProyecTWin.apk',
    '/apk/ProyecTWin.apk?v=1.4.0',
    'https://cdn.example.com/apk/app.apk',
  ])('acepta %s', (url) => {
    expect(urlApkSegura(url)).not.toBeNull()
  })

  it('normaliza una URL HTTPS absoluta', () => {
    expect(urlApkSegura('HTTPS://EXAMPLE.COM/apk/app.apk')).toBe('https://example.com/apk/app.apk')
  })

  it.each([
    '',
    '   ',
    ' /apk/x.apk',
    '/apk/x.apk ',
    '/apk/x apk.apk',
    '//evil.example/x.apk',
    'http://example.com/x.apk',
    'javascript:alert(1)',
    'data:text/plain,x',
    'file:///C:/x.apk',
    '/apk\\evil.apk',
    '/apk/../x',
    'https://user:pass@example.com/x.apk',
    'https://@example.com/x.apk',
    null,
    undefined,
    123,
  ])('rechaza %s', (url) => {
    expect(urlApkSegura(url)).toBeNull()
  })
})
