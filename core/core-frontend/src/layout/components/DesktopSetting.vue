<script lang="ts" setup>
import iconSetting from '@/assets/svg/icon-setting.svg'
import LangSelector from '@/layout/components/LangSelector.vue'
import { useRouter } from 'vue-router_2'
import icon_right_outlined from '@/assets/svg/icon_right_outlined.svg'
import dvPreviewDownload from '@/assets/svg/icon_download_outlined.svg'
import icon_more_outlined from '@/assets/svg/icon_more_outlined.svg'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { computed } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'

const appearanceStore = useAppearanceStoreWithOut()
const navigateBg = computed(() => appearanceStore.getNavigateBg)
const { push, resolve } = useRouter()
const redirectUser = () => {
  const sysMenu = resolve('/sys-setting')
  const kidPath = sysMenu.matched[0].children[0].path
  push(`${sysMenu.path}/${kidPath}`)
}

const downloadClick = params => {
  useEmitt().emitter.emit('data-export-center', params)
}
</script>

<template>
  <el-tooltip effect="dark" :content="$t('data_export.export_center')" placement="bottom">
    <el-icon
      style="margin-left: 10px"
      class="preview-download_icon"
      :class="navigateBg === 'light' && 'is-light-setting'"
      @click="downloadClick"
    >
      <Icon><dvPreviewDownload class="svg-icon" /></Icon>
    </el-icon>
  </el-tooltip>
  <el-tooltip
    class="box-item"
    effect="dark"
    :content="$t('commons.system_setting')"
    placement="top"
  >
    <div
      class="sys-setting in-iframe-setting"
      :class="{
        'is-light-setting': navigateBg && navigateBg === 'light'
      }"
    >
      <el-icon @click="redirectUser">
        <Icon><iconSetting class="svg-icon icon-setting" /></Icon>
      </el-icon>
    </div>
  </el-tooltip>

  <el-popover
    popper-class="popper-class_ai-copilot"
    placement="bottom-end"
    :width="224"
    trigger="hover"
  >
    <template #default>
      <div>
        <el-popover
          :teleported="false"
          popper-class="popper-class_ai-copilot"
          placement="left-start"
          :width="224"
          trigger="click"
          ><template #default>
            <div style="padding: 8px 0">
              <LangSelector />
            </div>
          </template>
          <template #reference>
            <div class="item-select_info">
              {{ $t('commons.language') }}
              <el-icon style="font-size: 16px">
                <Icon><icon_right_outlined></icon_right_outlined></Icon>
              </el-icon>
            </div> </template
        ></el-popover>
      </div>
    </template>
    <template #reference>
      <el-icon class="preview-download_icon" :class="navigateBg === 'light' && 'is-light-setting'">
        <Icon><icon_more_outlined class="svg-icon" /></Icon>
      </el-icon>
    </template>
  </el-popover>
</template>

<style lang="less" scoped>
.sys-setting {
  margin: 0 10px 0 0;
  padding: 5px;
  height: 28px;
  width: 28px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  &:hover {
    background-color: #1e2738;
  }
}
.in-iframe-setting {
  margin-left: 10px !important;
}
.is-light-setting {
  &:hover {
    background-color: #1f23291a !important;
  }
}
.preview-download_icon {
  padding: 5px;
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
</style>
<style lang="less">
.popper-class_ai-copilot {
  padding: 0 !important;

  .card-content_desk {
    display: flex;
    justify-content: space-between;
    padding: 12px 12px 8px;
  }
  .border-top {
    border-top: 1px solid #1f232926;
    padding-top: 4px;
    padding-bottom: 8px;
  }
  .item-select_info {
    cursor: pointer;
    height: 40px;
    padding: 9px 11px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #1f2329;
    .ed-icon {
      color: #8f959e;
    }

    &:hover {
      background: #1f23291a;
    }
  }
}
</style>
