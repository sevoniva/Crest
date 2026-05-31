<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import request from '@/config/axios'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'

const loading = ref(false)
const keyword = ref('')
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive<any>({ id: null, name: '', desc: '', typeCode: 0 })

const loadTable = async () => {
  loading.value = true
  try {
    const res = await request.post({ url: '/role/byCurOrg', data: { keyword: keyword.value } })
    const rows = res.data || []
    tableData.value = keyword.value
      ? rows.filter(row => String(row.name || '').includes(keyword.value))
      : rows
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  Object.assign(form, { id: null, name: '', desc: '', typeCode: 0 })
  isEdit.value = false
  dialogVisible.value = true
}

const openEdit = async (row: any) => {
  const res = await request.get({ url: `/role/detail/${row.id}` })
  Object.assign(form, res.data || row)
  isEdit.value = true
  dialogVisible.value = true
}

const save = async () => {
  if (!form.name?.trim()) {
    ElMessage.warning('请输入角色名称')
    return
  }
  await request.post({ url: isEdit.value ? '/role/edit' : '/role/create', data: form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await loadTable()
}

const remove = async (row: any) => {
  await ElMessageBox.confirm(`删除角色「${row.name}」后不可恢复，确认删除？`, '删除角色', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await request.post({ url: `/role/delete/${row.id}` })
  ElMessage.success('删除成功')
  await loadTable()
}

const roleDesc = (row: any) => {
  if (row.desc) return row.desc
  if (String(row.id) === '3') return '用于审计与巡检，仅可查看授权范围内的资源'
  if (row.root) return '系统内置角色'
  return '自定义角色'
}

onMounted(loadTable)
</script>

<template>
  <div class="manage-page">
    <p class="router-title">角色管理</p>
    <section class="content-card">
      <div class="card-head">
        <div class="head-main">
          <div class="head-title">角色列表</div>
          <div class="head-desc">管理当前组织下的角色，只读角色不可编辑或删除</div>
        </div>
        <div class="head-actions">
          <el-input v-model="keyword" clearable placeholder="搜索角色名称" @change="loadTable" />
          <el-button type="primary" @click="loadTable">查询</el-button>
          <el-button type="primary" @click="openCreate">新建角色</el-button>
        </div>
      </div>
      <el-table
        v-loading="loading"
        class="manage-table"
        :data="tableData"
        max-height="calc(100vh - 280px)"
      >
        <el-table-column prop="name" label="角色名称" min-width="220" show-overflow-tooltip />
        <el-table-column label="类型" width="140">
          <template #default="{ row }">
            <el-tag :type="row.root ? 'warning' : 'info'">{{
              row.root ? '系统内置' : '自定义'
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="row.readonly ? 'info' : 'success'">{{
              row.readonly ? '只读' : '可配置'
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="说明" min-width="280">
          <template #default="{ row }">
            <span class="muted">{{ roleDesc(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" :disabled="row.readonly" @click="openEdit(row)"
              >编辑</el-button
            >
            <el-button text type="danger" :disabled="row.readonly" @click="remove(row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新建角色'" width="520px">
      <el-form label-position="top">
        <el-form-item label="角色名称" required>
          <el-input v-model.trim="form.name" maxlength="64" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model.trim="form.desc"
            type="textarea"
            maxlength="200"
            placeholder="选填，简述该角色的职责范围"
          />
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
@import '../common/manage.less';
.manage-layout {
  display: block;
}
.muted {
  color: #64748b;
  font-size: 13px;
}
.head-actions {
  .ed-input {
    width: 260px;
  }
}
.manage-table {
  padding: 8px 8px 12px;
}
</style>
