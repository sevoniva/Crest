<script lang="ts" setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import {
  forceCenter,
  forceCollide,
  forceLink,
  forceManyBody,
  forceSimulation,
  forceX,
  forceY
} from 'd3-force'
import type { SimulationLinkDatum, SimulationNodeDatum } from 'd3-force'

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

type LayoutMode = 'layered' | 'knowledge'

interface LayoutNode extends RelationNode, SimulationNodeDatum {
  x?: number
  y?: number
  fx?: number | null
  fy?: number | null
  anchorX?: number
  anchorY?: number
  degree?: number
}

interface LayoutLink extends SimulationLinkDatum<LayoutNode> {
  source: string | LayoutNode
  target: string | LayoutNode
  type?: string
}

const props = withDefaults(
  defineProps<{
    graph?: RelationGraph | null
    loading?: boolean
    height?: string
    layoutMode?: LayoutMode
  }>(),
  {
    graph: null,
    loading: false,
    height: '100%',
    layoutMode: 'layered'
  }
)

const chartRef = ref<HTMLDivElement>()
let chart: any = null
let resizeObserver: ResizeObserver | null = null
let renderFrame: number | null = null
let resizeFrame: number | null = null
let resizeTimer: number | null = null
let lastWidth = 0
let lastHeight = 0
let resetViewOnNextRender = false
let knowledgeLayoutCacheKey = ''
let knowledgeLayoutCache = new Map<string, { x: number; y: number; degree: number }>()
const selectedNodeId = ref<string>()

const typeMeta: Record<string, { label: string; color: string; level: number; symbol: string }> = {
  datasource: { label: '数据源', color: '#3b82f6', level: 0, symbol: 'roundRect' },
  table: { label: '物理表', color: '#06b6d4', level: 1, symbol: 'roundRect' },
  table_field: { label: '物理字段', color: '#22c55e', level: 2, symbol: 'circle' },
  dataset_field: { label: '数据集字段', color: '#14b8a6', level: 3, symbol: 'circle' },
  dataset: { label: '数据集', color: '#84cc16', level: 4, symbol: 'roundRect' },
  chart_field: { label: '图表字段', color: '#f59e0b', level: 5, symbol: 'circle' },
  chart: { label: '图表', color: '#f97316', level: 6, symbol: 'roundRect' },
  dv: { label: '仪表板/大屏', color: '#ec4899', level: 7, symbol: 'roundRect' }
}

const dashedEdgeTypes = new Set([
  'table_table_field',
  'table_field_dataset_field',
  'table_field_join',
  'dataset_field_calc_field',
  'dataset_field_chart_field',
  'chart_field_chart'
])

const highlightedEdgeTypes = new Set([
  'dataset_table_join',
  'table_field_join',
  'dataset_field_calc_field'
])

const maxLevel = 7

const escapeHtml = (value: string) =>
  value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const hasData = computed(() => !!props.graph?.nodes?.length)
const graphNodes = computed(() => props.graph?.nodes || [])
const graphEdges = computed(() => props.graph?.edges || [])
const isLargeGraph = computed(() => {
  const nodeCount = graphNodes.value.length
  const edgeCount = graphEdges.value.length
  return nodeCount > 220 || edgeCount > 420
})

const isDenseGraph = computed(() => {
  const nodeCount = graphNodes.value.length
  const edgeCount = graphEdges.value.length
  return nodeCount > 60 || edgeCount > 120
})

const isRenderHeavyGraph = computed(() => {
  const nodeCount = graphNodes.value.length
  const edgeCount = graphEdges.value.length
  return nodeCount > 120 || edgeCount > 240
})

const categoryMeta = computed(() => {
  const categoryNames = new Set(
    graphNodes.value.map(node => (typeMeta[node.type] || typeMeta.dataset).label)
  )
  return Object.values(typeMeta).filter(item => categoryNames.has(item.label))
})

const nodeLookup = computed(() =>
  graphNodes.value.reduce<Record<string, RelationNode>>((acc, node) => {
    acc[node.id] = node
    return acc
  }, {})
)

