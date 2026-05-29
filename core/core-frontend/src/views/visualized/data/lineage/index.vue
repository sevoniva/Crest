<script lang="ts" setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Close, FullScreen, RefreshLeft, Search } from '@element-plus/icons-vue'
import RelationGraphView from '@/components/relation-chart/GraphView.vue'
import {
  getDatasourceRelationship,
  getDatasetRelationship,
  getPanelRelationship,
  getRelationshipOverview,
  listRelationResources
} from '@/api/relation'

interface RelationNode {
  id: string
  resourceId: string
  name: string
  type: string
  subType?: string
  updateTime?: number
}

interface RelationEdge {
  source: string
  target: string
  label?: string
  type?: string
}

interface RelationGraph {
  nodes: RelationNode[]
  edges: RelationEdge[]
  summary?: Record<string, number>
}

interface RelationResource {
  id: string
  name: string
  type: string
  subType?: string
  updateTime?: number
}

const graph = ref<RelationGraph>({
  nodes: [],
  edges: []
})
const resources = ref<RelationResource[]>([])
const queryType = ref('datasource')
const selectedResource = ref<string>()
const selectedTable = ref<string>()
const selectedField = ref<string>()
const keyword = ref('')
const showFieldNodes = ref(true)
const layoutMode = ref<'knowledge' | 'layered'>('knowledge')
const loading = ref(false)
const resourceLoading = ref(false)
const graphPanelRef = ref<HTMLElement>()
const isGraphFullscreen = ref(false)

const typeOptions = [
  { label: '全局', value: 'overview' },
  { label: '数据源', value: 'datasource' },
  { label: '数据集', value: 'dataset' },
  { label: '仪表板/大屏', value: 'dv' }
]

const layoutOptions = [
  { label: '图谱', value: 'knowledge' },
  { label: '分层', value: 'layered' }
]

const typeLabelMap: Record<string, string> = {
  datasource: '数据源',
  table: '物理表',
  table_field: '物理字段',
  dataset_field: '数据集字段',
  dataset: '数据集',
  chart_field: '图表字段',
  chart: '图表',
  dv: '仪表板/大屏'
}

const typeLevelMap: Record<string, number> = {
  datasource: 0,
  table: 1,
  table_field: 2,
  dataset_field: 3,
  dataset: 4,
  chart_field: 5,
  chart: 6,
  dv: 7
}

const fieldNodeTypes = new Set(['table_field', 'dataset_field', 'chart_field'])

const getTypeClass = (type: string) => `lineage-type-${String(type || '').replace(/_/g, '-')}`

const getNodeLookup = (sourceGraph: RelationGraph) => {
  return (sourceGraph.nodes || []).reduce<Record<string, RelationNode>>((acc, node) => {
    acc[node.id] = node
    return acc
  }, {})
}

const getEdgeLookup = (sourceGraph: RelationGraph) => {
  const bySource: Record<string, RelationEdge[]> = {}
  const byTarget: Record<string, RelationEdge[]> = {}
  ;(sourceGraph.edges || []).forEach(edge => {
    bySource[edge.source] = bySource[edge.source] || []
    bySource[edge.source].push(edge)
    byTarget[edge.target] = byTarget[edge.target] || []
    byTarget[edge.target].push(edge)
  })
  return { bySource, byTarget }
}

const edgeTarget = (edge: RelationEdge, current: string) => {
  return edge.source === current ? edge.target : edge.source
}

const collectConnectedNodeIds = (sourceGraph: RelationGraph, seedIds: string[]) => {
  const nodeLookup = getNodeLookup(sourceGraph)
  const { bySource, byTarget } = getEdgeLookup(sourceGraph)
  const visible = new Set<string>()
  const queue = seedIds.filter(id => !!nodeLookup[id])

  while (queue.length) {
    const current = queue.shift()
    if (!current || visible.has(current)) continue
    visible.add(current)
    ;[...(bySource[current] || []), ...(byTarget[current] || [])].forEach(edge => {
      const next = edgeTarget(edge, current)
      if (nodeLookup[next] && !visible.has(next)) {
        queue.push(next)
      }
    })
  }

  return visible
}

