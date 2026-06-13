<template>
  <div class="risk-page">
    <van-nav-bar title="风险与申诉" fixed>
      <template #left>
        <van-icon name="arrow-left" size="20" @click="$router.back()" />
      </template>
    </van-nav-bar>

    <div class="content pt-12 pb-4 px-3">
      <van-tabs v-model:active="activeTab" sticky offset-top="46px">
        <van-tab title="我的风险">
          <div class="tab-content">
            <van-pull-refresh v-model="recordRefreshing" @refresh="onRecordRefresh">
              <van-list
                v-model:loading="recordLoading"
                :finished="recordFinished"
                finished-text="没有更多了"
                @load="loadRecords"
              >
                <div
                  v-for="item in recordList"
                  :key="item.id"
                  class="risk-card mb-3 rounded-lg shadow-sm bg-white overflow-hidden"
                >
                  <div class="p-4">
                    <div class="flex items-center justify-between mb-2">
                      <div class="flex items-center gap-2">
                        <van-tag :type="getRiskTypeTagType(item.riskType)" size="medium">
                          {{ getRiskTypeText(item.riskType) }}
                        </van-tag>
                        <van-tag :type="getRiskLevelTagType(item.riskLevel)" size="medium">
                          {{ getRiskLevelText(item.riskLevel) }}
                        </van-tag>
                      </div>
                      <van-tag :type="getStatusTagType(item.status)" plain size="small">
                        {{ getStatusText(item.status) }}
                      </van-tag>
                    </div>
                    <p class="text-sm text-gray-700 mb-2">{{ item.description || '暂无详细描述' }}</p>
                    <div class="text-xs text-gray-400 mb-3">
                      记录时间：{{ formatDateTime(item.createTime) }}
                    </div>
                    <div v-if="item.nextAppealTime && item.status === 'rejected'" class="text-xs text-orange-500 mb-3">
                      下次可申诉时间：{{ formatDateTime(item.nextAppealTime) }}
                    </div>
                    <div class="flex gap-2">
                      <van-button
                        v-if="item.status === 'active' || item.status === 'rejected'"
                        size="small"
                        type="primary"
                        block
                        @click="handleAppeal(item)"
                      >
                        提交申诉
                      </van-button>
                      <van-button
                        v-if="item.status === 'appealing'"
                        size="small"
                        type="warning"
                        block
                        disabled
                      >
                        申诉审核中
                      </van-button>
                      <van-button
                        v-if="item.status === 'resolved'"
                        size="small"
                        type="success"
                        block
                        disabled
                      >
                        已解除
                      </van-button>
                    </div>
                  </div>
                </div>
                <div v-if="recordList.length === 0 && !recordLoading" class="text-center py-12 text-gray-400">
                  <van-icon name="shield-o" size="48" class="mb-2" />
                  <p>暂无风险记录</p>
                </div>
              </van-list>
            </van-pull-refresh>
          </div>
        </van-tab>

        <van-tab title="申诉记录">
          <div class="tab-content">
            <van-pull-refresh v-model="appealRefreshing" @refresh="onAppealRefresh">
              <van-list
                v-model:loading="appealLoading"
                :finished="appealFinished"
                finished-text="没有更多了"
                @load="loadAppeals"
              >
                <div
                  v-for="item in appealList"
                  :key="item.id"
                  class="appeal-card mb-3 rounded-lg shadow-sm bg-white overflow-hidden"
                >
                  <div class="p-4">
                    <div class="flex items-center justify-between mb-2">
                      <span class="text-sm font-semibold">申诉 #{item.id}</span>
                      <van-tag :type="getAppealStatusTagType(item.status)" size="medium">
                        {{ getAppealStatusText(item.status) }}
                      </van-tag>
                    </div>
                    <p class="text-sm text-gray-700 mb-2">申诉理由：{{ item.appealReason }}</p>
                    <div class="text-xs text-gray-400 mb-2">
                      提交时间：{{ formatDateTime(item.createTime) }}
                    </div>
                    <div v-if="item.enterpriseReviewComment" class="text-xs text-gray-600 mb-1">
                      企业意见：{{ item.enterpriseReviewComment }}
                    </div>
                    <div v-if="item.platformReviewComment" class="text-xs text-gray-600 mb-1">
                      平台意见：{{ item.platformReviewComment }}
                    </div>
                    <div v-if="item.finalComment" class="text-xs text-gray-700 bg-gray-50 p-2 rounded mt-2">
                      最终结论：{{ item.finalComment }}
                    </div>
                    <div v-if="item.nextAppealTime && item.status === 'rejected'" class="text-xs text-orange-500 mt-2">
                      下次可申诉时间：{{ formatDateTime(item.nextAppealTime) }}
                    </div>
                  </div>
                </div>
                <div v-if="appealList.length === 0 && !appealLoading" class="text-center py-12 text-gray-400">
                  <van-icon name="orders-o" size="48" class="mb-2" />
                  <p>暂无申诉记录</p>
                </div>
              </van-list>
            </van-pull-refresh>
          </div>
        </van-tab>
      </van-tabs>
    </div>

    <!-- 提交申诉弹窗 -->
    <van-popup
      v-model:show="appealDialogVisible"
      position="bottom"
      :style="{ height: '70%' }"
      round
      closeable
    >
      <div class="p-4 h-full flex flex-col">
        <h3 class="text-lg font-bold mb-4">提交申诉</h3>
        <div v-if="currentRiskRecord" class="bg-gray-50 rounded-lg p-3 mb-4">
          <div class="text-sm text-gray-600">
            <p>风险类型：{{ getRiskTypeText(currentRiskRecord.riskType) }}</p>
            <p>风险等级：{{ getRiskLevelText(currentRiskRecord.riskLevel) }}</p>
            <p>记录描述：{{ currentRiskRecord.description || '暂无' }}</p>
          </div>
        </div>
        <van-form @submit="handleSubmitAppeal" class="flex-1 overflow-y-auto">
          <van-cell-group inset>
            <van-field
              v-model="appealForm.appealReason"
              name="appealReason"
              label="申诉理由"
              type="textarea"
              rows="5"
              placeholder="请详细说明申诉理由..."
              :rules="[{ required: true, message: '请填写申诉理由' }]"
              autosize
            />
            <van-field
              v-model="appealForm.evidenceFiles"
              name="evidenceFiles"
              label="证明材料"
              placeholder="请输入证明材料文件ID（多个用逗号分隔）"
            />
          </van-cell-group>
          <div class="mt-4 flex gap-2">
            <van-button round block @click="appealDialogVisible = false">取消</van-button>
            <van-button round block type="primary" native-type="submit" :loading="submitting">
              提交申诉
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { showToast } from 'vant'
import { useRouter } from 'vue-router'
import { riskApi } from '@/api/risk'
import dayjs from 'dayjs'

