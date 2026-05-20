import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.resolve(__dirname, '..')
const dist = path.join(root, 'dist')

const blockedFileNamePatterns = [
  /mapbox/i,
  /maplibre/i,
  /pmtiles/i,
  /l7plot/i,
  /(^|[-_./])l7([-_./]|$)/i
]
const blockedContentPatterns = [
  /mapbox-gl/i,
  /maplibre-gl/i,
  /pmtiles/i,
  /@antv\/l7/i,
  /@antv\/l7plot/i
]

function walk(dir, result = []) {
  for (const item of fs.readdirSync(dir, { withFileTypes: true })) {
    const fullPath = path.join(dir, item.name)
    if (item.isDirectory()) {
      walk(fullPath, result)
    } else {
      result.push(fullPath)
    }
  }
  return result
}

function fail(message, details = []) {
  console.error(message)
  details.slice(0, 20).forEach(item => console.error(` - ${path.relative(root, item)}`))
  process.exit(1)
}

if (!fs.existsSync(dist)) {
  fail('dist 不存在，请先执行 npm run build:base 或 npm run build:distributed')
}

const files = walk(dist)
const blockedFiles = files.filter(file => {
  const relativePath = path.relative(dist, file)
  return blockedFileNamePatterns.some(pattern => pattern.test(relativePath))
})

if (blockedFiles.length) {
  fail('内部轻量构建不应包含地图相关产物', blockedFiles)
}

const textFiles = files.filter(file => /\.(js|css|html|json|svg|txt)$/i.test(file))
const blockedContentFiles = textFiles.filter(file => {
  const content = fs.readFileSync(file, 'utf8')
  return blockedContentPatterns.some(pattern => pattern.test(content))
})

if (blockedContentFiles.length) {
  fail('内部轻量构建产物中仍包含地图运行时引用', blockedContentFiles)
}

console.log(`内部轻量构建检查通过，共检查 ${files.length} 个产物文件`)