const neighborIds = computed(() => {
  if (!selectedNodeId.value) return new Set<string>()
  const ids = new Set<string>([selectedNodeId.value])
  graphEdges.value.forEach(edge => {
    if (edge.source === selectedNodeId.value) {
      ids.add(edge.target)
    }
    if (edge.target === selectedNodeId.value) {
      ids.add(edge.source)
    }
  })
  return ids
})

const selectedNode = computed(() => {
  return selectedNodeId.value ? nodeLookup.value[selectedNodeId.value] : undefined
})

const selectedStats = computed(() => {
  if (!selectedNodeId.value) return { upstream: 0, downstream: 0 }
  return graphEdges.value.reduce(
    (acc, edge) => {
      if (edge.target === selectedNodeId.value) {
        acc.upstream++
      }
      if (edge.source === selectedNodeId.value) {
        acc.downstream++
      }
      return acc
    },
    { upstream: 0, downstream: 0 }
  )
})

const getChartSize = () => ({
  width: chartRef.value?.clientWidth || 0,
  height: chartRef.value?.clientHeight || 0
})

const resizeChartToContainer = (force = false) => {
  const { width, height } = getChartSize()
  if (!width || !height || !chart) {
    return false
  }
  const sizeChanged = width !== lastWidth || height !== lastHeight
  if (force || sizeChanged) {
    if (sizeChanged) {
      resetViewOnNextRender = true
      knowledgeLayoutCacheKey = ''
      knowledgeLayoutCache.clear()
      lastWidth = width
      lastHeight = height
    }
    chart.resize({ width, height })
  }
  return true
}

const ensureChart = async () => {
  await nextTick()
  if (!chartRef.value) return
  const { width, height } = getChartSize()
  if (!width || !height) return
  if (!chart) {
    chart = echarts.init(chartRef.value, undefined, {
      renderer: 'canvas',
      devicePixelRatio: window.devicePixelRatio || 1,
      useDirtyRect: false,
      width,
      height
    })
    chart.on('click', handleChartClick)
  }
  resizeChartToContainer()
}

const clearFocus = () => {
  selectedNodeId.value = undefined
  scheduleRender()
}

const handleChartClick = (params: any) => {
  if (params.dataType === 'node' && params.data?.id) {
    selectedNodeId.value = selectedNodeId.value === params.data.id ? undefined : params.data.id
    scheduleRender()
    return
  }
  if (params.dataType !== 'edge') {
    clearFocus()
  }
}

const hashNode = (id: string) => {
  let hash = 0
  for (let i = 0; i < id.length; i++) {
    hash = (hash << 5) - hash + id.charCodeAt(i)
    hash |= 0
  }
  return Math.abs(hash)
}

const getNodeDegree = () => {
  return graphEdges.value.reduce<Record<string, number>>((acc, edge) => {
    acc[edge.source] = (acc[edge.source] || 0) + 1
    acc[edge.target] = (acc[edge.target] || 0) + 1
    return acc
  }, {})
}

const clamp = (value: number, min: number, max: number) => Math.max(min, Math.min(max, value))

const getNodeLevel = (node: RelationNode) =>
  clamp(node.level ?? typeMeta[node.type]?.level ?? 0, 0, maxLevel)

const getLevelX = (level: number, width: number, paddingX: number) => {
  const usableWidth = Math.max(1, width - paddingX * 2)
  return paddingX + (usableWidth * clamp(level, 0, maxLevel)) / maxLevel
}

const getBalancedSlots = (total: number) => {
  const middle = (total - 1) / 2
  return Array.from({ length: total }, (_, index) => index).sort((left, right) => {
    const distance = Math.abs(left - middle) - Math.abs(right - middle)
    return distance || left - right
  })
}

