<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import request from '@/config/axios'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'

const loading = ref(false)
const treeData = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive<any>({ id: null, pid: 0, name: '' })

const loadTree = async () => {
  loading.value = true
  try {
    const res = await request.post({ url: '/org/page/tree', data: {} })
    treeData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const openCreate = (row?: any) => {
  Object.assign(form, { id: null, pid: row?.id || 0, name: '' })
  isEdit.value = false
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  Object.assign(form, { id: row.id, pid: row.pid || 0, name: row.name })
  isEdit.value = true
  dialogVisible.value = true
}

const save = async () => {
  if (!form.name?.trim()) {
    ElMessage.warning('组织名称不能为空')
    return
  }
  await request.post({ url: isEdit.value ? '/org/page/edit' : '/org/page/create', data: form })
  ElMessage.success(isEdit.value ? '组织已更新' : '组织已创建')
  dialogVisible.value = false
  await loadTree()
}

const remove = async (row: any) => {
  await ElMessageBox.confirm(`确认删除「${row.name}」？`, '删除组织', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await request.post({ url: `/org/page/delete/${row.id}` })
  ElMessage.success('组织已删除')
  await loadTree()
}

onMounted(loadTree)
</script>

<template>
  <div class="org-manage">
    <p class="router-title">组织管理</p>
    <div class="table-wrap">
      <div class="toolbar">
        <el-button type="primary" @click="openCreate()">新建组织</el-button>
      </div>
      <el-table v-loading="loading" :data="treeData" row-key="id" default-expand-all>
        <el-table-column prop="name" label="组织名称" min-width="240" />
        <el-table-column prop="id" label="组织ID" width="180" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.readOnly ? 'info' : 'success'">{{ row.readOnly ? '内置' : '可编辑' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openCreate(row)">新建下级</el-button>
            <el-button text type="primary" :disabled="row.readOnly" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger" :disabled="row.readOnly" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑组织' : '新建组织'" width="480px">
      <el-form label-position="top">
        <el-form-item label="组织名称" required>
          <el-input v-model.trim="form.name" maxlength="64" />
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
.org-manage {
  min-height: 100%;
}
.router-title {
  margin: 0 0 16px;
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
}
.table-wrap {
  margin-top: 12px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.toolbar {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fff;
}
</style>
