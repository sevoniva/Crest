import fs from 'node:fs'
import path from 'node:path'

const root = path.resolve(new URL('..', import.meta.url).pathname)

const read = relativePath => fs.readFileSync(path.join(root, relativePath), 'utf8')
const assertIncludes = (content, expected, label) => {
  if (!content.includes(expected)) {
    throw new Error(`${label}: missing ${expected}`)
  }
}
const assertNotIncludes = (content, expected, label) => {
  if (content.includes(expected)) {
    throw new Error(`${label}: should not include ${expected}`)
  }
}

const router = read('src/router/index.ts')
const permission = read('src/permission.ts')
const login = read('src/views/login/index.vue')
const backendSso = fs.readFileSync(
  path.resolve(root, '../core-backend/src/main/java/io/crest/system/manage/SsoManage.java'),
  'utf8'
)
const callbackPath = path.join(root, 'src/views/login/SsoCallback.vue')

assertIncludes(router, "path: '/sso/callback'", 'router')
assertIncludes(permission, "'/sso/callback'", 'permission whitelist')
assertIncludes(login, 'ssoStatus.enabled', 'login page status gate')
assertNotIncludes(backendSso, '/#/login?ssoTicket=', 'backend callback redirect')
assertIncludes(backendSso, '/#/sso/callback?ticket=', 'backend callback redirect')

if (!fs.existsSync(callbackPath)) {
  throw new Error('callback page: missing src/views/login/SsoCallback.vue')
}
assertIncludes(fs.readFileSync(callbackPath, 'utf8'), '正在完成单点登录', 'callback page')
