import request from '@/utils/request'

/**
 * 风险与申诉API
 */
export const riskApi = {
  // ==================== 风险记录 ====================
  pageRecords(params) {
    return request({
      url: '/risk/record/page',
      method: 'get',
      params
    })
  },
  getUserActiveRecords(userId) {
    return request({
      url: `/risk/record/user/${userId}`,
      method: 'get'
    })
  },
  getRecordById(id) {
    return request({
      url: `/risk/record/${id}`,
      method: 'get'
    })
  },
  addRecord(data) {
    return request({
      url: '/risk/record',
      method: 'post',
      data
    })
  },
  resolveRecord(id, data) {
    return request({
      url: `/risk/record/${id}/resolve`,
      method: 'put',
      data
    })
  },
  checkApplyForHighRisk(userId) {
    return request({
      url: `/risk/check-apply/${userId}`,
      method: 'get'
    })
  },

  // ==================== 申诉管理 ====================
  pageAppeals(params) {
    return request({
      url: '/risk/appeal/page',
      method: 'get',
      params
    })
  },
  getUserAppeals(userId) {
    return request({
      url: `/risk/appeal/user/${userId}`,
      method: 'get'
    })
  },
  getAppealById(id) {
    return request({
      url: `/risk/appeal/${id}`,
      method: 'get'
    })
  },
  submitAppeal(data) {
    return request({
      url: '/risk/appeal',
      method: 'post',
      data
    })
  },
  canSubmitAppeal(params) {
    return request({
      url: '/risk/appeal/can-submit',
      method: 'get',
      params
    })
  },
  enterpriseReview(id, data) {
    return request({
      url: `/risk/appeal/${id}/enterprise-review`,
      method: 'put',
      data
    })
  },
  platformReview(id, data) {
    return request({
      url: `/risk/appeal/${id}/platform-review`,
      method: 'put',
      data
    })
  }
}
