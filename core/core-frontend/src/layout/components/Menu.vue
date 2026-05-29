<script lang="ts" setup>
import { computed } from 'vue'
import { ElMenu } from 'element-plus-secondary'
import { getCSSVariable } from '@/utils/color'
import { useRoute, useRouter } from 'vue-router_2'
import { isExternal } from '@/utils/validate'
import { useCache } from '@/hooks/web/useCache'
import MenuItem from './MenuItem.vue'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
const appearanceStore = useAppearanceStoreWithOut()
const tempColor = computed(() => {
  return {
    '--temp-color': '#eff6ff',
    '--temp-active-color':
      appearanceStore.themeColor === 'custom' ? appearanceStore.customColor : getCSSVariable()
  }
})
defineProps({
  collapse: Boolean
})

const route = useRoute()
const { wsCache } = useCache('localStorage')
const { push } = useRouter()
const menuList = computed(() => route.matched[0]?.children || [])
const path = computed(() => route.matched[0]?.path)

const activeIndex = computed(() => {
  const arr = route.path.split('/')
  return arr[arr.length - 1]
})
const menuSelect = (index: string, indexPath: string[]) => {
  //   自定义事件
  if (isExternal(index)) {
    const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
    window.open(index, openType)
  } else {
    push(`${path.value}/${indexPath.join('/')}`)
  }
}
</script>

<template>
  <el-menu
    :style="tempColor"
    @select="menuSelect"
    :default-active="activeIndex"
    class="el-menu-vertical"
    :collapse="collapse"
  >
    <MenuItem v-for="menu in menuList" :key="menu.path" :menu="menu"></MenuItem>
  </el-menu>
</template>

<style lang="less" scoped>
.ed-menu-vertical:not(.ed-menu--collapse) {
  width: 100%;
  min-height: 400px;
}

.ed-menu {
  padding: 10px 8px;
  background: #ffffff;
  border: none;
  overflow-x: hidden;

  :deep(.ed-menu-item),
  :deep(.ed-sub-menu__title) {
    height: 40px;
    margin: 3px 0;
    padding: 0 14px !important;
    color: #334155;
    font-family: var(--crest-font-sans);
    font-size: 14px;
    font-weight: 600;
    border-radius: 10px;
    transition:
      color 0.14s ease,
      background-color 0.14s ease;
  }

  :deep(.ed-icon) {
    color: #64748b;
    font-size: 18px;
    transition: color 0.14s ease;
  }

  :deep(.svg-icon.logo) {
    width: 18px;
    height: 18px;
  }

  .ed-menu-item:not(.is-active) {
    &:hover {
      color: #0f172a;
      background-color: #f8fafc !important;
    }
  }
  .is-active:not(.ed-sub-menu) {
    color: var(--temp-active-color);
    font-weight: 700;
    background-color: var(--temp-color);
    box-shadow: inset 3px 0 0 var(--temp-active-color);

    :deep(.ed-icon) {
      color: var(--temp-active-color);
    }
  }
  :deep(.ed-sub-menu) {
    margin: 0;
    .ed-sub-menu__title {
      &:hover {
        color: #0f172a;
        background-color: #f8fafc;
      }
    }
    .ed-menu-item:not(.is-active) {
      &:hover {
        background-color: #f8fafc !important;
      }
    }
    ul.ed-menu {
      li.ed-menu-item {
        i {
          width: 4px !important;
        }
      }
    }
  }
  :deep(.ed-sub-menu.is-active) {
    .ed-sub-menu__title {
      color: var(--temp-active-color);
      font-weight: 700;
    }
    .is-active {
      color: var(--temp-active-color);
      background-color: var(--temp-color);
      box-shadow: inset 3px 0 0 var(--temp-active-color);
    }
  }
}

.ed-menu--collapse {
  width: 64px;
  padding: 10px 8px;

  :deep(.ed-menu-item),
  :deep(.ed-sub-menu__title) {
    justify-content: center;
    padding: 0 !important;
  }
}
</style>
