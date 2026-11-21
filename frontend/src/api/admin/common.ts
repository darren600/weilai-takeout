import { adminHttp } from '@/api/request'

/** 文件上传（阿里云 OSS，本轮仅保留接口调用入口） */
export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return adminHttp.post('/admin/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
