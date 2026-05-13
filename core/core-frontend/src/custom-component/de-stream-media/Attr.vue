<template>
  <div class="attr-list de-collapse-style">
    <CommonAttr :themes="themes" :element="curComponent">
      <el-collapse-item
        :effect="themes"
        :title="t('visualization.stream_media_info')"
        name="streamLinks"
        v-if="curComponent && curComponent.streamMediaLinks && !mobileInPc"
      >
        <stream-media-links
          :link-info="curComponent.streamMediaLinks"
          :themes="themes"
        ></stream-media-links>
      </el-collapse-item>
    </CommonAttr>
  </div>
</template>

<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { storeToRefs } from 'pinia'
import StreamMediaLinks from '@/custom-component/de-stream-media/StreamMediaLinks.vue'
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
