<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import request from '@/config/axios'
import { ElMessage } from 'element-plus-secondary'
import PlatformOrgTree from '../common/PlatformOrgTree.vue'

const loading = ref(false)
const saving = ref(false)
const roles = ref<any[]>([])
const menuTree = ref<any[]>([])
const resourceTree = ref<any[]>([])
const activeRoleId = ref<any>()
const selectedOrgId = ref<any>(1)
const selectedOrgName = ref('默认组织')
const resourceType = ref('screen')
const activeTab = ref('menu')
const menuTreeRef = ref()
const resourceTreeRef = ref()
const menuLabelMap: Record<string, string> = {
  workbranch: '工作台',
  panel: '仪表盘',
  screen: '数据大屏',
  data: '数据准备',
  dataset: '数据集',
  datasource: '数据源',
  'sys-setting': '系统设置',
  parameter: '系统参数',
  font: '字体管理',
  'share-management': '分享管理',
  'site-setting': '站点设置',
  'user-management': '用户管理',
  'single-sign-on': '单点登录',
  'audit-log': '审计日志',
  'org-management': '组织管理',
  'role-management': '角色管理',
  'permission-management': '权限管理',
  'template-market': '模板市场',
  toolbox: '工具箱',
  'template-setting': '模板设置',
  association: '数据血缘',
  msg: '消息中心'
}

const roleName = computed(
  () => roles.value.find(role => String(role.id) === String(activeRoleId.value))?.name || ''
)
const activeRole = computed(() =>
  roles.value.find(role => String(role.id) === String(activeRoleId.value))
)
const treeProps = {
  children: 'children',
  label: (node: any) => menuLabelMap[node?.name] || node?.name || '-'
}

const loadRoles = async () => {
  const res = selectedOrgId.value
    ? await request.get({ url: `/role/queryWithOid/${selectedOrgId.value}` })
    : await request.post({ url: '/role/byCurOrg', data: {} })
  roles.value = res.data || []
  if (!roles.value.some(role => String(role.id) === String(activeRoleId.value))) {
    activeRoleId.value = roles.value[0]?.id
  }
}

const loadMenuTree = async () => {
  const res = await request.get({ url: '/auth/menuResource' })
  menuTree.value = res.data || []
}

const loadResourceTree = async () => {
  const res = await request.get({ url: `/auth/busiResource/${resourceType.value}` })
  resourceTree.value = res.data || []
}

const loadPermissions = async () => {
  if (!activeRoleId.value) return
  loading.value = true
  try {
    const [menuRes, resourceRes]: any[] = await Promise.all([
      request.post({ url: '/auth/menuPermission', data: { id: activeRoleId.value } }),
      request.post({
        url: '/auth/busiPermission',
        data: { id: activeRoleId.value, type: 2, flag: resourceType.value }
      })
    ])
    const checkedMenus = (menuRes.data?.permissions || []).map(item => item.id)
    const checkedResources = (resourceRes.data?.permissions || []).map(item => item.id)
    setTimeout(() => {
      menuTreeRef.value?.setCheckedKeys(checkedMenus)
      resourceTreeRef.value?.setCheckedKeys(checkedResources)
    })
  } finally {
    loading.value = false
  }
}

const saveMenu = async () => {
  saving.value = true
  try {
    const ids = menuTreeRef.value?.getCheckedKeys(false) || []
    await request.post({
      url: '/auth/saveMenuPer',
      data: { id: activeRoleId.value, permissions: ids.map(id => ({ id, weight: 7 })) }
    })
    ElMessage.success('菜单权限已保存')
  } finally {
    saving.value = false
  }
}

const saveResource = async () => {
  saving.value = true
  try {
    const ids = resourceTreeRef.value?.getCheckedKeys(false) || []
    await request.post({
      url: '/auth/saveBusiPer',
      data: {
        id: activeRoleId.value,
        type: 2,
        flag: resourceType.value,
        permissions: ids.map(id => ({ id, weight: 7 }))
      }
    })
    ElMessage.success('资源权限已保存')
  } finally {
    saving.value = false
  }
}

const changeResourceType = async () => {
  await loadResourceTree()
  await loadPermissions()
}

const selectRole = async (role: any) => {
  activeRoleId.value = role.id
  await loadPermissions()
}

const onOrgChange = async node => {
  selectedOrgName.value = node?.name || '默认组织'
  await loadRoles()
  await loadPermissions()
}

