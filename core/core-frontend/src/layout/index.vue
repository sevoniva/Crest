<script lang="ts" setup>
import { computed, ref } from 'vue'
import Header from './components/Header.vue'
import HeaderSystem from './components/HeaderSystem.vue'
import Sidebar from './components/Sidebar.vue'
import Menu from './components/Menu.vue'
import Main from './components/Main.vue'
import CollapseBar from './components/CollapseBar.vue'
import DeResourceArrow from '@/views/common/DeResourceArrow.vue'
import { ElContainer } from 'element-plus-secondary'
import { useRoute } from 'vue-router_2'
const route = useRoute()
const systemMenu = computed(() => route.path.includes('system'))
const settingMenu = computed(() => route.path.includes('sys-setting'))
const userCenterPage = computed(() => route.path.includes('/modify-pwd'))
const adminSurface = computed(() => settingMenu.value || userCenterPage.value)
const isCollapse = ref(false)
const settingSideWidth = 248
const setCollapse = () => {
  isCollapse.value = !isCollapse.value
}
const changeSettingSideStatus = (showSide: boolean) => {
  isCollapse.value = !showSide
}
</script>

<template>
  <div
    class="common-layout"
    :class="{
      'is-with-sider': systemMenu || settingMenu,
      'is-admin-surface': adminSurface,
      'is-setting-resource-sider': settingMenu,
      'is-user-center-surface': userCenterPage
    }"
  >
    <HeaderSystem v-if="settingMenu" :title="''" />
    <Header v-else></Header>
    <el-container class="layout-container">
      <template v-if="systemMenu || settingMenu">
        <template v-if="settingMenu">
          <DeResourceArrow
            class="setting-resource-arrow"
            :style="{ left: (isCollapse ? 0 : settingSideWidth - 12) + 'px' }"
            :isInside="isCollapse"
            @change-side-tree-status="changeSettingSideStatus"
          />
          <Sidebar v-if="!isCollapse" class="layout-sidebar setting-resource-sidebar">
            <div class="setting-resource-tree">
              <div class="tree-header">
                <div class="icon-methods">
                  <span class="title">{{ $t('commons.system_setting') }}</span>
                </div>
              </div>
              <Menu class="setting-resource-menu"></Menu>
            </div>
          </Sidebar>
        </template>
        <template v-else>
          <Sidebar v-if="!isCollapse" class="layout-sidebar">
            <div @click="setCollapse" v-if="systemMenu && !isCollapse" class="org-config-center">
              {{ $t('toolbox.org_center') }}
            </div>
            <Menu :style="{ height: systemMenu ? 'calc(100% - 48px)' : '100%' }"></Menu>
          </Sidebar>
          <el-aside class="layout-sidebar layout-sidebar-collapse" v-else>
            <Menu
              :collapse="isCollapse"
              :style="{ height: systemMenu ? 'calc(100% - 48px)' : '100%' }"
            ></Menu>
          </el-aside>
          <CollapseBar @setCollapse="setCollapse" :isCollapse="isCollapse"></CollapseBar>
        </template>
      </template>

      <Main class="layout-main" :class="{ 'with-sider': systemMenu || settingMenu }"></Main>
    </el-container>
  </div>
</template>

