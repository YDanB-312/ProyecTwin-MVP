// Anti-flicker: pinta el tema antes del primer paint. Preferencia en una
// cookie (no localStorage); por defecto claro. Se carga como archivo externo
// para que la CSP pueda usar script-src 'self' sin scripts inline.
try {
  var __m = document.cookie.match(/(?:^|; )theme=([^;]*)/)
  var __t = __m ? decodeURIComponent(__m[1]) : 'light'
  document.documentElement.dataset.theme = __t === 'dark' ? 'dark' : 'light'
} catch {
  document.documentElement.dataset.theme = 'light'
}
