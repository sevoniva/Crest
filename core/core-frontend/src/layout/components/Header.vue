<script lang="ts" setup>
import dvPreviewDownload from '@/assets/svg/icon_download_outlined.svg'
import crestLogoDark from '@/assets/img/crest-logo-horizontal-dark-192h.png'
import { computed, onMounted, ref } from 'vue'
import { usePermissionStore } from '@/store/modules/permission'
import { isExternal } from '@/utils/validate'
import { formatRoute } from '@/router/establish'
import HeaderMenuItem from './HeaderMenuItem.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { Icon } from '@/components/icon-custom'
import SystemCfg from './SystemCfg.vue'
import { useRouter, useRoute } from 'vue-router_2'
import AccountOperator from '@/layout/components/AccountOperator.vue'
import { isDesktop } from '@/utils/ModelUtil'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import DesktopSetting from './DesktopSetting.vue'

const appearanceStore = useAppearanceStoreWithOut()
const { push } = useRouter()
const route = useRoute()
import { useCache } from '@/hooks/web/useCache'
import { useI18n } from '@/hooks/web/useI18n'
const { wsCache } = useCache('localStorage')
const handleIconClick = () => {
  if (route.path === '/workbranch/index') return
  push('/workbranch/index')
}

const { t } = useI18n()

const desktop = isDesktop()
const activeIndex = computed(() => {
  if (route.path.includes('system')) {
    return '/system/user'
  }
  return route.path
})

const permissionStore = usePermissionStore()
const downloadClick = params => {
  useEmitt().emitter.emit('data-export-center', params)
}
const routers: any[] = formatRoute(permissionStore.getRoutersNotHidden as AppCustomRouteRecordRaw[])
const showSystem = ref(false)
const handleSelect = (index: string) => {
  // 自定义事件
  if (isExternal(index)) {
    const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
    window.open(index, openType)
  } else {
    push(index)
  }
}
const initShowSystem = () => {
  showSystem.value = permissionStore.getRouters.some(route => route.path === '/system')
}
const navigateBg = computed(() => appearanceStore.getNavigateBg)
const navigate = computed(() => appearanceStore.getNavigate)

onMounted(() => {
  initShowSystem()
})
</script>

<template>
  <el-header class="header-flex" :class="{ 'header-light': navigateBg === 'light' }">
    <img class="logo" :src="navigate || crestLogoDark" alt="Crest" @click="handleIconClick" />
    <el-menu
      :default-active="activeIndex"
      mode="horizontal"
      :ellipsis="false"
      @select="handleSelect"
      :effect="navigateBg === 'light' ? 'light' : 'dark'"
    >
      <HeaderMenuItem v-for="menu in routers" :key="menu.path" :menu="menu"></HeaderMenuItem>
    </el-menu>
    <div class="operate-setting" v-if="!desktop">
      <el-tooltip effect="dark" :content="t('data_export.export_center')" placement="bottom">
        <el-icon
          class="preview-download_icon"
          :class="navigateBg === 'light' && 'is-light-setting'"
          @click="downloadClick"
        >
          <Icon name="dv-preview-download"><dvPreviewDownload class="svg-icon" /></Icon>
        </el-icon>
      </el-tooltip>

      <SystemCfg v-if="showSystem" />
      <AccountOperator />
    </div>
    <div v-else class="operate-setting">
      <desktop-setting />
    </div>
  </el-header>
</template>

<style lang="less" scoped>
:deep(.ed-badge_custom) {
  --ed-badge-size: 14px;
  height: 28px;
  .ed-badge__content {
    right: 0;
    padding: 3px;
    border: none;
    font-size: 8px;
    transform: translateX(20%) translateY(-30%);
  }
}
.preview-download_icon {
  height: 28px;
  width: 28px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  &:hover {
    background-color: #1e2738;
  }
  &.is-light-setting {
    &:hover {
      background-color: #1f23291a !important;
    }
  }
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* 半透明黑色 */
  z-index: 10000;
}

.header-flex {
  margin-bottom: 0.5px;
  display: flex;
  align-items: center;
  height: 56px;
  background-color: #050e21;
  padding: 0 24px;
  .operate-setting {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 12px;
    &:focus {
      outline: none;
    }
  }

  .ed-menu.ed-menu--horizontal {
    border-bottom: none;
    background: transparent;
  }
}

.header-light {
  background-color: #ffffff !important;
  box-shadow: 0px 0.5px 0px 0px #1f232926 !important;
  .logo {
    color: #3371ff !important;
  }
}

.logo {
  width: 158px;
  height: 34px;
  margin-right: 48px;
  color: #ffffff;
  object-fit: contain;
  cursor: pointer;
}
</style>

<style lang="less">
.header-flex {
  .operate-setting {
    .ed-icon {
      cursor: pointer;
      color: rgba(255, 255, 255, 0.8);
      font-size: 20px;
    }
  }
}
.header-light {
  .operate-setting {
    .ed-icon {
      color: #646a73 !important;
    }
  }
}

.ai-icon {
  font-size: 24px !important;
}

.ai-icon-tips,
.copilot-icon-tips {
  font-size: 24px !important;
  z-index: 10001;
}
</style>