const collectFieldLineageNodeIds = (sourceGraph: RelationGraph, seedIds: string[]) => {
  const nodeLookup = getNodeLookup(sourceGraph)
  const { bySource, byTarget } = getEdgeLookup(sourceGraph)
  const visible = new Set<string>()
  const queue = seedIds.filter(id => !!nodeLookup[id])

  const pushEdge = (edge: RelationEdge) => {
    const next = edgeTarget(edge, edge.source)
    if (nodeLookup[next] && !visible.has(next)) {
      queue.push(next)
    }
  }

  const pushIncoming = (current: string, edgeTypes: string[]) => {
    ;(byTarget[current] || [])
      .filter(edge => !!edge.type && edgeTypes.includes(edge.type))
      .forEach(edge => {
        if (nodeLookup[edge.source] && !visible.has(edge.source)) {
          queue.push(edge.source)
        }
      })
  }

  const pushOutgoing = (current: string, edgeTypes: string[]) => {
    ;(bySource[current] || [])
      .filter(edge => !!edge.type && edgeTypes.includes(edge.type))
      .forEach(pushEdge)
  }

  while (queue.length) {
    const current = queue.shift()
    if (!current || visible.has(current)) continue
    visible.add(current)
    const node = nodeLookup[current]

    if (node.type === 'table_field') {
      pushIncoming(current, ['table_table_field', 'table_field_join'])
      pushOutgoing(current, ['table_field_dataset_field', 'table_field_join'])
      continue
    }
    if (node.type === 'dataset_field') {
      pushIncoming(current, ['table_field_dataset_field', 'dataset_field_calc_field'])
      pushOutgoing(current, [
        'dataset_field_dataset',
        'dataset_field_chart_field',
        'dataset_field_calc_field'
      ])
      continue
    }
    if (node.type === 'chart_field') {
      pushIncoming(current, ['dataset_field_chart_field'])
      pushOutgoing(current, ['chart_field_chart'])
      continue
    }
    if (node.type === 'table') {
      pushIncoming(current, ['datasource_table'])
      continue
    }
    if (node.type === 'chart') {
      pushOutgoing(current, ['chart_dv'])
    }
  }

  return visible
}

const buildVisibleGraph = (sourceGraph: RelationGraph, visible: Set<string>) => ({
  ...sourceGraph,
  nodes: sourceGraph.nodes.filter(node => visible.has(node.id)),
  edges: sourceGraph.edges.filter(edge => visible.has(edge.source) && visible.has(edge.target))
})

const summarizeGraph = (sourceGraph: RelationGraph) => {
  const nodes = sourceGraph.nodes || []
  return {
    datasourceCount: nodes.filter(node => node.type === 'datasource').length,
    tableCount: nodes.filter(node => node.type === 'table').length,
    tableFieldCount: nodes.filter(node => node.type === 'table_field').length,
    datasetFieldCount: nodes.filter(node => node.type === 'dataset_field').length,
    datasetCount: nodes.filter(node => node.type === 'dataset').length,
    chartFieldCount: nodes.filter(node => node.type === 'chart_field').length,
    chartCount: nodes.filter(node => node.type === 'chart').length,
    dvCount: nodes.filter(node => node.type === 'dv').length,
    edgeCount: sourceGraph.edges?.length || 0,
    totalCount: nodes.length
  }
}

const fieldsByTable = computed(() => {
  const sourceGraph = graph.value || { nodes: [], edges: [] }
  const nodeLookup = getNodeLookup(sourceGraph)
  const result: Record<string, RelationNode[]> = {}

  ;(sourceGraph.edges || []).forEach(edge => {
    const table = nodeLookup[edge.source]
    const field = nodeLookup[edge.target]
    if (table?.type === 'table' && field?.type === 'table_field') {
      result[table.id] = result[table.id] || []
      if (!result[table.id].some(item => item.id === field.id)) {
        result[table.id].push(field)
      }
    }
  })

  Object.keys(result).forEach(tableId => {
    result[tableId] = result[tableId].sort((left, right) =>
      (left.name || '').localeCompare(right.name || '')
    )
  })

  return result
})

