<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import request from '@/config/axios'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref<any[]>([])
const pager = reactive({ currentPage: 1, pageSize: 15, total: 0 })

const filters = reactive({
  operationType: '',
  resourceType: '',
  operatorAccount: '',
  startTime: '',
  endTime: ''
})

const operationTypes = [
  { label: '全部', value: '' },
  { label: '登录', value: 'LOGIN' },
  { label: '创建', value: 'CREATE' },
  { label: '修改', value: 'MODIFY' },
  { label: '删除', value: 'DELETE' },
  { label: '导出', value: 'EXPORT' },
  { label: '下载', value: 'DOWNLOAD' }
]

const resourceTypes = [
  { label: '全部', value: '' },
  { label: '用户', value: 'USER' },
  { label: '数据源', value: 'DATASOURCE' },
  { label: '数据集', value: 'DATASET' },
  { label: '仪表板', value: 'PANEL' },
  { label: '数据大屏', value: 'SCREEN' }
]

const getOperationTypeTag = (type: string) => {
  const map: Record<string, string> = {
    LOGIN: 'primary',
    CREATE: 'success',
    MODIFY: 'warning',
    DELETE: 'danger',
    EXPORT: 'info',
    DOWNLOAD: 'info'
  }
  return map[type] || 'info'
}

const getOperationTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    LOGIN: '登录系统',
    CREATE: '新建',
    MODIFY: '编辑',
    DELETE: '删除',
    READ: '查看',
    EXPORT: '导出',
    DOWNLOAD: '下载'
  }
  return map[type] || type
}

const getResourceTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    USER: '用户',
    DATASOURCE: '数据源',
    DATASET: '数据集',
    PANEL: '仪表板',
    SCREEN: '数据大屏',
    VIEW: '图表',
    DATA: '数据'
  }
  return map[type] || type
}

const getOperationDesc = (row: any) => {
  const op = row.operation_type
  const res = row.resource_type
  const url = row.request_url || ''

  if (op === 'LOGIN') return '用户登录系统'
  if (op === 'CREATE') return `新建${getResourceTypeLabel(res)}`
  if (op === 'DELETE') return `删除${getResourceTypeLabel(res)}`
  if (op === 'MODIFY') {
    if (url.includes('resetPwd')) return '重置用户密码'
    if (url.includes('modifyPwd')) return '修改密码'
    if (url.includes('enable')) return '变更用户状态'
    if (url.includes('switchLanguage')) return '切换语言'
    return `编辑${getResourceTypeLabel(res)}`
  }
  if (op === 'READ') return `查看${getResourceTypeLabel(res)}`
  if (op === 'EXPORT') return `导出${getResourceTypeLabel(res)}`
  if (op === 'DOWNLOAD') return `下载${getResourceTypeLabel(res)}`
  return row.resource_name || '-'
}

const loadTable = async () => {
  loading.value = true
  try {
    const res = await request.post({
      url: `/auditLog/pager/${pager.currentPage}/${pager.pageSize}`,
      data: {
        operationType: filters.operationType || undefined,
        resourceType: filters.resourceType || undefined,
        operatorAccount: filters.operatorAccount || undefined,
        startTime: filters.startTime || undefined,
        endTime: filters.endTime || undefined
      }
    })
    tableData.value = res.data?.records || []
    pager.total = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.operationType = ''
  filters.resourceType = ''
  filters.operatorAccount = ''
  filters.startTime = ''
  filters.endTime = ''
  loadTable()
}

onMounted(loadTable)
</script>

<template>
  <div class="audit-log-manage">
    <p class="router-title">审计日志</p>
    <div class="table-wrap">
      <div class="toolbar">
        <el-select v-model="filters.operationType" placeholder="操作类型" clearable>
          <el-option v-for="item in operationTypes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="filters.resourceType" placeholder="资源类型" clearable>
          <el-option v-for="item in resourceTypes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-input v-model="filters.operatorAccount" placeholder="操作人" clearable />
        <el-date-picker v-model="filters.startTime" type="datetime" placeholder="开始时间" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" />
        <el-date-picker v-model="filters.endTime" type="datetime" placeholder="结束时间" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" />
        <el-button type="primary" @click="loadTable">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      <el-table v-loading="loading" :data="tableData" max-height="calc(100vh - 300px)">
        <el-table-column label="操作时间" width="170">
          <template #default="{ row }">
            {{ row.operation_time ? dayjs(row.operation_time).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operation_type)" size="small">
              {{ getOperationTypeLabel(row.operation_type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作描述" min-width="180">
          <template #default="{ row }">
            {{ getOperationDesc(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="operator_account" label="操作人" width="100" />
        <el-table-column prop="operator_ip" label="IP地址" width="130" />
        <el-table-column label="资源类型" width="100">
          <template #default="{ row }">
            {{ getResourceTypeLabel(row.resource_type) }}
          </template>
        </el-table-column>
        <el-table-column prop="resource_id" label="资源ID" width="120" show-overflow-tooltip />
        <el-table-column prop="request_url" label="请求地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.response_code === 200 ? 'success' : 'danger'" size="small">
              {{ row.response_code === 200 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="80">
          <template #default="{ row }">
            {{ row.duration ? row.duration + 'ms' : '-' }}
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="pager.currentPage"
          v-model:page-size="pager.pageSize"
          layout="total, sizes, prev, pager, next"
          :total="pager.total"
          :page-sizes="[15, 30, 50, 100]"
          @size-change="loadTable"
          @current-change="loadTable"
        />
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.audit-log-manage {
  min-height: 100%;
}
.router-title {
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}
.toolbar {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px 12px 0 0;
  .ed-input {
    width: 150px;
  }
  .ed-select {
    width: 120px;
  }
}
.table-wrap {
  height: auto;
  min-height: 0;
  margin-top: 12px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.pager {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px 16px;
  background: #fff;
}
</style>
