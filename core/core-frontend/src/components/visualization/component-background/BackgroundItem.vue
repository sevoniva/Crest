<template>
  <div class="testcase-template">
    <div
      :class="[
        {
          ['template-img-active']: itemActive
        },
        'template-img'
      ]"
      :style="itemStyle"
      @click.stop="setBoard"
    >
      <Icon
        ><component
          class="svg-icon svg-background"
          :style="{ color: curComponent.commonBackground.innerImageColor }"
          :is="iconBoardMap[mainIconClass]"
        ></component
      ></Icon>
    </div>
    <span class="demonstration">{{ template.name }}</span>
  </div>
</template>

<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { hexColorToRGBA } from '@/views/chart/components/js/util'
import { storeToRefs } from 'pinia'
import { computed, toRefs } from 'vue'
const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)
import Icon from '@/components/icon-custom/src/Icon.vue'
import { iconBoardMap } from '@/components/icon-group/board-list'

const props = defineProps({
  template: {
    type: Object,
    default() {
      return {}
    }
  }
})

const { template } = toRefs(props)

const itemStyle = computed(() => {
  if (curComponent.value.commonBackground.backgroundColorSelect) {
    return {
      'background-color': hexColorToRGBA(
        curComponent.value.commonBackground.color,
        curComponent.value.commonBackground.alpha
      )
    }
  } else {
    return {}
  }
})

const mainIconClass = computed(() => {
  return template.value.url.replace('board/', '').replace('.svg', '')
})

const itemActive = computed(() => {
  return (
    curComponent.value &&
    curComponent.value.commonBackground &&
    curComponent.value.commonBackground.innerImage === template.value.url
  )
})

const setBoard = () => {
  curComponent.value.commonBackground.innerImage = template.value.url
}
</script>

<style scoped>
.testcase-template {
  display: inline-block;
  width: 90px;
  margin: 5px 0;
}

.demonstration {
  display: block;
  width: 130px;
  margin: 10px auto;
  overflow: hidden;
  font-size: 8px;
  color: gray;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.template-img {
  position: relative;
  width: 130px;
  height: 80px;
  margin: 0 auto;
  border-radius: 3px;
  box-sizing: border-box;
}

.template-img:hover {
  color: deepskyblue;
  cursor: pointer;
  border: solid 1px #4b8fdf;
  border-radius: 3px;
}

.template-img > i {
  display: none;
  float: right;
  margin: 2px;
  color: gray;
}

.template-img > i:hover {
  color: red;
}

.template-img:hover > .el-icon-error {
  display: inline;
}

.template-img:hover > .el-icon-edit {
  display: inline;
}

.template-img-active {
  color: deepskyblue;
  border: solid 1px red;
  border-radius: 3px;
}

.svg-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100% !important;
  height: 100% !important;
}

:deep(.ed-row) {
  flex-direction: column;
}
</style>
