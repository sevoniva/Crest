import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.resolve(__dirname, '..')
const repoRoot = path.resolve(root, '..', '..')
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

function assertMissing(relativePath, message) {
  const fullPath = path.join(repoRoot, relativePath)
  if (fs.existsSync(fullPath)) {
    fail(message, [fullPath])
  }
}

function assertFileDoesNotContain(relativePath, patterns, message) {
  const fullPath = path.join(repoRoot, relativePath)
  if (!fs.existsSync(fullPath)) {
    return
  }
  if (fs.statSync(fullPath).isDirectory()) {
    const matchedFiles = walk(fullPath).filter(file => {
      if (!/\.(vue|ts|js|json|mjs)$/i.test(file)) {
        return false
      }
      const content = fs.readFileSync(file, 'utf8')
      return patterns.some(pattern => pattern.test(content))
    })
    if (matchedFiles.length) {
      fail(message, matchedFiles)
    }
    return
  }
  const content = fs.readFileSync(fullPath, 'utf8')
  const matched = patterns.some(pattern => pattern.test(content))
  if (matched) {
    fail(message, [fullPath])
  }
}

function assertFileContains(relativePath, patterns, message) {
  const fullPath = path.join(repoRoot, relativePath)
  if (!fs.existsSync(fullPath)) {
    fail(message, [fullPath])
  }
  const content = fs.readFileSync(fullPath, 'utf8')
  const matched = patterns.every(pattern => pattern.test(content))
  if (!matched) {
    fail(message, [fullPath])
  }
}

assertMissing('installer/crest/docker-compose-apisix.yml', '内部轻量安装包不应包含 APISIX Compose 模板')
assertMissing('installer/crest/apisix', '内部轻量安装包不应包含 APISIX 配置目录')
assertMissing('core/core-frontend/src/api/plugin.ts', '内部轻量前端不应保留远程插件 API')
assertMissing('core/core-frontend/src/components/plugin', '内部轻量前端不应保留远程插件组件目录')
assertMissing('core/core-frontend/src/api/map.ts', '内部轻量前端不应保留地图 API')
assertMissing('core/core-frontend/src/store/modules/map.ts', '内部轻量前端不应保留地图状态模块')
assertMissing('core/core-frontend/src/internal-lite/map-stub.ts', '内部轻量前端不应依赖地图运行时替身')
assertMissing('core/core-frontend/src/views/chart/components/js/panel/charts/map', '内部轻量前端不应保留地图图表实现')
assertMissing('core/core-backend/src/main/java/io/crest/map', '内部轻量后端不应保留地图接口和管理模块')
assertMissing('sdk/api/api-base/src/main/java/io/crest/api/map', '内部轻量 SDK 不应保留地图 API')
assertMissing('sdk/api/api-base/src/main/java/io/crest/api/system/request/OnlineMapEditor.java', '内部轻量 SDK 不应保留在线地图配置对象')
assertFileDoesNotContain(
  'core/core-frontend/package.json',
  [/"@antv\/l7"/, /"@antv\/l7plot"/, /"@turf\/centroid"/],
  '内部轻量前端不应声明地图运行时依赖'
)
assertFileDoesNotContain(
  'core/core-frontend/package-lock.json',
  [/node_modules\/@antv\/l7/, /node_modules\/@antv\/l7plot/, /node_modules\/@turf\/centroid/],
  '内部轻量前端锁文件不应包含地图运行时依赖'
)
assertFileDoesNotContain(
  'core/core-backend/src/main/java/io/crest/share/server/ShareServer.java',
  [/ConditionalOnProperty/, /internal-lite/],
  '分享接口必须在内部轻量模式下保留'
)
assertFileDoesNotContain(
  'core/core-backend/src/main/java/io/crest/share/server/ShareTicketServer.java',
  [/ConditionalOnProperty/, /internal-lite/],
  '分享 ticket 接口必须在内部轻量模式下保留'
)
assertFileDoesNotContain('core/core-frontend/src', [/PluginComponent/], '前端源码不应保留商业插件组件引用')
assertFileDoesNotContain(
  'core/core-frontend/src',
  [
    /queryOnlineMap/,
    /saveOnlineMap/,
    /\/customGeo\//,
    /\/map\/worldTree/,
    /@antv\/l7/,
    /@antv\/l7plot/,
    /@turf\/centroid/
  ],
  '前端源码不应保留地图接口和地图运行时引用'
)

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
