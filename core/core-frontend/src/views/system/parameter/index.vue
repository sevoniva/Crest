<template>
  <div class="parameter-page">
    <p class="router-title">{{ t('commons.system_parameter_setting') }}</p>
    <el-tabs v-model="activeName" class="parameter-tabs">
      <el-tab-pane v-for="item in tabArray" :key="item.name" :label="item.label" :name="item.name" />
    </el-tabs>
    <div class="sys-setting-p">
      <div class="container-sys-param" :class="{ 'basic-info_bg': activeName === 'basic' }">
        <basic-info v-if="activeName === 'basic'" />
        <engine-info v-if="activeName === 'engine'" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import BasicInfo from './basic/BasicInfo.vue'
import EngineInfo from '@/views/system/parameter/engine/EngineInfo.vue'
const { t } = useI18n()

const tabArray = ref([
  { label: t('system.basic_settings'), name: 'basic' },
  { label: t('system.engine_settings'), name: 'engine' }
])

const activeName = ref('basic')
</script>
<style lang="less">
.parameter-page {
  width: min(100%, 1280px);
}
.router-title {
  color: #0f172a;
  font-feature-settings: 'clig' off, 'liga' off;
  font-family: var(--crest-font-sans, var(--de-custom_font, 'PingFang'));
  font-size: 18px;
  font-style: normal;
  font-weight: 700;
  line-height: 28px;
}
.sys-setting-p {
  width: 100%;
  height: auto;
  max-height: calc(100vh - 184px);
  box-sizing: border-box;
  margin-top: 14px;
}

.container-sys-param {
  max-height: 100%;
  height: auto;
  overflow-y: auto;
  background: transparent;
  border-radius: 0;
  &.basic-info_bg {
    background: none;
  }
}
.setting-max-h {
  height: 100% !important;
}
.parameter-tabs {
  margin-top: 18px;
}
.parameter-tabs.ed-tabs {
  --ed-tabs-header-height: 36px;
}
.parameter-tabs .ed-tabs__header {
  margin-bottom: 0;
}
.parameter-tabs .ed-tabs__nav-wrap::after {
  height: 0;
}
.parameter-tabs .ed-tabs__item {
  padding: 0 18px 0 0;
  height: 36px;
  color: #64748b;
  font-weight: 600;
}
.parameter-tabs .ed-tabs__item.is-active {
  color: #3b82f6;
}
.parameter-tabs .ed-tabs__active-bar {
  height: 3px;
  border-radius: 999px;
  background: #3b82f6;
}
</style>