const fitNodesToViewport = (
  nodes: LayoutNode[],
  width: number,
  height: number,
  paddingX: number,
  paddingY: number
) => {
  if (nodes.length < 8) return
  const usableWidth = Math.max(1, width - paddingX * 2)
  const usableHeight = Math.max(1, height - paddingY * 2)
  const minX = Math.min(...nodes.map(node => node.x ?? width / 2))
  const maxX = Math.max(...nodes.map(node => node.x ?? width / 2))
  const minY = Math.min(...nodes.map(node => node.y ?? height / 2))
  const maxY = Math.max(...nodes.map(node => node.y ?? height / 2))
  const spanX = Math.max(1, maxX - minX)
  const spanY = Math.max(1, maxY - minY)
  const isWideViewport = width >= 1180
  const isTallViewport = height >= 760
  const targetWidth = usableWidth * (isWideViewport ? 0.98 : 0.86)
  const targetHeight = usableHeight * (isTallViewport ? 0.9 : 0.78)
  const shouldFitX = isWideViewport || spanX < targetWidth
  const shouldFitY = nodes.length > 18 && (isTallViewport || spanY < targetHeight)

  nodes.forEach(node => {
    if (shouldFitX) {
      node.x =
        paddingX +
        (usableWidth - targetWidth) / 2 +
        (((node.x ?? width / 2) - minX) / spanX) * targetWidth
    }
    if (shouldFitY) {
      node.y =
        paddingY +
        (usableHeight - targetHeight) / 2 +
        (((node.y ?? height / 2) - minY) / spanY) * targetHeight
    }
  })
}

const isSelectedContext = (id: string) => {
  return !selectedNodeId.value || neighborIds.value.has(id)
}

const getNodeVisual = (node: RelationNode, degree = 0) => {
  const meta = typeMeta[node.type] || typeMeta.dataset
  const isField = node.type?.includes('field')
  const selected = selectedNodeId.value === node.id
  const visible = isSelectedContext(node.id)
  const knowledgeMode = props.layoutMode === 'knowledge'
  const showNeighborLabel = !!selectedNodeId.value && neighborIds.value.has(node.id)
  const importantFieldDegree = isRenderHeavyGraph.value ? 6 : isDenseGraph.value ? 4 : 2
  const showLabel =
    selected ||
    showNeighborLabel ||
    !isField ||
    (knowledgeMode && isField && degree >= importantFieldDegree) ||
    (!isDenseGraph.value && (!isField || graphNodes.value.length <= 90))
  const symbolSize = knowledgeMode
    ? isField
      ? Math.min(isRenderHeavyGraph.value ? 34 : 42, 18 + degree * 1.6)
      : node.type === 'dv'
      ? Math.min(isRenderHeavyGraph.value ? 60 : 74, 42 + degree * 1.8)
      : Math.min(isRenderHeavyGraph.value ? 56 : 68, 36 + degree * 1.8)
    : node.type === 'dv'
    ? [118, 42]
    : isField
    ? isDenseGraph.value
      ? [18, 18]
      : [62, 62]
    : node.type === 'table'
    ? [108, 40]
    : [104, 40]

  return {
    category: meta.label,
    itemStyle: {
      color: isField && !knowledgeMode ? '#ffffff' : meta.color,
      borderColor: selected ? '#1f2329' : isField && !knowledgeMode ? meta.color : '#ffffff',
      borderType: isField && !knowledgeMode ? 'dashed' : 'solid',
      borderWidth: selected ? 3 : isField && !knowledgeMode ? 1.6 : 2,
      opacity: visible ? 1 : 0.18,
      shadowBlur: selected ? 18 : isRenderHeavyGraph.value ? 0 : knowledgeMode ? 10 : 8,
      shadowOffsetY: selected ? 10 : knowledgeMode ? 7 : 0,
      shadowColor: selected ? `${meta.color}70` : `${meta.color}42`
    },
    symbol: knowledgeMode ? 'circle' : meta.symbol,
    symbolSize,
    label: {
      show: showLabel,
      color:
        knowledgeMode && isField ? '#334155' : knowledgeMode || !isField ? '#ffffff' : '#1f2329',
      position: knowledgeMode && isField ? 'right' : 'inside',
      distance: knowledgeMode && isField ? 6 : 0,
      width: knowledgeMode ? (isField ? 96 : 82) : node.type === 'dv' ? 96 : isField ? 52 : 82,
      overflow: 'truncate',
      fontSize: selected ? 12 : isField ? 10 : 12,
      fontWeight: selected ? 700 : 500
    }
  }
}

