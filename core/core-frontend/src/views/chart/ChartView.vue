<script lang="ts" setup>
import {
  shallowRef,
  defineAsyncComponent,
  ref,
  onBeforeUnmount,
  onBeforeMount,
  onMounted,
  nextTick
} from 'vue'
import { debounce } from 'lodash-es'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useLoading } from '@/hooks/web/useLoading'

const { close } = useLoading()
const currentComponent = shallowRef()
const Preview = defineAsyncComponent(() => import('@/views/data-visualization/PreviewCanvas.vue'))
const VisualizationEditor = defineAsyncComponent(
  () => import('@/views/data-visualization/index.vue')
)
const DashboardEditor = defineAsyncComponent(() => import('@/views/dashboard/index.vue'))

const Dashboard = defineAsyncComponent(() => import('@/pages/panel/DashboardPreview.vue'))
const ViewWrapper = defineAsyncComponent(() => import('@/pages/panel/ViewWrapper.vue'))
const Dataset = defineAsyncComponent(() => import('@/views/visualized/data/dataset/index.vue'))
const Datasource = defineAsyncComponent(
  () => import('@/views/visualized/data/datasource/index.vue')
)
const ScreenPanel = defineAsyncComponent(() => import('@/views/data-visualization/PreviewShow.vue'))
const DashboardPanel = defineAsyncComponent(
  () => import('@/views/dashboard/DashboardPreviewShow.vue')
)

const componentMap = {
  DashboardEditor,
  VisualizationEditor,
  ViewWrapper,
  Preview,
  Dashboard,
  Dataset,
  Datasource,
  ScreenPanel,
  DashboardPanel
}
const iframeStyle = ref(null)
const setStyle = debounce(() => {
  iframeStyle.value = {
    height: window.innerHeight + 'px',
    width: window.innerWidth + 'px'
  }
}, 300)
onBeforeMount(() => {
  window.addEventListener('resize', setStyle)
  setStyle()
})
onMounted(() => {
  close()
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', setStyle)
})

const showComponent = ref(false)

const initIframe = (name: string) => {
  showComponent.value = false
  nextTick(() => {
    currentComponent.value = componentMap[name || 'ViewWrapper']
    showComponent.value = true
  })
}

useEmitt({
  name: 'changeCurrentComponent',
  callback: initIframe
})
</script>

<template>
  <div :style="iframeStyle">
    <component :is="currentComponent" v-if="showComponent"></component>
  </div>
</template>
