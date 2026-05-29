<script lang="ts" setup>
import icon_left_outlined from '@/assets/svg/icon_left_outlined.svg'
import icon_right_outlined from '@/assets/svg/icon_right_outlined.svg'
import { useAppStoreWithOut } from '@/store/modules/app'
const appStore = useAppStoreWithOut()
defineProps({
  isInside: {
    type: Boolean,
    default: false
  }
})
const emits = defineEmits(['changeSideTreeStatus'])
const handleClick = val => {
  appStore.setArrowSide(val)
  emits('changeSideTreeStatus', val)
}
</script>

<template>
  <div
    @click="handleClick(false)"
    v-if="!isInside"
    class="arrow-side-tree arrow-side-tree-left"
  >
    <el-icon>
      <Icon name="icon_left_outlined"><icon_left_outlined class="svg-icon" /></Icon>
    </el-icon>
  </div>
  <div
    @click="handleClick(true)"
    v-else
    class="arrow-side-tree arrow-side-tree-right"
  >
    <el-icon>
      <Icon name="icon_right_outlined"><icon_right_outlined class="svg-icon" /></Icon>
    </el-icon>
  </div>
</template>

<style lang="less" scoped>
.arrow-side-tree-left {
  top: 44px;
  height: 28px;
  width: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
}

.arrow-side-tree-right {
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
  top: 44px;
  height: 28px;
  width: 22px;
  display: flex;
  align-items: center;
  padding-left: 2px;
  border-top-right-radius: 14px;
  border-bottom-right-radius: 14px;
  &:hover {
    padding-left: 4px;
    width: 28px;
  }
}

.arrow-side-tree {
  position: absolute;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #334155;
  cursor: pointer;
  z-index: 10;
  &:hover {
    border-color: #bfdbfe;
    background: #eff6ff;
    .ed-icon {
      color: var(--ed-color-primary);
    }
  }
  .ed-icon {
    font-size: 12px;
  }
}
</style>
