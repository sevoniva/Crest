<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue'
import request from '@/config/axios'
import { Icon } from '@/components/icon-custom'
import iconOrg from '@/assets/svg/icon_organization_outlined.svg'
import iconFolder from '@/assets/svg/folder.svg'
import iconExpand from '@/assets/svg/icon_expand-down_filled.svg'
import iconRefresh from '@/assets/svg/icon_refresh_outlined.svg'
import iconAdd from '@/assets/svg/icon_add_outlined.svg'
import iconRename from '@/assets/svg/icon_rename_outlined.svg'
import iconDelete from '@/assets/svg/icon_delete-trash_outlined.svg'
import iconLeft from '@/assets/svg/icon_left_outlined.svg'

const props = withDefaults(
  defineProps<{
    modelValue?: string | number | null
    title?: string
    selectableAll?: boolean
    manageable?: boolean
    collapsible?: boolean
    height?: string
  }>(),
  {
    title: '组织架构',
    selectableAll: false,
    manageable: false,
    collapsible: false,
    height: 'calc(100vh - 248px)'
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number | null): void
  (e: 'change', node: any | null): void
  (e: 'create', parent: any | null): void
  (e: 'edit', node: any): void
  (e: 'delete', node: any): void
}>()

const loading = ref(false)
const keyword = ref('')
const treeData = ref<any[]>([])
const currentKey = ref<any>(props.modelValue ?? null)
const treeRef = ref()
const expanded = ref(true)
const collapsed = ref(false)

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

const childCount = (data: any) => (data?.children?.length ? data.children.length : 0)

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

