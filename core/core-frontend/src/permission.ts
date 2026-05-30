import router from './router'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useAppStoreWithOut } from '@/store/modules/app'
import type { RouteRecordRaw } from 'vue-router_2'
import { getDefaultSettings } from '@/api/common'
import { useNProgress } from '@/hooks/web/useNProgress'
import { usePermissionStoreWithOut, pathValid, getFirstAuthMenu } from '@/store/modules/permission'
import { usePageLoading } from '@/hooks/web/usePageLoading'
import { getRoleRouters } from '@/api/common'
import { useCache } from '@/hooks/web/useCache'
import { checkPlatform } from '@/utils/utils'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { useEmbedded } from '@/store/modules/embedded'
import { useLoading } from '@/hooks/web/useLoading'
import { ElMessageBox } from 'element-plus-secondary'
const appearanceStore = useAppearanceStoreWithOut()
const { wsCache } = useCache()
const permissionStore = usePermissionStoreWithOut()
const interactiveStore = interactiveStoreWithOut()
const userStore = useUserStoreWithOut()
const appStore = useAppStoreWithOut()

const { start, done } = useNProgress()
const { open } = useLoading()
const { loadStart, loadDone } = usePageLoading()

const whiteList = ['/login', '/sso/callback', '/chart-view', '/admin-login', '/401'] // 不重定向白名单
const embeddedWindowWhiteList = ['/dvCanvas', '/dashboard', '/preview', '/dataset-embedded-form']
const embeddedRouteWhiteList = ['/dataset-embedded', '/dataset-form', '/dataset-embedded-form']

const parseRedirectLocation = (redirectPath: string) => {
  let target = redirectPath || '/workbranch/index'
  try {
    for (let i = 0; i < 5; i++) {
      const nextTarget = decodeURIComponent(target)
      if (nextTarget === target) {
        break
      }
      target = nextTarget
    }
  } catch {
    // keep the original path when an invalid escape sequence is passed in
  }
  const [path, search = ''] = target.split('?')
  const query: Record<string, string> = {}
  new URLSearchParams(search).forEach((value, key) => {
    query[key] = value
  })
  return {
    path,
    query
  }
}

router.beforeEach(async (to, from, next) => {
  if (['/chart-view'].includes(to.path)) {
    open()
  }
  start()
  loadStart()
  const platform = checkPlatform()
  let isDesktop = wsCache.get('app.desktop')
  if (isDesktop === null) {
    await appStore.setAppModel()
    isDesktop = appStore.getDesktop
  }
  await appearanceStore.setAppearance()
  await appearanceStore.setFontList()
  const defaultSort = await getDefaultSettings()
  wsCache.set('TreeSort-backend', defaultSort['basic.defaultSort'] ?? '1')
  wsCache.set('open-backend', defaultSort['basic.defaultOpen'] ?? '0')
  if (to.path === '/sso/callback') {
    permissionStore.setCurrentPath(to.path)
    next()
    return
  }
  if (wsCache.get('user.token') || isDesktop) {
    if (!userStore.getUid) {
      await userStore.setUser()
    }
    if (to.path === '/login') {
      next({ path: '/workbranch/index' })
    } else {
      permissionStore.setCurrentPath(to.path)
      if (permissionStore.getIsAddRouters) {
        let str = ''
        if (
          !Object.keys(to.query || {}).length &&
          ((from.query.redirect as string) || '?').split('?')[0] === to.path
        ) {
          str = ((window.location.hash as string) || '?').split('?').reverse()[0]
          if (str.includes('redirect=')) {
            str = ''
          }
        }
        if (str) {
          to.fullPath += '?' + str
          to.query = str.split('&').reduce((pre, itx) => {
            const [key, val] = itx.split('=')
            pre[key] = val
            return pre
          }, {})
        }
        if (!pathValid(to.path) && to.path !== '/404') {
          if (to.path.startsWith('/sys-setting')) {
            await noAdminPermission()
          }
          const firstPath = getFirstAuthMenu()
          next({ path: firstPath || '/404' })
          return
        }
        next()
        return
      }

      let roleRouters = (await getRoleRouters()) || []
      if (isDesktop) {
        roleRouters = roleRouters.filter(item => item.name !== 'system')
      }
      const routers: any[] = roleRouters as AppCustomRouteRecordRaw[]
      routers.forEach(item => (item['top'] = true))
      await permissionStore.generateRoutes(routers as AppCustomRouteRecordRaw[])

      permissionStore.getAddRouters.forEach(route => {
        router.addRoute(route as unknown as RouteRecordRaw) // 动态添加可访问路由表
      })

      const redirectTarget = parseRedirectLocation(
        (from.query.redirect as string) || to.fullPath || to.path
      )
      const nextData =
        to.path === redirectTarget.path
          ? {
              ...to,
              query: Object.keys(to.query).length ? to.query : redirectTarget.query,
              replace: true
            }
          : redirectTarget

      permissionStore.setIsAddRouters(true)
      await interactiveStore.initInteractive(true)

      if (!pathValid(to.path) && to.path !== '/404') {
        if (to.path.startsWith('/sys-setting')) {
          await noAdminPermission()
        }
        const firstPath = getFirstAuthMenu()
        next({ path: firstPath || '/404' })
        return
      }
      next(nextData)
    }
  } else {
    const embeddedStore = useEmbedded()
    if (
      embeddedStore.getToken &&
      appStore.getIsIframe &&
      embeddedRouteWhiteList.includes(to.path)
    ) {
      if (to.path.includes('/dataset-form')) {
        next({ path: '/dataset-embedded-form', query: to.query })
        return
      }
      permissionStore.setCurrentPath(to.path)
      next()
    } else if (
      to.name === 'link' ||
      to.path.startsWith('/de-link/') ||
      (!platform && embeddedWindowWhiteList.includes(to.path)) ||
      whiteList.includes(to.path)
    ) {
      await appearanceStore.setFontList()
      permissionStore.setCurrentPath(to.path)
      next()
    } else {
      next(`/login?redirect=${to.fullPath || to.path}`) // 否则全部重定向到登录页
    }
  }
})
const noAdminPermission = async () => {
  const promise = new Promise<void>((resolve, reject) => {
    ElMessageBox.confirm('当前页面仅对 admin 开放, 即将跳转首页', {
      confirmButtonType: 'primary',
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '',
      autofocus: false,
      showCancelButton: false,
      showClose: false
    })
      .then(() => {
        resolve()
      })
      .catch(() => {
        reject()
      })
  })
  return Promise.race([
    promise,
    new Promise<void>(resolve => {
      setTimeout(() => {
        ElMessageBox.close()
        resolve()
      }, 3000)
    })
  ])
}
router.afterEach(() => {
  done()
  loadDone()
})
