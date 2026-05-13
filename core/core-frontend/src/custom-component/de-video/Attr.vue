<template>
  <div class="attr-list de-collapse-style">
    <CommonAttr :themes="themes" :element="curComponent">
      <el-collapse-item
        :effect="themes"
        :title="t('visualization.link_info')"
        name="videoLinks"
        v-if="curComponent && curComponent.videoLinks && !mobileInPc"
      >
        <video-links :link-info="curComponent.videoLinks" :themes="themes"></video-links>
      </el-collapse-item>
    </CommonAttr>
  </div>
</template>

<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { storeToRefs } from 'pinia'
import VideoLinks from '@/custom-component/de-video/VideoLinks.vue'
import { useI18n } from '@/hooks/web/useI18n'
const dvMainStore = dvMainStoreWithOut()
const { curComponent, mobileInPc } = storeToRefs(dvMainStore)
const { t } = useI18n()

withDefaults(
  defineProps<{
    themes?: EditorTheme
  }>(),
  {
    themes: 'dark'
  }
)
</script>
