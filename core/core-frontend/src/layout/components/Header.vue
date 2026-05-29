<script lang="ts" setup>
import crestLogo from '@/assets/svg/logo.svg?url'
import { computed, onMounted, ref } from 'vue'
import { usePermissionStore } from '@/store/modules/permission'
import { isExternal } from '@/utils/validate'
import { formatRoute } from '@/router/establish'
import HeaderMenuItem from './HeaderMenuItem.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useRouter, useRoute } from 'vue-router_2'
import AccountOperator from '@/layout/components/AccountOperator.vue'
import { useCache } from '@/hooks/web/useCache'
import { useI18n } from '@/hooks/web/useI18n'

const { push } = useRouter()
const route = useRoute()
const { wsCache } = useCache('localStorage')
const { t } = useI18n()

const handleIconClick = () => {
  if (route.path === '/workbranch/index') return
  push('/workbranch/index')
}

const activeIndex = computed(() => {
  if (route.path.includes('system')) {
    return '/system/user'
  }
  return route.path
})

const permissionStore = usePermissionStore()
const routers: any[] = formatRoute(permissionStore.getRoutersNotHidden as AppCustomRouteRecordRaw[])
const ready = ref(false)

const downloadClick = params => {
  useEmitt().emitter.emit('data-export-center', params)
}

const handleSelect = (index: string) => {
  if (isExternal(index)) {
    const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
    window.open(index, openType)
  } else {
    push(index)
  }
}

onMounted(() => {
  ready.value = true
})
</script>

<template>
  <el-header class="header-flex">
    <img class="logo" :src="crestLogo" alt="Crest" @click="handleIconClick" />
    <el-menu
      :default-active="activeIndex"
      mode="horizontal"
      :ellipsis="false"
      effect="light"
      @select="handleSelect"
    >
      <HeaderMenuItem v-for="menu in routers" :key="menu.path" :menu="menu"></HeaderMenuItem>
    </el-menu>
    <div class="operate-setting" v-if="ready">
      <el-tooltip effect="dark" :content="t('data_export.export_center')" placement="bottom">
        <button class="top-action" type="button" @click="downloadClick">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
              d="M12 4v11m0 0l-4-4m4 4l4-4M5 19h14"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>
      </el-tooltip>
      <el-tooltip effect="dark" content="通知" placement="bottom">
        <button class="top-action notice-action" type="button">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
              d="M6 9a6 6 0 1112 0v4l1.5 3h-15L6 13V9zM10 19a2 2 0 004 0"
              stroke="currentColor"
              stroke-width="1.7"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <span class="notice-dot" />
        </button>
      </el-tooltip>
      <span class="top-divider" />
      <AccountOperator />
    </div>
  </el-header>
</template>

<style lang="less" scoped>
.header-flex {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  height: 60px;
  margin-bottom: 0;
  padding: 0 28px;
  overflow: hidden;
  background: linear-gradient(180deg, #edf2fb 0%, #fafbfe 100%);
  border-bottom: 1px solid #e2e8f0;

  &::before {
    position: absolute;
    inset: 0;
    pointer-events: none;
    content: '';
    background-image: radial-gradient(circle at 1px 1px, #3b6fd0 1px, transparent 0);
    background-size: 20px 20px;
    opacity: 0.05;
  }

  > * {
    position: relative;
    z-index: 1;
  }

  .operate-setting {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: auto;

    &:focus {
      outline: none;
    }
  }

  :deep(.ed-menu.ed-menu--horizontal) {
    height: 100%;
    background: transparent;
    border-bottom: none;
  }

  :deep(.ed-menu--horizontal > .ed-menu-item),
  :deep(.ed-menu--horizontal > .ed-sub-menu .ed-sub-menu__title) {
    height: 60px;
    padding: 0 18px;
    font-family: var(--crest-font-sans);
    font-size: 14px;
    font-weight: 500;
    color: #64748b;
    background: transparent !important;
    border-bottom: none;
    transition: color 0.15s ease;
  }

  :deep(.ed-menu--horizontal > .ed-menu-item:hover),
  :deep(.ed-menu--horizontal > .ed-sub-menu:hover .ed-sub-menu__title) {
    color: #0f172a;
    background: transparent !important;
  }

  :deep(.ed-menu--horizontal > .ed-menu-item.is-active),
  :deep(.ed-menu--horizontal > .ed-sub-menu.is-active .ed-sub-menu__title) {
    position: relative;
    font-weight: 600;
    color: #0f172a !important;
    background: transparent !important;
    border-bottom: none;
  }

  :deep(.ed-menu--horizontal > .ed-menu-item.is-active::after),
  :deep(.ed-menu--horizontal > .ed-sub-menu.is-active .ed-sub-menu__title::after) {
    position: absolute;
    right: 18px;
    bottom: -1px;
    left: 18px;
    height: 2px;
    content: '';
    background: #3b82f6;
    border-radius: 2px 2px 0 0;
  }
}

.logo {
  width: 158px;
  height: 34px;
  margin-right: 42px;
  object-fit: contain;
  cursor: pointer;
}

.top-action {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  padding: 0;
  color: #64748b;
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 8px;
  transition:
    color 0.14s ease,
    background 0.14s ease;

  &:hover {
    color: #0f172a;
    background: #f1f5f9;
  }
}

.notice-dot {
  position: absolute;
  top: 7px;
  right: 7px;
  width: 7px;
  height: 7px;
  background: #ef4444;
  border: 1.5px solid #ffffff;
  border-radius: 50%;
}

.top-divider {
  width: 1px;
  height: 22px;
  margin: 0 6px;
  background: #e2e8f0;
}
</style>
