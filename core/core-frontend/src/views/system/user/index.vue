<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import request from '@/config/axios'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import dayjs from 'dayjs'

const loading = ref(false)
const keyword = ref('')
const tableData = ref<any[]>([])
const pager = reactive({ currentPage: 1, pageSize: 15, total: 0 })
const dialogVisible = ref(false)
const isEdit = ref(false)
const roleId = ref(2)
const form = reactive<any>({
  id: null,
  account: '',
  name: '',
  email: '',
  phone: '',
  enable: true,
  roleIds: [2]
})

const loadTable = async () => {
  loading.value = true
  try {
    const res = await request.post({
      url: `/user/pager/${pager.currentPage}/${pager.pageSize}`,
      data: { keyword: keyword.value, timeDesc: true }
    })
    tableData.value = res.data?.records || []
    pager.total = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, { id: null, account: '', name: '', email: '', phone: '', enable: true, roleIds: [2] })
  roleId.value = 2
}

const openCreate = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

const openEdit = async row => {
  const res = await request.get({ url: `/user/queryById/${row.id}` })
  Object.assign(form, res.data || row)
  form.roleIds = (res.data?.roleIds || row.roleItems?.map(role => String(role.id)) || ['2']).map(Number)
  roleId.value = form.roleIds.some(item => Number(item) === 1) ? 1 : 2
  isEdit.value = true
  dialogVisible.value = true
}

const save = async () => {
  if (!form.account?.trim() || !form.name?.trim()) {
    ElMessage.warning('账号和姓名不能为空')
    return
  }
  form.roleIds = [roleId.value]
  await request.post({ url: isEdit.value ? '/user/edit' : '/user/create', data: form })
  ElMessage.success(isEdit.value ? '用户已更新' : '用户已创建，默认密码为 admin')
  dialogVisible.value = false
  await loadTable()
}

const toggleEnable = async row => {
  await request.post({ url: '/user/enable', data: { id: row.id, enable: !row.enable } })
  ElMessage.success(!row.enable ? '用户已启用' : '用户已停用')
  await loadTable()
}

const resetPwd = async row => {
  await ElMessageBox.confirm(`确认将「${row.name}」的密码重置为 admin？`, '重置密码', {
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
  ElMessage.success('用户已删除')
  await loadTable()
}

onMounted(loadTable)
</script>

<template>
  <div class="user-manage">
    <p class="router-title">用户管理</p>
    <div class="table-wrap">
      <div class="toolbar">
        <el-input v-model="keyword" clearable placeholder="搜索账号、姓名或邮箱" @change="loadTable" />
        <el-button type="primary" @click="loadTable">查询</el-button>
        <el-button type="primary" @click="openCreate">新建用户</el-button>
      </div>
      <el-table v-loading="loading" :data="tableData" height="calc(100vh - 280px)">
        <el-table-column prop="account" label="账号" min-width="140" />
        <el-table-column prop="name" label="姓名" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" min-width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enable ? 'success' : 'info'">{{ row.enable ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.roleItems?.some(role => String(role.id) === '1') ? 'warning' : 'info'">
              {{ row.roleItems?.some(role => String(role.id) === '1') ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ row.createTime ? dayjs(Number(row.createTime)).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button text type="primary" @click="toggleEnable(row)">{{ row.enable ? '停用' : '启用' }}</el-button>
            <el-button text type="primary" @click="resetPwd(row)">重置密码</el-button>
            <el-button v-if="String(row.id) !== '1'" text type="danger" @click="remove(row)">删除</el-button>
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
    </div>
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新建用户'" width="520px">
      <el-form label-position="top">
        <el-form-item label="账号" required>
          <el-input v-model.trim="form.account" :disabled="isEdit && String(form.id) === '1'" maxlength="64" />
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
          <el-radio-group v-model="roleId" :disabled="String(form.id) === '1'">
            <el-radio-button :label="1">管理员</el-radio-button>
            <el-radio-button :label="2">普通用户</el-radio-button>
          </el-radio-group>
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
  </div>
</template>

<style lang="less" scoped>
.user-manage {
  height: 100%;
}
.router-title {
  margin: 0 0 16px;
  font-size: 20px;
  font-weight: 500;
  color: #1f2329;
}
.toolbar {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px 12px 0 0;
  .ed-input {
    width: 280px;
  }
}
.table-wrap {
  height: calc(100vh - 176px);
  margin-top: 12px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}
.pager {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px 16px;
  background: #fff;
}
</style>