const tableOptions = computed(() => {
  const sourceGraph = graph.value || { nodes: [], edges: [] }
  return sourceGraph.nodes
    .filter(node => node.type === 'table')
    .map(node => ({
      ...node,
      fieldCount: fieldsByTable.value[node.id]?.length || 0
    }))
    .sort((left, right) => (left.name || '').localeCompare(right.name || ''))
})

const fieldOptions = computed(() => {
  if (!selectedTable.value) return []
  return fieldsByTable.value[selectedTable.value] || []
})

const filteredGraph = computed<RelationGraph>(() => {
  const sourceGraph = graph.value || { nodes: [], edges: [] }
  if (selectedField.value) {
    const visible = collectFieldLineageNodeIds(sourceGraph, [selectedField.value])
    return buildVisibleGraph(sourceGraph, visible)
  }

  const search = keyword.value.trim().toLowerCase()
  if (!search) {
    return sourceGraph
  }
  const matchedNodes = sourceGraph.nodes.filter(node => {
    const typeLabel = typeLabelMap[node.type] || node.type
    return [node.name, node.subType, typeLabel]
      .filter(Boolean)
      .some(item => String(item).toLowerCase().includes(search))
  })
  const fieldMatched = matchedNodes
    .filter(node => fieldNodeTypes.has(node.type))
    .map(node => node.id)
  const visible =
    fieldMatched.length > 0
      ? collectFieldLineageNodeIds(sourceGraph, fieldMatched)
      : collectConnectedNodeIds(
          sourceGraph,
          matchedNodes.map(node => node.id)
        )
  return buildVisibleGraph(sourceGraph, visible)
})

const displayGraph = computed<RelationGraph>(() => {
  if (showFieldNodes.value) {
    return filteredGraph.value
  }
  const nodes = filteredGraph.value.nodes.filter(node => !fieldNodeTypes.has(node.type))
  const visible = new Set(nodes.map(node => node.id))
  return {
    ...filteredGraph.value,
    nodes,
    edges: filteredGraph.value.edges.filter(
      edge => visible.has(edge.source) && visible.has(edge.target)
    )
  }
})

const summary = computed(() => {
  const hasFilter = !!selectedField.value || !!keyword.value.trim()
  const data = hasFilter ? summarizeGraph(filteredGraph.value) : graph.value?.summary || {}
  return [
    { label: '数据源', value: data.datasourceCount || 0, className: 'is-datasource' },
    { label: '物理表', value: data.tableCount || 0, className: 'is-table' },
    { label: '物理字段', value: data.tableFieldCount || 0, className: 'is-table-field' },
    { label: '数据集字段', value: data.datasetFieldCount || 0, className: 'is-dataset-field' },
    { label: '数据集', value: data.datasetCount || 0, className: 'is-dataset' },
    { label: '图表字段', value: data.chartFieldCount || 0, className: 'is-chart-field' },
    {
      label: '图表/看板',
      value: (data.chartCount || 0) + (data.dvCount || 0),
      className: 'is-chart'
    },
    { label: '依赖关系', value: data.edgeCount || 0, className: 'is-edge' }
  ]
})

const nodeMap = computed(() => {
  return (filteredGraph.value?.nodes || []).reduce<Record<string, RelationNode>>((acc, node) => {
    acc[node.id] = node
    return acc
  }, {})
})

const edgeRows = computed(() => {
  return (filteredGraph.value?.edges || []).map(edge => ({
    ...edge,
    sourceName: nodeMap.value[edge.source]?.name || edge.source,
    sourceType: typeLabelMap[nodeMap.value[edge.source]?.type] || '',
    targetName: nodeMap.value[edge.target]?.name || edge.target,
    targetType: typeLabelMap[nodeMap.value[edge.target]?.type] || ''
  }))
})

const resourceRows = computed(() => {
  return (filteredGraph.value?.nodes || []).slice().sort((left, right) => {
    const leftLevel = typeLevelMap[left.type] ?? 99
    const rightLevel = typeLevelMap[right.type] ?? 99
    return leftLevel === rightLevel
      ? (left.name || '').localeCompare(right.name || '')
      : leftLevel - rightLevel
  })
})

const resetLineagePicker = () => {
  selectedTable.value = undefined
  selectedField.value = undefined
  keyword.value = ''
}

