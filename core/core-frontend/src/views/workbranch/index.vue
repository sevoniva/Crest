<script lang="ts" setup>
import userImg from '@/assets/svg/user-img.svg'
import { useI18n } from '@/hooks/web/useI18n'
import { ref, shallowRef, computed } from 'vue'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { useRequestStoreWithOut } from '@/store/modules/request'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import ShortcutTable from './ShortcutTable.vue'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useRouter } from 'vue-router_2'
import { useCache } from '@/hooks/web/useCache'

const userStore = useUserStoreWithOut()
const interactiveStore = interactiveStoreWithOut()
const permissionStore = usePermissionStoreWithOut()
const requestStore = useRequestStoreWithOut()
const { t } = useI18n()
const busiDataMap = computed(() => interactiveStore.getData)
const busiCountCardList = ref([])
const { wsCache } = useCache()
const router = useRouter()
const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
const quickCreationList = shallowRef([
  {
    icon: '/svg/icon_dashboard.svg',
    name: 'panel',
    color: '#3B82F6',
    code: 'DASHBOARD'
  },
  {
    icon: '/svg/icon_data-visualization.svg',
    name: 'screen',
    color: '#1FB6A6',
    code: 'SCREEN'
  },
  {
    icon: '/svg/icon_dataset.svg',
    name: 'dataset',
    color: '#6E62E8',
    code: 'DATASET'
  },
  {
    icon: '/svg/icon_database.svg',
    name: 'datasource',
    color: '#F5A623',
    code: 'SOURCE'
  }
])

const fillCardInfo = () => {
  for (const key in busiDataMap.value) {
    if (key !== '3') {
      busiCountCardList.value.push(busiDataMap.value[key])
    }
    if (quickCreationList.value[key]) {
      quickCreationList.value[key]['menuAuth'] = busiDataMap.value[key]['menuAuth']
      quickCreationList.value[key]['anyManage'] = busiDataMap.value[key]['anyManage']
    }
  }
}
const quickCreate = (flag: number, hasAuth: boolean) => {
  if (!hasAuth) {
    return
  }
  switch (flag) {
    case 0:
      createPanel()
      break
    case 1:
      createScreen()
      break
    case 2:
      createDataset()
      break
    case 3:
      createDatasource()
      break
    default:
      break
  }
}

const createPanel = () => {
  const baseUrl = '#/dashboard?opt=create'
  window.open(baseUrl, openType)
}

const createScreen = () => {
  const baseUrl = '#/dvCanvas?opt=create'
  window.open(baseUrl, openType)
}
const createDataset = () => {
  let routeData = router.resolve({
    path: '/dataset-form'
  })
  window.open(routeData.href, openType)
}
const createDatasource = () => {
  const baseUrl = '#/data/datasource?opt=create'
  window.open(baseUrl, openType)
}

fillCardInfo()

const getLeafTimes = item => {
  const stack = [...(item?.treeNodes || [])]
  const times: number[] = []
  while (stack.length) {
    const node = stack.pop()
    if (node?.leaf) {
      const time = Number(node.createTime || node.updateTime || node.lastEditTime || 0)
      if (time) {
        times.push(time)
      }
    }
    if (node?.children?.length) {
      node.children.forEach(child => stack.push(child))
    }
  }
  return times
}

const getSparkValues = item => {
  const values = new Array(7).fill(0)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const dayMs = 24 * 60 * 60 * 1000
  const times = getLeafTimes(item)

  times.forEach(time => {
    const day = new Date(time)
    day.setHours(0, 0, 0, 0)
    const diff = Math.floor((today.getTime() - day.getTime()) / dayMs)
    if (diff >= 0 && diff < 7) {
      values[6 - diff] += 1
    }
  })

  if (times.length) {
    return values
  }
  return values.fill(Number(item?.leafNodeCount || 0))
}

const getSparkPoints = item => {
  const values = getSparkValues(item)
  const max = Math.max(...values)
  const min = Math.min(...values)
  const range = max - min || 1
  return values
    .map((value, index) => {
      const x = (46 / 6) * index
      const y = max === min ? (max ? 9 : 15) : 16 - ((value - min) / range) * 14
      return `${x.toFixed(1)},${y.toFixed(1)}`
    })
    .join(' ')
}

const getSparkLastPoint = item => {
  const point = getSparkPoints(item).split(' ').pop() || '46,9'
  const [x, y] = point.split(',')
  return { x, y }
}
</script>

