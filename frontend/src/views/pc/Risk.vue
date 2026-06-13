<template>
  <div class="risk-admin-page p-6">
    <el-tabs v-model="activeTab" class="mb-4">
      <el-tab-pane label="风险记录" name="records">
        <div class="mb-4 flex gap-3 items-center flex-wrap">
          <el-input
            v-model="recordSearch.userId"
            placeholder="用户ID"
            style="width: 150px"
            clearable
          />
          <el-select
            v-model="recordSearch.riskType"
            placeholder="风险类型"
            style="width: 150px"
            clearable
          >
            <el-option label="爽约" value="miss_appointment" />
            <el-option label="现场冲突" value="site_conflict" />
            <el-option label="证件造假" value="fake_cert" />
            <el-option label="其他" value="other" />
          </el-select>
          <el-select
            v-model="recordSearch.status"
            placeholder="状态"
            style="width: 150px"
            clearable
          >
            <el-option label="生效中" value="active" />
            <el-option label="申诉中" value="appealing" />
            <el-option label="已解除" value="resolved" />
            <el-option label="申诉驳回" value="rejected" />
          </el-select>
          <el-button type="primary" @click="searchRecords">查询</el-button>
          <el-button @click="resetRecordSearch">重置</el-button>
          <el-button type="success" @click="showAddRecordDialog = true">添加风险记录</el-button>
        </div>

        <el-table :data="recordTableData" border stripe v-loading="recordLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="userId" label="用户ID" width="100" />
          <el-table-column prop="userName" label="用户姓名" width="120" />
          <el-table-column prop="riskType" label="风险类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getRiskTypeTagType(row.riskType)">
                {{ getRiskTypeText(row.riskType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="riskLevel" label="风险等级" width="100">
            <template #default="{ row }">
              <el-tag :type="getRiskLevelTagType(row.riskLevel)">
                {{ getRiskLevelText(row.riskLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column prop="nextAppealTime" label="下次可申诉" width="170" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'active' || row.status === 'appealing' || row.status === 'rejected'"
                type="success"
                size="small"
                @click="handleResolveRecord(row)"
              >
                解除限制
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="mt-4"
          v-model:current-page="recordPagination.current"
          v-model:page-size="recordPagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="recordPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadRecords"
          @current-change="loadRecords"
        />
      </el-tab-pane>

      <el-tab-pane label="申诉管理" name="appeals">
        <div class="mb-4 flex gap-3 items-center flex-wrap">
          <el-input
            v-model="appealSearch.userId"
            placeholder="用户ID"
            style="width: 150px"
            clearable
          />
          <el-select
            v-model="appealSearch.status"
            placeholder="申诉状态"
            style="width: 180px"
            clearable
          >
            <el-option label="待企业复核" value="pending" />
            <el-option label="待平台复核" value="reviewing" />
            <el-option label="申诉通过" value="approved" />
            <el-option label="申诉驳回" value="rejected" />
          </el-select>
          <el-button type="primary" @click="searchAppeals">查询</el-button>
          <el-button @click="resetAppealSearch">重置</el-button>
        </div>

        <el-table :data="appealTableData" border stripe v-loading="appealLoading">
          <el-table-column prop="id" label="申诉ID" width="100" />
          <el-table-column prop="riskRecordId" label="风险记录ID" width="120" />
          <el-table-column prop="userId" label="用户ID" width="100" />
          <el-table-column prop="userName" label="申诉人" width="120" />
          <el-table-column prop="appealReason" label="申诉理由" min-width="200" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getAppealStatusTagType(row.status)">
                {{ getAppealStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="enterpriseReviewComment" label="企业意见" min-width="150" show-overflow-tooltip />
          <el-table-column prop="platformReviewComment" label="平台意见" min-width="150" show-overflow-tooltip />
          <el-table-column prop="finalComment" label="最终结论" min-width="150" show-overflow-tooltip />
          <el-table-column prop="createTime" label="提交时间" width="170" />
          <el-table-column prop="nextAppealTime" label="下次可申诉" width="170" />
          <el-table-column label="操作" width="320" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'pending'"
                type="primary"
                size="small"
                @click="handleEnterpriseReview(row, true)"
              >
                企业通过
              </el-button>
              <el-button
                v-if="row.status === 'pending'"
                type="danger"
                size="small"
                @click="handleEnterpriseReview(row, false)"
              >
                企业驳回
              </el-button>
              <el-button
                v-if="row.status === 'reviewing'"
                type="success"
                size="small"
                @click="handlePlatformReview(row, true)"
              >
                平台通过
              </el-button>
              <el-button
                v-if="row.status === 'reviewing'"
                type="warning"
                size="small"
                @click="handlePlatformReview(row, false)"
              >
                平台驳回
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="mt-4"
          v-model:current-page="appealPagination.current"
          v-model:page-size="appealPagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="appealPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadAppeals"
          @current-change="loadAppeals"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 添加风险记录弹窗 -->
    <el-dialog v-model="showAddRecordDialog" title="添加风险记录" width="500px">
      <el-form :model="addRecordForm" label-width="100px" ref="addRecordFormRef">
        <el-form-item label="用户ID" prop="userId" :rules="[{ required: true, message: '请输入用户ID' }]">
          <el-input v-model.number="addRecordForm.userId" type="number" />
        </el-form-item>
        <el-form-item label="用户姓名">
          <el-input v-model="addRecordForm.userName" />
        </el-form-item>
        <el-form-item label="风险类型" prop="riskType" :rules="[{ required: true, message: '请选择风险类型' }]">
          <el-select v-model="addRecordForm.riskType" style="width: 100%">
            <el-option label="爽约" value="miss_appointment" />
            <el-option label="现场冲突" value="site_conflict" />
            <el-option label="证件造假" value="fake_cert" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级" prop="riskLevel" :rules="[{ required: true, message: '请选择风险等级' }]">
          <el-select v-model="addRecordForm.riskLevel" style="width: 100%">
            <el-option label="受限" value="restricted" />
            <el-option label="禁止" value="banned" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="addRecordForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddRecordDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAddRecord">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 复核弹窗 -->
    <el-dialog v-model="showReviewDialog" :title="reviewDialogTitle" width="500px">
      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="复核意见" :rules="[{ required: true, message: '请填写复核意见' }]">
          <el-input v-model="reviewForm.comment" type="textarea" :rows="3" placeholder="请填写复核意见" />
        </el-form-item>
        <el-form-item v-if="reviewForm.needNextAppealTime" label="下次可申诉时间">
          <el-date-picker
            v-model="reviewForm.nextAppealTime"
            type="datetime"
            placeholder="选择下次可申诉时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button :type="reviewForm.isApproved ? 'success' : 'danger'" @click="submitReview">
          {{ reviewForm.isApproved ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { riskApi } from '@/api/risk'
import dayjs from 'dayjs'

const activeTab = ref('records')
const operatorId = ref(1)
const operatorName = ref('管理员')

// ==================== 风险记录 ====================
const recordLoading = ref(false)
const recordTableData = ref([])
const recordPagination = reactive({ current: 1, size: 10, total: 0 })
const recordSearch = reactive({ userId: null, riskType: '', status: '' })
const showAddRecordDialog = ref(false)
const addRecordFormRef = ref(null)
const addRecordForm = reactive({
  userId: null,
  userName: '',
  riskType: '',
  riskLevel: 'restricted',
  description: ''
})

const loadRecords = async () => {
  recordLoading.value = true
  try {
    const res = await riskApi.pageRecords({
      current: recordPagination.current,
      size: recordPagination.size,
      ...recordSearch
    })
    if (res.code === 200) {
      recordTableData.value = res.data.records
      recordPagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    recordLoading.value = false
  }
}

const searchRecords = () => {
  recordPagination.current = 1
  loadRecords()
}

const resetRecordSearch = () => {
  recordSearch.userId = null
  recordSearch.riskType = ''
  recordSearch.status = ''
  searchRecords()
}

const submitAddRecord = async () => {
  if (!addRecordForm.userId || !addRecordForm.riskType || !addRecordForm.riskLevel) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    const res = await riskApi.addRecord({
      ...addRecordForm,
      reportUserId: operatorId.value,
      reportUserName: operatorName.value
    })
    if (res.code === 200) {
      ElMessage.success('添加成功')
      showAddRecordDialog.value = false
      Object.assign(addRecordForm, { userId: null, userName: '', riskType: '', riskLevel: 'restricted', description: '' })
      loadRecords()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('添加失败')
  }
}

const handleResolveRecord = async (row) => {
  try {
    await ElMessageBox.confirm('确认解除该用户的风险限制吗？', '提示', { type: 'warning' })
    const res = await riskApi.resolveRecord(row.id, {
      operatorId: operatorId.value,
      operatorName: operatorName.value
    })
    if (res.code === 200) {
      ElMessage.success('解除成功')
      loadRecords()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// ==================== 申诉管理 ====================
const appealLoading = ref(false)
const appealTableData = ref([])
const appealPagination = reactive({ current: 1, size: 10, total: 0 })
const appealSearch = reactive({ userId: null, status: '' })

const showReviewDialog = ref(false)
const reviewDialogTitle = ref('')
const currentReviewAppeal = ref(null)
const reviewType = ref('')
const reviewForm = reactive({
  comment: '',
  isApproved: false,
  needNextAppealTime: false,
  nextAppealTime: null
})

const loadAppeals = async () => {
  appealLoading.value = true
  try {
    const res = await riskApi.pageAppeals({
      current: appealPagination.current,
      size: appealPagination.size,
      ...appealSearch
    })
    if (res.code === 200) {
      appealTableData.value = res.data.records
      appealPagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    appealLoading.value = false
  }
}

const searchAppeals = () => {
  appealPagination.current = 1
  loadAppeals()
}

const resetAppealSearch = () => {
  appealSearch.userId = null
  appealSearch.status = ''
  searchAppeals()
}

const handleEnterpriseReview = (row, approved) => {
  currentReviewAppeal.value = row
  reviewType.value = 'enterprise'
  reviewDialogTitle.value = approved ? '企业复核通过' : '企业复核驳回'
  reviewForm.comment = ''
  reviewForm.isApproved = approved
  reviewForm.needNextAppealTime = !approved
  reviewForm.nextAppealTime = !approved ? dayjs().add(7, 'day').toDate() : null
  showReviewDialog.value = true
}

const handlePlatformReview = (row, approved) => {
  currentReviewAppeal.value = row
  reviewType.value = 'platform'
  reviewDialogTitle.value = approved ? '平台复核通过' : '平台复核驳回'
  reviewForm.comment = ''
  reviewForm.isApproved = approved
  reviewForm.needNextAppealTime = !approved
  reviewForm.nextAppealTime = !approved ? dayjs().add(7, 'day').toDate() : null
  showReviewDialog.value = true
}

const submitReview = async () => {
  if (!reviewForm.comment) {
    ElMessage.warning('请填写复核意见')
    return
  }
  if (reviewForm.needNextAppealTime && !reviewForm.nextAppealTime) {
    ElMessage.warning('请选择下次可申诉时间')
    return
  }
  try {
    let res
    if (reviewType.value === 'enterprise') {
      res = await riskApi.enterpriseReview(currentReviewAppeal.value.id, {
        reviewerId: operatorId.value,
        reviewerName: operatorName.value,
        comment: reviewForm.comment,
        approved: reviewForm.isApproved
      })
    } else {
      res = await riskApi.platformReview(currentReviewAppeal.value.id, {
        reviewerId: operatorId.value,
        reviewerName: operatorName.value,
        comment: reviewForm.comment,
        approved: reviewForm.isApproved,
        nextAppealTime: reviewForm.nextAppealTime
          ? dayjs(reviewForm.nextAppealTime).format('YYYY-MM-DD HH:mm:ss')
          : null
      })
    }
    if (res.code === 200) {
      ElMessage.success('复核成功')
      showReviewDialog.value = false
      loadAppeals()
      loadRecords()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('复核失败')
  }
}

// ==================== 工具方法 ====================
const getRiskTypeText = (type) => {
  const texts = { miss_appointment: '爽约', site_conflict: '现场冲突', fake_cert: '证件造假', other: '其他' }
  return texts[type] || type
}
const getRiskTypeTagType = (type) => {
  const types = { miss_appointment: 'warning', site_conflict: 'danger', fake_cert: 'danger', other: '' }
  return types[type] || 'info'
}
const getRiskLevelText = (level) => {
  const texts = { restricted: '受限', banned: '禁止' }
  return texts[level] || level
}
const getRiskLevelTagType = (level) => {
  const types = { restricted: 'warning', banned: 'danger' }
  return types[level] || 'info'
}
const getStatusText = (status) => {
  const texts = { active: '生效中', appealing: '申诉中', resolved: '已解除', rejected: '申诉驳回' }
  return texts[status] || status
}
const getStatusTagType = (status) => {
  const types = { active: 'danger', appealing: 'warning', resolved: 'success', rejected: 'info' }
  return types[status] || ''
}
const getAppealStatusText = (status) => {
  const texts = { pending: '待企业复核', reviewing: '待平台复核', approved: '申诉通过', rejected: '申诉驳回' }
  return texts[status] || status
}
const getAppealStatusTagType = (status) => {
  const types = { pending: 'warning', reviewing: '', approved: 'success', rejected: 'danger' }
  return types[status] || 'info'
}

onMounted(() => {
  loadRecords()
  loadAppeals()
})
</script>

<style scoped>
.risk-admin-page {
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}
</style>