const layoutLayeredNodes = () => {
  const nodes = graphNodes.value
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
    return {
      ...node,
      x: Math.round(width * (lanes[level] ?? 0.5)),
      y: Math.round(step * (index + 1)),
      ...getNodeVisual(node)
    }
  })
}

const getKnowledgeLayoutCacheKey = (width: number, height: number) => {
  const roundedWidth = Math.round(width / 20) * 20
  const roundedHeight = Math.round(height / 20) * 20
  const nodeKey = graphNodes.value
    .map(node => `${node.id}:${node.type}:${node.level ?? ''}`)
    .join('|')
  const edgeKey = graphEdges.value
    .map(edge => `${edge.source}>${edge.target}:${edge.type ?? ''}`)
    .join('|')
  return `${roundedWidth}x${roundedHeight}|${nodeKey}|${edgeKey}`
}

const getKnowledgeNodesFromCache = (centerX: number, centerY: number) => {
  if (!knowledgeLayoutCache.size) return null
  const nodes = graphNodes.value.map(node => {
    const cached = knowledgeLayoutCache.get(node.id)
    if (!cached) return null
    return {
      ...node,
      x: cached.x,
      y: cached.y,
      degree: cached.degree,
      ...getNodeVisual(node, cached.degree)
    }
  })

  if (nodes.some(node => !node)) {
    return null
  }
  return nodes.map(node => ({
    ...node,
    x: node?.x ?? centerX,
    y: node?.y ?? centerY
  }))
}

