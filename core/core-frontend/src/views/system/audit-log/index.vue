<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import request from '@/config/axios'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref<any[]>([])
const pager = reactive({ currentPage: 1, pageSize: 15, total: 0 })

// 筛选条件
const filters = reactive({
  operationType: '',
  resourceType: '',
  operatorAccount: '',
  startTime: '',
  endTime: ''
})

// 操作类型选项
const operationTypes = [
  { label: '全部', value: '' },
  { label: '登录', value: 'LOGIN' },
  { label: '创建', value: 'CREATE' },
  { label: '修改', value: 'MODIFY' },
  { label: '删除', value: 'DELETE' },
  { label: '导出', value: 'EXPORT' },
  { label: '下载', value: 'DOWNLOAD' }
]

// 资源类型选项
const resourceTypes = [
  { label: '全部', value: '' },
  { label: '用户', value: 'USER' },
  { label: '数据源', value: 'DATASOURCE' },
  { label: '数据集', value: 'DATASET' },
  { label: '仪表板', value: 'PANEL' },
  { label: '数据大屏', value: 'SCREEN' }
]

// 操作类型标签颜色
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

// 操作类型中文
const getOperationTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    LOGIN: '登录',
    CREATE: '创建',
    MODIFY: '修改',
    DELETE: '删除',
    EXPORT: '导出',
    DOWNLOAD: '下载',
    READ: '读取'
  }
  return map[type] || type
}

// 资源类型中文
const getResourceTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    USER: '用户',
    DATASOURCE: '数据源',
    DATASET: '数据集',
    PANEL: '仪表板',
    SCREEN: '数据大屏',
    VIEW: '图表',
    ROLE: '角色',
    ORG: '组织'
  }
  return map[type] || type
}

// 加载数据
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

// 重置筛选
const resetFilters = () => {
  filters.operationType = ''
  filters.resourceType = ''
  filters.operatorAccount = ''
  filters.startTime = ''
  filters.endTime = ''
  loadTable()
}

// 导出
const exportData = async () => {
  // TODO: 实现导出功能
}

onMounted(loadTable)
</script>

<template>
  <div class="audit-log-manage">
    <p class="router-title">审计日志</p>
    <div class="table-wrap">
      <!-- 筛选栏 -->
      <div class="toolbar">
        <div class="filter-group">
          <el-select
            v-model="filters.operationType"
            placeholder="操作类型"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="item in operationTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-select
            v-model="filters.resourceType"
            placeholder="资源类型"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="item in resourceTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-input
            v-model="filters.operatorAccount"
            placeholder="操作人账号"
            clearable
            style="width: 150px"
          />
          <el-date-picker
            v-model="filters.startTime"
            type="datetime"
            placeholder="开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 180px"
          />
          <el-date-picker
            v-model="filters.endTime"
            type="datetime"
            placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 180px"
          />
        </div>
        <div class="action-group">
          <el-button type="primary" @click="loadTable">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" max-height="calc(100vh - 340px)">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operation_type)" size="small">
              {{ getOperationTypeLabel(row.operation_type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="资源类型" width="100">
          <template #default="{ row }">
            {{ getResourceTypeLabel(row.resource_type) }}
          </template>
        </el-table-column>
        <el-table-column prop="resource_id" label="资源ID" width="120" show-overflow-tooltip />
        <el-table-column
          prop="resource_name"
          label="操作描述"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column prop="operator_account" label="操作人" width="120" />
        <el-table-column prop="operator_ip" label="IP地址" width="150" />
        <el-table-column prop="request_method" label="请求方法" width="80" />
        <el-table-column prop="request_url" label="请求URL" min-width="200" show-overflow-tooltip />
        <el-table-column label="响应状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.response_code === 200 ? 'success' : 'danger'" size="small">
              {{ row.response_code }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时(ms)" width="80" />
        <el-table-column label="操作时间" width="180">
          <template #default="{ row }">
            {{ row.operation_time ? dayjs(row.operation_time).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

<style scoped lang="less">
.audit-log-manage {
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;

  .router-title {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 16px;
    color: var(--deTextPrimary, #1f2329);
  }

  .table-wrap {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: var(--deCardBg, #fff);
    border-radius: 4px;
    padding: 16px;
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
    flex-wrap: wrap;
    gap: 8px;

    .filter-group {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    .action-group {
      display: flex;
      gap: 8px;
    }
  }

  .pager {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
