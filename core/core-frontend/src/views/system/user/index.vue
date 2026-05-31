<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import request from '@/config/axios'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import dayjs from 'dayjs'
import PlatformOrgTree from '../common/PlatformOrgTree.vue'

const loading = ref(false)
const keyword = ref('')
const tableData = ref<any[]>([])
const selectedRows = ref<any[]>([])
const pager = reactive({ currentPage: 1, pageSize: 15, total: 0 })
const dialogVisible = ref(false)
const isEdit = ref(false)
const roleOptions = ref<any[]>([])
const orgOptions = ref<any[]>([])
const selectedOrgId = ref<any>(null)
const selectedOrgName = ref('全部组织')
const form = reactive<any>({
  id: null,
  oid: null,
  account: '',
  name: '',
  email: '',
  phone: '',
  enable: true,
  roleIds: [2],
  authType: 'LOCAL'
})

const orgTreeRef = ref()
const orgDialogVisible = ref(false)
const orgIsEdit = ref(false)
const orgForm = reactive<any>({ id: null, pid: 0, name: '', parentName: '根目录' })

const isSsoUser = row => String(row?.authType || '').toUpperCase() === 'SSO'
const authTypeLabel = row => (isSsoUser(row) ? '单点登录' : '本地账号')
const authTypeTag = row => (isSsoUser(row) ? 'success' : 'info')

const loadTable = async () => {
  loading.value = true
  try {
    const res = await request.post({
      url: `/user/pager/${pager.currentPage}/${pager.pageSize}`,
      data: { keyword: keyword.value, oid: selectedOrgId.value, timeDesc: true }
    })
    tableData.value = res.data?.records || []
    pager.total = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  const res = selectedOrgId.value
    ? await request.get({ url: `/role/queryWithOid/${selectedOrgId.value}` })
    : await request.post({ url: '/role/byCurOrg', data: {} })
  roleOptions.value = res.data || []
}

const loadOrgOptions = async () => {
  const res = await request.post({ url: '/org/page/tree', data: {} })
  orgOptions.value = res.data || []
}

const onOrgChange = async node => {
  selectedOrgName.value = node?.name || '全部组织'
  pager.currentPage = 1
  await Promise.all([loadTable(), loadRoles()])
}

const onFormOrgChange = async () => {
  const res = form.oid
    ? await request.get({ url: `/role/queryWithOid/${form.oid}` })
    : await request.post({ url: '/role/byCurOrg', data: {} })
  roleOptions.value = res.data || []
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    oid: selectedOrgId.value,
    account: '',
    name: '',
    email: '',
    phone: '',
    enable: true,
    roleIds: [2],
    authType: 'LOCAL'
  })
}

const openCreate = () => {
  resetForm()
  onFormOrgChange()
  isEdit.value = false
  dialogVisible.value = true
}

const openEdit = async row => {
  const res = await request.get({ url: `/user/queryById/${row.id}` })
  Object.assign(form, res.data || row)
  form.oid = res.data?.oid || row.oid || selectedOrgId.value
  form.roleIds = (res.data?.roleIds || row.roleItems?.map(role => String(role.id)) || ['2']).map(
    Number
  )
  await onFormOrgChange()
  isEdit.value = true
  dialogVisible.value = true
}