<style lang="less" scoped>
.common-layout {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
  color: #0f172a;
  min-width: 1000px;
  overflow-x: auto;
  font-family: var(--crest-font-sans, var(--de-custom_font, 'PingFang'));

  .layout-container {
    flex: 1;
    min-height: 0;
    position: relative;
    background: #f8fafc;

    .layout-sidebar {
      height: calc(100vh - 108px);
      background: #ffffff;
      border-right: 1px solid #e2e8f0;
      overflow-x: hidden;
    }

    .layout-sidebar-collapse {
      width: 64px;
      background: #ffffff;
      border-right: 1px solid #e2e8f0;
      overflow-x: hidden;
    }

    .org-config-center {
      height: 48px;
      padding-left: 24px;
      display: flex;
      align-items: center;
      font-size: 14px;
      font-weight: 600;
      line-height: 22px;
      color: #64748b;
      border-bottom: 1px solid #e2e8f0;
      position: sticky;
      top: 0;
      left: 0;
      background: #fff;
      z-index: 10;
    }

    .layout-main {
      flex: 1;
      min-width: 0;
      min-height: 0;
      background-color: #f8fafc;
      padding: 0;
    }

    .with-sider {
      padding: 24px 28px 28px 28px;
    }
    .with-sider:has(.appearance-foot) {
      padding: 16px 24px 0px 24px !important;
    }
  }

  &.is-user-center-surface {
    .layout-main {
      padding: clamp(24px, 2vw, 34px) clamp(28px, 4vw, 72px);
    }
  }

  &.is-admin-surface {
    .layout-sidebar {
      width: 248px !important;
      flex: 0 0 248px;
    }

    .layout-sidebar-collapse {
      width: 64px !important;
      flex: 0 0 64px;
    }

    &:has(.layout-sidebar) :deep(.de-collapse-bar) {
      width: 248px !important;
    }

    &:has(.layout-sidebar-collapse) :deep(.de-collapse-bar) {
      width: 64px !important;
      padding-right: 0;
      padding-left: 22px;
    }

    :deep(.router-title.router-title),
    :deep(.route-title.route-title) {
      margin: 0 0 16px;
      color: #0f172a;
      font-family: var(--crest-font-sans);
      font-size: 18px;
      font-weight: 700;
      line-height: 28px;
      letter-spacing: 0;
    }

    :deep(.sys-setting-p.sys-setting-p),
    :deep(.table-wrap.table-wrap),
    :deep(.font-content_overflow.font-content_overflow) {
      height: auto;
      max-height: calc(100vh - 184px);
      margin-top: 12px;
    }

    :deep(.container-sys-param.container-sys-param),
    :deep(.table-wrap.table-wrap),
    :deep(.setting-panel.setting-panel),
    :deep(.info-template-container.info-template-container),
    :deep(.font-content_item.font-content_item),
    :deep(.user-tabs.user-tabs),
    :deep(.base-info.base-info) {
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 14px;
      box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
    }

    :deep(.container-sys-param.basic-info_bg) {
      background: transparent;
      border: 0;
      box-shadow: none;
    }

    :deep(.toolbar.toolbar) {
      align-items: center;
      padding: 16px;
      background: #ffffff;
      border-bottom: 1px solid #f1f5f9;
      border-radius: 14px 14px 0 0;
    }

    :deep(.pager.pager) {
      padding: 12px 16px 16px;
      background: #ffffff;
      border-top: 1px solid #f1f5f9;
    }

    :deep(.ed-tabs__header) {
      margin: 0;
    }

    :deep(.ed-tabs__nav-wrap::after) {
      display: none;
    }

    :deep(.ed-tabs__active-bar) {
      height: 2px;
      background: #3b82f6;
      border-radius: 2px 2px 0 0;
    }

    :deep(.ed-tabs__item) {
      height: 38px;
      color: #64748b;
      font-family: var(--crest-font-sans);
      font-size: 14px;
      font-weight: 500;
    }

    :deep(.ed-tabs__item.is-active) {
      color: #3b82f6;
      font-weight: 700;
    }

    :deep(.ed-table) {
      --ed-table-header-bg-color: #ffffff;
      --ed-table-tr-bg-color: #ffffff;
      color: #334155;
      font-family: var(--crest-font-sans);
      font-size: 13px;
    }

    :deep(.ed-table__inner-wrapper::before) {
      display: none;
    }

    :deep(.ed-table th.ed-table__cell) {
      padding: 10px 0;
      color: #94a3b8;
      font-family: var(--crest-font-mono);
      font-size: 11.5px;
      font-weight: 500;
      letter-spacing: 0;
      background: #ffffff;
      border-bottom: 1px solid #f1f5f9;
    }

    :deep(.ed-table td.ed-table__cell) {
      padding: 12px 0;
      border-bottom: 1px solid #f1f5f9;
    }

    :deep(.ed-table__row:hover > td.ed-table__cell) {
      background: #fafbfc;
    }

    :deep(.ed-table__empty-block) {
      min-height: 180px;
    }

    :deep(.ed-input__wrapper),
    :deep(.ed-select__wrapper) {
      border-radius: 8px;
      box-shadow: 0 0 0 1px #e2e8f0 inset;
      transition:
        box-shadow 0.14s ease,
        background 0.14s ease;
    }

    :deep(.ed-input__wrapper.is-focus),
    :deep(.ed-select__wrapper.is-focused) {
      box-shadow:
        0 0 0 1px #3b82f6 inset,
        0 0 0 3px rgba(59, 130, 246, 0.1);
    }

    :deep(.ed-button) {
      border-radius: 8px;
      font-family: var(--crest-font-sans);
      font-weight: 600;
    }

    :deep(.ed-button--primary:not(.is-text)) {
      background: #3b82f6;
      border-color: #3b82f6;
    }

    :deep(.ed-button--primary:not(.is-text):hover),
    :deep(.ed-button--primary:not(.is-text):focus) {
      background: #2563eb;
      border-color: #2563eb;
    }

    :deep(.ed-button.is-text),
    :deep(.ed-button--primary.is-text),
    :deep(.ed-button--danger.is-text) {
      height: auto;
      padding: 0 4px;
      background: transparent !important;
      border-color: transparent !important;
      box-shadow: none !important;
    }

    :deep(.ed-button--primary.is-text) {
      color: #3b82f6;
    }

    :deep(.ed-button--primary.is-text:hover) {
      color: #2563eb;
      background: #eff6ff !important;
    }

    :deep(.ed-button--danger.is-text) {
      color: #ef4444;
    }

    :deep(.ed-button--danger.is-text:hover) {
      color: #dc2626;
      background: #fef2f2 !important;
    }

    :deep(.ed-pagination .number.is-active) {
      color: #ffffff;
      background: #0f172a;
      border-radius: 6px;
    }
  }

  &.is-setting-resource-sider {
    .layout-container {
      .layout-sidebar {
        height: calc(100vh - 60px);
      }

      .layout-main {
        transition: padding 0.18s ease;
      }
    }

    .setting-resource-sidebar {
      background: #ffffff;
      border-right: 1px solid #e2e8f0;
      box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
      z-index: 4;
    }

    .setting-resource-tree {
      width: 100%;
      height: 100%;
      padding: 18px 12px 0;
      display: flex;
      flex-direction: column;
      background: #ffffff;
      color: #0f172a;
      font-family: var(--crest-font-sans);

      .tree-header {
        padding: 0 4px 14px;
        border-bottom: 1px solid #e2e8f0;
      }

      .icon-methods {
        min-height: 34px;
        display: flex;
        align-items: center;
        color: #0f172a;

        .title {
          overflow: hidden;
          color: #0f172a;
          font-size: 16px;
          font-weight: 700;
          line-height: 24px;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    .setting-resource-menu {
      flex: 1;
      min-height: 0;
      padding-top: 10px;

      :deep(.ed-menu) {
        min-height: auto;
        padding: 0;
      }

      :deep(.ed-menu-item),
      :deep(.ed-sub-menu__title) {
        height: 36px;
        margin: 0 0 4px;
        padding: 0 12px !important;
        border-radius: 10px;
        font-size: 14px;
        font-weight: 600;
      }

      :deep(.ed-menu-item.is-active) {
        box-shadow: inset 3px 0 0 var(--temp-active-color);
      }
    }

    :deep(.setting-resource-arrow.arrow-side-tree-left) {
      top: 44px;
    }

    :deep(.setting-resource-arrow.arrow-side-tree-right) {
      top: 44px;
    }
  }
}
</style>