const layoutKnowledgeNodes = () => {
  const width = chartRef.value?.clientWidth || 1000
  const height = chartRef.value?.clientHeight || 560
  const centerX = width / 2
  const centerY = height / 2
  const paddingX = clamp(width * (width >= 1180 ? 0.045 : 0.075), 48, width >= 1180 ? 96 : 170)
  const paddingY = clamp(height * (height >= 760 ? 0.08 : 0.12), 54, height >= 760 ? 96 : 130)
  const usableHeight = Math.max(1, height - paddingY * 2)
  const cacheKey = getKnowledgeLayoutCacheKey(width, height)
  if (knowledgeLayoutCacheKey === cacheKey) {
    const cachedNodes = getKnowledgeNodesFromCache(centerX, centerY)
    if (cachedNodes) {
      return cachedNodes
    }
  }

  const degreeMap = getNodeDegree()
  const grouped = graphNodes.value.reduce<Record<number, RelationNode[]>>((acc, node) => {
    const level = getNodeLevel(node)
    acc[level] = acc[level] || []
    acc[level].push(node)
    return acc
  }, {})
  const slotIndexMap = new Map<string, number>()
  Object.keys(grouped).forEach(levelKey => {
    const group = grouped[Number(levelKey)]
      .slice()
      .sort(
        (left, right) =>
          (degreeMap[right.id] || 0) - (degreeMap[left.id] || 0) ||
          (left.name || '').localeCompare(right.name || '')
      )
    const slots = getBalancedSlots(group.length)
    group.forEach((node, index) => {
      slotIndexMap.set(node.id, slots[index] ?? index)
    })
  })
  const centerId = graphNodes.value
    .slice()
    .sort((left, right) => (degreeMap[right.id] || 0) - (degreeMap[left.id] || 0))[0]?.id

  const nodes: LayoutNode[] = graphNodes.value.map(node => {
    const hash = hashNode(node.id)
    const level = getNodeLevel(node)
    const groupSize = grouped[level]?.length || 1
    const slot = slotIndexMap.get(node.id) ?? 0
    const stepY = usableHeight / (groupSize + 1)
    const jitterX = ((hash % 1000) / 1000 - 0.5) * clamp(width * 0.055, 32, 110)
    const jitterY = ((Math.floor(hash / 1000) % 1000) / 1000 - 0.5) * Math.min(stepY * 0.5, 78)
    const anchorX = getLevelX(level, width, paddingX)
    const anchorY = paddingY + stepY * (slot + 1)
    const isHub = centerId === node.id && (degreeMap[node.id] || 0) >= 4
    return {
      ...node,
      degree: degreeMap[node.id] || 0,
      anchorX,
      anchorY,
      x: isHub ? anchorX : anchorX + jitterX,
      y: isHub ? centerY : anchorY + jitterY,
      fx: null,
      fy: null
    }
  })

  const layoutNodeIds = new Set(nodes.map(node => node.id))
  const links: LayoutLink[] = graphEdges.value
    .filter(edge => layoutNodeIds.has(edge.source) && layoutNodeIds.has(edge.target))
    .map(edge => ({ source: edge.source, target: edge.target, type: edge.type }))

  const collideBase = isRenderHeavyGraph.value ? 16 : isDenseGraph.value ? 22 : 34
  const simulation = forceSimulation(nodes)
    .force(
      'link',
      forceLink<LayoutNode, LayoutLink>(links)
        .id(node => node.id)
        .distance(link =>
          highlightedEdgeTypes.has(link.type || '')
            ? isRenderHeavyGraph.value
              ? 82
              : 112
            : isRenderHeavyGraph.value
            ? 98
            : isDenseGraph.value
            ? 132
            : 168
        )
        .strength(isRenderHeavyGraph.value ? 0.18 : 0.26)
    )
    .force(
      'charge',
      forceManyBody().strength(isRenderHeavyGraph.value ? -130 : isDenseGraph.value ? -210 : -330)
    )
    .force(
      'collide',
      forceCollide<LayoutNode>(
        node => collideBase + Math.min(22, (node.degree || 0) * 2)
      ).iterations(isRenderHeavyGraph.value ? 1 : isDenseGraph.value ? 2 : 3)
    )
    .force('center', forceCenter(centerX, centerY).strength(0.008))
    .force(
      'x',
      forceX<LayoutNode>(
        node => node.anchorX ?? getLevelX(getNodeLevel(node), width, paddingX)
      ).strength(isRenderHeavyGraph.value ? 0.16 : 0.12)
    )
    .force('y', forceY<LayoutNode>(node => node.anchorY ?? centerY).strength(0.045))
    .stop()

  const ticks = isLargeGraph.value
    ? 64
    : isRenderHeavyGraph.value
    ? 88
    : isDenseGraph.value
    ? 118
    : 168
  for (let i = 0; i < ticks; i++) {
    simulation.tick()
  }

  fitNodesToViewport(nodes, width, height, paddingX, paddingY)

  const layoutNodes = nodes.map(node => {
    const x = Math.round(clamp(node.x || centerX, 36, width - 36))
    const y = Math.round(clamp(node.y || centerY, 36, height - 36))
    const degree = node.degree || 0
    return {
      ...node,
      x,
      y,
      degree,
      ...getNodeVisual(node, degree)
    }
  })

  knowledgeLayoutCacheKey = cacheKey
  knowledgeLayoutCache = new Map(
    layoutNodes.map(node => [node.id, { x: node.x, y: node.y, degree: node.degree || 0 }])
  )

  return layoutNodes
}

const layoutNodes = () => {
  return props.layoutMode === 'knowledge' ? layoutKnowledgeNodes() : layoutLayeredNodes()
}