<template>
  <div class="workbranch" v-loading="requestStore.loadingMap[permissionStore.currentPath]">
    <div class="info-quick-creation">
      <div class="user-info work-card">
        <div class="profile-row">
          <el-icon class="main-color user-icon-container">
            <Icon name="user-img"><userImg class="svg-icon" /></Icon>
          </el-icon>
          <div class="info">
            <div class="name-role flex-align-center">
              <span :title="userStore.getName" class="name ellipsis">{{ userStore.getName }}</span>
            </div>
            <span v-if="userStore.getUid" class="id"> {{ `ID: ${userStore.getUid}` }} </span>
          </div>
        </div>
        <div
          class="stat-item"
          :class="{ 'de-item-hidden': !item['menuAuth'] }"
          v-for="(item, index) in busiCountCardList"
          :key="index"
        >
          <span
            class="stat-bar"
            :style="{ backgroundColor: quickCreationList[index]?.color || '#3B82F6' }"
          />
          <span class="stat-meta">
            <span class="name">
              {{ t(`auth.${quickCreationList[index].name}`) }}
            </span>
            <span class="code">{{ quickCreationList[index]?.code }}</span>
          </span>
          <svg class="sparkline" viewBox="0 0 46 18" aria-hidden="true">
            <polyline
              :points="getSparkPoints(item)"
              fill="none"
              :stroke="quickCreationList[index]?.color || '#3B82F6'"
              stroke-width="1.6"
              stroke-linecap="round"
              stroke-linejoin="round"
              opacity="0.85"
            />
            <circle
              :cx="getSparkLastPoint(item).x"
              :cy="getSparkLastPoint(item).y"
              r="2"
              :fill="quickCreationList[index]?.color || '#3B82F6'"
            />
          </svg>
          <span class="num"> {{ item['menuAuth'] ? item['leafNodeCount'] : '*' }} </span>
        </div>
      </div>

      <div class="quick-creation work-card">
        <span class="label"> {{ t('work_branch.create_quickly') }} </span>
        <div class="item-creation">
          <div
            :key="ele.name"
            class="item"
            :class="{
              'quick-create-disabled': !ele['menuAuth'] || !ele['anyManage']
            }"
            :style="{ '--accent': ele.color }"
            v-for="(ele, index) in quickCreationList"
            @click="quickCreate(index, ele['menuAuth'] && ele['anyManage'])"
          >
            <el-tooltip
              v-if="!ele['menuAuth'] || !ele['anyManage']"
              class="box-item"
              effect="dark"
              :content="t('work_branch.permission_to_create')"
              placement="top"
            >
              <div class="empty-tooltip-container" />
            </el-tooltip>
            <span class="quick-icon" :style="{ backgroundColor: ele.color }">
              <img :src="ele.icon" :alt="t(`auth.${ele.name}`)" />
            </span>
            <span class="name">
              {{ t(`auth.${ele.name}`) }}
            </span>
          </div>
        </div>
      </div>
    </div>
    <div class="workbranch-content">
      <el-scrollbar class="workbranch-content-scroll">
        <shortcut-table />
      </el-scrollbar>
    </div>
  </div>
</template>

