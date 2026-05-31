<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue'
import request from '@/config/axios'

const props = withDefaults(
  defineProps<{
    modelValue?: string | number | null
    title?: string
    selectableAll?: boolean
    height?: string
  }>(),
  {
    title: '组织架构',
    selectableAll: false,
    height: 'calc(100vh - 232px)'
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number | null): void
  (e: 'change', node: any | null): void
}>()

const loading = ref(false)
const keyword = ref('')
const treeData = ref<any[]>([])
const currentKey = ref<any>(props.modelValue ?? null)
const treeRef = ref()

const treeProps = { children: 'children', label: 'name' }
const selectedName = computed(() => {
  if (props.selectableAll && !currentKey.value) return '全部组织'
  return findNode(treeData.value, currentKey.value)?.name || '请选择组织'
})

const findNode = (nodes: any[], id: any): any | null => {
  for (const node of nodes || []) {
    if (String(node.id) === String(id)) return node
    const child = findNode(node.children || [], id)
    if (child) return child
  }
  return null
}

const loadTree = async () => {
  loading.value = true
  try {
    const res = await request.post({ url: '/org/page/tree', data: { keyword: keyword.value } })
    treeData.value = res.data || []
    if (!props.selectableAll && !currentKey.value) {
      const first = treeData.value[0]
      if (first?.id) selectNode(first)
    }
  } finally {
    loading.value = false
  }
}

const selectAll = () => {
  currentKey.value = null
  emit('update:modelValue', null)
  emit('change', null)
}

const selectNode = (node: any) => {
  currentKey.value = node.id
  emit('update:modelValue', node.id)
  emit('change', node)
}

watch(
  () => props.modelValue,
  value => {
    currentKey.value = value ?? null
  }
)

watch(keyword, value => treeRef.value?.filter(value))

const filterNode = (value: string, data: any) => {
  if (!value) return true
  return String(data.name || '').includes(value)
}

defineExpose({ loadTree, treeData })

onMounted(loadTree)
</script>

<template>
  <aside class="platform-org-tree">
    <div class="tree-head">
      <div>
        <div class="tree-title">{{ title }}</div>
        <div class="tree-current">{{ selectedName }}</div>
      </div>
    </div>
    <el-input v-model="keyword" clearable placeholder="搜索组织" />
    <button v-if="selectableAll" class="all-node" :class="{ active: !currentKey }" @click="selectAll">
      <span class="node-icon">全</span>
      <span>全部组织</span>
    </button>
    <el-scrollbar :height="height" class="tree-scroll">
      <el-tree
        ref="treeRef"
        v-loading="loading"
        :data="treeData"
        node-key="id"
        default-expand-all
        highlight-current
        :current-node-key="currentKey"
        :expand-on-click-node="false"
        :filter-node-method="filterNode"
        :props="treeProps"
        @node-click="selectNode"
      >
        <template #default="{ node, data }">
          <span class="org-node">
            <span class="node-icon">{{ node.level }}</span>
            <span class="node-label">{{ data.name }}</span>
          </span>
        </template>
      </el-tree>
    </el-scrollbar>
  </aside>
</template>

<style lang="less" scoped>
.platform-org-tree {
  display: flex;
  flex-direction: column;
  min-width: 280px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.tree-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}
.tree-title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}
.tree-current {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}
.all-node {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 100%;
  margin: 12px 0 4px;
  padding: 9px 10px;
  color: #334155;
  cursor: pointer;
  background: #fff;
  border: 0;
  border-radius: 10px;
  text-align: left;
  &.active {
    color: #1d4ed8;
    background: #eff6ff;
  }
}
.tree-scroll {
  margin-top: 8px;
}
.org-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.node-icon {
  display: inline-flex;
  flex: 0 0 22px;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  background: #dbeafe;
  border-radius: 7px;
}
.node-label {
  overflow: hidden;
  color: #0f172a;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}
:deep(.ed-tree-node__content) {
  height: 38px;
  border-radius: 10px;
}
:deep(.ed-tree--highlight-current .ed-tree-node.is-current > .ed-tree-node__content) {
  background: #eff6ff;
}
</style>
