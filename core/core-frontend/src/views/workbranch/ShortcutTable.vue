<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive, onMounted, computed, watch } from 'vue'
import type { TabsPaneContext } from 'element-plus-secondary'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { useRouter } from 'vue-router_2'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { shortcutOption } from './ShortcutOption'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { storeApi } from '@/api/visualization/dataVisualization'
import { useCache } from '@/hooks/web/useCache'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useEmbedded } from '@/store/modules/embedded'
const userStore = useUserStoreWithOut()
const { resolve } = useRouter()
const { t } = useI18n()
const interactiveStore = interactiveStoreWithOut()
const { wsCache } = useCache()
const appStore = useAppStoreWithOut()
const embeddedStore = useEmbedded()
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

import { cloneDeep } from 'lodash-es'
const { push } = useRouter()
defineProps({
  expand: {
    type: Boolean,
    default: false
  }
})
const desktop = wsCache.get('app.desktop')
const panelKeyword = ref()
const activeName = ref('store')
const activeCommand = ref('all_types')
const state = reactive({
  tableData: [],
  curTypeList: [],
  tableColumn: []
})
const busiDataMap = computed(() => interactiveStore.getData)
const iconMap = {
  panel: '/svg/icon_dashboard.svg',
  panelMobile: '/svg/icon_dashboard.svg',
  dashboard: '/svg/icon_dashboard.svg',
  dashboardMobile: '/svg/icon_dashboard.svg',
  dashboardMobileDisabled: '/svg/icon_dashboard.svg',
  screen: '/svg/icon_data-visualization.svg',
  dataV: '/svg/icon_data-visualization.svg',
  dataset: '/svg/icon_dataset.svg',
  datasource: '/svg/icon_database.svg'
}
const typeColorMap = {
  panel: '#3B82F6',
  panelMobile: '#3B82F6',
  dashboard: '#3B82F6',
  screen: '#1FB6A6',
  dataV: '#1FB6A6',
  dataset: '#6E62E8',
  datasource: '#F5A623'
}

const jumpActiveCheck = row => {
  return row && ['dashboard', 'panel', 'dataV', 'screen'].includes(row.type)
}

const handleClick = (ele: TabsPaneContext) => {
  if (ele.paneName === 'recent' || ele.paneName === 'store') {
    loading.value = true
    shortcutOption.setBusiFlag(ele.paneName)
    state.curTypeList = shortcutOption
      .getBusiList()
      .filter(busi => busi === 'all_types' || busiAuthList.includes(busi))
    state.tableColumn = shortcutOption.getColumnList()
    loadTableData()
  }
}
const getBusiListWithPermission = () => {
  const baseFlagList = ['panel', 'screen', 'dataset', 'datasource']
  const busiFlagList = []
  for (const key in busiDataMap.value) {
    if (busiDataMap.value[key].menuAuth) {
      busiFlagList.push(baseFlagList[parseInt(key)])
    }
  }
  baseTablePaneList.value[0].disabled = !busiFlagList?.length
  baseTablePaneList.value[1].disabled =
    !busiFlagList.includes('panel') && !busiFlagList.includes('screen')
  return busiFlagList
}
const triggerFilterPanel = () => {
  loadTableData()
}
const selectType = item => {
  activeCommand.value = item
  loadTableData()
}
const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
const preview = (row, disabled = false) => {
  if (!disabled) {
    const id = activeName.value === 'recent' ? row.id : row.resourceId
    const dvType = ['dashboard', 'panel'].includes(row.type) ? 'dashboard' : 'dataV'
    const routeUrl = resolve({
      path: '/preview',
      query: { dvId: id, dvType }
    })
    window.open(routeUrl.href, '_blank')
  }
}

const openDataset = id => {
  const routeUrl = resolve({
    path: '/dataset-form',
    query: { id: id }
  })
  window.open(routeUrl.href, openType)
}
const formatterTime = (_, _column, cellValue) => {
  return dayjs(new Date(cellValue)).format('YYYY-MM-DD HH:mm:ss')
}
const formatterRelativeTime = cellValue => {
  if (!cellValue) return '-'
  return dayjs(new Date(cellValue)).fromNow()
}

const typeMap = {
  screen: t('work_branch.big_data_screen'),
  dataV: t('work_branch.big_data_screen'),
  dashboard: t('work_branch.dashboard'),
  panel: t('work_branch.dashboard'),
  dataset: t('work_branch.data_set'),
  datasource: t('work_branch.data_source')
}

