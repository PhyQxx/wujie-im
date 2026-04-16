import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
    { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') },
    {
      path: '/',
      component: () => import('@/views/Main.vue'),
      redirect: '/conversation',
      children: [
        { path: '/conversation', name: 'Conversation', component: () => import('@/views/Main.vue') },
        { path: '/contacts', name: 'Contacts', component: () => import('@/views/Contacts.vue') },
        { path: '/robots', name: 'MyRobots', component: () => import('@/views/MyRobots.vue') },
        { path: '/settings', name: 'Settings', component: () => import('@/views/Settings.vue') },
        { path: '/admin', name: 'AdminDashboard', component: () => import('@/views/AdminDashboard.vue') }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('accessToken')
  if (!token && to.path !== '/login' && to.path !== '/register') {
    next('/login')
  } else {
    next()
  }
})

export default router