<style lang="less" scoped>
.workbranch {
  display: grid;
  grid-template-columns: clamp(320px, 18vw, 360px) minmax(0, 1fr);
  gap: 18px;
  align-items: start;
  width: 100%;
  min-height: calc(100vh - 60px);
  padding: clamp(22px, 2vw, 32px) clamp(28px, 4vw, 72px);
  overflow: auto;
  font-family: var(--crest-font-sans);
  background: #f8fafc;
  --workbranch-card-radius: 14px;

  .main-btn {
    display: inline-flex;
    height: 20px;
    padding: 0 6px;
    align-items: center;
  }

  .work-card {
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: var(--workbranch-card-radius);
    box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  }

  .info-quick-creation {
    display: flex;
    flex-direction: column;
    gap: 16px;
    width: 100%;
    min-height: 0;

    .main-color {
      color: #3b82f6;
      background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
    }

    .user-info {
      display: flex;
      flex-direction: column;
      gap: 11px;
      padding: 22px 24px 18px;

      .profile-row {
        display: flex;
        align-items: center;
        gap: 14px;
        margin-bottom: 7px;
      }

      .user-icon-container {
        position: relative;
        flex: none;
        width: 52px !important;
        height: 52px !important;

        &::after {
          position: absolute;
          right: -2px;
          bottom: -2px;
          width: 14px;
          height: 14px;
          content: '';
          background: #22c55e;
          border: 2.5px solid #ffffff;
          border-radius: 50%;
        }
      }

      .ed-icon {
        font-size: 22px;
        padding: 0;
        border-radius: 50%;
      }

      .info {
        display: flex;
        flex: 1;
        align-items: flex-start;
        flex-wrap: wrap;
        min-width: 0;

        .name-role {
          width: 100%;
          margin-bottom: 3px;
          color: #0f172a;
          font-family: var(--crest-font-sans);
          font-style: normal;

          .name {
            max-width: 210px;
            font-size: 17px;
            font-weight: 600;
            line-height: 24px;
          }
        }

        .id {
          width: 200px;
          color: #64748b;
          font-family: var(--crest-font-mono);
          font-size: 12px;
          font-weight: 400;
          line-height: 18px;
        }
      }

      .stat-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 6px 0;
        font-family: var(--crest-font-sans);
        font-style: normal;

        &:first-of-type {
          padding-top: 16px;
          border-top: 1px solid #f1f5f9;
        }

        .stat-bar {
          flex: none;
          width: 7px;
          height: 24px;
          border-radius: 2px;
        }

        .stat-meta {
          display: flex;
          flex: 1;
          flex-direction: column;
          min-width: 0;
        }

        .name {
          color: #64748b;
          font-size: 12.5px;
          font-weight: 400;
          line-height: 18px;
        }

        .code {
          color: #94a3b8;
          font-family: var(--crest-font-mono);
          font-size: 10.5px;
          line-height: 16px;
        }

        .sparkline {
          flex: none;
          width: 46px;
          height: 18px;
        }

        .num {
          min-width: 34px;
          color: #0f172a;
          font-size: 22px;
          font-weight: 700;
          line-height: 22px;
          text-align: right;
          font-variant-numeric: tabular-nums;
        }
      }

      .de-item-hidden {
        cursor: not-allowed;
      }
    }

    .quick-creation {
      display: flex;
      flex-direction: column;
      min-height: 0;
      padding: 18px 22px 22px;

      .label {
        color: #334155;
        font-feature-settings: 'clig' off, 'liga' off;
        font-family: var(--crest-font-sans);
        font-size: 13.5px;
        font-style: normal;
        font-weight: 600;
        line-height: 20px;
      }

      .item-creation {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 10px;
        align-content: start;
        flex: 1;
        margin-top: 14px;

        .item {
          position: relative;
          display: flex;
          align-items: center;
          gap: 11px;
          min-height: 64px;
          padding: 12px 13px;
          overflow: hidden;
          cursor: pointer;
          background: #ffffff;
          border: 1px solid #e2e8f0;
          border-radius: 10px;
          transition:
            border-color 0.16s ease,
            box-shadow 0.16s ease,
            transform 0.16s ease;

          &::after {
            position: absolute;
            inset: 0;
            pointer-events: none;
            content: '';
            background: linear-gradient(135deg, var(--accent, #3b82f6) 0%, transparent 38%);
            opacity: 0;
            transition: opacity 0.2s ease;
          }

          &:hover {
            border-color: transparent;
            box-shadow:
              0 4px 14px -4px rgba(15, 23, 42, 0.1),
              0 0 0 1.5px var(--accent, #3b82f6);
            transform: translateY(-1px);
          }

          &:hover::after {
            opacity: 0.08;
          }

          .quick-icon {
            position: relative;
            z-index: 1;
            display: flex;
            flex: none;
            align-items: center;
            justify-content: center;
            width: 36px;
            height: 36px;
            border-radius: 8px;

            img {
              width: 22px;
              height: 22px;
              object-fit: contain;
            }
          }

          .name {
            position: relative;
            z-index: 1;
            min-width: 0;
            color: #0f172a;
            font-family: var(--crest-font-sans);
            font-size: 13.5px;
            font-style: normal;
            font-weight: 600;
            line-height: 20px;
          }
        }

        .item-quick {
          width: 100%;
          .main-color-quick {
            font-size: 32px;
            margin-right: 12px;
          }
        }
        .quick-create-disabled {
          cursor: not-allowed;
          color: var(--ed-color-info-light-5);
          background-color: var(--ed-color-info-light-9);
          border-color: var(--ed-color-info-light-8);
          .name {
            color: var(--ed-color-info-light-5) !important;
          }
          .quick-icon {
            background-color: var(--ed-color-primary-light-8) !important;
            border-color: var(--ed-color-info-light-8) !important;
          }
          .empty-tooltip-container {
            width: 146px;
            position: absolute;
            height: 52px;
            margin-left: -16px;
          }
          .empty-tooltip-container-template {
            width: 300px;
            position: absolute;
            height: 52px;
            margin-left: -16px;
          }
          .template-create {
            opacity: 0.3;
          }
        }
      }
    }
  }

  .workbranch-content {
    min-width: 0;
    height: clamp(520px, calc(100vh - 220px), 640px);
    min-height: 520px;
    overflow: hidden;
    border-radius: var(--workbranch-card-radius);

    .workbranch-content-scroll {
      height: 100%;
      border-radius: inherit;

      :deep(.ed-scrollbar__wrap),
      :deep(.ed-scrollbar__view) {
        height: 100%;
        border-radius: inherit;
      }
    }
  }
}

@media (max-width: 1180px) {
  .workbranch {
    grid-template-columns: 300px minmax(0, 1fr);
    padding: 20px;
  }
}

@media (max-width: 900px) {
  .workbranch {
    grid-template-columns: 1fr;
    height: auto;
  }

  .workbranch .workbranch-content {
    height: auto;
    min-height: 520px;
  }
}
</style>
