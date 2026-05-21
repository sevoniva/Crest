<script lang="ts" setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

interface RelationNode {
  id: string
  resourceId: string | number
  name: string
  type: string
  subType?: string
  description?: string
  updateTime?: number
  level?: number
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
}

const props = withDefaults(
  defineProps<{
    graph?: RelationGraph | null
    loading?: boolean
    height?: string
  }>(),
  {
    graph: null,
    loading: false,
    height: '100%'
  }
)

const chartRef = ref<HTMLDivElement>()
let chart: any = null
let resizeObserver: ResizeObserver | null = null
let renderFrame: number | null = null
let resizeFrame: number | null = null
let lastWidth = 0
let lastHeight = 0

const typeMeta: Record<string, { label: string; color: string; level: number; symbol: string }> = {
  datasource: { label: '数据源', color: '#2f6bff', level: 0, symbol: 'roundRect' },
  table: { label: '物理表', color: '#00a3a3', level: 1, symbol: 'roundRect' },
  table_field: { label: '物理字段', color: '#008fb3', level: 2, symbol: 'roundRect' },
  dataset_field: { label: '数据集字段', color: '#13a35b', level: 3, symbol: 'roundRect' },
  dataset: { label: '数据集', color: '#4f8f00', level: 4, symbol: 'roundRect' },
  chart_field: { label: '图表字段', color: '#c47a00', level: 5, symbol: 'roundRect' },
  chart: { label: '图表', color: '#d15b18', level: 6, symbol: 'roundRect' },
  dv: { label: '仪表板/大屏', color: '#c43e63', level: 7, symbol: 'roundRect' }
}

const hasData = computed(() => !!props.graph?.nodes?.length)
const isLargeGraph = computed(() => {
  const nodeCount = props.graph?.nodes?.length || 0
  const edgeCount = props.graph?.edges?.length || 0
  return nodeCount > 220 || edgeCount > 420
})

const ensureChart = async () => {
  await nextTick()
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
}

const layoutNodes = () => {
  const nodes = props.graph?.nodes || []
  const width = chartRef.value?.clientWidth || 1000
  const height = chartRef.value?.clientHeight || 560
  const lanes = [0.04, 0.16, 0.29, 0.43, 0.57, 0.7, 0.83, 0.96]
  const grouped = nodes.reduce<Record<number, RelationNode[]>>((acc, node) => {
    const level = node.level ?? typeMeta[node.type]?.level ?? 0
    acc[level] = acc[level] || []
    acc[level].push(node)
    return acc
  }, {})

  return nodes.map(node => {
    const level = node.level ?? typeMeta[node.type]?.level ?? 0
    const group = grouped[level] || []
    const index = group.findIndex(item => item.id === node.id)
    const step = height / (group.length + 1)
    const meta = typeMeta[node.type] || typeMeta.dataset
    return {
      ...node,
      x: width * (lanes[level] ?? 0.5),
      y: step * (index + 1),
      category: meta.label,
      itemStyle: {
        color: meta.color,
        borderColor: '#ffffff',
        borderWidth: 2,
        shadowBlur: isLargeGraph.value ? 0 : 12,
        shadowColor: `${meta.color}40`
      },
      symbol: meta.symbol,
      symbolSize:
        node.type === 'dv'
          ? [118, 42]
          : node.type?.includes('field')
            ? [96, 30]
            : node.type === 'table'
              ? [108, 40]
              : [104, 40],
      label: {
        show: !isLargeGraph.value || !node.type?.includes('field'),
        color: '#ffffff',
        width: node.type === 'dv' ? 96 : node.type?.includes('field') ? 78 : 82,
        overflow: 'truncate',
        fontSize: node.type?.includes('field') ? 11 : 12,
        fontWeight: 500
      }
    }
  })
}

const renderChart = async () => {
  await ensureChart()
  if (!chart) return
  if (!hasData.value) {
    chart.clear()
    return
  }

  chart.setOption({
    animation: !isLargeGraph.value,
    animationDurationUpdate: isLargeGraph.value ? 0 : 450,
    tooltip: {
      trigger: 'item',
      confine: true,
      formatter: params => {
        if (params.dataType === 'edge') {
          return params.data?.label || '资源依赖'
        }
        const data = params.data || {}
        const meta = typeMeta[data.type] || {}
        const subType = data.subType ? `<br/>类型：${data.subType}` : ''
        return `${meta.label || '资源'}<br/>${data.name || ''}${subType}`
      }
    },
    legend: {
      top: 12,
      right: 16,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        color: '#646a73'
      },
      data: Object.values(typeMeta).map(item => item.label)
    },
    series: [
      {
        type: 'graph',
        layout: 'none',
        roam: true,
        draggable: !isLargeGraph.value,
        edgeSymbol: ['none', 'arrow'],
        edgeSymbolSize: [4, 8],
        categories: Object.values(typeMeta).map(item => ({
          name: item.label
        })),
        data: layoutNodes(),
        links: props.graph?.edges || [],
        lineStyle: {
          color: '#8f959e',
          width: isLargeGraph.value ? 0.9 : 1.3,
          curveness: 0.18,
          opacity: isLargeGraph.value ? 0.46 : 0.72
        },
        emphasis: {
          focus: isLargeGraph.value ? 'none' : 'adjacency',
          lineStyle: {
            width: 2.4,
            opacity: 1
          }
        }
      }
    ]
  }, true)
}

const scheduleRender = () => {
  if (renderFrame !== null) {
    cancelAnimationFrame(renderFrame)
  }
  renderFrame = requestAnimationFrame(() => {
    renderFrame = null
    renderChart()
  })
}

const scheduleResize = () => {
  if (resizeFrame !== null) {
    cancelAnimationFrame(resizeFrame)
  }
  resizeFrame = requestAnimationFrame(() => {
    resizeFrame = null
    const width = chartRef.value?.clientWidth || 0
    const height = chartRef.value?.clientHeight || 0
    if (width === lastWidth && height === lastHeight) {
      return
    }
    lastWidth = width
    lastHeight = height
    chart?.resize()
    scheduleRender()
  })
}

onMounted(() => {
  scheduleRender()
  if (chartRef.value) {
    resizeObserver = new ResizeObserver(scheduleResize)
    resizeObserver.observe(chartRef.value)
  }
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  if (renderFrame !== null) {
    cancelAnimationFrame(renderFrame)
  }
  if (resizeFrame !== null) {
    cancelAnimationFrame(resizeFrame)
  }
  chart?.dispose()
  chart = null
})

watch(
  () => props.graph,
  () => scheduleRender()
)
</script>

<template>
  <div v-loading="loading" class="relation-graph-view" :style="{ height }">
    <div v-if="!hasData && !loading" class="relation-empty">
      <span>暂无血缘关系</span>
    </div>
    <div ref="chartRef" class="relation-canvas"></div>
  </div>
</template>

<style lang="less" scoped>
.relation-graph-view {
  position: relative;
  min-height: 360px;
  overflow: hidden;
  background:
    linear-gradient(90deg, rgba(31, 35, 41, 0.04) 1px, transparent 1px),
    linear-gradient(rgba(31, 35, 41, 0.04) 1px, transparent 1px),
    #f8fafc;
  background-size: 32px 32px;
}

.relation-canvas {
  width: 100%;
  height: 100%;
}

.relation-empty {
  position: absolute;
  inset: 0;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8f959e;
  font-size: 14px;
}
</style>
