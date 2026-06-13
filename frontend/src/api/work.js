import request from '@/utils/request'

/**
 * 工作管理API
 */
export const workApi = {
  // 新增工作
  save(data) {
    return request({
      url: '/work',
      method: 'post',
      data
    })
  },
  // 更新工作
  update(id, data) {
    return request({
      url: `/work/${id}`,
      method: 'put',
      data
    })
  },
  // 删除工作
  delete(id) {
    return request({
      url: `/work/${id}`,
      method: 'delete'
    })
  },
  // 根据ID查询工作
  getById(id) {
    return request({
      url: `/work/${id}`,
      method: 'get'
    })
  },
  // 分页查询
  page(params) {
    return request({
      url: '/work/page',
      method: 'get',
      params
    })
  },
  // 报名工作
  signup(data) {
    return request({
      url: '/work/signup',
      method: 'post',
      data
    })
  },
  // 分页查询报名记录
  pageSignups(params) {
    return request({
      url: '/work/signup/page',
      method: 'get',
      params
    })
  },
  // 取消报名
  cancelSignup(id, userId) {
    return request({
      url: `/work/signup/${id}/cancel`,
      method: 'put',
      data: { userId }
    })
  }
}