const toggleExpand = () => {
  expanded.value = !expanded.value
  const nodesMap = treeRef.value?.store?.nodesMap || {}
  Object.keys(nodesMap).forEach(key => {
    nodesMap[key].expanded = expanded.value
  })
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
  <div class="platform-org-tree" :class="{ collapsed }">
    <button
      v-if="collapsible && collapsed"
      class="rail"
      title="展开组织架构"
      @click="collapsed = false"
    >
      <span class="rail-icon"
        ><Icon name="organization"><iconOrg class="svg-icon" /></Icon
      ></span>
      <span class="rail-text">组织架构</span>
    </button>

    <aside v-show="!collapsed" class="tree-body">
      <div class="tree-head">
        <div class="tree-head-info">
          <div class="tree-title">{{ title }}</div>
          <div class="tree-current">{{ selectedName }}</div>
        </div>
        <div class="tree-head-actions">
          <el-tooltip v-if="manageable" content="新建组织" placement="top">
            <button class="head-btn" @click="emit('create', null)">
              <Icon name="icon_add_outlined"><iconAdd class="svg-icon" /></Icon>
            </button>
          </el-tooltip>
          <el-tooltip :content="expanded ? '折叠全部' : '展开全部'" placement="top">
            <button class="head-btn" :class="{ collapsed: !expanded }" @click="toggleExpand">
              <Icon name="icon_expand-down_filled"><iconExpand class="svg-icon" /></Icon>
            </button>
          </el-tooltip>
          <el-tooltip content="刷新" placement="top">
            <button class="head-btn" @click="loadTree">
              <Icon name="icon_refresh_outlined"><iconRefresh class="svg-icon" /></Icon>
            </button>
          </el-tooltip>
          <el-tooltip v-if="collapsible" content="收起" placement="top">
            <button class="head-btn" @click="collapsed = true">
              <Icon name="icon_left_outlined"><iconLeft class="svg-icon" /></Icon>
            </button>
          </el-tooltip>
        </div>
      </div>
      <el-input v-model="keyword" clearable placeholder="搜索组织" />
      <button
        v-if="selectableAll"
        class="all-node"
        :class="{ active: !currentKey }"
        @click="selectAll"
      >
        <span class="node-icon all"
          ><Icon name="organization"><iconOrg class="svg-icon" /></Icon
        ></span>
        <span class="node-label">全部组织</span>
      </button>
      <el-scrollbar :height="height" class="tree-scroll">
        <el-tree
          ref="treeRef"
          v-loading="loading"
          :data="treeData"
          node-key="id"
          :indent="16"
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
              <span class="node-icon" :class="{ root: node.level === 1 }">
                <Icon v-if="node.level === 1" name="organization"
                  ><iconOrg class="svg-icon"
                /></Icon>
                <Icon v-else name="folder"><iconFolder class="svg-icon" /></Icon>
              </span>
              <span class="node-label">{{ data.name }}</span>
              <span v-if="!manageable && childCount(data)" class="node-count">{{
                childCount(data)
              }}</span>
              <span v-if="manageable" class="node-actions" @click.stop>
                <el-tooltip content="新建下级" placement="top">
                  <button class="node-btn" @click.stop="emit('create', data)">
                    <Icon name="icon_add_outlined"><iconAdd class="svg-icon" /></Icon>
                  </button>
                </el-tooltip>
                <el-tooltip content="重命名" placement="top">
                  <button
                    class="node-btn"
                    :disabled="data.readOnly"
                    @click.stop="emit('edit', data)"
                  >
                    <Icon name="icon_rename_outlined"><iconRename class="svg-icon" /></Icon>
                  </button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <button
                    class="node-btn danger"
                    :disabled="data.readOnly"
                    @click.stop="emit('delete', data)"
                  >
                    <Icon name="icon_delete-trash_outlined"><iconDelete class="svg-icon" /></Icon>
                  </button>
                </el-tooltip>
              </span>
            </span>
          </template>
        </el-tree>
      </el-scrollbar>
    </aside>
  </div>
</template>

<style lang="less" scoped>
.platform-org-tree {
  display: flex;
  flex: 0 0 280px;
  width: 280px;
  transition: flex-basis 0.2s, width 0.2s;
  &.collapsed {
    flex: 0 0 44px;
    width: 44px;
  }
}
.rail {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 40px;
  padding: 16px 0;
  color: #475569;
  cursor: pointer;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  &:hover {
    color: #1d4ed8;
    border-color: #bfdbfe;
  }
  .rail-icon .svg-icon {
    width: 18px;
    height: 18px;
  }
  .rail-text {
    writing-mode: vertical-lr;
    letter-spacing: 2px;
    font-size: 13px;
    font-weight: 600;
  }
}
.tree-body {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
  padding: 16px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.tree-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 12px;
}
.tree-head-info {
  min-width: 0;
}
.tree-title {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}
.tree-current {
  margin-top: 2px;
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.tree-head-actions {
  display: flex;
  flex: 0 0 auto;
  gap: 4px;
}
.head-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  color: #64748b;
  cursor: pointer;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.15s;
  .svg-icon {
    width: 16px;
    height: 16px;
  }
  &:hover {
    color: #1d4ed8;
    background: #eff6ff;
    border-color: #bfdbfe;
  }
  &.collapsed .svg-icon {
    transform: rotate(-90deg);
  }
}
.all-node {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 100%;
  margin: 10px 0 2px;
  padding: 7px 8px;
  color: #334155;
  cursor: pointer;
  background: #fff;
  border: 0;
  border-radius: 8px;
  text-align: left;
  &.active {
    color: #1d4ed8;
    background: #eff6ff;
    .node-label {
      color: #1d4ed8;
    }
  }
}
.tree-scroll {
  margin-top: 6px;
}
.org-node {
  display: inline-flex;
  flex: 1;
  align-items: center;
  gap: 8px;
  min-width: 0;
  padding-right: 8px;
}
.node-icon {
  display: inline-flex;
  flex: 0 0 22px;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  color: #2563eb;
  background: #dbeafe;
  border-radius: 6px;
  .svg-icon {
    width: 14px;
    height: 14px;
  }
  &.root,
  &.all {
    color: #1d4ed8;
    background: #dbeafe;
  }
}
.node-label {
  flex: 1;
  overflow: hidden;
  color: #0f172a;
  font-size: 14px;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.node-count {
  flex: 0 0 auto;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
  text-align: center;
  background: #f1f5f9;
  border-radius: 9px;
}
.node-actions {
  display: none;
  flex: 0 0 auto;
  gap: 2px;
}
.node-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  color: #64748b;
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 6px;
  .svg-icon {
    width: 14px;
    height: 14px;
  }
  &:hover {
    color: #1d4ed8;
    background: #e0ecff;
  }
  &.danger:hover {
    color: #dc2626;
    background: #fee2e2;
  }
  &:disabled {
    color: #cbd5e1;
    cursor: not-allowed;
    background: transparent;
  }
}
:deep(.ed-tree-node__content:hover) .node-actions,
:deep(.ed-tree-node.is-current) .node-actions {
  display: inline-flex;
}
:deep(.ed-tree-node__content:hover) .node-count,
:deep(.ed-tree-node.is-current) .node-count {
  display: none;
}
:deep(.ed-tree) {
  background: transparent;
}
:deep(.ed-tree-node__content) {
  height: 34px;
  border-radius: 8px;
}
:deep(.ed-tree-node__children) {
  position: relative;
}
:deep(.ed-tree-node__children)::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 17px;
  left: 26px;
  width: 1px;
  background: #e2e8f0;
}
:deep(.ed-tree--highlight-current .ed-tree-node.is-current > .ed-tree-node__content) {
  background: #eff6ff;
  .node-label {
    color: #1d4ed8;
  }
}
</style>
