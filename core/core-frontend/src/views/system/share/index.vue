<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import request from '@/config/axios'
import dayjs from 'dayjs'
import useClipboard from 'vue-clipboard3'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'

const { toClipboard } = useClipboard()
const loading = ref(false)
const keyword = ref('')
const type = ref('')
const tableData = ref<any[]>([])
const pager = reactive({ currentPage: 1, pageSize: 15, total: 0 })
const dialogVisible = ref(false)
const current = reactive<any>({})

const typeMap = {
  dashboard: '仪表板',
  panel: '仪表板',
  dataV: '数据大屏',
  screen: '数据大屏'
}

const shareLink = computed(() => {
  if (!current.uuid) return ''
  return `${window.location.origin}${window.location.pathname}#/de-link/${current.uuid}`
})

const formatTime = val => {
  if (!val) return '长期有效'
  return dayjs(Number(val)).format('YYYY-MM-DD HH:mm:ss')
}

const loadTable = async () => {
  loading.value = true
  try {
    const queryType = type.value === 'dashboard' ? 'panel' : type.value === 'dataV' ? 'screen' : ''
    const res = await request.post({
      url: `/share/pager/${pager.currentPage}/${pager.pageSize}`,
      data: { keyword: keyword.value, type: queryType, asc: false }
    })
    tableData.value = res.data?.records || []
    pager.total = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

const openDetail = async row => {
  const res = await request.get({ url: `/share/detail/${row.resourceId}` })
  Object.assign(current, row, res.data || {})
  dialogVisible.value = true
}

const saveExp = async () => {
  await request.post({
    url: '/share/editExp',
    data: { resourceId: current.resourceId, exp: current.exp || 0 }
  })
  ElMessage.success('有效期已更新')
  await loadTable()
}

const savePwd = async () => {
  await request.post({
    url: '/share/editPwd',
    data: { resourceId: current.resourceId, pwd: current.pwd || '', autoPwd: false }
  })
  ElMessage.success('访问密码已更新')
  await loadTable()
}

const disableShare = async row => {
  await ElMessageBox.confirm(`确认关闭「${row.name}」的公开分享？`, '关闭分享', {
    confirmButtonText: '关闭',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await request.post({ url: `/share/switcher/${row.resourceId}` })
  ElMessage.success('分享已关闭')
  await loadTable()
}

const copyLink = async () => {
  await toClipboard(current.pwd ? `${shareLink.value},${current.pwd}` : shareLink.value)
  ElMessage.success('链接已复制')
}

onMounted(loadTable)
</script>

<template>
  <div class="share-manage">
    <p class="router-title">分享管理</p>
    <div class="table-wrap">
      <div class="toolbar">
        <el-select v-model="type" clearable placeholder="全部类型" @change="loadTable">
          <el-option label="仪表板" value="dashboard" />
          <el-option label="数据大屏" value="dataV" />
        </el-select>
        <el-input v-model="keyword" clearable placeholder="搜索名称" @change="loadTable" />
        <el-button type="primary" @click="loadTable">查询</el-button>
      </div>
      <el-table v-loading="loading" :data="tableData" height="calc(100vh - 280px)">
        <el-table-column prop="name" label="资源名称" min-width="220" show-overflow-tooltip />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">{{ typeMap[row.type] || row.type }}</template>
        </el-table-column>
        <el-table-column prop="creator" label="创建人" width="130" />
        <el-table-column label="分享时间" width="180">
          <template #default="{ row }">{{ formatTime(row.time) }}</template>
        </el-table-column>
        <el-table-column label="有效期" width="180">
          <template #default="{ row }">{{ formatTime(row.exp) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.extFlag1 ? 'success' : 'info'">{{ row.extFlag1 ? '可访问' : '资源异常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openDetail(row)">设置</el-button>
            <el-button text type="danger" @click="disableShare(row)">关闭</el-button>
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
    <el-dialog v-model="dialogVisible" title="分享设置" width="560px">
      <el-form label-position="top">
        <el-form-item label="公开链接">
          <el-input :model-value="shareLink" readonly>
            <template #append>
              <el-button @click="copyLink">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker
            v-model="current.exp"
            type="datetime"
            value-format="x"
            clearable
            placeholder="不设置则长期有效"
          />
          <el-button class="inline-btn" @click="saveExp">保存有效期</el-button>
        </el-form-item>
        <el-form-item label="访问密码">
          <el-input v-model="current.pwd" clearable placeholder="留空表示不启用密码" />
          <el-button class="inline-btn" @click="savePwd">保存密码</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<style lang="less" scoped>
.share-manage {
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
  .ed-select {
    width: 140px;
  }
  .ed-input {
    width: 260px;
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
.inline-btn {
  margin-left: 12px;
}
</style>