const tablePickerPlaceholder = computed(() => {
  return tableOptions.value.length ? `选择表（${tableOptions.value.length}）` : '暂无可选表'
})

const fieldPickerPlaceholder = computed(() => {
  if (!selectedTable.value) {
    return '先选择表'
  }
  return fieldOptions.value.length ? `选择字段（${fieldOptions.value.length}）` : '该表暂无字段'
})

const filterStats = computed(() => {
  if (queryType.value === 'overview') {
    return ''
  }
  const fieldCount = Object.values(fieldsByTable.value).reduce(
    (total, fields) => total + fields.length,
    0
  )
  return `${tableOptions.value.length} 张表 / ${fieldCount} 个物理字段`
})

const graphHeight = computed(() => (isGraphFullscreen.value ? '100vh' : '100%'))

const getDefaultResource = (items: RelationResource[]) => {
  if (!items.length) return undefined
  if (queryType.value === 'datasource') {
    const builtin = items.find(item =>
      ['demo', 'crest', '内置'].some(keyword => item.name?.toLowerCase().includes(keyword))
    )
    if (builtin) {
      return builtin.id
    }
  }
  return items[0]?.id
}

const handleTableChange = () => {
  selectedField.value = undefined
  if (selectedTable.value) {
    keyword.value = ''
  }
}

const handleFieldChange = () => {
  if (selectedField.value) {
    keyword.value = ''
  }
}

const handleKeywordInput = () => {
  if (keyword.value.trim()) {
    selectedField.value = undefined
  }
}

watch(
  () => graph.value,
  () => {
    if (selectedTable.value && !tableOptions.value.some(item => item.id === selectedTable.value)) {
      selectedTable.value = undefined
    }
    if (selectedField.value && !fieldOptions.value.some(item => item.id === selectedField.value)) {
      selectedField.value = undefined
    }
  },
  { deep: true }
)

const loadResources = async () => {
  if (queryType.value === 'overview') {
    resources.value = []
    selectedResource.value = undefined
    return
  }
  resourceLoading.value = true
  try {
    resources.value = await listRelationResources(queryType.value)
    if (!resources.value.some(item => item.id === selectedResource.value)) {
      selectedResource.value = getDefaultResource(resources.value)
    }
  } finally {
    resourceLoading.value = false
  }
}

const loadGraph = async () => {
  loading.value = true
  try {
    if (queryType.value === 'overview') {
      graph.value = await getRelationshipOverview()
      return
    }
    if (!selectedResource.value) {
      graph.value = { nodes: [], edges: [] }
      return
    }
    if (queryType.value === 'datasource') {
      graph.value = await getDatasourceRelationship(selectedResource.value)
    } else if (queryType.value === 'dataset') {
      graph.value = await getDatasetRelationship(selectedResource.value)
    } else {
      graph.value = await getPanelRelationship(selectedResource.value)
    }
  } finally {
    loading.value = false
  }
}

const handleTypeChange = async () => {
  selectedResource.value = undefined
  resetLineagePicker()
  await loadResources()
  await loadGraph()
}

const handleSearch = async () => {
  if (!keyword.value && !selectedField.value) {
    await loadGraph()
  }
}

const handleReset = async () => {
  resetLineagePicker()
  await loadGraph()
}

const handleResourceChange = async () => {
  resetLineagePicker()
  await loadGraph()
}

const syncFullscreenState = () => {
  isGraphFullscreen.value = document.fullscreenElement === graphPanelRef.value
}

const toggleGraphFullscreen = async () => {
  if (isGraphFullscreen.value) {
    await document.exitFullscreen?.()
  } else {
    await graphPanelRef.value?.requestFullscreen?.()
  }
  await nextTick()
  syncFullscreenState()
}

onMounted(async () => {
  document.addEventListener('fullscreenchange', syncFullscreenState)
  await loadResources()
  await loadGraph()
})

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', syncFullscreenState)
})
</script>