const init = async () => {
  await Promise.all([loadRoles(), loadMenuTree(), loadResourceTree()])
  await loadPermissions()
}

onMounted(init)
</script>

<template>
  <div class="permission-manage">
    <p class="router-title">权限管理</p>
    <div class="permission-layout" v-loading="loading">
      <PlatformOrgTree v-model="selectedOrgId" height="calc(100vh - 232px)" @change="onOrgChange" />
      <aside class="role-panel">
        <div class="panel-title">角色配置</div>
        <div class="panel-desc">{{ selectedOrgName }}</div>
        <button
          v-for="role in roles"
          :key="role.id"
          class="role-item"
          :class="{ active: String(activeRoleId) === String(role.id) }"
          @click="selectRole(role)"
        >
          <span>
            <strong>{{ role.name }}</strong>
            <small>{{ role.readonly ? '只读角色' : role.root ? '系统角色' : '自定义角色' }}</small>
          </span>
          <i :class="{ readonly: role.readonly }"></i>
        </button>
      </aside>
      <section class="permission-panel">
        <div class="panel-head">
          <div>
            <div class="panel-title">{{ roleName || '请选择角色' }}</div>
            <div class="panel-desc">
              {{
                activeRole?.readonly
                  ? '只读角色用于审计巡检，建议仅授予查看类菜单和资源。'
                  : '配置角色可访问的菜单和业务资源，最终权限由后端判断。'
              }}
            </div>
          </div>
          <el-segmented
            v-model="activeTab"
            :options="[
              { label: '菜单权限', value: 'menu' },
              { label: '资源权限', value: 'resource' }
            ]"
          />
        </div>

        <div v-show="activeTab === 'menu'" class="permission-card">
          <div class="card-toolbar">
            <div>
              <div class="card-title">菜单权限</div>
              <div class="card-desc">控制左侧导航和系统设置菜单可见范围</div>
            </div>
            <el-button type="primary" :loading="saving" @click="saveMenu">保存菜单权限</el-button>
          </div>
          <el-tree
            ref="menuTreeRef"
            class="permission-tree"
            :data="menuTree"
            show-checkbox
            node-key="id"
            default-expand-all
            :props="treeProps"
          />
        </div>

        <div v-show="activeTab === 'resource'" class="permission-card">
          <div class="card-toolbar">
            <div>
              <div class="card-title">资源权限</div>
              <div class="card-desc">控制数据源、数据集、仪表盘和数据大屏的访问范围</div>
            </div>
            <div class="resource-actions">
              <el-select v-model="resourceType" @change="changeResourceType">
                <el-option label="仪表盘" value="panel" />
                <el-option label="数据大屏" value="screen" />
                <el-option label="数据集" value="dataset" />
                <el-option label="数据源" value="datasource" />
              </el-select>
              <el-button type="primary" :loading="saving" @click="saveResource">保存资源权限</el-button>
            </div>
          </div>
          <el-tree
            ref="resourceTreeRef"
            class="permission-tree"
            :data="resourceTree"
            show-checkbox
            node-key="id"
            default-expand-all
            :props="treeProps"
          />
        </div>
      </section>
    </div>
  </div>
</template>

<style lang="less" scoped>
.permission-manage {
  min-height: 100%;
}
.router-title {
  margin: 0 0 16px;
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
}
.permission-layout {
  display: grid;
  grid-template-columns: 300px 280px minmax(0, 1fr);
  gap: 16px;
  align-items: stretch;
  min-height: calc(100vh - 190px);
}
.role-panel,
.permission-panel {
  padding: 16px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.permission-panel {
  min-width: 0;
}
.panel-title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}
.panel-desc {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}
.role-item {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding: 12px;
  cursor: pointer;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  color: #334155;
  text-align: left;
  strong,
  small {
    display: block;
  }
  small {
    margin-top: 4px;
    color: #64748b;
  }
  i {
    width: 8px;
    height: 28px;
    background: #3b82f6;
    border-radius: 999px;
    &.readonly {
      background: #94a3b8;
    }
  }
  &.active {
    color: #0f172a;
    background: #eff6ff;
    border-color: #3b82f6;
  }
}
.panel-head,
.card-toolbar {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
}
.permission-card {
  margin-top: 16px;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}
.card-title {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}
.card-desc {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}
.resource-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  .ed-select {
    width: 160px;
  }
}
.permission-tree {
  margin-top: 14px;
  padding: 10px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}
:deep(.ed-tree-node__content) {
  height: 36px;
  border-radius: 8px;
}
</style>
