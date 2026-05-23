<script lang="ts" setup>
import icon_app_outlined from '@/assets/svg/icon_app_outlined.svg'
import icon_dashboard_outlined from '@/assets/svg/icon_dashboard_outlined.svg'
import icon_database_outlined from '@/assets/svg/icon_database_outlined.svg'
import icon_operationAnalysis_outlined from '@/assets/svg/icon_operation-analysis_outlined.svg'
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
    icon: icon_dashboard_outlined,
    name: 'panel',
    color: '#3370ff'
  },
  {
    icon: icon_operationAnalysis_outlined,
    name: 'screen',
    color: '#00d6b9'
  },
  {
    icon: icon_app_outlined,
    name: 'dataset',
    color: '#16c0ff'
  },
  {
    icon: icon_database_outlined,
    name: 'datasource',
    color: '#7f3bf6'
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
</script>

<template>
  <div class="workbranch" v-loading="requestStore.loadingMap[permissionStore.currentPath]">
    <div class="info-quick-creation">
      <div class="user-info border-radius-12">
        <el-icon class="main-color user-icon-container">
          <Icon name="user-img"><userImg class="svg-icon" /></Icon>
        </el-icon>
        <div class="info">
          <div class="name-role flex-align-center">
            <span :title="userStore.getName" style="max-width: 200px" class="name ellipsis">{{
              userStore.getName
            }}</span>
            <span class="role main-btn" />
          </div>
          <span v-if="userStore.getUid" class="id"> {{ `ID: ${userStore.getUid}` }} </span>
        </div>
        <div
          class="item"
          :class="{ 'de-item-hidden': !item['menuAuth'] }"
          v-for="(item, index) in busiCountCardList"
          :key="index"
        >
          <span class="name">
            {{ t(`auth.${quickCreationList[index].name}`) }}
          </span>
          <span class="num"> {{ item['menuAuth'] ? item['leafNodeCount'] : '*' }} </span>
        </div>
      </div>

      <div class="quick-creation border-radius-12">
        <span class="label"> {{ t('work_branch.create_quickly') }} </span>
        <div class="item-creation">
          <div
            :key="ele.name"
            class="item border-radius-12"
            :class="{
              'quick-create-disabled': !ele['menuAuth'] || !ele['anyManage']
            }"
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
            <el-icon class="main-color" :style="{ backgroundColor: ele.color }">
              <Icon><component class="svg-icon" :is="ele.icon"></component></Icon>
            </el-icon>
            <span class="name">
              {{ t(`auth.${ele.name}`) }}
            </span>
          </div>
        </div>
      </div>
    </div>
    <div class="workbranch-content">
      <el-scrollbar style="height: 100%">
        <shortcut-table />
      </el-scrollbar>
    </div>
  </div>
</template>

<style lang="less" scoped>
.workbranch {
  width: 100vw;
  height: calc(100vh - 56px);
  background: #f5f6f7;
  padding: 24px;
  display: flex;
  justify-content: space-between;

  .main-btn {
    display: inline-flex;
    height: 20px;
    padding: 0 6px;
    align-items: center;
  }

  .info-quick-creation {
    width: 360px;
    .main-color {
      background: var(--ed-color-primary);
      width: 32px;
      height: 32px;
    }
    .user-info {
      padding: 24px 16px 16px 16px;
      background: #fff;
      border-radius: 6px;
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      .user-icon-container {
        width: 48px !important;
        height: 48px !important;
      }
      .ed-icon {
        font-size: 48px;
        padding: 8px;
        border-radius: 50%;
      }

      .info {
        margin: 0 0 24px 12px;
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        width: calc(100% - 60px);
        height: 50px;
        .name-role {
          margin-bottom: 4px;
          color: #1f2329;
          font-family: var(--de-custom_font, 'PingFang');
          font-style: normal;
          .name {
            font-size: 16px;
            font-weight: 500;
            line-height: 24px;
          }

          .role {
            width: 55px;
            display: inline-flex;
            margin-left: 4px;
            height: 20px;
            padding: 0 6px;
            align-items: center;
            font-size: 12px;
            color: var(--ed-color-primary-dark-2, #2b5fd9);
            border-radius: 2px;
          }
        }
        .id {
          color: #646a73;
          font-size: 14px;
          font-weight: 400;
          line-height: 22px;
          width: 200px;
        }
      }

      .item {
        font-family: var(--de-custom_font, 'PingFang');
        font-style: normal;
        display: flex;
        flex-direction: column;
        width: 109px;
        height: 70px;
        padding: 8px;

        .name {
          color: #646a73;
          font-weight: 400;
          line-height: 22px;
          font-size: 14px;
        }
        .num {
          margin-top: 4px;
          color: #1f2329;
          font-size: 20px;
          font-weight: 500;
          line-height: 28px;
          letter-spacing: -0.2px;
        }
      }

      .de-item-hidden {
        cursor: not-allowed;
      }
    }

    .quick-creation {
      border-radius: 6px;
      background: #fff;
      margin-top: 16px;
      padding: 24px;

      .label {
        color: #1f2329;
        font-feature-settings: 'clig' off, 'liga' off;
        font-family: var(--de-custom_font, 'PingFang');
        font-size: 16px;
        font-style: normal;
        font-weight: 500;
        line-height: 24px;
      }

      .item-creation {
        display: flex;
        justify-content: space-between;
        flex-wrap: wrap;
        .item {
          padding: 12px;
          width: 150px;
          margin-top: 16px;
          border-radius: 6px;
          border: 1px solid #dee0e3;
          display: flex;
          align-items: center;
          cursor: pointer;
          &:hover {
            box-shadow: 0px 6px 24px 0px rgba(31, 35, 41, 0.08);
          }

          .main-color {
            font-size: 21.33px;
            padding: 5.33px;
            margin-right: 12px;
            border-radius: 8px;
            color: #fff;
          }

          .name {
            color: #1f2329;
            font-family: var(--de-custom_font, 'PingFang');
            font-size: 14px;
            font-style: normal;
            font-weight: 400;
            line-height: 22px;
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
          .main-color {
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
    width: calc(100% - 376px);
  }
}
</style>