<template>
  <div class="lineage-page">
    <div class="lineage-toolbar">
      <div class="lineage-title">
        <h1>数据血缘</h1>
        <span>{{ filteredGraph.nodes.length || 0 }} 个节点</span>
      </div>
      <div class="lineage-actions">
        <el-select v-model="queryType" class="type-select" @change="handleTypeChange">
          <el-option
            v-for="item in typeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select
          v-if="queryType !== 'overview'"
          v-model="selectedResource"
          class="resource-select"
          filterable
          :loading="resourceLoading"
          placeholder="选择资源"
          @change="handleResourceChange"
        >
          <el-option v-for="item in resources" :key="item.id" :label="item.name" :value="item.id">
            <span class="resource-option">
              <span>{{ item.name }}</span>
              <small>{{ item.subType || typeLabelMap[item.type] }}</small>
            </span>
          </el-option>
        </el-select>
        <el-select
          v-if="queryType !== 'overview'"
          v-model="selectedTable"
          class="table-select"
          clearable
          filterable
          popper-class="lineage-select-dropdown"
          :disabled="!tableOptions.length"
          :placeholder="tablePickerPlaceholder"
          @change="handleTableChange"
        >
          <el-option
            v-for="item in tableOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          >
            <span class="resource-option">
              <span>{{ item.name }}</span>
              <small>{{ item.fieldCount }} 字段</small>
            </span>
          </el-option>
        </el-select>
        <el-select
          v-if="queryType !== 'overview'"
          v-model="selectedField"
          class="field-select"
          clearable
          filterable
          popper-class="lineage-select-dropdown"
          :disabled="!selectedTable || !fieldOptions.length"
          :placeholder="fieldPickerPlaceholder"
          @change="handleFieldChange"
        >
          <el-option
            v-for="item in fieldOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          >
            <span class="resource-option">
              <span>{{ item.name }}</span>
              <small>{{ item.subType || typeLabelMap[item.type] }}</small>
            </span>
          </el-option>
        </el-select>
        <el-input
          v-if="queryType !== 'overview'"
          v-model="keyword"
          class="keyword-input"
          clearable
          :prefix-icon="Search"
          placeholder="搜索字段/资源"
          @input="handleKeywordInput"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
        <el-button
          :icon="RefreshLeft"
          aria-label="清空筛选"
          title="清空筛选"
          @click="handleReset"
        ></el-button>
        <el-radio-group v-model="layoutMode" class="layout-mode-group" size="small">
          <el-radio-button v-for="item in layoutOptions" :key="item.value" :label="item.value">
            {{ item.label }}
          </el-radio-button>
        </el-radio-group>
        <el-switch
          v-model="showFieldNodes"
          class="field-node-switch"
          inline-prompt
          active-text="字段"
          inactive-text="资源"
        />
        <span v-if="filterStats" class="filter-stats">{{ filterStats }}</span>
      </div>
    </div>

    <div class="lineage-summary">
      <div v-for="item in summary" :key="item.label" class="summary-item" :class="item.className">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>

    <div class="lineage-content">
      <section
        ref="graphPanelRef"
        class="graph-panel"
        :class="{ 'is-fullscreen': isGraphFullscreen }"
      >
        <div class="graph-panel-tools">
          <el-button
            circle
            :icon="isGraphFullscreen ? Close : FullScreen"
            :aria-label="isGraphFullscreen ? '退出全屏' : '全屏查看'"
            :title="isGraphFullscreen ? '退出全屏' : '全屏查看'"
            @click="toggleGraphFullscreen"
          />
        </div>
        <RelationGraphView
          :graph="displayGraph"
          :loading="loading"
          :layout-mode="layoutMode"
          :height="graphHeight"
        />
      </section>

      <aside class="detail-panel">
        <div class="detail-block">
          <div class="block-title">
            <span>资源</span>
            <small>{{ resourceRows.length }} 项</small>
          </div>
          <el-table class="detail-table" :data="resourceRows" height="240" size="small">
            <el-table-column label="类型" width="94">
              <template #default="{ row }">
                <span class="type-dot-tag" :class="getTypeClass(row.type)">
                  <i></i>
                  {{ typeLabelMap[row.type] || row.type }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="名称" show-overflow-tooltip />
          </el-table>
        </div>

        <div class="detail-block">
          <div class="block-title">
            <span>依赖</span>
            <small>{{ edgeRows.length }} 条</small>
          </div>
          <el-table class="detail-table" :data="edgeRows" height="260" size="small">
            <el-table-column label="上游" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="edge-cell">
                  <span>{{ row.sourceName }}</span>
                  <small>{{ row.sourceType }}</small>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="下游" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="edge-cell">
                  <span>{{ row.targetName }}</span>
                  <small>{{ row.targetType }}</small>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </aside>
    </div>
  </div>
</template>

<style lang="less" scoped>
.lineage-page {
  height: 100%;
  padding: 22px 26px;
  overflow: hidden;
  background: #f8fafc;
  color: #0f172a;
  display: flex;
  flex-direction: column;
  gap: 14px;
  font-family: var(--crest-font-sans);
}

.lineage-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 68px;
  padding: 14px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.lineage-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 0 0 auto;
  min-width: 150px;

  h1 {
    margin: 0;
    font-size: 18px;
    line-height: 28px;
    font-weight: 700;
    letter-spacing: 0;
    white-space: nowrap;
  }

  span {
    flex: none;
    padding: 2px 8px;
    border-radius: 999px;
    background: #f1f5f9;
    color: #64748b;
    font-family: var(--crest-font-mono);
    font-size: 12px;
    line-height: 18px;
    white-space: nowrap;
  }
}