const formatLinks = () => {
  return graphEdges.value.map(edge => {
    const isHighlighted = highlightedEdgeTypes.has(edge.type || '')
    const selectedContext =
      !selectedNodeId.value ||
      edge.source === selectedNodeId.value ||
      edge.target === selectedNodeId.value
    const showLabel = !isDenseGraph.value && edge.type === 'dataset_field_calc_field'
    return {
      ...edge,
      lineStyle: {
        color: selectedContext ? (isHighlighted ? '#2563eb' : '#94a3b8') : '#d7dde8',
        type: dashedEdgeTypes.has(edge.type || '') ? 'dashed' : 'solid',
        width: selectedContext ? (isHighlighted ? 2.1 : isLargeGraph.value ? 0.9 : 1.35) : 0.7,
        curveness:
          props.layoutMode === 'knowledge'
            ? 0.12
            : edge.type === 'dataset_table_join'
            ? 0.08
            : 0.18,
        opacity: selectedContext ? (isHighlighted ? 0.9 : isLargeGraph.value ? 0.5 : 0.74) : 0.16
      },
      label: {
        show: showLabel || (!!selectedNodeId.value && selectedContext && !!edge.label),
        formatter: edge.label || '',
        color: '#245bdb',
        fontSize: 10,
        backgroundColor: 'rgba(255,255,255,0.82)',
        borderRadius: 4,
        padding: [2, 4]
      }
    }
  })
}

const renderChart = async () => {
  await ensureChart()
  if (!chart) return
  if (!resizeChartToContainer()) {
    scheduleResize()
    return
  }
  if (!hasData.value) {
    chart.clear()
    return
  }

  if (resetViewOnNextRender) {
    chart.clear()
    resetViewOnNextRender = false
  }

  chart.setOption(
    {
      animation: props.layoutMode !== 'knowledge' && !isLargeGraph.value,
      animationDurationUpdate:
        props.layoutMode === 'knowledge' || isRenderHeavyGraph.value ? 0 : 180,
      animationEasingUpdate: 'quadraticOut',
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
          const description = data.description
            ? `<br/><span style="color:#646a73">${escapeHtml(String(data.description)).replace(
                /\n/g,
                '<br/>'
              )}</span>`
            : ''
          return `${meta.label || '资源'}<br/>${escapeHtml(
            data.name || ''
          )}${subType}${description}`
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
        data: categoryMeta.value.map(item => item.label)
      },
      series: [
        {
          type: 'graph',
          layout: 'none',
          roam: true,
          draggable: true,
          animation: props.layoutMode !== 'knowledge' && !isRenderHeavyGraph.value,
          progressive: isRenderHeavyGraph.value ? 300 : 0,
          progressiveThreshold: 300,
          hoverLayerThreshold: isRenderHeavyGraph.value ? 1000000 : 3000,
          edgeSymbol: ['none', 'arrow'],
          edgeSymbolSize: props.layoutMode === 'knowledge' ? [3, 7] : [4, 8],
          categories: categoryMeta.value.map(item => ({
            name: item.label
          })),
          data: layoutNodes(),
          links: formatLinks(),
          lineStyle: {
            color: '#8f959e',
            width: isLargeGraph.value ? 0.9 : 1.3,
            curveness: props.layoutMode === 'knowledge' ? 0.12 : 0.18,
            opacity: isLargeGraph.value ? 0.46 : 0.72
          },
          labelLayout: {
            hideOverlap: true
          },
          emphasis: {
            focus: props.layoutMode === 'knowledge' || !isLargeGraph.value ? 'adjacency' : 'none',
            lineStyle: {
              width: 2.4,
              opacity: 1
            }
          }
        }
      ]
    },
    {
      notMerge: false,
      lazyUpdate: false,
      replaceMerge: ['series']
    }
  )
  resizeChartToContainer(true)
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
    if (!resizeChartToContainer()) return
    if (resizeTimer !== null) {
      window.clearTimeout(resizeTimer)
    }
    resizeTimer = window.setTimeout(() => {
      resizeTimer = null
      scheduleRender()
    }, 80)
  })
}

onMounted(() => {
  scheduleRender()
  window.setTimeout(scheduleResize, 120)
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
  if (resizeTimer !== null) {
    window.clearTimeout(resizeTimer)
  }
  chart?.dispose()
  chart = null
})