const getTypeColor = type => {
  return typeColorMap[type] || '#3B82F6'
}

const getAssetCode = row => {
  const id = activeName.value === 'recent' ? row.id : row.resourceId
  return id ? String(id) : '-'
}

const getUserInitial = value => {
  const name = value || userStore.getName || ''
  return name ? String(name).trim().charAt(0).toUpperCase() : '-'
}

const loadTableData = () => {
  loading.value = true
  const queryType = activeCommand.value === 'all_types' ? '' : activeCommand.value
  shortcutOption
    .loadData({ type: queryType, keyword: panelKeyword.value, asc: !orderDesc.value })
    .then(res => {
      state.tableData = res.data
    })
    .finally(() => {
      imgType.value = getEmptyImg()
      emptyDesc.value = getEmptyDesc()
      loading.value = false
    })
}

const baseTablePaneList = ref([
  { title: t('work_branch.my_collection'), name: 'store', disabled: false },
  { title: t('work_branch.recently_used'), name: 'recent', disabled: false }
])

const dfTablePaneList = ref([])

const busiAuthList = getBusiListWithPermission()

const tablePaneList = computed(() => {
  const list = cloneDeep(!!busiAuthList.length ? baseTablePaneList.value : [])
  for (const valueElement of dfTablePaneList.value) {
    list.push(valueElement)
  }
  return list
})

const firstChangeActiveName = ref(false)

watch(
  () => tablePaneList.value.length,
  () => {
    if (tablePaneList.value.length > 0 && !firstChangeActiveName.value) {
      firstChangeActiveName.value = true
      activeName.value = tablePaneList.value[0].name
    }
  }
)

onMounted(() => {
  !!busiAuthList.length &&
    handleClick({
      paneName: 'store',
      uid: 0,
      slots: undefined,
      props: undefined,
      active: false,
      index: '',
      isClosable: false
    })
})
const orderDesc = ref(true)
const loading = ref(false)
const sortChange = param => {
  orderDesc.value = true
  const type = param.order.substring(0, param.order.indexOf('ending'))
  orderDesc.value = type === 'desc'
  loadTableData()
}

const handleCellClick = row => {
  if (row && !checkDisabled(row)) {
    const sourceId = activeName.value === 'recent' ? row.id : row.resourceId
    if (['dashboard', 'panel'].includes(row.type)) {
      window.open('#/panel/index?dvId=' + sourceId, '_self')
    } else if (['dataV', 'screen'].includes(row.type)) {
      window.open('#/screen/index?dvId=' + sourceId, '_self')
    } else if (['dataset'].includes(row.type)) {
      const routeName =
        embeddedStore.getToken && appStore.getIsIframe ? 'dataset-embedded' : 'dataset'
      push({
        name: routeName,
        params: {
          id: sourceId
        }
      })
    } else if (['datasource'].includes(row.type)) {
      push({
        name: 'datasource',
        params: {
          id: sourceId
        }
      })
    }
  }
}

const executeStore = rowInfo => {
  const param = {
    id: rowInfo.id,
    type: rowInfo.type
  }
  storeApi(param).then(() => {
    rowInfo.favorite = !rowInfo.favorite
  })
}

const checkDisabled = row => {
  return activeName.value === 'store' && !row.extFlag1
}

const executeCancelStore = rowInfo => {
  if (!checkDisabled(rowInfo)) {
    const param = {
      id: rowInfo.resourceId,
      type: rowInfo.type === 'dataV' ? 'screen' : 'panel'
    }
    storeApi(param).then(() => {
      loadTableData()
    })
  }
}

const imgType = ref()
const emptyDesc = ref('')
const getEmptyImg = (): string => {
  if (panelKeyword.value) {
    return 'tree'
  }
  return 'noneWhite'
}

const getEmptyDesc = (): string => {
  if (panelKeyword.value) {
    return t('work_branch.relevant_content_found')
  }
  if (activeName.value === 'recent') {
    return t('work_branch.no_content_yet')
  }
  if (activeName.value === 'store') {
    return t('work_branch.no_favorites_yet')
  }
  return ''
}
</script>

