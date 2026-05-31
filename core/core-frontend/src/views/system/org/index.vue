<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import request from '@/config/axios'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import PlatformOrgTree from '../common/PlatformOrgTree.vue'

const loading = ref(false)
const treeData = ref<any[]>([])
const selectedOrg = ref<any>(null)
const selectedOrgId = ref<any>(1)
const dialogVisible = ref(false)
const isEdit = ref(false)
const orgTreeRef = ref()
const form = reactive<any>({ id: null, pid: 0, name: '' })

const loadTree = async () => {
  loading.value = true
  try {
    const res = await request.post({ url: '/org/page/tree', data: {} })
    treeData.value = res.data || []
    if (!selectedOrg.value) {
      selectedOrg.value = treeData.value[0] || null
      selectedOrgId.value = selectedOrg.value?.id || 1
    }
  } finally {
    loading.value = false
  }
}

const refresh = async () => {
  await Promise.all([loadTree(), orgTreeRef.value?.loadTree?.()])
}

const onOrgChange = (node: any) => {
  selectedOrg.value = node
}

const openCreate = (row?: any) => {
  const parent = row || selectedOrg.value
  Object.assign(form, { id: null, pid: parent?.id || 0, name: '' })
  isEdit.value = false
  dialogVisible.value = true
}

const openEdit = (row?: any) => {
  const target = row || selectedOrg.value
  if (!target) return
  Object.assign(form, { id: target.id, pid: target.pid || 0, name: target.name })
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
  await refresh()
}

const remove = async (row?: any) => {
  const target = row || selectedOrg.value
  if (!target) return
  await ElMessageBox.confirm(`确认删除「${target.name}」？`, '删除组织', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await request.post({ url: `/org/page/delete/${target.id}` })
  ElMessage.success('组织已删除')
  selectedOrg.value = null
  await refresh()
}

onMounted(loadTree)
</script>

<template>
  <div class="org-manage">
    <p class="router-title">组织管理</p>
    <div class="manage-layout">
      <PlatformOrgTree ref="orgTreeRef" v-model="selectedOrgId" @change="onOrgChange" />
      <section class="org-panel">
        <div class="panel-head">
          <div>
            <div class="panel-title">{{ selectedOrg?.name || '组织详情' }}</div>
            <div class="panel-desc">维护多级组织目录，用户和资源会按组织边界隔离。</div>
          </div>
          <div class="panel-actions">
            <el-button type="primary" @click="openCreate()">新建下级</el-button>
            <el-button :disabled="!selectedOrg || selectedOrg.readOnly" @click="openEdit()">编辑</el-button>
            <el-button
              type="danger"
              :disabled="!selectedOrg || selectedOrg.readOnly"
              @click="remove()"
              >删除</el-button
            >
          </div>
        </div>
        <div class="detail-grid">
          <div class="detail-item">
            <span>组织ID</span>
            <strong>{{ selectedOrg?.id || '-' }}</strong>
          </div>
          <div class="detail-item">
            <span>上级ID</span>
            <strong>{{ selectedOrg?.pid || 0 }}</strong>
          </div>
          <div class="detail-item">
            <span>状态</span>
            <strong>{{ selectedOrg?.readOnly ? '内置组织' : '可编辑' }}</strong>
          </div>
        </div>
        <el-table
          v-loading="loading"
          class="org-table"
          :data="treeData"
          row-key="id"
          default-expand-all
        >
          <el-table-column prop="name" label="目录结构" min-width="260" />
          <el-table-column prop="id" label="组织ID" width="180" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.readOnly ? 'info' : 'success'">{{
                row.readOnly ? '内置' : '可编辑'
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" @click="openCreate(row)">新建下级</el-button>
              <el-button text type="primary" :disabled="row.readOnly" @click="openEdit(row)"
                >编辑</el-button
              >
              <el-button text type="danger" :disabled="row.readOnly" @click="remove(row)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑组织' : '新建组织'" width="480px">
      <el-form label-position="top">
        <el-form-item v-if="!isEdit" label="上级组织">
          <el-input :model-value="form.pid || '根目录'" disabled />
        </el-form-item>
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
.manage-layout {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 16px;
}
.org-panel {
  min-width: 0;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 18px;
  border-bottom: 1px solid #e2e8f0;
}
.panel-title {
  color: #0f172a;
  font-size: 17px;
  font-weight: 700;
}
.panel-desc {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}
.panel-actions {
  display: flex;
  gap: 10px;
}
.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}
.detail-item {
  padding: 12px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  span {
    display: block;
    color: #64748b;
    font-size: 12px;
  }
  strong {
    display: block;
    margin-top: 6px;
    color: #0f172a;
    font-size: 16px;
  }
}
.org-table {
  border-top: 0;
}
</style>