.lineage-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  min-width: 0;
  flex-wrap: wrap;
}

.type-select {
  width: 132px;
}

.resource-select {
  width: 230px;
}

.table-select {
  width: 190px;
}

.field-select {
  width: 220px;
}

.keyword-input {
  width: 170px;
}

.field-node-switch {
  margin-left: 2px;
}

.layout-mode-group {
  flex-shrink: 0;
}

.filter-stats {
  color: #64748b;
  font-family: var(--crest-font-mono);
  font-size: 12px;
  line-height: 22px;
  white-space: nowrap;
}

.resource-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;

  small {
    color: #94a3b8;
    font-family: var(--crest-font-mono);
  }
}

.lineage-summary {
  display: grid;
  grid-template-columns: repeat(8, minmax(96px, 1fr));
  gap: 10px;
}

.summary-item {
  position: relative;
  height: 64px;
  padding: 10px 14px 10px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);

  &::before {
    position: absolute;
    left: 0;
    top: 14px;
    bottom: 14px;
    width: 4px;
    border-radius: 0 3px 3px 0;
    background: var(--summary-color, #3b82f6);
    content: '';
  }

  span {
    color: #64748b;
    font-size: 13px;
  }

  strong {
    font-size: 24px;
    line-height: 30px;
    font-weight: 700;
    font-family: var(--crest-font-mono);
    color: var(--summary-color, #3b82f6);
  }

  &.is-datasource {
    --summary-color: #f5a623;
  }

  &.is-table {
    --summary-color: #3b82f6;
  }

  &.is-table-field {
    --summary-color: #60a5fa;
  }

  &.is-dataset-field {
    --summary-color: #8b5cf6;
  }

  &.is-dataset {
    --summary-color: #6e62e8;
  }

  &.is-chart-field {
    --summary-color: #1fb6a6;
  }

  &.is-chart {
    --summary-color: #10b981;
  }

  &.is-edge {
    --summary-color: #334155;
  }
}

.lineage-content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  gap: 16px;
  flex: 1;
  min-height: 0;
}

.graph-panel,
.detail-panel {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.graph-panel {
  position: relative;
}

.graph-panel.is-fullscreen {
  width: 100vw;
  height: 100vh;
  border: 0;
  border-radius: 0;
}

.graph-panel-tools {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 4;
  display: flex;
  align-items: center;
  gap: 8px;

  :deep(.ed-button) {
    border-color: #e2e8f0;
    background: #ffffff;
    color: #334155;
    box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);

    &:hover {
      border-color: #bfdbfe;
      background: #eff6ff;
      color: #3b82f6;
    }
  }
}

.graph-panel.is-fullscreen .graph-panel-tools {
  top: 16px;
  left: 16px;
}

.detail-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 14px;
  overflow: auto;
}

.detail-block {
  display: flex;
  flex-direction: column;
  padding: 12px;
  min-height: 0;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.block-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;

  small {
    color: #64748b;
    font-family: var(--crest-font-mono);
    font-size: 11.5px;
    font-weight: 500;
  }
}