const save = async () => {
  if (!form.account?.trim() || !form.name?.trim()) {
    ElMessage.warning('账号和姓名不能为空')
    return
  }
  if (!form.oid) {
    ElMessage.warning('请选择所属组织')
    return
  }
  await request.post({ url: isEdit.value ? '/user/edit' : '/user/create', data: form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await loadTable()
}

const toggleEnable = async row => {
  await request.post({ url: '/user/enable', data: { id: row.id, enable: !row.enable } })
  ElMessage.success(!row.enable ? '用户已启用' : '用户已停用')
  await loadTable()
}

const resetPwd = async row => {
  await ElMessageBox.confirm(`确认重置「${row.name}」的本地密码？`, '重置密码', {
    confirmButtonText: '重置',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await request.post({ url: `/user/resetPwd/${row.id}` })
  ElMessage.success('密码已重置')
}

const remove = async row => {
  await ElMessageBox.confirm(`确认删除「${row.name}」？`, '删除用户', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await request.post({ url: `/user/delete/${row.id}` })
  ElMessage.success('删除成功')
  await loadTable()
}

const batchRemove = async () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请选择用户')
    return
  }
  await ElMessageBox.confirm(`确认删除已选择的 ${selectedRows.value.length} 个用户？`, '批量删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await request.post({ url: '/user/batchDel', data: selectedRows.value.map(row => row.id) })
  ElMessage.success('删除成功')
  selectedRows.value = []
  await loadTable()
}

const downloadTemplate = async () => {
  const res: any = await request.post({ url: '/user/excelTemplate', responseType: 'blob' })
  const blob = new Blob([res.data], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'user-import-template.xlsx'
  link.click()
  URL.revokeObjectURL(url)
}

const importUsers = async options => {
  const data = new FormData()
  data.append('file', options.file)
  const res = await request.post({
    url: '/user/batchImport',
    headersType: 'multipart/form-data',
    data
  })
  ElMessage.success(
    `导入完成：成功 ${res.data?.successCount || 0}，失败 ${res.data?.errorCount || 0}`
  )
  await loadTable()
}

const openOrgCreate = (parent: any) => {
  Object.assign(orgForm, {
    id: null,
    pid: parent?.id || 0,
    name: '',
    parentName: parent?.name || '根目录'
  })
  orgIsEdit.value = false
  orgDialogVisible.value = true
}

const openOrgEdit = (node: any) => {
  Object.assign(orgForm, {
    id: node.id,
    pid: node.pid || 0,
    name: node.name,
    parentName: node.name
  })
  orgIsEdit.value = true
  orgDialogVisible.value = true
}

const saveOrg = async () => {
  if (!orgForm.name?.trim()) {
    ElMessage.warning('请输入组织名称')
    return
  }
  await request.post({
    url: orgIsEdit.value ? '/org/page/edit' : '/org/page/create',
    data: { id: orgForm.id, pid: orgForm.pid, name: orgForm.name }
  })
  ElMessage.success('保存成功')
  orgDialogVisible.value = false
  await Promise.all([orgTreeRef.value?.loadTree?.(), loadOrgOptions()])
}

const removeOrg = async (node: any) => {
  await ElMessageBox.confirm(`删除组织「${node.name}」后不可恢复，确认删除？`, '删除组织', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await request.post({ url: `/org/page/delete/${node.id}` })
  ElMessage.success('删除成功')
  if (String(node.id) === String(selectedOrgId.value)) {
    selectedOrgId.value = null
    selectedOrgName.value = '全部组织'
  }
  await Promise.all([orgTreeRef.value?.loadTree?.(), loadOrgOptions(), loadTable()])
}

onMounted(async () => {
  await Promise.all([loadTable(), loadRoles(), loadOrgOptions()])
})
</script>

<template>
  <div class="manage-page">
    <p class="router-title">用户管理</p>
    <div class="manage-layout">
      <PlatformOrgTree
        ref="orgTreeRef"
        v-model="selectedOrgId"
        title="组织架构"
        selectable-all
        manageable
        collapsible
        @change="onOrgChange"
        @create="openOrgCreate"
        @edit="openOrgEdit"
        @delete="removeOrg"
      />
      <section class="content-card">
        <div class="card-head">
          <div class="head-main">
            <div class="head-title">{{ selectedOrgName }}</div>
            <div class="head-desc">查看并维护所选组织下的用户、角色与账号状态</div>
          </div>
          <div class="head-actions">
            <el-input
              v-model="keyword"
              clearable
              placeholder="搜索账号、姓名或邮箱"
              @change="loadTable"
            />
            <el-button type="primary" @click="loadTable">查询</el-button>
            <el-button type="primary" @click="openCreate">新建用户</el-button>
            <el-button :disabled="!selectedRows.length" @click="batchRemove">批量删除</el-button>
            <el-button @click="downloadTemplate">下载模板</el-button>
            <el-upload :show-file-list="false" :http-request="importUsers" accept=".xlsx,.xls,.csv">
              <el-button>批量导入</el-button>
            </el-upload>
          </div>
        </div>
        <el-table
          v-loading="loading"
          class="manage-table"
          :data="tableData"
          max-height="calc(100vh - 320px)"
          @selection-change="selectedRows = $event"
        >
          <el-table-column type="selection" width="48" />
          <el-table-column prop="account" label="账号" min-width="130" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="account-cell">
                <span class="account-name">{{ row.account }}</span>
                <el-tag v-if="isSsoUser(row)" size="small" type="success" disable-transitions
                  >SSO</el-tag
                >
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" min-width="110" show-overflow-tooltip />
          <el-table-column prop="orgName" label="所属组织" min-width="120" show-overflow-tooltip />
          <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
          <el-table-column label="角色" min-width="160">
            <template #default="{ row }">
              <div class="role-tags">
                <el-tag v-for="role in row.roleItems || []" :key="role.id" size="small" type="info">
                  {{ role.name }}
                </el-tag>
                <span v-if="!(row.roleItems || []).length" class="muted">-</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <span class="status-dot" :class="{ on: row.enable }"></span>
              <span>{{ row.enable ? '启用' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最近登录" width="160">
            <template #default="{ row }">
              <span class="muted">{{
                row.lastLoginTime
                  ? dayjs(Number(row.lastLoginTime)).format('YYYY-MM-DD HH:mm')
                  : '从未登录'
              }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button text type="primary" @click="toggleEnable(row)">{{
                row.enable ? '停用' : '启用'
              }}</el-button>
              <el-dropdown trigger="click">
                <el-button text type="primary" class="more-btn">更多</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="!isSsoUser(row)" @click="resetPwd(row)"
                      >重置密码</el-dropdown-item
                    >
                    <el-dropdown-item
                      v-if="String(row.id) !== '1'"
                      class="danger-item"
                      @click="remove(row)"
                      >删除</el-dropdown-item
                    >
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager">
          <el-pagination
            v-model:current-page="pager.currentPage"
            v-model:page-size="pager.pageSize"
            layout="total, sizes, prev, pager, next"
            :total="pager.total"
            @size-change="loadTable"
            @current-change="loadTable"
          />
        </div>
      </section>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新建用户'" width="560px">
      <el-form label-position="top">
        <el-form-item label="所属组织" required>
          <el-tree-select
            v-model="form.oid"
            :data="orgOptions"
            node-key="id"
            check-strictly
            filterable
            default-expand-all
            :props="{ children: 'children', label: 'name' }"
            placeholder="请选择所属组织"
            @change="onFormOrgChange"
          />
        </el-form-item>
        <el-form-item label="账号" required>
          <el-input
            v-model.trim="form.account"
            :disabled="isEdit && (String(form.id) === '1' || isSsoUser(form))"
            maxlength="64"
          />
        </el-form-item>
        <el-form-item v-if="isEdit" label="认证来源">
          <el-tag :type="authTypeTag(form)">{{ authTypeLabel(form) }}</el-tag>
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model.trim="form.name" maxlength="64" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model.trim="form.email" maxlength="120" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model.trim="form.phone" maxlength="32" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="form.roleIds"
            multiple
            class="full-width"
            :disabled="String(form.id) === '1'"
            placeholder="请选择角色"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.name"
              :value="Number(role.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enable" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="orgDialogVisible"
      :title="orgIsEdit ? '重命名组织' : '新建组织'"
      width="480px"
    >
      <el-form label-position="top">
        <el-form-item v-if="!orgIsEdit" label="上级组织">
          <el-input :model-value="orgForm.parentName" disabled />
        </el-form-item>
        <el-form-item label="组织名称" required>
          <el-input v-model.trim="orgForm.name" maxlength="64" placeholder="请输入组织名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orgDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveOrg">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="less" scoped>
@import '../common/manage.less';
.head-actions {
  flex: 1 1 620px;
  min-width: min(100%, 620px);
  .ed-input {
    flex: 1 1 240px;
    max-width: 320px;
  }
}
.manage-table {
  padding: 8px 8px 0;
}
.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.account-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  .account-name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 6px;
  vertical-align: middle;
  background: #cbd5e1;
  border-radius: 50%;
  &.on {
    background: #22c55e;
  }
}
.muted {
  color: #94a3b8;
}
.more-btn {
  padding-left: 4px;
}
:deep(.danger-item) {
  color: #dc2626;
}
.pager {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px;
}
</style>
