<script lang="ts" setup>
import icon_expandRight_filled from '@/assets/svg/icon_expand-right_filled.svg'
import { ref } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { timestampFormatDate } from '../dataset/form/util'
import { useI18n } from '@/hooks/web/useI18n'
defineProps({
  name: propTypes.string.def(''),
  time: propTypes.number.def(0),
  showTime: propTypes.bool.def(false)
})
const active = ref(true)
const { t } = useI18n()
defineExpose({
  active
})
</script>

<template>
  <div :class="[active ? 'active' : 'deactivate', 'base-info-content']">
    <p class="title" @click="active = !active">
      <el-icon style="font-size: 10px">
        <Icon name="icon_expand-right_filled"><icon_expandRight_filled class="svg-icon" /></Icon>
      </el-icon>
      <span class="name">{{ name }}</span>
      <span v-show="showTime" class="time">
        {{ t('data_source.data_time') }}{{ timestampFormatDate(time) }}</span
      >
    </p>
    <slot :active="active"></slot>
  </div>
</template>

<style lang="less" scoped>
.base-info-content {
  padding: 24px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
  margin: 16px 24px 0 24px;
  position: relative;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);

  & + .base-info-content {
    margin-top: 16px;
  }

  .update-records-time {
    color: #64748b;
    font-family: var(--crest-font-sans);
    font-size: 14px;
    font-style: normal;
    font-weight: 400;
    line-height: 22px;
    margin-left: 8px;
  }

  .title {
    display: flex;
    align-items: center;
    cursor: pointer;
  }

  .name {
    color: #0f172a;
    font-family: var(--crest-font-sans);
    font-size: 16px;
    font-style: normal;
    font-weight: 500;
    line-height: 24px;
    margin-left: 8px;
  }
  .time {
    color: #94a3b8;
    font-family: var(--crest-font-mono);
    font-size: 14px;
    line-height: 22px;
    padding: 0 0 0 8px;
  }
  &.active {
    .title {
      .ed-icon {
        transform: rotate(90deg);
        color: var(--ed-color-primary);
      }
    }
    overflow: auto;
    height: auto;
  }

  &.deactivate {
    height: 72px;
    overflow: hidden;
    .title {
      .ed-icon {
        transform: rotate(0);
        color: var(--ed-color-primary);
      }
    }
  }
}
</style>
