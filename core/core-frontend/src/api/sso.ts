import request from '@/config/axios'

export const ssoConfigApi = () => request.get({ url: '/sso/config' })

export const saveSsoConfigApi = data => request.post({ url: '/sso/config', data })

export const validateSsoConfigApi = data => request.post({ url: '/sso/validate', data })