<template>
  <div
    class="dashboard-type border-radius-12"
    :class="expand && 'expand'"
    v-if="tablePaneList.length"
    v-loading="loading"
  >
    <el-tabs v-model="activeName" class="dashboard-type-tabs" @tab-click="handleClick">
      <el-tab-pane
        v-for="item in tablePaneList"
        :key="item.name"
        :disabled="item.disabled"
        :label="item.title"
        :name="item.name"
      >
        <template #label>
          <span class="custom-tabs-label">
            <el-tooltip
              placement="top"
              v-if="item.disabled"
              :content="t('work_branch.permission_denied')"
            >
              <span>{{ item.title }}</span>
            </el-tooltip>
            <span v-else>{{ item.title }}</span>
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>
    <template v-if="busiAuthList.length">
      <div v-if="activeName === 'recent' || activeName === 'store'" class="asset-toolbar">
        <div class="type-chip-list">
          <button
            v-for="item in state.curTypeList"
            :key="item"
            type="button"
            class="type-chip"
            :class="{ active: activeCommand === item }"
            @click="selectType(item)"
          >
            <span
              v-if="item !== 'all_types'"
              class="type-dot"
              :style="{ backgroundColor: getTypeColor(item) }"
            />
            <span>{{ t(`auth.${item}`) }}</span>
          </button>
        </div>
        <div class="search">
          <el-input
            v-model="panelKeyword"
            clearable
            @change="triggerFilterPanel"
            :placeholder="t('work_branch.search_keyword')"
          >
            <template #prefix>
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2" />
                <path
                  d="M16.5 16.5 21 21"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
            </template>
          </el-input>
        </div>
      </div>
      <div v-if="activeName === 'recent' || activeName === 'store'" class="panel-table">
        <GridTable
          :show-pagination="false"
          :table-data="state.tableData"
          @sort-change="sortChange"
          @cell-click="handleCellClick"
          :empty-desc="emptyDesc"
          :empty-img="imgType"
          class="workbranch-grid"
        >
          <el-table-column key="name" width="280" prop="name" :label="t('common.name')">
            <template v-slot:default="scope">
              <div class="name-content asset-name" :class="{ 'jump-active': jumpActiveCheck(scope.row) }">
                <span
                  class="asset-icon"
                  :class="{ disabled: checkDisabled(scope.row) }"
                  :style="{ backgroundColor: getTypeColor(scope.row.type) }"
                >
                  <img
                    :src="
                      scope.row.extFlag
                        ? iconMap[
                            scope.row.type +
                              'Mobile' +
                              (checkDisabled(scope.row) ? 'Disabled' : '')
                          ]
                        : iconMap[scope.row.type]
                    "
                    :alt="typeMap[scope.row.type]"
                  />
                </span>
                <span class="asset-title-wrap">
                  <el-tooltip placement="top">
                    <template #content>{{ scope.row.name }}</template>
                    <span class="asset-title ellipsis" :class="{ 'color-disabled': checkDisabled(scope.row) }">
                      {{ scope.row.name }}
                    </span>
                  </el-tooltip>
                  <span class="asset-code">{{ getAssetCode(scope.row) }}</span>
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column
            v-for="item in state.tableColumn"
            :key="item.label"
            :prop="item.field"
            show-overflow-tooltip
            :sortable="item.type === 'time'"
            :label="item.label"
          >
            <template #default="scope">
              <span :class="{ 'jump-active': jumpActiveCheck(scope.row) }">
                <span v-if="item.type && item.type === 'time'" class="time-cell">
                  <span class="time-relative">{{ formatterRelativeTime(scope.row[item.field]) }}</span>
                  <span class="time-full">{{ formatterTime(null, null, scope.row[item.field]) }}</span>
                </span>
                <span v-else-if="item.field && item.field === 'type'" class="asset-tag" :style="{ '--tag-color': getTypeColor(scope.row[item.field]) }">
                  <span class="tag-dot" />
                  {{ typeMap[scope.row[item.field]] }}
                </span>
                <span v-else-if="desktop && item.field && item.field.endsWith('tor')" class="owner-cell">
                  <span class="owner-avatar">{{ getUserInitial(userStore.getName) }}</span>
                  <span>{{ userStore.getName }}</span>
                </span>
                <span v-else-if="item.field && item.field.endsWith('tor')" class="owner-cell">
                  <span class="owner-avatar">{{ getUserInitial(scope.row[item.field]) }}</span>
                  <span>{{ scope.row[item.field] }}</span>
                </span>
                <span v-else>{{ scope.row[item.field] }}</span>
              </span>
            </template>
          </el-table-column>

          <el-table-column width="100" fixed="right" key="_operation" :label="$t('common.operate')">
            <template #default="scope">
              <div
                class="table-actions"
                :class="{ 'opt-disabled': checkDisabled(scope.row) }"
              >
                <template v-if="['dashboard', 'dataV', 'panel', 'screen'].includes(scope.row.type)">
                  <el-tooltip
                    effect="dark"
                    :content="t('work_branch.new_page_preview')"
                    :disabled="checkDisabled(scope.row)"
                    placement="top"
                  >
                    <button
                      type="button"
                      class="opbtn"
                      @click.stop="preview(scope.row, checkDisabled(scope.row))"
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path
                          d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7-10-7-10-7z"
                          stroke="currentColor"
                          stroke-width="1.7"
                        />
                        <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.7" />
                      </svg>
                    </button>
                  </el-tooltip>
                  <el-tooltip
                    v-if="
                      activeName === 'store' ||
                      (activeName === 'recent' && ['screen', 'panel'].includes(scope.row.type))
                    "
                    effect="dark"
                    :disabled="checkDisabled(scope.row)"
                    :content="
                      activeName === 'store'
                        ? t('work_branch.cancel_favorites')
                        : t('work_branch.my_collection')
                    "
                    placement="top"
                  >
                    <button
                      type="button"
                      class="opbtn"
                      :class="{ favorited: activeName === 'store' || scope.row.favorite }"
                      @click.stop="
                        activeName === 'store'
                          ? executeCancelStore(scope.row)
                          : executeStore(scope.row)
                      "
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true">
                        <path
                          d="M12 3l2.7 5.6 6.3.9-4.6 4.4 1.1 6.1L12 17l-5.5 3 1.1-6.1L3 9.5l6.3-.9z"
                          fill="none"
                          stroke="currentColor"
                          stroke-width="1.6"
                          stroke-linejoin="round"
                        />
                      </svg>
                    </button>
                  </el-tooltip>
                </template>

                <template v-if="['dataset'].includes(scope.row.type)">
                  <el-tooltip
                    effect="dark"
                    :content="t('work_branch.open_dataset')"
                    placement="top"
                  >
                    <button
                      type="button"
                      class="opbtn"
                      @click.stop="
                        openDataset(activeName === 'recent' ? scope.row.id : scope.row.resourceId)
                      "
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path
                          d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7-10-7-10-7z"
                          stroke="currentColor"
                          stroke-width="1.7"
                        />
                        <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.7" />
                      </svg>
                    </button>
                  </el-tooltip>
                </template>
              </div>
            </template>
          </el-table-column>
        </GridTable>
        <div class="table-footer">
          <span>共 {{ state.tableData.length }} 项</span>
        </div>
      </div>
    </template>
  </div>
  <el-empty
    class="dashboard-type border-radius-12"
    v-else
    :description="t('work_branch.administrator_for_authorization')"
  />
