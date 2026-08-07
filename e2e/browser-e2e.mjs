// Recorrido completo dentro de un navegador real: login, feed, publicar,
// like, y tiempo real entre la web (:5173) y la app móvil (:5174).
import puppeteer from 'puppeteer-core'

const CHROME = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'
const ok = (m, d = '') => console.log(`  ✓ ${m.padEnd(50)} ${d}`)
const fail = (m, d = '') => { console.log(`  ✗ ${m.padEnd(50)} ${d}`); process.exitCode = 1 }

const browser = await puppeteer.launch({
  executablePath: CHROME,
  headless: 'new',
  args: ['--no-sandbox', '--disable-dev-shm-usage'],
})

const errores = []
async function abrir(url, etiqueta) {
  const page = await browser.newPage()
  page.on('pageerror', (e) => errores.push(`[${etiqueta}] ${e.message}`))
  page.on('console', (m) => m.type() === 'error' && errores.push(`[${etiqueta}] ${m.text()}`))
  await page.goto(url, { waitUntil: 'networkidle2' })
  return page
}

console.log('\n— WEB (:5173) —')
const web = await abrir('http://localhost:5173', 'web')
await web.waitForSelector('.auth__title')
ok('pantalla de login renderizada')

await web.click('button[type="submit"]')
await web.waitForSelector('.post', { timeout: 15000 })
ok('login correcto, feed cargado')

const alias = await web.$eval('.topbar__link', (e) => e.textContent)
ok('sesión iniciada', alias)

const nPosts = await web.$$eval('.post', (n) => n.length)
ok('publicaciones en el feed', String(nPosts))

const enVivo = await web.$eval('.pill', (e) => e.textContent)
enVivo.includes('en vivo') ? ok('WebSocket conectado', `«${enVivo}»`) : fail('WebSocket', enVivo)

// Publicar
const mensaje = `Publicado desde el navegador ${Date.now()}`
await web.type('.composer__input', mensaje)
await web.click('.composer button[type="submit"]')
await web.waitForFunction(
  (m) => [...document.querySelectorAll('.post__message')].some((p) => p.textContent === m),
  { timeout: 15000, polling: 300 }, mensaje,
)
ok('publicar desde la interfaz')

// Botón de like deshabilitado en publicaciones propias
const propioDeshabilitado = await web.$$eval('.post', (posts) =>
  posts.filter((p) => p.querySelector('.badge')).every((p) => p.querySelector('.like').disabled))
propioDeshabilitado ? ok('like deshabilitado en las propias') : fail('like propio habilitado')

console.log('\n— MÓVIL (:5174) —')
const movil = await abrir('http://localhost:5174', 'movil')
await new Promise((r) => setTimeout(r, 1500))
const textoMovil = await movil.evaluate(() => document.body.innerText)
textoMovil.includes('Periferia') ? ok('app móvil renderizada') : fail('app móvil en blanco')

const btnMovil = (await movil.evaluateHandle(() =>
  [...document.querySelectorAll('*')].find((e) => e.innerText?.trim() === 'Entrar'))).asElement()
const cajaBtn = await btnMovil.boundingBox()
await movil.mouse.click(cajaBtn.x + cajaBtn.width / 2, cajaBtn.y + cajaBtn.height / 2)

// Esperar a que el FlatList haya pintado de verdad, no solo a que cambie la sesión.
await movil.waitForFunction(() => document.body.innerText.includes('♡'), { timeout: 20000, polling: 300 })
ok('login desde la app móvil', '@mafe')

const estadoMovil = await movil.evaluate(() =>
  document.body.innerText.includes('en vivo') ? 'en vivo' : 'reconectando')
estadoMovil === 'en vivo' ? ok('WebSocket del móvil conectado') : fail('WebSocket del móvil', estadoMovil)

const postsMovil = await movil.evaluate(() => (document.body.innerText.match(/[♡♥]/g) || []).length)
ok('publicaciones en el feed móvil', String(postsMovil))

console.log('\n— TIEMPO REAL ENTRE AMBAS —')

// Instantánea de todos los contadores de la web ANTES de tocar nada.
const antes = await web.evaluate(() =>
  [...document.querySelectorAll('.post')].map((p) => p.querySelector('.like__count').textContent).join(','))
ok('contadores de la web antes', antes)

// El móvil (mafe) da like a la primera publicación que puede.
// Se usa el ratón REAL de Puppeteer: Pressable de react-native-web escucha
// eventos de puntero, y un .click() sintético del DOM no los dispara.
const caja = await movil.evaluate(() => {
  const c = [...document.querySelectorAll('*')].find(
    (e) => e.textContent?.trim() === '♡' && e.children.length === 0)
  if (!c) return null
  const r = c.getBoundingClientRect()
  return { x: r.x + r.width / 2, y: r.y + r.height / 2 }
})

if (!caja) {
  fail('no se encontró ningún corazón likeable en el móvil')
} else {
  await movil.mouse.click(caja.x, caja.y)
  ok('like dado desde la app móvil')

  // La web NO recarga: el cambio solo puede llegar por el WebSocket.
  const llego = await web
    .waitForFunction((prev) =>
      [...document.querySelectorAll('.post')]
        .map((p) => p.querySelector('.like__count').textContent).join(',') !== prev,
      { timeout: 12000, polling: 300 }, antes)
    .then(() => true).catch((e) => { console.log('    [waitForFunction]', e.message.split('\n')[0]); return false })

  const despues = await web.evaluate(() =>
    [...document.querySelectorAll('.post')].map((p) => p.querySelector('.like__count').textContent).join(','))

  llego
    ? ok('LIKE DEL MÓVIL VISTO EN LA WEB EN VIVO', `${antes} → ${despues}`)
    : fail('el contador de la web no cambió', `sigue en ${despues}`)
}

console.log('\n— ERRORES DE CONSOLA —')
errores.length ? errores.forEach((e) => fail(e)) : ok('ninguno en ninguna de las dos apps')

await browser.close()
console.log()
