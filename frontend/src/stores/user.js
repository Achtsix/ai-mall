import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),
  actions: {
    setLogin(data) {
      this.token = data.token
      this.userInfo = data.user
      this.role = data.user?.role || 'USER'
      localStorage.setItem('token', data.token)
      localStorage.setItem('role', this.role)
      localStorage.setItem('userInfo', JSON.stringify(data.user || {}))
    },
    updateUserInfo(data) {
      this.userInfo = { ...(this.userInfo || {}), ...data }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },
    logout() {
      this.token = ''
      this.role = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('userInfo')
    }
  }
})
