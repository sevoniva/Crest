<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import request from '@/config/axios'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import PlatformOrgTree from '../common/PlatformOrgTree.vue'

const loading = ref(false)
const keyword = ref('')
const selectedOrgId = ref<any>(1)
const selectedOrgName = ref('默认组织')
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive<any>({ id: null, name: '', desc: '', typeCode: 0 })

const loadTable = async () => {
  loading.value = true
  try {
    const res = selectedOrgId.value
      ? await request.get({ url: `/role/queryWithOid/${selectedOrgId.value}` })
      : await request.post({ url: '/role/byCurOrg', data: { keyword: keyword.value } })
    const rows = res.data || []
    tableData.value = keyword.value
      ? rows.filter(row => String(row.name || '').includes(keyword.value))
      : rows
  } finally {
    loading.value = false
  }
}

const onOrgChange = async node => {
  selectedOrgName.value = node?.name || '默认组织'
  await loadTable()
}

const openCreate = () => {
  Object.assign(form, { id: null, oid: selectedOrgId.value, name: '', desc: '', typeCode: 0 })
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
    ElMessage.warning('角色名称不能为空')
    return
  }
  await request.post({ url: isEdit.value ? '/role/edit' : '/role/create', data: form })
  ElMessage.success(isEdit.value ? '角色已更新' : '角色已创建')
  dialogVisible.value = false
  await loadTable()
}

const remove = async (row: any) => {
  await ElMessageBox.confirm(`确认删除「${row.name}」？`, '删除角色', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await request.post({ url: `/role/delete/${row.id}` })
  ElMessage.success('角色已删除')
  await loadTable()
}

onMounted(loadTable)
</script>

<template>
  <div class="role-manage">
    <p class="router-title">角色管理</p>
    <div class="manage-layout">
      <PlatformOrgTree v-model="selectedOrgId" @change="onOrgChange" />
      <section class="table-wrap">
        <div class="toolbar">
          <div>
            <div class="toolbar-title">{{ selectedOrgName }}</div>
            <div class="toolbar-sub">配置组织内角色，审计只读角色仅用于巡检查看</div>
          </div>
          <div class="toolbar-actions">
            <el-input v-model="keyword" clearable placeholder="搜索角色名称" @change="loadTable" />
            <el-button type="primary" @click="loadTable">查询</el-button>
            <el-button type="primary" @click="openCreate">新建角色</el-button>
          </div>
        </div>
        <el-table v-loading="loading" :data="tableData" max-height="calc(100vh - 342px)">
          <el-table-column prop="name" label="角色名称" min-width="220" />
          <el-table-column label="类型" width="140">
            <template #default="{ row }">
              <el-tag :type="row.root ? 'warning' : 'info'">{{
                row.root ? '系统内置' : '自定义'
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="配置状态" width="130">
            <template #default="{ row }">
              <el-tag :type="row.readonly ? 'info' : 'success'">{{
                row.readonly ? '只读角色' : '可配置'
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="说明" min-width="260">
            <template #default="{ row }">
              <span class="muted">
                {{
                  String(row.id) === '3'
                    ? '审计和巡检场景使用，只允许查看授权范围内资源'
                    : row.root
                      ? '系统基础角色'
                      : '业务自定义角色'
                }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
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
    </div>
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新建角色'" width="520px">
      <el-form label-position="top">
        <el-form-item label="角色名称" required>
          <el-input v-model.trim="form.name" maxlength="64" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model.trim="form.desc" type="textarea" maxlength="200" />
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
.role-manage {
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
.table-wrap {
  min-width: 0;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.toolbar {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #e2e8f0;
}
.toolbar-title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}
.toolbar-sub,
.muted {
  color: #64748b;
  font-size: 12px;
}
.toolbar-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  .ed-input {
    width: 260px;
  }
}
</style>
