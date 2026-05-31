<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import request from '@/config/axios'
import { ElMessage } from 'element-plus-secondary'

const loading = ref(false)
const saving = ref(false)
const roles = ref<any[]>([])
const menuTree = ref<any[]>([])
const resourceTree = ref<any[]>([])
const checkedMenus = ref<any[]>([])
const checkedResources = ref<any[]>([])
const activeRoleId = ref<any>()
const resourceType = ref('panel')
const menuTreeRef = ref()
const resourceTreeRef = ref()

const roleName = computed(() => roles.value.find(role => String(role.id) === String(activeRoleId.value))?.name || '')
const treeProps = { children: 'children', label: 'name' }

const loadRoles = async () => {
  const res = await request.post({ url: '/role/byCurOrg', data: {} })
  roles.value = res.data || []
  activeRoleId.value = roles.value[0]?.id
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
    checkedMenus.value = (menuRes.data?.permissions || []).map(item => item.id)
    checkedResources.value = (resourceRes.data?.permissions || []).map(item => item.id)
    setTimeout(() => {
      menuTreeRef.value?.setCheckedKeys(checkedMenus.value)
      resourceTreeRef.value?.setCheckedKeys(checkedResources.value)
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

const init = async () => {
  await Promise.all([loadRoles(), loadMenuTree(), loadResourceTree()])
  await loadPermissions()
}

const changeResourceType = async () => {
  await loadResourceTree()
  await loadPermissions()
}

const selectRole = async (role: any) => {
  activeRoleId.value = role.id
  await loadPermissions()
}

onMounted(init)
</script>

<template>
  <div class="permission-manage">
    <p class="router-title">权限管理</p>
    <div class="permission-layout" v-loading="loading">
      <aside class="role-panel">
        <div class="panel-title">角色</div>
        <button
          v-for="role in roles"
          :key="role.id"
          class="role-item"
          :class="{ active: String(activeRoleId) === String(role.id) }"
          @click="selectRole(role)"
        >
          <span>{{ role.name }}</span>
          <small>{{ role.root ? '系统' : '自定义' }}</small>
        </button>
      </aside>
      <section class="permission-panel">
        <div class="panel-head">
          <div>
            <div class="panel-title">{{ roleName || '请选择角色' }}</div>
            <div class="panel-desc">后端会按角色权限过滤菜单和业务资源，前端只负责展示状态。</div>
          </div>
        </div>
        <el-tabs>
          <el-tab-pane label="菜单权限">
            <div class="tree-toolbar">
              <el-button type="primary" :loading="saving" @click="saveMenu">保存菜单权限</el-button>
            </div>
            <el-tree
              ref="menuTreeRef"
              :data="menuTree"
              show-checkbox
              node-key="id"
              default-expand-all
              :props="treeProps"
            />
          </el-tab-pane>
          <el-tab-pane label="资源权限">
            <div class="tree-toolbar">
              <el-select v-model="resourceType" @change="changeResourceType">
                <el-option label="仪表盘" value="panel" />
                <el-option label="数据大屏" value="screen" />
                <el-option label="数据集" value="dataset" />
                <el-option label="数据源" value="datasource" />
              </el-select>
              <el-button type="primary" :loading="saving" @click="saveResource">保存资源权限</el-button>
            </div>
            <el-tree
              ref="resourceTreeRef"
              :data="resourceTree"
              show-checkbox
              node-key="id"
              default-expand-all
              :props="treeProps"
            />
          </el-tab-pane>
        </el-tabs>
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
  grid-template-columns: 280px 1fr;
  gap: 16px;
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
  border-radius: 10px;
  color: #334155;
  text-align: left;
  &.active {
    color: #0f172a;
    background: #eff6ff;
    border-color: #3b82f6;
  }
  small {
    color: #64748b;
  }
}
.panel-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.tree-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 14px;
}
</style>