const router = useRouter()
const activeTab = ref(0)

const recordLoading = ref(false)
const recordFinished = ref(false)
const recordRefreshing = ref(false)
const recordList = ref([])
const recordPagination = reactive({ current: 1, size: 10, total: 0 })

const appealLoading = ref(false)
const appealFinished = ref(false)
const appealRefreshing = ref(false)
const appealList = ref([])
const appealPagination = reactive({ current: 1, size: 10, total: 0 })

const appealDialogVisible = ref(false)
const currentRiskRecord = ref(null)
const submitting = ref(false)
const currentUserId = ref(1)

const appealForm = reactive({
  appealReason: '',
  evidenceFiles: ''
})

const loadRecords = async () => {
  if (recordRefreshing.value || recordList.value.length === 0) {
    recordPagination.current = 1
    if (recordRefreshing.value) {
      recordList.value = []
      recordFinished.value = false
    }
  }
  recordLoading.value = true
  try {
    const res = await riskApi.pageRecords({
      current: recordPagination.current,
      size: recordPagination.size,
      userId: currentUserId.value
    })
    if (res.code === 200) {
      if (recordRefreshing.value || recordPagination.current === 1) {
        recordList.value = res.data.records
        recordRefreshing.value = false
      } else {
        recordList.value.push(...res.data.records)
      }
      recordPagination.total = res.data.total
      if (recordList.value.length >= res.data.total) {
        recordFinished.value = true
      } else {
        recordPagination.current++
      }
    }
  } catch (error) {
    console.error(error)
    recordFinished.value = true
  } finally {
    recordLoading.value = false
  }
}

const onRecordRefresh = () => {
  recordFinished.value = false
  recordRefreshing.value = true
  loadRecords()
}

