<script lang="ts" setup>
import crestLogo from '@/assets/svg/logo.svg?url'
import icon_left_outlined from '@/assets/svg/icon_left_outlined.svg'
import { ElHeader } from 'element-plus-secondary'
import { useRouter } from 'vue-router_2'
import AccountOperator from '@/layout/components/AccountOperator.vue'
import { useI18n } from '@/hooks/web/useI18n'
import { isDesktop } from '@/utils/ModelUtil'
const { push } = useRouter()
const { t } = useI18n()
const desktop = isDesktop()
withDefaults(
  defineProps<{
    title: string
  }>(),
  {}
)
const backToMain = () => {
  push('/workbranch/index')
}
</script>

<template>
  <el-header class="header-flex system-header">
    <img class="logo" :src="crestLogo" alt="Crest" />
    <el-divider direction="vertical" />
    <span class="system">{{ title || t('commons.system_setting') }}</span>
    <div class="operate-setting">
      <span @click="backToMain" class="work-bar flex-align-center">
        <el-icon>
          <Icon name="icon_left_outlined"><icon_left_outlined class="svg-icon" /></Icon>
        </el-icon>
        <span class="work">{{ t('work_branch.back_to_work_branch') }}</span>
      </span>

      <AccountOperator v-if="!desktop" />
    </div>
  </el-header>
</template>

<style lang="less" scoped>
.system-header {
  font-family: var(--crest-font-sans, var(--de-custom_font, 'PingFang'));

  .logo {
    width: 158px;
    height: 34px;
    object-fit: contain;
  }

  .ed-divider {
    margin: 0 24px;
    border-color: #dbe4f0;
  }
  .system {
    color: #0f172a;
    font-size: 16px;
    font-style: normal;
    font-weight: 500;
    line-height: 24px;
  }

  .work-bar {
    margin-right: 20px;
    color: #64748b;
    font-size: 14px;
    font-style: normal;
    font-weight: 400;
    line-height: 22px;
    cursor: pointer;
    .ed-icon {
      margin-right: 4px;
      font-size: 16px;
    }
  }

  .avatar {
    margin: 0 -7px 0 20px !important;
  }
}
.header-flex {
  position: relative;
  margin-bottom: 0.5px;
  display: flex;
  align-items: center;
  height: 56px;
  overflow: hidden;
  background: linear-gradient(180deg, #edf2fb 0%, #ffffff 100%);
  border-bottom: 1px solid #e2e8f0;
  padding: 0 24px;

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
    margin-left: auto;
    display: flex;
    align-items: center;
    &:focus {
      outline: none;
    }
  }
}
</style>

<style lang="less">
.header-flex {
  .operate-setting {
    .ed-icon {
      cursor: pointer;
      color: #64748b;
      font-size: 20px;
    }
  }
}
</style>