watch(
  () => props.graph,
  () => {
    if (selectedNodeId.value && !nodeLookup.value[selectedNodeId.value]) {
      selectedNodeId.value = undefined
    }
    knowledgeLayoutCacheKey = ''
    knowledgeLayoutCache.clear()
    scheduleRender()
  }
)

watch(
  () => props.layoutMode,
  () => {
    selectedNodeId.value = undefined
    knowledgeLayoutCacheKey = ''
    knowledgeLayoutCache.clear()
    scheduleRender()
  }
)

watch(
  () => props.height,
  () => {
    resetViewOnNextRender = true
    knowledgeLayoutCacheKey = ''
    knowledgeLayoutCache.clear()
    scheduleResize()
  }
)
</script>

<template>
  <div
    v-loading="loading"
    class="relation-graph-view"
    :class="`is-${layoutMode}`"
    :style="{ height }"
  >
    <div v-if="!hasData && !loading" class="relation-empty">
      <span>暂无血缘关系</span>
    </div>
    <div ref="chartRef" class="relation-canvas"></div>
    <div v-if="selectedNode" class="relation-float-card">
      <div class="float-card-type">
        {{ typeMeta[selectedNode.type]?.label || selectedNode.type }}
      </div>
      <strong>{{ selectedNode.name }}</strong>
      <span v-if="selectedNode.subType">{{ selectedNode.subType }}</span>
      <div class="float-card-stats">
        <small>上游 {{ selectedStats.upstream }}</small>
        <small>下游 {{ selectedStats.downstream }}</small>
      </div>
      <button type="button" @click="clearFocus">取消聚焦</button>
    </div>
  </div>
</template>

<style lang="less" scoped>
.relation-graph-view {
  position: relative;
  min-height: 360px;
  overflow: hidden;
  background: radial-gradient(circle at 18% 16%, rgba(59, 130, 246, 0.1), transparent 30%),
    radial-gradient(circle at 76% 24%, rgba(20, 184, 166, 0.1), transparent 28%),
    linear-gradient(90deg, rgba(31, 35, 41, 0.035) 1px, transparent 1px),
    linear-gradient(rgba(31, 35, 41, 0.035) 1px, transparent 1px), #f8fafc;
  background-size: 32px 32px;
}

.relation-graph-view.is-knowledge {
  background: radial-gradient(circle at 14% 18%, rgba(59, 130, 246, 0.12), transparent 32%),
    radial-gradient(circle at 84% 18%, rgba(20, 184, 166, 0.12), transparent 30%),
    radial-gradient(circle at 72% 84%, rgba(236, 72, 153, 0.08), transparent 34%),
    linear-gradient(90deg, rgba(100, 116, 139, 0.045) 1px, transparent 1px),
    linear-gradient(rgba(100, 116, 139, 0.045) 1px, transparent 1px), #f8fbff;
  background-size: auto, auto, auto, 28px 28px, 28px 28px, auto;
}

.relation-canvas {
  width: 100%;
  height: 100%;
}

.relation-float-card {
  position: absolute;
  left: 16px;
  bottom: 16px;
  z-index: 2;
  width: 248px;
  padding: 14px;
  border: 1px solid rgba(222, 224, 227, 0.9);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 14px 38px rgba(31, 35, 41, 0.13);
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  gap: 8px;

  strong {
    color: #1f2329;
    font-size: 14px;
    line-height: 20px;
    font-weight: 600;
  }

  span {
    color: #646a73;
    font-size: 12px;
    line-height: 18px;
  }

  button {
    height: 28px;
    border: 1px solid #dee0e3;
    border-radius: 6px;
    background: #ffffff;
    color: #245bdb;
    cursor: pointer;
  }
}

.float-card-type {
  color: #8f959e;
  font-size: 12px;
  line-height: 16px;
}

.float-card-stats {
  display: flex;
  gap: 8px;

  small {
    padding: 3px 8px;
    border-radius: 999px;
    background: #f2f4f7;
    color: #646a73;
  }
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
