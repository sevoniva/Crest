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
  { label: '新建', value: 'CREATE' },
  { label: '编辑', value: 'MODIFY' },
  { label: '删除', value: 'DELETE' },
  { label: '查看', value: 'READ' },
  { label: '授权', value: 'AUTHORIZE' },
  { label: '取消授权', value: 'UNAUTHORIZE' },
  { label: '上传', value: 'UPLOADFILE' },
  { label: '绑定', value: 'BIND' },
  { label: '解绑', value: 'UNBIND' },
  { label: '导出', value: 'EXPORT' },
  { label: '下载', value: 'DOWNLOAD' },
  { label: '清理', value: 'CLEAR' }
]

const resourceTypes = [
  { label: '全部', value: '' },
  { label: '用户', value: 'USER' },
  { label: '角色', value: 'ROLE' },
  { label: '组织', value: 'ORG' },
  { label: '菜单权限', value: 'MENU' },
  { label: '数据', value: 'DATA' },
  { label: '数据源', value: 'DATASOURCE' },
  { label: '数据集', value: 'DATASET' },
  { label: '仪表盘', value: 'PANEL' },
  { label: '数据大屏', value: 'SCREEN' },
  { label: '图表', value: 'VIEW' },
  { label: '分享链接', value: 'LINK' },
  { label: '数据源驱动', value: 'DRIVER' },
  { label: '驱动文件', value: 'DRIVER_FILE' },
  { label: 'API Key', value: 'APIKEY' },
  { label: '数据填报', value: 'DATA_FILLING' },
  { label: '报告任务', value: 'REPORT_TASK' },
  { label: '同步数据源', value: 'SYNC_DATASOURCE' },
  { label: '同步任务', value: 'SYNC_TASK' },
  { label: '同步任务日志', value: 'SYNC_TASK_LOG' }
]

const getOperationTypeTag = (type: string) => {
  const map: Record<string, string> = {
    LOGIN: 'primary',
    CREATE: 'success',
    MODIFY: 'warning',
    DELETE: 'danger',
    READ: 'info',
    AUTHORIZE: 'primary',
    UNAUTHORIZE: 'warning',
    UPLOADFILE: 'success',
    BIND: 'success',
    UNBIND: 'warning',
    EXPORT: 'info',
    DOWNLOAD: 'info',
    CLEAR: 'danger'
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
    AUTHORIZE: '授权',
    UNAUTHORIZE: '取消授权',
    CREATELINK: '创建分享链接',
    DELETELINK: '删除分享链接',
    MODIFYLINK: '更新分享链接',
    UPLOADFILE: '上传',
    BIND: '绑定',
    UNBIND: '解绑',
    EXPORT: '导出',
    DOWNLOAD: '下载',
    TEMPLATE_EXPORT: '导出模板',
    APP_TEMPLATE_EXPORT: '导出应用模板',
    PDF_EXPORT: '导出 PDF',
    IMG_EXPORT: '导出图片',
    TASK_ENABLE: '启用任务',
    TASK_DISENABLE: '停用任务',
    TASK_RUN_IMMEDIATELY: '立即执行任务',
    SYNC_TASK_ENABLE: '启用同步任务',
    SYNC_TASK_DISENABLE: '停用同步任务',
    SYNC_TASK_RUN_IMMEDIATELY: '立即执行同步任务',
    SYNC_TASK_RUN_TERMINATION: '终止同步任务',
    CLEAR: '清理'
  }
  return map[type] || type
}

const getResourceTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    USER: '用户',
    ROLE: '角色',
    ORG: '组织',
    MENU: '菜单权限',
    DATASOURCE: '数据源',
    DATASET: '数据集',
    PANEL: '仪表盘',
    SCREEN: '数据大屏',
    VIEW: '图表',
    LINK: '分享链接',
    DRIVER: '数据源驱动',
    DRIVER_FILE: '驱动文件',
    APIKEY: 'API Key',
    DATA_FILLING: '数据填报',
    DATA: '数据',
    REPORT_TASK: '报告任务',
    SYNC_DATASOURCE: '同步数据源',
    SYNC_TASK: '同步任务',
    SYNC_TASK_LOG: '同步任务日志'
  }
  return map[type] || type
}

const getOperationDesc = (row: any) => {
  if (row.operation_desc) return row.operation_desc
  if (row.resource_name) return row.resource_name
  const op = row.operation_type
  const res = row.resource_type
  const url = row.request_url || ''

  if (url.includes('/auth/busiPermission')) return '查看业务资源权限'
  if (url.includes('/auth/menuPermission')) return '查看菜单权限'
  if (url.includes('/auth/saveBusiPer')) return '配置业务资源权限'
  if (url.includes('/auth/saveMenuPer')) return '配置菜单权限'
  if (url.includes('/role/byCurOrg') || url.includes('/role/query')) return '查询角色列表'
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
  if (op === 'AUTHORIZE') return `配置${getResourceTypeLabel(res)}`
  if (op === 'BIND') return `绑定${getResourceTypeLabel(res)}`
  if (op === 'UNBIND') return `解绑${getResourceTypeLabel(res)}`
  if (op === 'EXPORT') return `导出${getResourceTypeLabel(res)}`
  if (op === 'DOWNLOAD') return `下载${getResourceTypeLabel(res)}`
  return `${getOperationTypeLabel(op)}${getResourceTypeLabel(res)}`
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