</template>

<style lang="less" scoped>
.dashboard-type {
  height: 100%;
  min-height: 0;
  padding: 18px 24px 0;
  margin-top: 0;
  overflow: hidden;
  font-family: var(--crest-font-sans);
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);

  :deep(.ed-loading-mask) {
    border-radius: inherit;
  }

  &.expand {
    height: calc(100% - 89px);
  }

  .dashboard-type-tabs {
    position: relative;
    margin-bottom: 0;

    &::after {
      position: absolute;
      right: 0;
      bottom: 0;
      left: 0;
      height: 1px;
      content: '';
      background: #f1f5f9;
    }

    :deep(.ed-tabs__header) {
      margin: 0;
    }

    :deep(.ed-tabs__nav-wrap::after) {
      display: none;
    }

    :deep(.ed-tabs__active-bar) {
      height: 2px;
      background: #3b82f6;
      border-radius: 2px 2px 0 0;
    }

    :deep(.ed-tabs__item) {
      height: 34px;
      padding: 0 26px 14px 0;
      font-family: var(--crest-font-sans);
      font-size: 14.5px;
      font-weight: 500;
      color: #64748b;
    }

    :deep(.ed-tabs__item.is-active) {
      font-weight: 600;
      color: #3b82f6;
    }
  }

  .asset-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 14px;
    padding: 14px 0;
  }

  .type-chip-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    min-width: 0;
  }

  .type-chip {
    display: inline-flex;
    align-items: center;
    gap: 7px;
    height: 30px;
    padding: 0 11px;
    color: #334155;
    font-family: var(--crest-font-sans);
    font-size: 12.5px;
    font-weight: 500;
    cursor: pointer;
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 7px;
    transition:
      color 0.14s ease,
      background 0.14s ease,
      border-color 0.14s ease;

    &:hover {
      border-color: #cbd5e1;
    }

    &.active {
      color: #ffffff;
      background: #3b82f6;
      border-color: #3b82f6;
    }
  }

  .type-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
  }

  .search {
    flex: none;
    text-align: right;

    .ed-input {
      width: 240px;
    }

    :deep(.ed-input__wrapper) {
      height: 34px;
      padding: 0 12px;
      border-radius: 8px;
      box-shadow: 0 0 0 1px #e2e8f0 inset;
      transition:
        box-shadow 0.14s ease,
        background 0.14s ease;
    }

    :deep(.ed-input__wrapper.is-focus) {
      box-shadow:
        0 0 0 1px #3b82f6 inset,
        0 0 0 3px rgba(59, 130, 246, 0.1);
    }

    :deep(.ed-input__prefix) {
      color: #64748b;
    }
  }

  .panel-table {
    display: flex;
    flex-direction: column;
    height: calc(100% - 90px);
    min-height: 0;

    :deep(.ed-table) {
      --ed-table-header-bg-color: #ffffff;
      --ed-table-tr-bg-color: #ffffff;
      color: #334155;
      font-family: var(--crest-font-sans);
      font-size: 13.5px;
    }

    :deep(.ed-table__inner-wrapper::before) {
      display: none;
    }

    :deep(.ed-table th.ed-table__cell) {
      padding: 10px 0;
      color: #94a3b8;
      font-family: var(--crest-font-mono);
      font-size: 11.5px;
      font-weight: 500;
      letter-spacing: 0;
      background: #ffffff;
      border-bottom: 1px solid #f1f5f9;
    }

    :deep(.ed-table td.ed-table__cell) {
      padding: 12px 0;
      border-bottom: 1px solid #f1f5f9;
    }

    :deep(.ed-table__row) {
      cursor: pointer;
      transition: background 0.12s ease;
    }

    :deep(.ed-table__row:hover > td.ed-table__cell) {
      background: #fafbfc;
    }

    .workbranch-grid {
      flex: 1;
      min-height: 0;
    }

    .name-content {
      display: flex;
      align-items: center;
    }
  }
}