.edge-cell {
  display: flex;
  flex-direction: column;
  line-height: 18px;

  small {
    color: #94a3b8;
    font-family: var(--crest-font-mono);
  }
}

.type-dot-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 84px;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--type-bg, #eff6ff);
  color: var(--type-color, #3b82f6);
  font-family: var(--crest-font-mono);
  font-size: 12px;
  line-height: 18px;
  white-space: nowrap;

  i {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--type-color, #3b82f6);
  }
}

.lineage-type-datasource {
  --type-color: #f5a623;
  --type-bg: #fff7ed;
}

.lineage-type-table,
.lineage-type-table-field {
  --type-color: #3b82f6;
  --type-bg: #eff6ff;
}

.lineage-type-dataset,
.lineage-type-dataset-field {
  --type-color: #6e62e8;
  --type-bg: #f3f0ff;
}

.lineage-type-chart,
.lineage-type-chart-field,
.lineage-type-dv {
  --type-color: #10b981;
  --type-bg: #ecfdf5;
}

:deep(.ed-input__wrapper) {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  box-shadow: none;
  background: #ffffff;

  &:hover {
    border-color: #bfdbfe;
  }

  &.is-focus {
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.14);
  }
}

:deep(.ed-button) {
  height: 32px;
  border-radius: 10px;
  font-family: var(--crest-font-sans);
  font-weight: 600;
}

:deep(.ed-button--primary) {
  background: #3b82f6;
  border-color: #3b82f6;

  &:hover,
  &:focus {
    background: #2563eb;
    border-color: #2563eb;
  }
}

:deep(.ed-radio-button__inner) {
  border-color: #e2e8f0;
  color: #64748b;
  font-family: var(--crest-font-sans);
  font-weight: 600;
}

:deep(.ed-radio-button__original-radio:checked + .ed-radio-button__inner) {
  background: #0f172a;
  border-color: #0f172a;
  color: #ffffff;
  box-shadow: -1px 0 0 0 #0f172a;
}

:deep(.ed-table) {
  color: #334155;
  font-family: var(--crest-font-sans);

  .ed-table__inner-wrapper::before {
    display: none;
  }

  th.ed-table__cell {
    height: 36px;
    padding: 8px 0;
    background: #ffffff;
    border-bottom: 1px solid #f1f5f9;
    color: #94a3b8;
    font-family: var(--crest-font-mono);
    font-size: 11.5px;
    font-weight: 500;
    letter-spacing: 0;
  }

  td.ed-table__cell {
    height: 42px;
    padding: 8px 0;
    border-bottom: 1px solid #f1f5f9;
  }

  .ed-table__row:hover > td.ed-table__cell {
    background: #fafbfc;
  }
}

.detail-table {
  flex: 1;
  min-height: 0;
  border-radius: 8px;

  :deep(.ed-table__body-wrapper) {
    border-radius: 0 0 8px 8px;
  }

  :deep(.ed-scrollbar__bar.is-vertical) {
    width: 5px;
  }

  :deep(.ed-empty) {
    padding: 38px 0;
  }

  :deep(.ed-empty__description) {
    margin-top: 4px;
    color: #94a3b8;
    font-size: 12px;
  }
}

@media (max-width: 1180px) {
  .lineage-page {
    overflow: auto;
  }

  .lineage-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .lineage-actions {
    justify-content: flex-start;
  }

  .lineage-summary {
    grid-template-columns: repeat(2, minmax(120px, 1fr));
  }

  .lineage-content {
    grid-template-columns: 1fr;
  }

  .detail-panel {
    display: none;
  }
}
</style>

<style lang="less">
.lineage-select-dropdown {
  border: 1px solid #e2e8f0 !important;
  border-radius: 12px !important;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.1) !important;

  .ed-select-dropdown__wrap {
    max-height: 420px;
  }

  .ed-select-dropdown__item {
    height: 34px;
    border-radius: 8px;
    color: #334155;
    font-family: var(--crest-font-sans);
    font-weight: 500;

    &.hover,
    &:hover {
      background: #f8fafc;
    }

    &.selected {
      color: #3b82f6;
      background: #eff6ff;
      font-weight: 700;
    }
  }
}
</style>
