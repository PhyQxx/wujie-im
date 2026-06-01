<template>
  <div class="admin-page" :key="activeTab">
    <div class="admin-header">
      <h2>{{ tabTitle }}</h2>
    </div>

    <!-- 数据看板 -->
    <div v-if="activeTab === 'stats'" class="stats-section">
      <div class="stat-cards">
        <div class="stat-card dark" v-for="stat in stats" :key="stat.label">
          <div class="stat-icon" :style="{ background: stat.iconBg }">
            <svg v-if="stat.key === 'totalUsers'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
            <svg v-else-if="stat.key === 'onlineUsers'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
            <svg v-else-if="stat.key === 'requestsPerMinute'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path></svg>
          </div>
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>

      <div class="monitor-grid">
        <div class="monitor-card">
          <div class="monitor-header">
            <h3>JVM 内存状态</h3>
            <el-tag size="small" type="success">运行中</el-tag>
          </div>
          <div class="mem-chart-wrap">
            <el-progress type="dashboard" :percentage="Math.round((deepStats.jvmHeapUsed / deepStats.jvmHeapMax) * 100) || 0" :color="colors">
              <template #default="{ percentage }">
                <span class="percentage-value">{{ percentage }}%</span>
                <span class="percentage-label">已用堆内存</span>
              </template>
            </el-progress>
            <div class="mem-info">
              <div class="mem-item"><span>已使用:</span> <strong>{{ deepStats.jvmHeapUsed }} MB</strong></div>
              <div class="mem-item"><span>最大可用:</span> <strong>{{ deepStats.jvmHeapMax }} MB</strong></div>
            </div>
          </div>
        </div>

        <div class="monitor-card">
          <div class="monitor-header">
            <h3>Redis & 运行时长</h3>
          </div>
          <div class="extra-stats">
            <div class="extra-item">
              <div class="extra-label">Redis 命中率 / 客户端</div>
              <div class="extra-value">
                {{ deepStats.redisHitRate }}% <span class="divider">/</span> {{ deepStats.redisClients }} <span class="unit">连接</span>
              </div>
            </div>
            <div class="extra-item">
              <div class="extra-label">系统运行时长</div>
              <div class="extra-value">{{ formatUptime(deepStats.uptime) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 运营数据 -->
    <div v-if="activeTab === 'operations'" class="section">
      <div class="stat-cards" style="grid-template-columns: repeat(4, 1fr); margin-bottom: 20px;">
        <div class="stat-card dark" v-for="op in operationStats" :key="op.label">
          <div class="stat-icon" :style="{ background: op.iconBg }">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>
          </div>
          <div class="stat-value">{{ op.value }}</div>
          <div class="stat-label">{{ op.label }}</div>
        </div>
      </div>
      <div class="section-header">
        <h3>运营概览</h3>
      </div>
      <div class="ops-grid">
        <div class="ops-card">
          <div class="ops-card-title">消息活跃度</div>
          <div class="ops-card-value">{{ stats.find(s => s.key === 'todayMessages')?.value || 0 }} <span>条/今日</span></div>
        </div>
        <div class="ops-card">
          <div class="ops-card-title">新增用户</div>
          <div class="ops-card-value">{{ stats.find(s => s.key === 'todayUsers')?.value || 0 }} <span>人/今日</span></div>
        </div>
        <div class="ops-card">
          <div class="ops-card-title">活跃群组</div>
          <div class="ops-card-value">{{ stats.find(s => s.key === 'totalGroups')?.value || 0 }} <span>个</span></div>
        </div>
        <div class="ops-card">
          <div class="ops-card-title">机器人</div>
          <div class="ops-card-value">{{ stats.find(s => s.key === 'totalRobots')?.value || 0 }} <span>个</span></div>
        </div>
      </div>
    </div>

    <!-- 用户管理 -->
    <div v-if="activeTab === 'users'" class="section">
      <div class="section-header">
        <h3>用户管理</h3>
        <div style="display:flex;gap:8px;">
          <el-button type="primary" size="small" @click="openCreateUserDialog">创建用户</el-button>
          <el-button type="danger" size="small" :disabled="!selectedUserIds.length" @click="batchDeleteUsers">批量删除 ({{ selectedUserIds.length }})</el-button>
          <el-input v-model="userSearch" placeholder="搜索用户/手机号" style="width:200px" clearable @change="loadUsers" />
        </div>
      </div>
      <el-table :data="users" stripe v-loading="loading" @selection-change="handleUserSelectionChange">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="createTime" label="注册时间" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" @click="openEditUserDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleUserStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="resetPassword(row.id)">重置密码</el-button>
            <el-button size="small" type="danger" @click="deleteUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="showCreateUserDialog" title="创建用户" width="400px">
        <el-form :model="createUserForm" label-width="80px">
          <el-form-item label="用户名">
            <el-input v-model="createUserForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="createUserForm.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="createUserForm.email" placeholder="可选" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateUserDialog = false">取消</el-button>
          <el-button type="primary" @click="createUser">创建</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="showEditUserDialog" title="编辑用户" width="420px">
        <el-form :model="editUserForm" label-width="80px">
          <el-form-item label="用户名">
            <el-input v-model="editUserForm.username" />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="editUserForm.nickname" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="editUserForm.email" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="editUserForm.phone" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showEditUserDialog = false">取消</el-button>
          <el-button type="primary" @click="saveEditUser">保存</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- 内容审核 -->
    <div v-if="activeTab === 'content'" class="section">
      <div class="section-header">
        <h3>敏感词管理</h3>
        <el-button type="primary" size="small" @click="showAddWord = true">添加敏感词</el-button>
      </div>
      <div class="word-tags">
        <el-tag v-for="w in sensitiveWords" :key="w.id" closable @close="deleteWord(w.id)">
          {{ w.word }}
        </el-tag>
        <span v-if="!sensitiveWords.length" class="empty-text">暂无敏感词</span>
      </div>
      <el-dialog v-model="showAddWord" title="添加敏感词" width="360px">
        <el-input v-model="newWord" placeholder="输入敏感词" />
        <template #footer>
          <el-button @click="showAddWord = false">取消</el-button>
          <el-button type="primary" @click="addWord">添加</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- 群组管理 -->
    <div v-if="activeTab === 'groups'" class="section">
      <div class="section-header">
        <h3>群组管理</h3>
        <div style="display:flex;gap:8px;">
          <el-button type="primary" size="small" @click="showCreateGroupDialog = true">创建群组</el-button>
          <el-input v-model="groupSearch" placeholder="搜索群名称" style="width:200px" clearable @change="loadGroups" />
        </div>
      </div>
      <el-table :data="groups" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="群名称" />
        <el-table-column prop="ownerId" label="群主ID" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="viewGroupMessages(row)">查看消息</el-button>
            <el-button size="small" type="danger" @click="dissolveGroup(row)">解散</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="showCreateGroupDialog" title="创建群组" width="420px">
        <el-form :model="createGroupForm" label-width="80px">
          <el-form-item label="群名称">
            <el-input v-model="createGroupForm.name" placeholder="请输入群名称" />
          </el-form-item>
          <el-form-item label="群类型">
            <el-select v-model="createGroupForm.type" style="width:100%">
              <el-option label="公开群" value="PUBLIC" />
              <el-option label="私密群" value="PRIVATE" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateGroupDialog = false">取消</el-button>
          <el-button type="primary" @click="createGroup">创建</el-button>
        </template>
      </el-dialog>

      <!-- 群消息查看 -->
      <el-dialog v-model="showGroupMessages" :title="'群消息 - ' + (selectedGroup?.name || '')" width="640px" destroy-on-close>
        <div class="msg-list" v-loading="msgLoading">
          <div v-for="msg in groupMessages" :key="msg.id" class="msg-item">
            <span class="msg-sender">用户{{ msg.senderId }}</span>
            <span class="msg-type">[{{ msg.contentType }}]</span>
            <span class="msg-content">{{ msg.content }}</span>
            <span class="msg-time">{{ msg.createTime }}</span>
          </div>
          <div v-if="!groupMessages.length" class="empty-text">暂无消息</div>
        </div>
        <template #footer>
          <el-button size="small" @click="loadMoreMessages" :disabled="!hasMoreMessages">加载更多</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- 机器人管理 -->
    <div v-if="activeTab === 'robots'" class="section">
      <div class="section-header">
        <h3>机器人管理</h3>
        <div style="display:flex;gap:8px;">
          <el-button type="primary" size="small" @click="showCreateRobotDialog = true">新增机器人</el-button>
          <el-input v-model="robotSearch" placeholder="搜索机器人" style="width:200px" clearable @change="loadRobots" />
        </div>
      </div>
      <el-table :data="robots" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '运行中' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="创建人" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="editRobot(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteRobot(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="showCreateRobotDialog" title="新增机器人" width="420px">
        <el-form :model="createRobotForm" label-width="80px">
          <el-form-item label="名称">
            <el-input v-model="createRobotForm.name" placeholder="请输入机器人名称" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="createRobotForm.type" style="width:100%">
              <el-option label="AI 机器人" value="AI" />
              <el-option label="通知机器人" value="NOTIFICATION" />
              <el-option label="工具机器人" value="TOOL" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateRobotDialog = false">取消</el-button>
          <el-button type="primary" @click="createRobot">创建</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="showEditRobotDialog" title="编辑机器人" width="480px">
        <el-form :model="editRobotForm" label-width="90px">
          <el-form-item label="名称">
            <el-input v-model="editRobotForm.name" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editRobotForm.description" type="textarea" :rows="2" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="editRobotForm.status" style="width:100%">
              <el-option label="启用" value="ACTIVE" />
              <el-option label="停用" value="INACTIVE" />
            </el-select>
          </el-form-item>
          <el-form-item label="响应模式">
            <el-select v-model="editRobotForm.responseMode" style="width:100%">
              <el-option label="仅@回复" value="MENTION" />
              <el-option label="所有消息" value="ALL" />
              <el-option label="延迟回复" value="DELAYED" />
            </el-select>
          </el-form-item>
          <el-form-item label="上下文长度">
            <el-input-number v-model="editRobotForm.contextSize" :min="1" :max="100" />
          </el-form-item>
          <el-form-item label="AI配置">
            <el-select v-model="editRobotForm.aiConfigId" clearable placeholder="选择AI配置" style="width:100%">
              <el-option v-for="cfg in aiConfigs" :key="cfg.id" :label="cfg.name || cfg.provider + ' - ' + cfg.model" :value="cfg.id" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showEditRobotDialog = false">取消</el-button>
          <el-button type="primary" @click="saveEditRobot">保存</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- AI配置 -->
    <div v-if="activeTab === 'ai'" class="section">
      <div class="section-header">
        <h3>AI模型配置</h3>
        <el-button type="primary" size="small" @click="openAiConfigDialog()">添加配置</el-button>
      </div>
      <el-table :data="aiConfigs" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="provider" label="提供商" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.provider }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="模型" />
        <el-table-column prop="temperature" label="温度" width="80" />
        <el-table-column prop="maxTokens" label="最大Token" width="100" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openAiConfigDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="copyAiConfig(row)">复制</el-button>
            <el-button size="small" type="danger" @click="deleteAiConfig(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="showAiDialog" title="AI配置" width="480px">
        <el-form :model="aiForm" label-width="90px">
          <el-form-item label="配置名称">
            <el-input v-model="aiForm.name" placeholder="如：通用客服模型" />
          </el-form-item>
          <el-form-item label="提供商">
            <el-select v-model="aiForm.provider" style="width:100%">
              <el-option label="MiniMax" value="MINIMAX" />
              <el-option label="GLM" value="GLM" />
              <el-option label="DeepSeek" value="DEEPSEEK" />
              <el-option label="自定义 (OpenAI兼容)" value="CUSTOM" />
            </el-select>
          </el-form-item>
          <el-form-item label="模型">
            <el-input v-model="aiForm.model" placeholder="如：glm-4-flash, deepseek-chat" />
          </el-form-item>
          <el-form-item label="API Key">
            <el-input v-model="aiForm.apiKey" type="password" show-password />
          </el-form-item>
          <el-form-item label="自定义URL">
            <el-input v-model="aiForm.apiUrl" placeholder="留空使用默认地址" />
          </el-form-item>
          <el-form-item label="系统提示词">
            <el-input v-model="aiForm.systemPrompt" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="温度">
            <el-input-number v-model="aiForm.temperature" :min="0" :max="2" :step="0.1" />
          </el-form-item>
          <el-form-item label="最大Token">
            <el-input-number v-model="aiForm.maxTokens" :min="1" :max="32000" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showAiDialog = false">取消</el-button>
          <el-button type="primary" @click="saveAiConfig">保存</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- 系统配置 -->
    <div v-if="activeTab === 'system'" class="section">
      <div class="section-header">
        <h3>系统配置</h3>
        <el-button size="small" @click="loadSystemConfigs">刷新</el-button>
      </div>
      <div class="config-list">
        <div v-for="cfg in systemConfigs" :key="cfg.id" class="config-item">
          <div class="config-info">
            <div class="config-key">{{ cfg.configKey }}</div>
            <div class="config-desc">{{ cfg.description }}</div>
          </div>
          <div class="config-value">
            <el-switch
              v-if="cfg.configValue === 'true' || cfg.configValue === 'false'"
              :model-value="cfg.configValue === 'true'"
              @change="(v: boolean) => updateSystemConfig(cfg.configKey, String(v))"
              active-text="开启" inactive-text="关闭"
            />
            <el-input v-else v-model="cfg.configValue" style="width:200px" size="small">
              <template #append>
                <el-button @click="updateSystemConfig(cfg.configKey, cfg.configValue)">保存</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
      <div v-if="!systemConfigs.length" class="empty-text">暂无配置</div>
    </div>

    <!-- 公告管理 -->
    <div v-if="activeTab === 'announcements'" class="section">
      <div class="section-header">
        <h3>公告管理</h3>
        <el-button type="primary" size="small" @click="showCreateAnnouncement = true">发布公告</el-button>
      </div>
      <el-table :data="announcementList" stripe style="width:100%">
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'IMPORTANT' ? 'danger' : 'info'" size="small">{{ row.type === 'IMPORTANT' ? '重要' : '系统' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170">
          <template #default="{ row }">{{ row.publishTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editAnnouncement(row)">编辑</el-button>
            <el-button v-if="row.status === 0" size="small" type="success" @click="publishAnnouncement(row.id)">发布</el-button>
            <el-button v-else size="small" type="warning" @click="unpublishAnnouncement(row.id)">撤回</el-button>
            <el-button size="small" type="danger" @click="deleteAnnouncement(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="showCreateAnnouncement" :title="editingAnnouncement ? '编辑公告' : '发布公告'" width="500px">
        <el-form :model="announcementForm" label-width="80px">
          <el-form-item label="标题">
            <el-input v-model="announcementForm.title" placeholder="公告标题" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="announcementForm.type">
              <el-option label="系统" value="SYSTEM" />
              <el-option label="重要" value="IMPORTANT" />
            </el-select>
          </el-form-item>
          <el-form-item label="内容">
            <el-input v-model="announcementForm.content" type="textarea" :rows="6" placeholder="公告内容" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateAnnouncement = false">取消</el-button>
          <el-button type="primary" @click="saveAnnouncement">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { adminApi } from '@/api/admin'
import { announcementApi } from '@/api/announcement'
import { groupApi } from '@/api/group'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const loading = ref(false)

// 从路由 meta 读取当前 tab
const activeTab = computed(() => (route.meta.adminTab as string) || 'stats')

const tabTitles: Record<string, string> = {
  stats: '数据看板',
  operations: '运营数据',
  users: '用户管理',
  content: '内容审核',
  groups: '群组管理',
  robots: '机器人管理',
  ai: 'AI 配置',
  system: '系统配置',
  announcements: '公告管理'
}
const tabTitle = computed(() => tabTitles[activeTab.value] || '管理后台')

const stats = ref([
  { label: '总用户数', value: 0, key: 'totalUsers', iconBg: '#3B82F6' },
  { label: '在线用户', value: 0, key: 'onlineUsers', iconBg: '#10B981' },
  { label: 'API 频率', value: 0, key: 'requestsPerMinute', iconBg: '#F59E0B' },
  { label: '今日消息', value: 0, key: 'todayMessages', iconBg: '#EF4444' },
  { label: '今日新增', value: 0, key: 'todayUsers', iconBg: '#8B5CF6' }
])

const operationStats = ref([
  { label: '日活用户', value: 0, iconBg: '#3B82F6' },
  { label: '日均消息', value: 0, iconBg: '#10B981' },
  { label: '消息总量', value: 0, iconBg: '#F59E0B' },
  { label: '活跃群组', value: 0, iconBg: '#8B5CF6' }
])

const deepStats = ref({
  jvmHeapUsed: 0,
  jvmHeapMax: 1,
  redisClients: 0,
  redisHitRate: '0.00',
  uptime: 0
})

const colors = [
  { color: '#10B981', percentage: 40 },
  { color: '#F59E0B', percentage: 70 },
  { color: '#EF4444', percentage: 90 }
]

const users = ref<any[]>([])
const userSearch = ref('')
const selectedUserIds = ref<number[]>([])
const groups = ref<any[]>([])
const groupSearch = ref('')
const robots = ref<any[]>([])
const robotSearch = ref('')
const sensitiveWords = ref<any[]>([])
const showAddWord = ref(false)
const newWord = ref('')

// AI配置
const aiConfigs = ref<any[]>([])
const showAiDialog = ref(false)
const aiForm = ref<any>({})

// 系统配置
const systemConfigs = ref<any[]>([])

// 创建用户
const showCreateUserDialog = ref(false)
const createUserForm = ref({ username: '', password: '', email: '' })

// 编辑用户
const showEditUserDialog = ref(false)
const editUserForm = ref<any>({})

// 创建群组
const showCreateGroupDialog = ref(false)
const createGroupForm = ref({ name: '', type: 'PUBLIC' })

// 创建机器人
const showCreateRobotDialog = ref(false)
const createRobotForm = ref({ name: '', type: 'AI' })

// 编辑机器人
const showEditRobotDialog = ref(false)
const editRobotForm = ref<any>({})

// 群消息
const showGroupMessages = ref(false)
const selectedGroup = ref<any>(null)
const groupMessages = ref<any[]>([])
const msgLoading = ref(false)
const hasMoreMessages = ref(true)

let statsTimer: any = null
onMounted(() => {
  loadStats()
  loadTabData()
  statsTimer = setInterval(loadStats, 10000)
})

onUnmounted(() => {
  if (statsTimer) clearInterval(statsTimer)
})

// 创建用户
function openCreateUserDialog() {
  createUserForm.value = { username: '', password: '', email: '' }
  showCreateUserDialog.value = true
}

async function createUser() {
  const f = createUserForm.value
  if (!f.username.trim() || !f.password.trim()) {
    ElMessage.warning('用户名和密码不能为空')
    return
  }
  try {
    await adminApi.register(f)
    ElMessage.success('用户创建成功')
    showCreateUserDialog.value = false
    await loadUsers()
  } catch (_e: any) {
    ElMessage.error(_e?.response?.data?.msg || '创建失败')
  }
}

// 创建群组
async function createGroup() {
  if (!createGroupForm.value.name.trim()) {
    ElMessage.warning('请输入群名称')
    return
  }
  await groupApi.create({
    name: createGroupForm.value.name.trim(),
    type: createGroupForm.value.type,
    ownerId: Number(localStorage.getItem('userId'))
  })
  ElMessage.success('群组创建成功')
  showCreateGroupDialog.value = false
  createGroupForm.value = { name: '', type: 'PUBLIC' }
  await loadGroups()
}

// 创建机器人
async function createRobot() {
  if (!createRobotForm.value.name.trim()) {
    ElMessage.warning('请输入机器人名称')
    return
  }
  try {
    await adminApi.createRobot({
      name: createRobotForm.value.name.trim(),
      type: createRobotForm.value.type,
      ownerId: Number(localStorage.getItem('userId'))
    })
    ElMessage.success('机器人创建成功')
    showCreateRobotDialog.value = false
    createRobotForm.value = { name: '', type: 'AI' }
    await loadRobots()
  } catch (_e: any) {
    ElMessage.error(_e?.response?.data?.msg || '创建失败')
  }
}

// 编辑机器人
async function editRobot(row: any) {
  // 确保 AI 配置列表已加载，供下拉选择
  if (!aiConfigs.value.length) {
    try { await loadAiConfigs() } catch {}
  }
  editRobotForm.value = {
    id: row.id,
    name: row.name,
    description: row.description || '',
    status: row.status,
    responseMode: row.responseMode || 'MENTION',
    contextSize: row.contextSize || 20,
    aiConfigId: row.aiConfigId || null
  }
  showEditRobotDialog.value = true
}

async function saveEditRobot() {
  if (!editRobotForm.value.name?.trim()) {
    ElMessage.warning('请输入机器人名称')
    return
  }
  try {
    const { id, ...data } = editRobotForm.value
    await adminApi.updateRobot(id, data)
    ElMessage.success('机器人更新成功')
    showEditRobotDialog.value = false
    await loadRobots()
  } catch (_e: any) {
    ElMessage.error(_e?.response?.data?.msg || '更新失败')
  }
}

// 删除机器人
async function deleteRobot(row: any) {
  try {
    await adminApi.deleteRobot(row.id)
    robots.value = robots.value.filter(r => r.id !== row.id)
    ElMessage.success('机器人已删除')
  } catch (_e: any) {
    ElMessage.error(_e?.response?.data?.msg || '删除失败')
  }
}

// 监听路由变化加载对应数据
watch(() => route.meta.adminTab, () => {
  loadTabData()
})

function loadTabData() {
  const tab = activeTab.value
  if (tab === 'users') loadUsers()
  else if (tab === 'groups') loadGroups()
  else if (tab === 'robots') loadRobots()
  else if (tab === 'content') loadSensitiveWords()
  else if (tab === 'ai') loadAiConfigs()
  else if (tab === 'system') loadSystemConfigs()
  else if (tab === 'announcements') loadAnnouncements()
  else if (tab === 'operations') loadStats()
}

async function loadStats() {
  try {
    const res = await adminApi.stats()
    if (res.data) {
      stats.value.forEach(s => { 
        s.value = res.data[s.key] !== undefined ? res.data[s.key] : 0 
      })
      
      // 更新深层指标
      deepStats.value = {
        jvmHeapUsed: res.data.jvmHeapUsed || 0,
        jvmHeapMax: res.data.jvmHeapMax || 1,
        redisClients: res.data.redisClients || 0,
        redisHitRate: res.data.redisHitRate || '0.00',
        uptime: res.data.uptime || 0
      }
      
      // 同步运营数据
      operationStats.value[0].value = res.data['onlineUsers'] || 0
      operationStats.value[1].value = res.data['requestsPerMinute'] || 0
      operationStats.value[2].value = res.data['totalMessages'] || 0
      operationStats.value[3].value = res.data['totalGroups'] || 0
    }
  } catch (_e) {}
}

function formatUptime(seconds: number) {
  if (seconds < 60) return seconds + ' 秒'
  if (seconds < 3600) return Math.floor(seconds / 60) + ' 分钟'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  return h + ' 小时 ' + m + ' 分'
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await adminApi.users(userSearch.value)
    users.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadGroups() {
  loading.value = true
  try {
    const res = await adminApi.groups(groupSearch.value)
    groups.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadRobots() {
  loading.value = true
  try {
    const res = await adminApi.robots(robotSearch.value)
    robots.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadSensitiveWords() {
  const res = await adminApi.sensitiveWords()
  sensitiveWords.value = res.data || []
}

async function toggleUserStatus(user: any) {
  const newStatus = user.status === 1 ? 0 : 1
  await adminApi.updateUserStatus(user.id, newStatus)
  user.status = newStatus
  ElMessage.success('状态已更新')
}

async function resetPassword(userId: number) {
  await adminApi.resetPassword(userId, '123456')
  ElMessage.success('密码已重置为 123456')
}

function openEditUserDialog(row: any) {
  editUserForm.value = {
    id: row.id,
    username: row.username,
    nickname: row.nickname || '',
    email: row.email || '',
    phone: row.phone || ''
  }
  showEditUserDialog.value = true
}

async function saveEditUser() {
  const { id, ...data } = editUserForm.value
  try {
    await adminApi.updateUser(id, data)
    ElMessage.success('用户信息已更新')
    showEditUserDialog.value = false
    await loadUsers()
  } catch (_e: any) {
    ElMessage.error(_e?.response?.data?.msg || '更新失败')
  }
}

function handleUserSelectionChange(rows: any[]) {
  selectedUserIds.value = rows.map(r => r.id)
}

async function deleteUser(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.nickname || row.username}」？`, '提示', { type: 'warning' })
    await adminApi.deleteUser(row.id)
    ElMessage.success('用户已删除')
    await loadUsers()
  } catch {}
}

async function batchDeleteUsers() {
  if (!selectedUserIds.value.length) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedUserIds.value.length} 个用户？`, '提示', { type: 'warning' })
    await adminApi.batchDeleteUsers(selectedUserIds.value)
    ElMessage.success('批量删除成功')
    selectedUserIds.value = []
    await loadUsers()
  } catch {}
}

async function dissolveGroup(group: any) {
  await adminApi.dissolveGroup(group.id)
  groups.value = groups.value.filter(g => g.id !== group.id)
  ElMessage.success('群已解散')
}

async function addWord() {
  if (!newWord.value.trim()) return
  await adminApi.addSensitiveWord({ word: newWord.value.trim() })
  ElMessage.success('已添加')
  showAddWord.value = false
  newWord.value = ''
  await loadSensitiveWords()
}

async function deleteWord(id: number) {
  await adminApi.deleteSensitiveWord(id)
  sensitiveWords.value = sensitiveWords.value.filter(w => w.id !== id)
  ElMessage.success('已删除')
}

// AI配置
async function loadAiConfigs() {
  loading.value = true
  try {
    const res = await adminApi.aiConfigs()
    aiConfigs.value = res.data || []
  } finally { loading.value = false }
}

function openAiConfigDialog(row?: any) {
  if (row) {
    aiForm.value = { ...row }
  } else {
    aiForm.value = { provider: 'MINIMAX', temperature: 0.7, maxTokens: 2048 }
  }
  showAiDialog.value = true
}

async function saveAiConfig() {
  await adminApi.saveAiConfig(aiForm.value)
  showAiDialog.value = false
  await loadAiConfigs()
  ElMessage.success('保存成功')
}

async function deleteAiConfig(id: number) {
  await adminApi.deleteAiConfig(id)
  aiConfigs.value = aiConfigs.value.filter(c => c.id !== id)
  ElMessage.success('已删除')
}

function copyAiConfig(row: any) {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { id, createTime, updateTime, ...rest } = row
  aiForm.value = { 
    ...rest,
    name: (rest.name || '') + ' (副本)'
  }
  showAiDialog.value = true
}

// 系统配置
async function loadSystemConfigs() {
  try {
    const res = await adminApi.systemConfigs()
    systemConfigs.value = res.data || []
  } catch (_e) {}
}

async function updateSystemConfig(key: string, value: string) {
  await adminApi.updateSystemConfig(key, value)
  ElMessage.success('已更新')
}

// 群消息查看
async function viewGroupMessages(group: any) {
  selectedGroup.value = group
  showGroupMessages.value = true
  groupMessages.value = []
  hasMoreMessages.value = true
  await loadGroupMessages()
}

async function loadGroupMessages() {
  if (!selectedGroup.value) return
  msgLoading.value = true
  try {
    const beforeId = groupMessages.value.length ? groupMessages.value[groupMessages.value.length - 1].id : undefined
    const res = await adminApi.groupMessages(selectedGroup.value.id, 30, beforeId)
    const msgs = res.data || []
    if (!msgs.length) { hasMoreMessages.value = false; return }
    groupMessages.value.push(...msgs)
    if (msgs.length < 30) hasMoreMessages.value = false
  } finally {
    msgLoading.value = false
  }
}

async function loadMoreMessages() {
  await loadGroupMessages()
}

// ===== 公告管理 =====
const announcementList = ref<any[]>([])
const showCreateAnnouncement = ref(false)
const editingAnnouncement = ref<any>(null)
const announcementForm = ref({ title: '', content: '', type: 'SYSTEM' })

async function loadAnnouncements() {
  try {
    const res = await announcementApi.adminList()
    announcementList.value = res.data || []
  } catch { ElMessage.error('加载公告失败') }
}

function editAnnouncement(row: any) {
  editingAnnouncement.value = row
  announcementForm.value = { title: row.title, content: row.content, type: row.type }
  showCreateAnnouncement.value = true
}

async function saveAnnouncement() {
  if (!announcementForm.value.title) { ElMessage.warning('请输入标题'); return }
  try {
    if (editingAnnouncement.value) {
      await announcementApi.adminUpdate(editingAnnouncement.value.id, announcementForm.value)
      ElMessage.success('更新成功')
    } else {
      await announcementApi.adminCreate(announcementForm.value)
      ElMessage.success('创建成功')
    }
    showCreateAnnouncement.value = false
    editingAnnouncement.value = null
    announcementForm.value = { title: '', content: '', type: 'SYSTEM' }
    await loadAnnouncements()
  } catch { ElMessage.error('操作失败') }
}

async function publishAnnouncement(id: number) {
  try {
    await announcementApi.adminPublish(id, {})
    ElMessage.success('已发布')
    await loadAnnouncements()
  } catch { ElMessage.error('发布失败') }
}

async function unpublishAnnouncement(id: number) {
  try {
    await announcementApi.adminUnpublish(id)
    ElMessage.success('已撤回')
    await loadAnnouncements()
  } catch { ElMessage.error('撤回失败') }
}

async function deleteAnnouncement(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' })
    await announcementApi.adminDelete(id)
    ElMessage.success('已删除')
    await loadAnnouncements()
  } catch {}
}
</script>

<style scoped>
.admin-page { padding: 24px; height: 100%; overflow: auto; }
.admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.admin-header h2 { font-size: 20px; font-weight: 700; }
.stat-cards { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; margin-bottom: 8px; }
.stat-card { background: #1F2937; border-radius: 12px; padding: 20px; display: flex; flex-direction: column; align-items: center; gap: 12px; }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.stat-icon svg { width: 24px; height: 24px; color: white; }
.stat-value { font-size: 28px; font-weight: 700; color: white; }
.stat-label { font-size: 13px; color: #9CA3AF; margin-top: -4px; }
.section { background: var(--surface-1); border: 1px solid var(--border); border-radius: 12px; padding: 20px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-header h3 { font-weight: 600; font-size: 16px; }
.word-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.word-tags .el-tag { font-size: 14px; padding: 8px 12px; }
.empty-text { color: var(--text-muted); padding: 20px; }
.config-list { display: flex; flex-direction: column; gap: 12px; }
.config-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border: 1px solid var(--border); border-radius: 8px; }
.config-info {}
.config-key { font-weight: 600; font-size: 14px; }
.config-desc { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }
.config-value {}
.msg-list { max-height: 400px; overflow-y: auto; display: flex; flex-direction: column; gap: 8px; }
.msg-item { display: flex; align-items: baseline; gap: 6px; font-size: 13px; padding: 6px 8px; border-radius: 6px; }
.msg-item:hover { background: var(--surface-3); }
.msg-sender { color: var(--primary); font-weight: 600; flex-shrink: 0; }
.msg-type { color: var(--text-muted); font-size: 11px; flex-shrink: 0; }
.msg-content { flex: 1; word-break: break-all; }
.msg-time { color: var(--text-muted); font-size: 11px; flex-shrink: 0; }
.ops-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.ops-card { background: white; border: 1px solid var(--border); border-radius: 10px; padding: 16px 20px; }
.ops-card-title { font-size: 13px; color: var(--text-secondary); margin-bottom: 8px; }
.ops-card-value { font-size: 24px; font-weight: 700; color: var(--text-primary); }
.ops-card-value span { font-size: 12px; font-weight: 400; color: var(--text-muted); }

.monitor-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-top: 20px; }
.monitor-card { background: white; border: 1px solid var(--border); border-radius: 12px; padding: 20px; }
.monitor-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.monitor-header h3 { font-size: 15px; font-weight: 600; margin: 0; }
.mem-chart-wrap { display: flex; align-items: center; justify-content: space-around; }
.mem-info { display: flex; flex-direction: column; gap: 8px; }
.mem-item { font-size: 13px; color: var(--text-secondary); }
.mem-item strong { color: var(--text-primary); margin-left: 4px; }
.percentage-value { display: block; margin-top: 10px; font-size: 20px; font-weight: 700; }
.percentage-label { display: block; margin-top: 2px; font-size: 10px; color: var(--text-muted); }
.extra-stats { display: flex; flex-direction: column; gap: 20px; padding-top: 10px; }
.extra-item {}
.extra-label { font-size: 12px; color: var(--text-muted); margin-bottom: 4px; }
.extra-value { font-size: 20px; font-weight: 700; color: var(--text-primary); }
.extra-value span.unit { font-size: 12px; font-weight: 400; color: var(--text-muted); margin-left: 4px; }
.extra-value span.divider { margin: 0 8px; color: var(--border); font-weight: 300; }
</style>
