import request from '@/config/axios'

export function getDatasourceRelationship(id) {
  return request.post({ url: `/relation/datasource/${id}` }).then(res => {
    return res?.data
  })
}

export function getDatasetRelationship(id) {
  return request.post({ url: `/relation/dataset/${id}` }).then(res => {
    return res?.data
  })
}

export function getPanelRelationship(id) {
  return request.post({ url: `/relation/dv/${id}` }).then(res => {
    return res?.data
  })
}

export function getRelationshipOverview() {
  return request.post({ url: '/relation/overview' }).then(res => {
    return res?.data
  })
}

export function listRelationResources(type = 'all', data = {}) {
  return request.post({ url: `/relation/resources/${type}`, data }).then(res => {
    return res?.data
  })
}

export function resourceCheckPermission(id) {
  return request.post({
    url: `/resource/checkPermission/${id}`
  })
}