const loadAppeals = async () => {
  if (appealRefreshing.value || appealList.value.length === 0) {
    appealPagination.current = 1
    if (appealRefreshing.value) {
      appealList.value = []
      appealFinished.value = false
    }
  }
  appealLoading.value = true
  try {
    const res = await riskApi.pageAppeals({
      current: appealPagination.current,
      size: appealPagination.size,
      userId: currentUserId.value
    })
    if (res.code === 200) {
      if (appealRefreshing.value || appealPagination.current === 1) {
        appealList.value = res.data.records
        appealRefreshing.value = false
      } else {
        appealList.value.push(...res.data.records)
      }
      appealPagination.total = res.data.total
      if (appealList.value.length >= res.data.total) {
        appealFinished.value = true
      } else {
        appealPagination.current++
      }
    }
  } catch (error) {
    console.error(error)
    appealFinished.value = true
  } finally {
    appealLoading.value = false
  }
}

const onAppealRefresh = () => {
  appealFinished.value = false
  appealRefreshing.value = true
  loadAppeals()
}

const handleAppeal = async (item) => {
  const res = await riskApi.canSubmitAppeal({
    riskRecordId: item.id,
    userId: currentUserId.value
  })
  if (res.code === 200 && res.data.canSubmit) {
    currentRiskRecord.value = item
    appealForm.appealReason = ''
    appealForm.evidenceFiles = ''
    appealDialogVisible.value = true
  } else {
    const nextTime = res.data?.nextAppealTime
    if (nextTime) {
      showToast(`暂不可申诉，下次可申诉时间：${formatDateTime(nextTime)}`)
    } else {
      showToast('当前不可提交申诉')
    }
  }
}

const handleSubmitAppeal = async () => {
  if (!currentRiskRecord.value) return
  submitting.value = true
  try {
    const res = await riskApi.submitAppeal({
      riskRecordId: currentRiskRecord.value.id,
      userId: currentUserId.value,
      userName: '当前用户',
      appealReason: appealForm.appealReason,
      evidenceFiles: appealForm.evidenceFiles || null
    })
    if (res.code === 200) {
      showToast({ type: 'success', message: '申诉提交成功' })
      appealDialogVisible.value = false
      loadRecords()
      activeTab.value = 1
      loadAppeals()
    } else {
      showToast({ type: 'fail', message: res.message })
    }
  } catch (error) {
    console.error(error)
    showToast({ type: 'fail', message: '提交失败' })
  } finally {
    submitting.value = false
  }
}

const getRiskTypeText = (type) => {
  const texts = {
    miss_appointment: '爽约',
    site_conflict: '现场冲突',
    fake_cert: '证件造假',
    other: '其他'
  }
  return texts[type] || type
}

const getRiskTypeTagType = (type) => {
  const types = {
    miss_appointment: 'warning',
    site_conflict: 'danger',
    fake_cert: 'danger',
    other: 'primary'
  }
  return types[type] || 'default'
}

const getRiskLevelText = (level) => {
  const texts = {
    restricted: '受限',
    banned: '禁止'
  }
  return texts[level] || level
}

const getRiskLevelTagType = (level) => {
  const types = {
    restricted: 'warning',
    banned: 'danger'
  }
  return types[level] || 'default'
}

const getStatusText = (status) => {
  const texts = {
    active: '生效中',
    appealing: '申诉中',
    resolved: '已解除',
    rejected: '申诉驳回'
  }
  return texts[status] || status
}

const getStatusTagType = (status) => {
  const types = {
    active: 'danger',
    appealing: 'warning',
    resolved: 'success',
    rejected: 'default'
  }
  return types[status] || 'default'
}

const getAppealStatusText = (status) => {
  const texts = {
    pending: '待企业复核',
    reviewing: '待平台复核',
    approved: '申诉通过',
    rejected: '申诉驳回'
  }
  return texts[status] || status
}

const getAppealStatusTagType = (status) => {
  const types = {
    pending: 'warning',
    reviewing: 'primary',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'default'
}

const formatDateTime = (dt) => {
  if (!dt) return '-'
  return dayjs(dt).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.risk-page {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.risk-card,
.appeal-card {
  border: 1px solid #f0f0f0;
}
</style>
