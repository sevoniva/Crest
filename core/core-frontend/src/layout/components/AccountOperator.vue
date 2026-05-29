<script lang="ts" setup>
import userImg from '@/assets/svg/user-img.svg'
import icon_expandDown_filled from '@/assets/svg/icon_expand-down_filled.svg'
import { computed, ref, unref } from 'vue'
import { Icon } from '@/components/icon-custom'
import { useUserStoreWithOut } from '@/store/modules/user'
import { logoutApi } from '@/api/login'
import { logoutHandler } from '@/utils/logout'
import { useI18n } from '@/hooks/web/useI18n'
import LangSelector from './LangSelector.vue'
import router from '@/router'
import { useCache } from '@/hooks/web/useCache'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
const appearanceStore = useAppearanceStoreWithOut()
const navigateBg = computed(() => appearanceStore.getNavigateBg)
const { wsCache } = useCache()
const userStore = useUserStoreWithOut()
const { t } = useI18n()

interface LinkItem {
  id: number
  label: string
  link?: string
  method?: string
}
const linkList = ref([] as LinkItem[])

const inPlatformClient = computed(() => !!wsCache.get('de-platform-client'))

const logout = async () => {
  await logoutApi()
  logoutHandler()
}

const linkLoaded = items => {
  items.forEach(item => linkList.value.push(item))
  linkList.value.sort(compare('id'))
}

const compare = (property: string) => {
  return (a, b) => a[property] - b[property]
}

const executeMethod = (item: LinkItem) => {
  if (item.link) {
    router.push(item.link)
  }
}

const name = computed(() => userStore.getName)
const uid = computed(() => userStore.getUid)

const buttonRef = ref()
const popoverRef = ref()

const divLanguageRef = ref()
const popoverLanguageRef = ref()

const openLanguage = () => {
  unref(popoverLanguageRef).popperRef?.delayHide?.()
}

const openPopover = () => {
  unref(popoverRef).popperRef?.delayHide?.()
}

if (uid.value === '1') {
  linkLoaded([{ id: 4, link: '/sys-setting/parameter', label: t('commons.system_setting') }])
  const desktop = wsCache.get('app.desktop')
  if (!desktop) {
    linkLoaded([{ id: 2, link: '/modify-pwd/index', label: t('user.change_password') }])
  }
}
</script>

<template>
  <div
    class="top-info-container"
    :class="{ 'is-light-top-info': navigateBg && navigateBg === 'light' }"
    ref="buttonRef"
    v-click-outside="openPopover"
  >
    <el-icon class="main-color">
      <Icon name="user-img"><userImg class="svg-icon" /></Icon>
    </el-icon>
    <span class="uname-span">{{ name }}</span>
    <el-icon class="el-icon-animate">
      <Icon name="icon_expand-down_filled"><icon_expandDown_filled class="svg-icon" /></Icon>
    </el-icon>
  </div>
  <el-popover
    ref="popoverRef"
    :virtual-ref="buttonRef"
    trigger="click"
    title=""
    virtual-triggering
    placement="bottom-start"
    popper-class="uinfo-popover"
    width="224"
  >
    <div class="uinfo-container">
      <div class="uinfo-header de-container">
        <span class="uinfo-name">{{ name }}</span>
        <span class="uinfo-id">{{ `ID: ${uid}` }}</span>
      </div>
      <el-divider />
      <div class="uinfo-main">
        <div
          class="uinfo-main-item de-container"
          v-for="link in linkList"
          :key="link.id"
          @click="executeMethod(link)"
        >
          <span>{{ link.label }}</span>
        </div>

        <div class="uinfo-main-item de-container">
          <div class="about-parent" ref="divLanguageRef" v-click-outside="openLanguage">
            <span>{{ $t('commons.language') }}</span>
            <el-icon class="el-icon-animate">
              <ArrowRight />
            </el-icon>
          </div>
          <el-popover
            ref="popoverLanguageRef"
            :virtual-ref="divLanguageRef"
            trigger="hover"
            title=""
            virtual-triggering
            placement="left"
            width="224"
            popper-class="language-popover"
          >
            <LangSelector />
          </el-popover>
        </div>
      </div>
      <el-divider />
      <div class="uinfo-footer" v-if="!inPlatformClient">
        <div class="uinfo-main-item de-container" @click="logout">
          <span>{{ t('common.exit_system') }}</span>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<style lang="less">
.el-icon-animate {
  width: 12px;
  height: 12px;
  font-size: 14px !important;
}
.is-light-top-info {
  .uname-span {
    font-family: var(--crest-font-sans);
    color: #0f172a !important;
  }
  &:hover {
    background-color: #ffffff !important;
  }
}
.top-info-container {
  height: 40px;
  display: flex;
  align-items: center;
  padding: 5px 10px 5px 5px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 999px;
  overflow: hidden;
  cursor: pointer;
  transition:
    background 0.14s ease,
    border-color 0.14s ease;
  &:hover {
    background-color: #ffffff;
    border-color: #e2e8f0;
  }
  .main-color {
    width: 30px;
    height: 30px;
    color: #3b82f6;
    background: linear-gradient(135deg, #dbeafe, #bfdbfe);
    border-radius: 50%;
  }
  .uname-span {
    margin-left: 9px;
    font-family: var(--crest-font-sans);
    font-size: 14px;
    font-weight: 500;
    color: #0f172a;
  }
  .ed-icon {
    margin: 0 0 0 6px;
    color: #64748b;
  }
}
.uinfo-container {
  width: 100%;
  height: 100%;
  .de-container {
    padding: 0 13px 10px;
  }
  .ed-divider--horizontal {
    margin: 0 0 !important;
    color: #1f2329;
    opacity: 0.35;
  }
  .uinfo-header {
    span {
      display: block;
    }
    .uinfo-name {
      font-size: 14px;
      font-weight: 500;
      color: #1f2329;
    }
    .uinfo-id {
      font-size: 14px;
      font-weight: 400;
      color: #646a73;
      margin-top: 5px;
    }
  }
  .uinfo-main,
  .uinfo-footer {
    width: 100%;
    .uinfo-main-item {
      width: 100%;
      height: 40px;
      line-height: 40px;
      cursor: pointer;
      &:hover {
        background-color: #f2f2f2;
      }
      .about-parent {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
    }
  }
}
.uinfo-popover {
  max-height: 372px;
  .ed-popper__arrow {
    display: none;
  }
  .ed-popover__title {
    display: none;
  }
  padding-left: 0 !important;
  padding-right: 0 !important;
  padding-bottom: 0 !important;
}
.language-popover {
  // max-height: 112px;
  .ed-popper__arrow {
    display: none;
  }
  padding: var(--ed-popover-padding) 0 !important;
}
</style>
