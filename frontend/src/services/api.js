const base = '/api'

async function request(path, options = {}) {
  const headers = options.headers ? { ...options.headers } : {}
  const response = await fetch(`${base}${path}`, {
    credentials: 'include',
    ...options,
    headers
  })
  const text = await response.text()
  let body
  try {
    body = text ? JSON.parse(text) : null
  } catch {
    body = { code: response.status, description: text || response.statusText }
  }
  if (!response.ok) {
    throw new Error(body?.description || body?.msg || response.statusText)
  }
  if (body && typeof body.code === 'number' && body.code !== 200) {
    throw new Error(body.description || body.msg || '操作失败')
  }
  return body?.data ?? body
}

function form(path, data) {
  return request(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
    body: new URLSearchParams(data).toString()
  })
}

function json(path, data, method = 'POST') {
  return request(path, {
    method,
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    body: JSON.stringify(data ?? {})
  })
}

export const api = {
  login: (account, password) => form('/user/login', { account, password }),
  register: (account, password, checkPassword) => form('/user/register', { account, password, checkPassword }),
  logout: () => request('/user/logout', { method: 'POST' }),
  current: () => request('/user/current'),
  updateProfile: data => json('/user/profile/update', data),
  userProfile: userId => request(`/user/${userId}/profile`),
  favorites: () => request('/user/getFav'),
  adminUsers: () => request('/user/admin/list'),
  banUser: userId => request(`/user/admin/ban/${userId}`, { method: 'POST' }),
  unbanUser: userId => request(`/user/admin/unban/${userId}`, { method: 'POST' }),

  blogs: (pageNo = 1, pageSize = 10) => request(`/blog/getBlog?pageNo=${pageNo}&pageSize=${pageSize}`),
  hot: (limit = 10) => request(`/hot/list?limit=${limit}`),
  hotByCategory: (category, limit = 10) => request(`/hot/category?category=${encodeURIComponent(category)}&limit=${limit}`),
  search: data => json('/blog/search', data),
  authorBlogs: (userId, pageNo = 1, pageSize = 10) => request(`/blog/author/${userId}?pageNo=${pageNo}&pageSize=${pageSize}`),
  addBlog: data => json('/blog/add', data),
  updateBlog: (blogId, data) => json(`/blog/${blogId}/update`, data),
  detail: blogId => request(`/blog/${blogId}`),
  deleteBlog: blogId => request(`/blog/${blogId}`, { method: 'POST' }),
  adminBlogs: (pageNo = 1, pageSize = 100) => request(`/blog/admin/list?pageNo=${pageNo}&pageSize=${pageSize}`),
  offlineBlog: blogId => request(`/blog/admin/${blogId}/offline`, { method: 'POST' }),
  publishBlog: blogId => request(`/blog/admin/${blogId}/publish`, { method: 'POST' }),
  status: blogId => request(`/blog/blog/${blogId}/status`),

  thumb: blogId => json('/thumb/do', { blogid: blogId }),
  unthumb: blogId => json('/thumb/undo', { blogid: blogId }),
  favorite: blogId => json('/favorite/do', { blogId }),
  unfavorite: blogId => json('/favorite/undo', { blogId }),
  share: (blogId, url, path = 'web') => json('/share/share', { blogId, url, path }),

  comments: (blogId, pageNo = 1, pageSize = 20) => request(`/comment/page?blogId=${blogId}&pageNo=${pageNo}&pageSize=${pageSize}`),
  addComment: (blogId, content) => json('/comment/add', { blogId, content }),
  replyComment: (blogId, parentId, content) => json('/comment/reply', { blogId, parentId, content }),
  deleteComment: commentId => request(`/comment/${commentId}`, { method: 'DELETE' }),

  unreadCount: () => request('/message/unread/count'),
  messages: (pageNo = 1, pageSize = 20) => request(`/message/page?pageNo=${pageNo}&pageSize=${pageSize}`),
  readMessage: messageId => request(`/message/read/${messageId}`, { method: 'POST' }),
  readAll: () => request('/message/readall', { method: 'POST' }),
  deleteMessage: messageId => request(`/message/${messageId}`, { method: 'DELETE' })
}
