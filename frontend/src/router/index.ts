import { createRouter, createWebHistory } from 'vue-router'
import wsClient from '@/utils/websocket'

// @ts-ignore
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
    { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') },
    {
      path: '/',
      component: () => import('@/views/Layout.vue'),
      redirect: '/conversation',
      children: [
        { path: '/conversation', name: 'Conversation', component: () => import('@/views/Main.vue') },
        { path: '/contacts', name: 'Contacts', component: () => import('@/views/Contacts.vue') },
        { path: '/settings', name: 'Settings', component: () => import('@/views/Settings.vue') },
        { path: '/notification', name: 'Notification', component: () => import('@/views/Notification.vue') },
        // 管理后台
        { path: '/admin', redirect: '/admin/stats' },
        { path: '/admin/stats', name: 'AdminStats', component: () => import('@/views/AdminDashboard.vue'), meta: { adminTab: 'stats', requiresAdmin: true } },
        { path: '/admin/operations', name: 'AdminOperations', component: () => import('@/views/AdminDashboard.vue'), meta: { adminTab: 'operations', requiresAdmin: true } },
        { path: '/admin/users', name: 'AdminUsers', component: () => import('@/views/AdminDashboard.vue'), meta: { adminTab: 'users', requiresAdmin: true } },
        { path: '/admin/content', name: 'AdminContent', component: () => import('@/views/AdminDashboard.vue'), meta: { adminTab: 'content', requiresAdmin: true } },
        { path: '/admin/groups', name: 'AdminGroups', component: () => import('@/views/AdminDashboard.vue'), meta: { adminTab: 'groups', requiresAdmin: true } },
        { path: '/admin/robots', name: 'AdminRobots', component: () => import('@/views/AdminDashboard.vue'), meta: { adminTab: 'robots', requiresAdmin: true } },
        { path: '/admin/ai', name: 'AdminAI', component: () => import('@/views/AdminDashboard.vue'), meta: { adminTab: 'ai', requiresAdmin: true } },
        { path: '/admin/system', name: 'AdminSystem', component: () => import('@/views/AdminDashboard.vue'), meta: { adminTab: 'system', requiresAdmin: true } },
        // 其他
        { path: '/group/:id', name: 'GroupDetail', component: () => import('@/views/GroupDetail.vue') },
        { path: '/robot/:id/config', name: 'RobotConfig', component: () => import('@/views/RobotConfig.vue') }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('accessToken')
  if (!token && to.path !== '/login' && to.path !== '/register') {
    next('/login')
  } else if (to.meta.requiresAdmin && localStorage.getItem('isAdmin') !== 'true') {
    next('/conversation')
  } else {
    next()
  }
})

export default router