.asset-name {
  min-width: 0;
}

.asset-icon {
  display: inline-flex;
  flex: none;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  margin-right: 12px;
  border-radius: 8px;

  &.disabled {
    background: #cbd5e1 !important;
  }

  img {
    width: 18px;
    height: 18px;
    object-fit: contain;
  }
}

.asset-title-wrap {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.asset-title {
  max-width: 220px;
  color: #0f172a;
  font-size: 13.5px;
  font-weight: 600;
  line-height: 18px;
}

.asset-code {
  margin-top: 2px;
  color: #94a3b8;
  font-family: var(--crest-font-mono);
  font-size: 11px;
  line-height: 15px;
}

.asset-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 9px;
  color: var(--tag-color);
  font-size: 11.5px;
  font-weight: 600;
  background: color-mix(in srgb, var(--tag-color) 12%, white);
  border-radius: 5px;
}

.tag-dot {
  width: 5px;
  height: 5px;
  background: var(--tag-color);
  border-radius: 50%;
}

.owner-cell {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #334155;
  font-size: 13px;
}

.owner-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  color: #1d4ed8;
  font-size: 10.5px;
  font-weight: 600;
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  border-radius: 50%;
}

.time-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.time-relative {
  color: #0f172a;
  font-size: 13px;
  font-weight: 500;
}

.time-full {
  margin-top: 1px;
  color: #94a3b8;
  font-family: var(--crest-font-mono);
  font-size: 11px;
}

.table-actions {
  display: flex;
  justify-content: flex-end;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.14s ease;
}

:deep(.ed-table__row:hover) .table-actions {
  opacity: 1;
}

.opbtn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  color: #64748b;
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 6px;
  transition:
    color 0.12s ease,
    background 0.12s ease;

  &:hover {
    color: #3b82f6;
    background: #f1f5f9;
  }

  &.favorited {
    color: #f5a623;
  }
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  padding: 12px 0 14px;
  color: #64748b;
  font-family: var(--crest-font-sans);
  font-size: 12.5px;
  border-top: 1px solid #f1f5f9;
}

.workbranch-grid :deep(.ed-empty) {
  padding: 80px 0 !important;

  .ed-empty__description {
    margin-top: 0;
    line-height: 20px !important;
  }
}

.jump-active {
  cursor: pointer;
}

.color-disabled {
  color: #bbbfc4;
}

.opt-disabled {
  cursor: not-allowed;
  opacity: 0.2;
}
</style>
<style lang="less">
.menu-panel-select_popper {
  width: 140px;
  background: #fff;
}
</style>
