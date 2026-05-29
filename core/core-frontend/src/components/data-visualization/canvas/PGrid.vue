<template>
  <div class="grid">
    <div v-for="(yItem, index) in positionBox" :key="index + 'y'" class="outer-class">
      <div v-for="(xItem, idx) in yItem" :key="idx + 'x'" :style="classInfo" class="inner-class">
        {{ xItem.el ? '1' : '0' }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, PropType, toRefs } from 'vue'

const props = defineProps({
  positionBox: {
    type: Array as PropType<Array<Array<Record<string, any>>>>
  },
  matrixStyle: {
    type: Object as PropType<Record<string, any>>
  }
})

const { positionBox, matrixStyle } = toRefs(props)

const classInfo = computed(() => {
  return {
    width: matrixStyle.value.width + 'px',
    height: matrixStyle.value.height + 'px'
  }
})
</script>

<style lang="less" scoped>
.grid {
  position: absolute;
  top: 0;
  left: 0;
}
.outer-class {
  float: left;
  width: 105%;
}

.inner-class {
  float: left;
  border: 1px solid #b3d4fc;
}
</style>
