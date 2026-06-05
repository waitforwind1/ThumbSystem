<template>
  <div :class="['app-shell', { 'home-fixed-shell': ['home', 'hot', 'compose', 'editBlog'].includes(activeView) }, `view-${activeView}`]">
    <header class="top-nav">
      <div class="nav-inner">
        <button class="brand" type="button" @click="switchView('home')">
          <span class="brand-mark">T</span>
          <span class="brand-copy">
            <strong>Thumbs</strong>
            <small>内容互动社区</small>
          </span>
        </button>

        <nav class="nav-tabs" aria-label="主导航">
          <button v-for="item in tabs" :key="item.key" :class="{ active: activeView === item.key }" type="button" @click="switchView(item.key)">
            <component :is="item.icon" :size="17" />
            <span>{{ item.label }}</span>
            <em v-if="item.key === 'messages' && unreadCount">{{ unreadCount }}</em>
          </button>
        </nav>

        <div class="nav-search">
          <Search :size="17" />
          <input v-model.trim="searchForm.keyWord" placeholder="搜索文章、观点、项目复盘" @keyup.enter="runSearch" />
        </div>

        <div v-if="currentUser" class="nav-user">
          <button class="avatar-button" type="button" @click="switchView('profile')">
            <img v-if="currentUser.avatar" :src="currentUser.avatar" alt="用户头像" />
            <span v-else>{{ avatarText }}</span>
          </button>
          <button class="button subtle" type="button" @click="logout">
            <LogOut :size="16" /> 退出
          </button>
        </div>
        <div v-else class="nav-auth-actions">
          <button class="button subtle" type="button" @click="openAuth('login')">
            <LogIn :size="16" /> 登录
          </button>
          <button class="button primary" type="button" @click="openAuth('register')">
            <UserPlus :size="16" /> 注册
          </button>
        </div>
      </div>
    </header>

    <main class="page">
      <p v-if="notice" :class="['notice', noticeType]">{{ notice }}</p>

      <section v-if="activeView === 'auth'" class="auth-page">
        <div class="auth-panel">
          <section class="auth-intro">
            <p class="eyebrow">加入 Thumbs</p>
            <h1>登录后参与内容互动和社区创作</h1>
            <p>发布博客、收藏文章、参与评论、接收站内消息都会绑定到你的账号。这里使用独立认证页，避免把账号密码输入框散落在内容页面里。</p>
            <div class="auth-benefits">
              <span><ShieldCheck :size="16" /> 会话状态统一校验</span>
              <span><Bookmark :size="16" /> 收藏和点赞状态持久展示</span>
              <span><Bell :size="16" /> 互动消息集中提醒</span>
            </div>
          </section>

          <form class="auth-form-card" @submit.prevent="submitAuth">
            <div class="auth-form-head">
              <div>
                <p class="eyebrow">{{ authMode === 'login' ? 'Account Login' : 'Create Account' }}</p>
                <h2>{{ authMode === 'login' ? '账号登录' : '注册账号' }}</h2>
              </div>
              <button class="button subtle" type="button" @click="switchView(lastListView)">
                <X :size="16" /> 返回
              </button>
            </div>
            <div class="auth-tabs">
              <button :class="{ active: authMode === 'login' }" type="button" @click="authMode = 'login'">登录</button>
              <button :class="{ active: authMode === 'register' }" type="button" @click="authMode = 'register'">注册</button>
            </div>
            <label>
              <span>账号</span>
              <input v-model.trim="authForm.account" autocomplete="username" placeholder="请输入账号" />
            </label>
            <label>
              <span>密码</span>
              <input v-model.trim="authForm.password" :autocomplete="authMode === 'login' ? 'current-password' : 'new-password'" type="password" placeholder="请输入密码" />
            </label>
            <label v-if="authMode === 'register'">
              <span>确认密码</span>
              <input v-model.trim="authForm.checkPassword" autocomplete="new-password" type="password" placeholder="请再次输入密码" />
            </label>
            <button class="button primary full" type="submit">
              {{ authMode === 'login' ? '登录并进入社区' : '创建账号' }}
            </button>
            <p class="auth-switch">
              {{ authMode === 'login' ? '还没有账号？' : '已有账号？' }}
              <button type="button" @click="authMode = authMode === 'login' ? 'register' : 'login'">
                {{ authMode === 'login' ? '立即注册' : '去登录' }}
              </button>
            </p>
          </form>
        </div>
      </section>

      <section v-else-if="activeView === 'home'" class="home-layout">
        <section :class="['feed-column', { 'has-filter': selectedTag }]">
          <div v-if="selectedTag" class="filter-summary">
            <div>
              <span>当前标签</span>
              <strong>{{ selectedTag }}</strong>
            </div>
            <button class="button subtle" type="button" @click="clearTagFilter">
              <X :size="16" /> 清除筛选
            </button>
          </div>

          <div v-if="blogList.length" class="post-grid">
            <article v-for="blog in blogList" :key="blog.id" class="post-card" @click="openDetail(blog)">
              <div class="post-cover">
                <img v-if="blog.coverImage && !blog.coverBroken" :src="blog.coverImage" :alt="blog.title || '文章封面'" @error="markCoverBroken(blog)" />
                <div v-else class="cover-fallback">{{ getInitial(blog.title || 'T') }}</div>
              </div>
              <div class="post-body">
                <div class="post-tags">
                  <button v-if="blog.category" type="button" @click.stop="filterByCategory(blog.category)">{{ blog.category }}</button>
                  <button v-for="tag in splitTags(blog.tag).slice(0, 2)" :key="`${blog.id}-${tag}`" type="button" @click.stop="filterByTag(tag)">{{ tag }}</button>
                </div>
                <h2>{{ blog.title || '未命名文章' }}</h2>
              </div>
              <footer class="post-footer" @click.stop>
                <button class="author-chip" type="button" @click="openAuthor(blog)">
                  <img v-if="blog.avatar" :src="blog.avatar" alt="作者头像" />
                  <span v-else>{{ getInitial(blog.username || blog.userId) }}</span>
                  <strong>{{ blog.username || `用户 ${blog.userId || '-'}` }}</strong>
                </button>
                <div class="post-stats">
                  <button :class="{ active: blog.hasThumb }" type="button" title="点赞" @click="toggleThumb(blog)">
                    <ThumbsUp :size="14" /> {{ blog.thumbCount || 0 }}
                  </button>
                  <button :class="{ active: blog.hasFavorite }" type="button" title="收藏" @click="toggleFavorite(blog)">
                    <Bookmark :size="14" /> {{ blog.favoriteCount || 0 }}
                  </button>
                  <button type="button" title="评论" @click="openDetail(blog)">
                    <MessageCircle :size="14" /> {{ blog.commentCount || 0 }}
                  </button>
                </div>
                <time>{{ formatDate(blog.createTime) }}</time>
              </footer>
            </article>
          </div>

          <div v-if="!blogList.length" class="empty-state">
            <FileText :size="36" />
            <strong>{{ selectedTag ? '该标签暂无文章' : '还没有内容' }}</strong>
            <span>{{ selectedTag ? '换一个标签看看，或者清除筛选回到全部内容。' : '发布第一篇文章，建立你的社区内容流。' }}</span>
          </div>

          <nav v-if="blogList.length" class="pagination-bar" aria-label="文章分页">
            <button class="button subtle" type="button" :disabled="pageNo <= 1" @click="changePage(pageNo - 1)">上一页</button>
            <button v-for="page in pageButtons" :key="page" :class="['page-dot', { active: pageNo === page }]" type="button" @click="changePage(page)">
              {{ page }}
            </button>
            <button class="button subtle" type="button" :disabled="!hasNextPage" @click="changePage(pageNo + 1)">下一页</button>
          </nav>
        </section>

        <aside class="right-rail">
          <section class="panel">
            <div class="panel-head">
              <h3>今日热榜</h3>
              <button type="button" @click="switchView('hot')">查看全部</button>
            </div>
            <ol class="mini-rank">
              <li v-for="(blog, index) in hotList.slice(0, 6)" :key="blog.id" @click="openDetail(blog)">
                <span>{{ index + 1 }}</span>
                <div>
                  <strong>{{ blog.title }}</strong>
                  <small>{{ blog.thumbCount || 0 }} 赞 · {{ blog.commentCount || 0 }} 评 · {{ blog.shareCount || 0 }} 分享</small>
                </div>
              </li>
            </ol>
          </section>

          <section class="panel tag-panel">
            <div class="panel-head">
              <h3>标签发现</h3>
              <button type="button" @click="clearTagFilter">全部</button>
            </div>
            <div class="tag-cloud">
              <button v-for="(tag, index) in visibleTags" :key="tag" :class="['tag-tile', `tag-tile-${index % 6}`, { active: selectedTag === tag }]" type="button" @click="filterByTag(tag)">
                {{ tag }}
              </button>
            </div>
          </section>
        </aside>
      </section>

      <section v-else-if="activeView === 'hot'" class="hot-page">
        <header class="section-hero">
          <div>
            <p class="eyebrow">Product Hunt 风热榜</p>
            <h1>社区热榜</h1>
            <span>按热度、点赞和互动发现值得阅读的内容。</span>
          </div>
          <button class="button primary" type="button" @click="loadHot">
            <RefreshCw :size="16" /> 更新热榜
          </button>
        </header>
        <div class="filter-pills">
          <button v-for="category in categories" :key="category" :class="{ active: hotCategory === category }" type="button" @click="filterHot(category)">
            {{ category || '全部' }}
          </button>
        </div>
        <div class="hot-rank-list">
          <article v-for="(blog, index) in pagedHotList" :key="blog.id" class="rank-card" @click="openDetail(blog)">
            <div class="rank-number">#{{ (hotPageNo - 1) * hotPageSize + index + 1 }}</div>
            <div class="rank-main">
              <div class="post-meta">
                <span v-if="blog.category" class="category">{{ blog.category }}</span>
                <span>{{ blog.username || '社区作者' }}</span>
                <span>{{ formatDate(blog.createTime) }}</span>
              </div>
              <h2>{{ blog.title }}</h2>
              <p>{{ blog.summary || excerpt(blog.content) }}</p>
            </div>
            <div class="rank-score">
              <button :class="{ active: blog.hasThumb }" type="button" @click.stop="toggleThumb(blog)">
                <ThumbsUp :size="16" /> {{ blog.thumbCount || 0 }}
              </button>
              <button type="button" @click.stop="openDetail(blog)">
                <MessageCircle :size="16" /> {{ blog.commentCount || 0 }}
              </button>
              <button type="button" @click.stop="shareBlog(blog)">
                <Share2 :size="16" /> {{ blog.shareCount || 0 }}
              </button>
            </div>
          </article>
          <div v-if="!pagedHotList.length" class="empty-state hot-empty">
            <Flame :size="34" />
            <strong>暂无热榜内容</strong>
            <span>有新的点赞、评论和分享后会在这里展示。</span>
          </div>
        </div>
        <nav v-if="hotList.length" class="pagination-bar hot-pagination" aria-label="热榜分页">
          <button class="button subtle" type="button" :disabled="hotPageNo <= 1" @click="changeHotPage(hotPageNo - 1)">上一页</button>
          <button v-for="page in hotPageButtons" :key="page" :class="['page-dot', { active: hotPageNo === page }]" type="button" @click="changeHotPage(page)">
            {{ page }}
          </button>
          <button class="button subtle" type="button" :disabled="!hasNextHotPage" @click="changeHotPage(hotPageNo + 1)">下一页</button>
        </nav>
      </section>

      <section v-else-if="activeView === 'detail'" class="detail-page">
        <button class="button subtle back-button" type="button" @click="switchView('home')">返回内容流</button>
        <article v-if="selectedBlog" class="article-shell">
          <header class="article-head">
            <div class="post-meta centered">
              <span v-if="selectedBlog.category" class="category">{{ selectedBlog.category }}</span>
              <span>{{ formatDate(selectedBlog.createTime) }}</span>
              <span>{{ selectedBlog.viewCount || 0 }} 阅读</span>
            </div>
            <h1>{{ selectedBlog.title }}</h1>
            <p>{{ selectedBlog.summary || excerpt(selectedBlog.content, 120) }}</p>
            <button class="article-author" type="button" @click="openAuthor(selectedBlog)">
              <img v-if="selectedBlog.avatar" :src="selectedBlog.avatar" alt="作者头像" />
              <span v-else>{{ getInitial(selectedBlog.username || selectedBlog.userId) }}</span>
              <div>
                <strong>{{ selectedBlog.username || `用户 ${selectedBlog.userId || '-'}` }}</strong>
                <small>发布于 {{ formatDate(selectedBlog.createTime) }}</small>
              </div>
            </button>
          </header>
          <div class="article-actions">
            <button :class="{ active: selectedBlog.hasThumb }" type="button" @click="toggleThumb(selectedBlog)">
              <ThumbsUp :size="17" /> {{ selectedBlog.thumbCount || 0 }}
            </button>
            <button :class="{ active: selectedBlog.hasFavorite }" type="button" @click="toggleFavorite(selectedBlog)">
              <Bookmark :size="17" /> {{ selectedBlog.favoriteCount || 0 }}
            </button>
            <button :class="{ active: selectedBlog.hasShare }" type="button" @click="shareBlog(selectedBlog)">
              <Share2 :size="17" /> {{ selectedBlog.shareCount || 0 }}
            </button>
          </div>
          <div class="article-content">{{ selectedBlog.content }}</div>
        </article>
        <section class="comments-shell">
          <div class="comments-head">
            <div>
              <h2>评论</h2>
              <span>{{ totalCommentCount }} 条讨论</span>
            </div>
          </div>
          <div class="comment-editor">
            <textarea v-model.trim="commentText" rows="3" placeholder="写下你的观点，保持具体、友善、有信息量"></textarea>
            <button class="button primary" type="button" @click="postComment">
              <Send :size="16" /> 发表评论
            </button>
          </div>
          <article
            v-for="item in flattenedComments"
            :key="item.comment.id"
            :class="['comment-card', { nested: item.depth > 0 }]"
            :style="{ '--comment-depth': item.depth }"
          >
            <button class="comment-avatar" type="button" @click="openCommentUser(item.comment)">
              <img v-if="item.comment.avatar" :src="item.comment.avatar" alt="评论用户头像" />
              <span v-else>{{ getInitial(item.comment.username || item.comment.userId) }}</span>
            </button>
            <div class="comment-body">
              <header>
                <button type="button" @click="openCommentUser(item.comment)">{{ item.comment.username || `用户 ${item.comment.userId}` }}</button>
                <span v-if="item.comment.replyUsername" class="reply-to">回复 @{{ item.comment.replyUsername }}</span>
                <span>{{ formatDate(item.comment.crteateTime || item.comment.createTime) }}</span>
              </header>
              <p>{{ item.comment.content }}</p>
              <div class="comment-actions">
                <button type="button"><ArrowUp :size="14" /> 赞</button>
                <button type="button" @click="startReply(item.comment)"><Reply :size="14" /> 回复</button>
              </div>
              <div v-if="replyTarget?.id === item.comment.id" class="reply-editor">
                <input v-model.trim="replyText" :placeholder="`回复 ${item.comment.username || '这条评论'}`" @keyup.enter="postReply" />
                <button class="button primary" type="button" @click="postReply">发送</button>
                <button class="button subtle" type="button" @click="cancelReply">取消</button>
              </div>
            </div>
          </article>
        </section>
      </section>

      <section v-else-if="activeView === 'compose'" class="compose-page">
        <header class="section-hero compact-hero">
          <div>
            <p class="eyebrow">发布博客</p>
            <h1>写一篇真正能被读懂的内容</h1>
            <span>标题、摘要、分类和正文保持清晰，方便社区发现和讨论。</span>
          </div>
        </header>
        <form class="editor-card" @submit.prevent="publishBlog">
          <label>
            <span>标题</span>
            <input v-model.trim="blogForm.title" placeholder="例如：Redis 点赞状态一致性问题复盘" />
          </label>
          <div class="form-grid">
            <label>
              <span>分类</span>
              <input v-model.trim="blogForm.category" placeholder="技术 / 项目 / 学习 / 生活" />
            </label>
            <label>
              <span>封面链接</span>
              <input v-model.trim="blogForm.coverImage" placeholder="https://..." />
            </label>
          </div>
          <label>
            <span>摘要</span>
            <input v-model.trim="blogForm.summary" placeholder="用一句话说明这篇文章解决了什么问题" />
          </label>
          <label>
            <span>标签</span>
            <input v-model.trim="tagInput" placeholder="用逗号分隔，例如 Redis, Vue, Spring Boot" />
          </label>
          <label>
            <span>正文</span>
            <textarea v-model.trim="blogForm.content" rows="16" placeholder="从背景、问题、方案、结果几个部分展开。"></textarea>
          </label>
          <div class="editor-actions">
            <button class="button subtle" type="button" @click="resetBlogForm">
              <X :size="16" /> 清空
            </button>
            <button class="button primary" type="submit">
              <PenLine :size="16" /> 发布文章
            </button>
          </div>
        </form>
      </section>

      <section v-else-if="activeView === 'profile'" class="profile-page">
        <div class="profile-cover">
          <div class="profile-identity">
            <img v-if="currentUser?.avatar" :src="currentUser.avatar" alt="用户头像" />
            <span v-else>{{ avatarText }}</span>
            <div>
              <h1>{{ currentUser?.username || currentUser?.account || '未登录用户' }}</h1>
              <p>{{ currentUser ? `账号 ${currentUser.account} · ID ${currentUser.id}` : '登录后可维护个人主页、头像和收藏内容' }}</p>
            </div>
          </div>
          <button v-if="currentUser" class="button subtle" type="button" @click="logout">
            <LogOut :size="16" /> 退出登录
          </button>
        </div>

        <div class="profile-grid">
          <section class="settings-card">
            <h2>个人资料</h2>
            <form v-if="currentUser" @submit.prevent="updateProfile">
              <label>
                <span>昵称</span>
                <input v-model.trim="profileForm.username" placeholder="请输入昵称" />
              </label>
              <label>
                <span>头像 URL</span>
                <input v-model.trim="profileForm.avatar" placeholder="https://example.com/avatar.png" />
              </label>
              <button class="button primary" type="submit">
                <Save :size="16" /> 保存资料
              </button>
            </form>
            <div v-else class="profile-guest">
              <LockKeyhole :size="34" />
              <strong>请先登录后维护个人资料</strong>
              <span>个人主页用于展示头像、昵称和收藏内容，正式项目里不在资料页直接塞入账号密码表单。</span>
              <button class="button primary" type="button" @click="openAuth('login')">前往登录</button>
            </div>
          </section>

          <section class="settings-card">
            <h2>我的收藏</h2>
            <article v-for="blog in favoriteList" :key="blog.id" class="saved-row" @click="openDetail(blog)">
              <strong>{{ blog.title }}</strong>
              <span>{{ blog.summary || excerpt(blog.content) }}</span>
            </article>
            <div v-if="!favoriteList.length" class="empty-state small">
              <Bookmark :size="28" />
              <span>暂无收藏内容</span>
            </div>
          </section>

        </div>
      </section>

      <section v-else-if="activeView === 'myArticles'" class="my-articles-page">
        <header class="section-hero compact-hero">
          <div>
            <p class="eyebrow">内容管理</p>
            <h1>我的文章</h1>
            <span>集中管理已发布内容，支持分页查看、进入详情、独立页面修改和删除。</span>
          </div>
          <button class="button subtle" type="button" @click="refreshMyBlogs">
            <RefreshCw :size="16" /> 刷新
          </button>
        </header>

        <section class="settings-card my-articles-panel">
          <div class="card-head-row">
            <h2>已发布文章</h2>
            <span class="list-count">共 {{ myBlogPool.length }} 篇</span>
          </div>
          <div class="my-blog-list">
            <article v-for="blog in myBlogList" :key="blog.id" class="my-blog-row">
              <div>
                <strong>{{ blog.title }}</strong>
                <span>{{ formatDate(blog.createTime) }} · {{ blog.thumbCount || 0 }} 赞 · {{ blog.commentCount || 0 }} 评 · {{ blog.shareCount || 0 }} 分享</span>
              </div>
              <div class="row-actions">
                <button class="button subtle" type="button" @click="openDetail(blog)">查看</button>
                <button class="button primary" type="button" @click="startEditBlog(blog)">
                  <PenLine :size="15" /> 修改
                </button>
                <button class="button danger" type="button" @click="deleteMyBlog(blog)">
                  <Trash2 :size="15" /> 删除
                </button>
              </div>
            </article>
          </div>
          <div v-if="!myBlogList.length" class="empty-state small">
            <FileText :size="28" />
            <span>暂无已发布文章</span>
          </div>
          <nav v-if="myBlogPool.length" class="pagination-bar">
            <button class="button subtle" type="button" :disabled="myBlogPageNo === 1" @click="changeMyBlogPage(myBlogPageNo - 1)">上一页</button>
            <button
              v-for="page in myBlogPageButtons"
              :key="page"
              :class="['button', page === myBlogPageNo ? 'primary' : 'subtle']"
              type="button"
              @click="changeMyBlogPage(page)"
            >
              {{ page }}
            </button>
            <button class="button subtle" type="button" :disabled="!hasNextMyBlogPage" @click="changeMyBlogPage(myBlogPageNo + 1)">下一页</button>
          </nav>
        </section>
      </section>

      <section v-else-if="activeView === 'editBlog'" class="compose-page edit-page">
        <header class="section-hero compact-hero">
          <div>
            <p class="eyebrow">修改文章</p>
            <h1>维护已发布内容</h1>
            <span>在独立页面完成标题、封面、标签和正文修改，保存后返回我的文章。</span>
          </div>
          <button class="button subtle" type="button" @click="cancelEditBlog">
            <X :size="16" /> 返回我的文章
          </button>
        </header>
        <form class="editor-card" @submit.prevent="updateBlog">
          <label>
            <span>标题</span>
            <input v-model.trim="editBlogForm.title" placeholder="请输入标题" />
          </label>
          <div class="form-grid">
            <label>
              <span>分类</span>
              <input v-model.trim="editBlogForm.category" placeholder="技术 / 项目 / 学习 / 生活" />
            </label>
            <label>
              <span>封面链接</span>
              <input v-model.trim="editBlogForm.coverImage" placeholder="https://..." />
            </label>
          </div>
          <label>
            <span>摘要</span>
            <input v-model.trim="editBlogForm.summary" placeholder="文章摘要" />
          </label>
          <label>
            <span>标签</span>
            <input v-model.trim="editTagInput" placeholder="用逗号分隔，例如 Redis, Vue" />
          </label>
          <label>
            <span>正文</span>
            <textarea v-model.trim="editBlogForm.content" rows="16" placeholder="文章正文"></textarea>
          </label>
          <div class="editor-actions">
            <button class="button subtle" type="button" @click="cancelEditBlog">
              <X :size="16" /> 取消
            </button>
            <button class="button primary" type="submit">
              <Save :size="16" /> 保存修改
            </button>
          </div>
        </form>
      </section>

      <section v-else-if="activeView === 'publicProfile'" class="profile-page">
        <div class="profile-cover public">
          <div class="profile-identity">
            <img v-if="viewingProfile?.avatar" :src="viewingProfile.avatar" alt="用户头像" />
            <span v-else>{{ getInitial(viewingProfile?.userName || viewingProfile?.account || 'U') }}</span>
            <div>
              <h1>{{ viewingProfile?.userName || viewingProfile?.account || '社区用户' }}</h1>
              <p>ID {{ viewingProfile?.id || '-' }} · 加入于 {{ formatDate(viewingProfile?.createTime) }}</p>
            </div>
          </div>
          <div class="profile-metrics">
            <span><strong>{{ viewingProfile?.blogCount || 0 }}</strong> 文章</span>
            <span><strong>{{ viewingProfile?.thumbCount || 0 }}</strong> 获赞</span>
            <span><strong>{{ viewingProfile?.favoriteCount || 0 }}</strong> 收藏</span>
          </div>
        </div>

        <section class="settings-card">
          <h2>TA 的文章</h2>
          <div class="author-article-grid">
            <article v-for="blog in authorBlogList" :key="blog.id" class="saved-row" @click="openDetail(blog)">
              <strong>{{ blog.title }}</strong>
              <span>{{ blog.summary || excerpt(blog.content) }}</span>
            </article>
          </div>
          <div v-if="!authorBlogList.length" class="empty-state small">
            <FileText :size="28" />
            <span>暂无公开文章</span>
          </div>
        </section>
      </section>

      <section v-else-if="activeView === 'admin'" class="admin-page">
        <header class="section-hero compact-hero">
          <div>
            <p class="eyebrow">Admin Console</p>
            <h1>管理后台</h1>
            <span>按管理员身份集中处理用户状态和文章上下架。</span>
          </div>
          <div class="toolbar-actions">
            <button class="button subtle" type="button" @click="loadAdminData">
              <RefreshCw :size="16" /> 刷新
            </button>
          </div>
        </header>

        <div class="admin-tabs">
          <button :class="{ active: adminMode === 'users' }" type="button" @click="adminMode = 'users'">
            <Users :size="16" /> 用户管理
          </button>
          <button :class="{ active: adminMode === 'blogs' }" type="button" @click="adminMode = 'blogs'">
            <FileText :size="16" /> 文章管理
          </button>
        </div>

        <section v-if="adminMode === 'users'" class="settings-card admin-panel">
          <div class="card-head-row">
            <h2>用户列表</h2>
            <span class="list-count">共 {{ adminUsers.length }} 人</span>
          </div>
          <div class="admin-table">
            <div class="admin-row admin-head">
              <span>用户</span>
              <span>角色</span>
              <span>状态</span>
              <span>注册时间</span>
              <span>操作</span>
            </div>
            <div v-for="user in adminUsers" :key="user.id" class="admin-row">
              <span>
                <strong>{{ user.username || user.account }}</strong>
                <small>ID {{ user.id }} · {{ user.account }}</small>
              </span>
              <span>
                <em :class="['status-pill', Number(user.isAdmin || 0) === 1 ? 'success' : 'neutral']">
                  {{ Number(user.isAdmin || 0) === 1 ? '管理员' : '普通用户' }}
                </em>
              </span>
              <span>
                <em :class="['status-pill', Number(user.status || 0) === 1 ? 'danger' : 'success']">
                  {{ userStatusText(user.status) }}
                </em>
              </span>
              <span>{{ formatDate(user.createTime) }}</span>
              <span>
                <button
                  :class="['button', Number(user.status || 0) === 1 ? 'primary' : 'danger']"
                  type="button"
                  :disabled="user.id === currentUser?.id"
                  @click="toggleUserBan(user)"
                >
                  {{ Number(user.status || 0) === 1 ? '解封' : '封禁' }}
                </button>
              </span>
            </div>
          </div>
          <div v-if="!adminUsers.length" class="empty-state small">
            <Users :size="28" />
            <span>暂无用户数据</span>
          </div>
        </section>

        <section v-else class="settings-card admin-panel">
          <div class="card-head-row">
            <h2>文章列表</h2>
            <span class="list-count">共 {{ adminBlogs.length }} 篇</span>
          </div>
          <div class="admin-table blog-admin-table">
            <div class="admin-row admin-head">
              <span>文章</span>
              <span>作者</span>
              <span>互动</span>
              <span>状态</span>
              <span>操作</span>
            </div>
            <div v-for="blog in adminBlogs" :key="blog.id" class="admin-row">
              <span>
                <strong>{{ blog.title || '未命名文章' }}</strong>
                <small>ID {{ blog.id }} · {{ formatDate(blog.createTime) }}</small>
              </span>
              <span>{{ blog.username || `用户 ${blog.userId || '-'}` }}</span>
              <span>{{ blog.thumbCount || 0 }} 赞 · {{ blog.commentCount || 0 }} 评 · {{ blog.shareCount || 0 }} 分享</span>
              <span>
                <em :class="['status-pill', Number(blog.status || 0) === 4 ? 'danger' : 'success']">
                  {{ blogStatusText(blog.status) }}
                </em>
              </span>
              <span class="row-actions">
                <button class="button subtle" type="button" @click="openDetail(blog)">查看</button>
                <button
                  :class="['button', Number(blog.status || 0) === 4 ? 'primary' : 'danger']"
                  type="button"
                  @click="toggleBlogPublish(blog)"
                >
                  {{ Number(blog.status || 0) === 4 ? '恢复发布' : '下架' }}
                </button>
              </span>
            </div>
          </div>
          <div v-if="!adminBlogs.length" class="empty-state small">
            <FileText :size="28" />
            <span>暂无文章数据</span>
          </div>
        </section>
      </section>

      <section v-else-if="activeView === 'messages'" class="messages-page">
        <header class="section-hero compact-hero">
          <div>
            <p class="eyebrow">站内消息</p>
            <h1>通知中心</h1>
            <span>点赞、收藏、评论和系统通知会在这里汇总。</span>
          </div>
          <div class="toolbar-actions">
            <button class="button subtle" type="button" @click="loadMessages">
              <RefreshCw :size="16" /> 刷新
            </button>
            <button class="button primary" type="button" @click="readAll">
              <CheckCheck :size="16" /> 全部已读
            </button>
          </div>
        </header>
        <article v-for="message in messages" :key="message.id" :class="['message-card', { unread: message.isRead === 0 }]">
          <div class="message-dot"></div>
          <div>
            <h2>{{ message.title || '系统通知' }}</h2>
            <p>{{ message.content }}</p>
            <span>{{ formatDate(message.createTime) }}</span>
          </div>
          <button class="button subtle" type="button" @click="readMessage(message)">
            <Check :size="16" /> 已读
          </button>
        </article>
        <div v-if="!messages.length" class="empty-state">
          <Bell :size="36" />
          <strong>暂无通知</strong>
          <span>有新的互动后会在这里提醒你。</span>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  ArrowUp,
  Bell,
  Bookmark,
  Check,
  CheckCheck,
  FileText,
  Flame,
  Home,
  LogIn,
  LogOut,
  Mail,
  MessageCircle,
  PenLine,
  RefreshCw,
  Reply,
  Save,
  Search,
  Send,
  Share2,
  ShieldCheck,
  Tag,
  ThumbsUp,
  Trash2,
  User,
  Users,
  UserPlus,
  LockKeyhole,
  X
} from 'lucide-vue-next'
import { api } from './services/api'

const baseTabs = [
  { key: 'home', label: '首页', icon: Home },
  { key: 'hot', label: '热榜', icon: Flame },
  { key: 'compose', label: '发布', icon: PenLine },
  { key: 'profile', label: '个人主页', icon: User },
  { key: 'myArticles', label: '我的文章', icon: FileText },
  { key: 'messages', label: '消息', icon: Mail }
]
const adminTab = { key: 'admin', label: '管理', icon: ShieldCheck }

const categories = ['', '技术', '项目', '学习', '生活', '产品']

const initialView = new URLSearchParams(window.location.search).get('view')
const activeView = ref(['hot', 'compose'].includes(initialView) ? initialView : 'home')
const lastListView = ref('home')
const authMode = ref('login')
const currentUser = ref(readStoredUser())
const blogList = ref([])
const blogPool = ref([])
const hotList = ref([])
const favoriteList = ref([])
const myBlogPool = ref([])
const myBlogList = ref([])
const messages = ref([])
const adminUsers = ref([])
const adminBlogs = ref([])
const adminMode = ref('users')
const comments = ref([])
const selectedBlog = ref(null)
const viewingProfile = ref(null)
const authorBlogList = ref([])
const unreadCount = ref(0)
const hotCategory = ref('')
const selectedTag = ref('')
const pageNo = ref(1)
const pageSize = 6
const hotPageNo = ref(1)
const hotPageSize = 4
const myBlogPageNo = ref(1)
const myBlogPageSize = 6
const notice = ref('')
const noticeType = ref('success')
const commentText = ref('')
const replyText = ref('')
const replyTarget = ref(null)
const tagInput = ref('')
const editTagInput = ref('')
const editingBlogId = ref(null)

const authForm = reactive({ account: '', password: '', checkPassword: '' })
const profileForm = reactive({ username: '', avatar: '' })
const searchForm = reactive({ keyWord: '', tag: '', category: '' })
const blogForm = reactive({
  title: '',
  category: '',
  coverImage: '',
  summary: '',
  content: '',
  tags: []
})
const editBlogForm = reactive({
  title: '',
  category: '',
  coverImage: '',
  summary: '',
  content: '',
  tags: []
})

const avatarText = computed(() => getInitial(currentUser.value?.username || currentUser.value?.account || 'U'))
const isAdmin = computed(() => Number(currentUser.value?.isAdmin || 0) === 1)
const tabs = computed(() => (isAdmin.value ? [...baseTabs, adminTab] : baseTabs))
const flattenedComments = computed(() => flattenComments(comments.value))
const totalCommentCount = computed(() => flattenedComments.value.length)
const totalBlogPages = computed(() => Math.max(1, Math.ceil(blogPool.value.length / pageSize)))
const hasNextPage = computed(() => pageNo.value * pageSize < blogPool.value.length)
const pagedHotList = computed(() => {
  const start = (hotPageNo.value - 1) * hotPageSize
  return hotList.value.slice(start, start + hotPageSize)
})
const totalHotPages = computed(() => Math.max(1, Math.ceil(hotList.value.length / hotPageSize)))
const hasNextHotPage = computed(() => hotList.value.length > hotPageNo.value * hotPageSize)
const totalMyBlogPages = computed(() => Math.max(1, Math.ceil(myBlogPool.value.length / myBlogPageSize)))
const hasNextMyBlogPage = computed(() => myBlogPageNo.value * myBlogPageSize < myBlogPool.value.length)
const pageButtons = computed(() => {
  const start = Math.max(1, pageNo.value - 2)
  const end = Math.min(totalBlogPages.value, start + 4)
  return Array.from({ length: end - start + 1 }, (_, index) => start + index)
})
const hotPageButtons = computed(() => {
  const start = Math.max(1, hotPageNo.value - 2)
  const end = Math.min(totalHotPages.value, start + 4)
  return Array.from({ length: end - start + 1 }, (_, index) => start + index)
})
const myBlogPageButtons = computed(() => {
  const start = Math.max(1, myBlogPageNo.value - 2)
  const end = Math.min(totalMyBlogPages.value, start + 4)
  return Array.from({ length: end - start + 1 }, (_, index) => start + index)
})
const visibleTags = computed(() => {
  const defaults = ['Redis', 'Spring Boot', 'Vue', 'MySQL', 'RabbitMQ', '项目复盘', '高并发', '缓存', '前端', '后端', '面试', '产品']
  const dynamic = [...blogPool.value, ...hotList.value].flatMap(blog => splitTags(blog.tag))
  return [...new Set([...dynamic, ...defaults])].slice(0, 10)
})

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem('thumbs_user') || 'null')
  } catch {
    localStorage.removeItem('thumbs_user')
    return null
  }
}

function setCurrentUser(user) {
  currentUser.value = user || null
  if (user) {
    localStorage.setItem('thumbs_user', JSON.stringify(user))
    profileForm.username = user.username || ''
    profileForm.avatar = user.avatar || ''
  } else {
    localStorage.removeItem('thumbs_user')
    profileForm.username = ''
    profileForm.avatar = ''
  }
}

function show(message, type = 'success') {
  notice.value = message
  noticeType.value = type
  window.clearTimeout(show.timer)
  show.timer = window.setTimeout(() => {
    notice.value = ''
  }, 2600)
}

async function guarded(task, success) {
  try {
    await task()
    if (success) show(success)
  } catch (error) {
    show(error.message || '操作失败', 'error')
  }
}

async function switchView(view) {
  if ((view === 'compose' || view === 'messages' || view === 'profile' || view === 'myArticles' || view === 'editBlog' || view === 'admin') && !currentUser.value) {
    openAuth('login')
    return
  }
  if (view === 'admin' && !isAdmin.value) {
    show('只有管理员可以进入管理后台', 'error')
    activeView.value = 'home'
    return
  }
  activeView.value = view
  if (view === 'home') {
    lastListView.value = 'home'
    await refreshBlogs()
  }
  if (view === 'hot') {
    lastListView.value = 'hot'
    await loadHot()
  }
  if (view === 'profile') await loadFavorites()
  if (view === 'myArticles') {
    myBlogPageNo.value = 1
    await loadMyBlogs()
  }
  if (view === 'messages') await loadMessages()
  if (view === 'admin') await loadAdminData()
}

function openAuth(mode = 'login') {
  authMode.value = mode
  activeView.value = 'auth'
}

function requireLogin(message = '请先登录后再操作') {
  if (currentUser.value) return true
  show(message, 'error')
  openAuth('login')
  return false
}

function handleProtectedAction(view) {
  if (!requireLogin(view === 'compose' ? '请先登录后发布文章' : '请先登录后操作')) return
  switchView(view)
}

async function submitAuth() {
  await guarded(async () => {
    if (authMode.value === 'login') {
      setCurrentUser(await api.login(authForm.account, authForm.password))
      await afterLogin()
      activeView.value = isAdmin.value ? 'admin' : 'home'
      if (isAdmin.value) await loadAdminData()
    } else {
      await api.register(authForm.account, authForm.password, authForm.checkPassword || authForm.password)
      authMode.value = 'login'
    }
  }, authMode.value === 'login' ? '登录成功' : '注册成功，请登录')
}

async function logout() {
  await guarded(async () => {
    try {
      await api.logout()
    } finally {
      setCurrentUser(null)
    }
    messages.value = []
    unreadCount.value = 0
    blogList.value = blogList.value.map(clearUserState)
    hotList.value = hotList.value.map(clearUserState)
    adminUsers.value = []
    adminBlogs.value = []
    if (activeView.value === 'profile' || activeView.value === 'myArticles' || activeView.value === 'messages' || activeView.value === 'compose' || activeView.value === 'editBlog' || activeView.value === 'admin') {
      activeView.value = 'home'
    }
  }, '已退出登录')
}

async function afterLogin() {
  await Promise.allSettled([refreshBlogs(), loadHot(), loadUnread(), loadFavorites()])
  if (isAdmin.value) await loadAdminData()
}

async function restoreSession() {
  if (!currentUser.value) return
  try {
    setCurrentUser(await api.current())
  } catch {
    setCurrentUser(null)
  }
}

async function refreshBlogs() {
  const requestPageSize = 50
  const blogs = selectedTag.value
    ? await api.search({ tag: selectedTag.value, pageNo: 1, pageSize: requestPageSize })
    : await api.blogs(1, requestPageSize)
  blogPool.value = Array.isArray(blogs) ? blogs : []
  blogList.value = sliceCurrentBlogPage(blogPool.value)
  await hydrateBlogStatuses(blogList.value)
  if (!hotList.value.length) await loadHot()
}

async function loadHot() {
  const limit = 50
  hotList.value = hotCategory.value ? await api.hotByCategory(hotCategory.value, limit) : await api.hot(limit)
  if (hotPageNo.value > totalHotPages.value) {
    hotPageNo.value = totalHotPages.value
  }
  await hydrateBlogStatuses(hotList.value)
}

async function filterByCategory(category) {
  searchForm.category = category
  selectedTag.value = ''
  pageNo.value = 1
  await runSearch()
}

async function filterByTag(tag) {
  selectedTag.value = tag
  searchForm.tag = tag
  searchForm.category = ''
  pageNo.value = 1
  await runSearch()
}

async function clearTagFilter() {
  selectedTag.value = ''
  searchForm.tag = ''
  searchForm.category = ''
  pageNo.value = 1
  await refreshBlogs()
}

async function changePage(page) {
  if (page < 1 || page > totalBlogPages.value || page === pageNo.value) return
  pageNo.value = page
  blogList.value = sliceCurrentBlogPage(blogPool.value)
  await hydrateBlogStatuses(blogList.value)
}

async function filterHot(category) {
  hotCategory.value = category
  hotPageNo.value = 1
  await loadHot()
}

async function changeHotPage(page) {
  if (page < 1 || page > totalHotPages.value || page === hotPageNo.value) return
  hotPageNo.value = page
  await hydrateBlogStatuses(pagedHotList.value)
}

async function runSearch() {
  await guarded(async () => {
    activeView.value = 'home'
    lastListView.value = 'home'
    pageNo.value = 1
    const blogs = await api.search({
      keyWord: searchForm.keyWord,
      tag: selectedTag.value || searchForm.tag,
      category: searchForm.category,
      pageNo: 1,
      pageSize: 50
    })
    blogPool.value = Array.isArray(blogs) ? blogs : []
    blogList.value = sliceCurrentBlogPage(blogPool.value)
    await hydrateBlogStatuses(blogList.value)
  }, '搜索完成')
}

function sliceCurrentBlogPage(blogs) {
  const start = (pageNo.value - 1) * pageSize
  return blogs.slice(start, start + pageSize)
}

async function hydrateBlogStatuses(blogs) {
  if (!currentUser.value || !blogs?.length) return
  const statuses = await Promise.allSettled(blogs.map(blog => api.status(blog.id)))
  statuses.forEach((status, index) => {
    if (status.status !== 'fulfilled') return
    Object.assign(blogs[index], status.value)
  })
}

async function openDetail(blog) {
  lastListView.value = activeView.value === 'hot' ? 'hot' : 'home'
  selectedBlog.value = { ...blog }
  activeView.value = 'detail'
  await guarded(async () => {
    selectedBlog.value = await api.detail(blog.id)
    await hydrateBlogStatuses([selectedBlog.value])
    comments.value = await api.comments(blog.id)
  })
}

async function openAuthor(blog) {
  if (!blog?.userId) return
  activeView.value = 'publicProfile'
  await guarded(async () => {
    viewingProfile.value = await api.userProfile(blog.userId)
    authorBlogList.value = await api.authorBlogs(blog.userId, 1, 8)
    await hydrateBlogStatuses(authorBlogList.value)
  })
}

function openCommentUser(comment) {
  openAuthor({ userId: comment.userId })
}

function startReply(comment) {
  if (!requireLogin('请先登录后回复评论')) return
  replyTarget.value = comment
  replyText.value = ''
}

function cancelReply() {
  replyTarget.value = null
  replyText.value = ''
}

async function publishBlog() {
  if (!requireLogin('请先登录后发布文章')) return
  await guarded(async () => {
    const tags = tagInput.value.split(',').map(item => item.trim()).filter(Boolean)
    await api.addBlog({ ...blogForm, tags })
    resetBlogForm()
    pageNo.value = 1
    selectedTag.value = ''
    await refreshBlogs()
    activeView.value = 'home'
  }, '发布成功')
}

function resetBlogForm() {
  Object.assign(blogForm, { title: '', category: '', coverImage: '', summary: '', content: '', tags: [] })
  tagInput.value = ''
}

async function toggleThumb(blog) {
  if (!requireLogin('请先登录后点赞')) return
  await guarded(async () => {
    if (blog.hasThumb) {
      await api.unthumb(blog.id)
      blog.hasThumb = false
      blog.thumbCount = Math.max((blog.thumbCount || 1) - 1, 0)
    } else {
      await api.thumb(blog.id)
      blog.hasThumb = true
      blog.thumbCount = (blog.thumbCount || 0) + 1
    }
  })
}

async function toggleFavorite(blog) {
  if (!requireLogin('请先登录后收藏')) return
  await guarded(async () => {
    if (blog.hasFavorite) {
      await api.unfavorite(blog.id)
      blog.hasFavorite = false
      blog.favoriteCount = Math.max((blog.favoriteCount || 1) - 1, 0)
    } else {
      await api.favorite(blog.id)
      blog.hasFavorite = true
      blog.favoriteCount = (blog.favoriteCount || 0) + 1
    }
    await loadFavorites()
  })
}

async function shareBlog(blog) {
  if (!requireLogin('请先登录后分享')) return
  await guarded(async () => {
    const url = `${window.location.origin}/blog/${blog.id}`
    await api.share(blog.id, url, 'web')
    blog.hasShare = true
    blog.shareCount = (blog.shareCount || 0) + 1
  }, '分享已记录')
}

async function postComment() {
  if (!requireLogin('请先登录后发表评论')) return
  if (!selectedBlog.value || !commentText.value) return
  await guarded(async () => {
    await api.addComment(selectedBlog.value.id, commentText.value)
    commentText.value = ''
    comments.value = await api.comments(selectedBlog.value.id)
  }, '评论成功')
}

async function postReply() {
  if (!requireLogin('请先登录后回复评论')) return
  if (!selectedBlog.value || !replyTarget.value || !replyText.value) return
  await guarded(async () => {
    await api.replyComment(selectedBlog.value.id, replyTarget.value.id, replyText.value)
    replyText.value = ''
    replyTarget.value = null
    comments.value = await api.comments(selectedBlog.value.id)
  }, '回复成功')
}

function flattenComments(list, depth = 0, result = []) {
  if (!Array.isArray(list)) return result
  for (const comment of list) {
    result.push({ comment, depth: Math.min(depth, 6) })
    if (comment.children?.length) {
      flattenComments(comment.children, depth + 1, result)
    }
  }
  return result
}

async function updateProfile() {
  if (!currentUser.value) return
  await guarded(async () => {
    await api.updateProfile({
      newUserName: profileForm.username || null,
      newAvatar: profileForm.avatar || null
    })
    setCurrentUser(await api.current())
    await refreshBlogs()
  }, '资料已保存')
}

async function loadFavorites() {
  if (!currentUser.value) {
    favoriteList.value = []
    return
  }
  favoriteList.value = await api.favorites()
}

async function loadMyBlogs() {
  if (!currentUser.value) {
    myBlogPool.value = []
    myBlogList.value = []
    return
  }
  const blogs = await api.authorBlogs(currentUser.value.id, 1, 60)
  myBlogPool.value = Array.isArray(blogs) ? blogs : []
  if (myBlogPageNo.value > totalMyBlogPages.value) {
    myBlogPageNo.value = totalMyBlogPages.value
  }
  myBlogList.value = sliceCurrentMyBlogPage(myBlogPool.value)
  await hydrateBlogStatuses(myBlogList.value)
}

function sliceCurrentMyBlogPage(blogs) {
  const start = (myBlogPageNo.value - 1) * myBlogPageSize
  return blogs.slice(start, start + myBlogPageSize)
}

async function changeMyBlogPage(page) {
  if (page < 1 || page > totalMyBlogPages.value || page === myBlogPageNo.value) return
  myBlogPageNo.value = page
  myBlogList.value = sliceCurrentMyBlogPage(myBlogPool.value)
  await hydrateBlogStatuses(myBlogList.value)
}

async function refreshMyBlogs() {
  myBlogPageNo.value = 1
  await loadMyBlogs()
}

function startEditBlog(blog) {
  editingBlogId.value = blog.id
  Object.assign(editBlogForm, {
    title: blog.title || '',
    category: blog.category || '',
    coverImage: blog.coverImage || '',
    summary: blog.summary || '',
    content: blog.content || '',
    tags: splitTags(blog.tag)
  })
  editTagInput.value = splitTags(blog.tag).join(', ')
  activeView.value = 'editBlog'
}

function cancelEditBlog() {
  editingBlogId.value = null
  Object.assign(editBlogForm, { title: '', category: '', coverImage: '', summary: '', content: '', tags: [] })
  editTagInput.value = ''
  activeView.value = 'myArticles'
}

async function updateBlog() {
  if (!editingBlogId.value) return
  await guarded(async () => {
    const tags = editTagInput.value.split(',').map(item => item.trim()).filter(Boolean)
    await updateBlogRequest(editingBlogId.value, { ...editBlogForm, tags })
    cancelEditBlog()
    await Promise.allSettled([loadMyBlogs(), refreshBlogs(), loadHot()])
    activeView.value = 'myArticles'
  }, '文章已修改')
}

async function updateBlogRequest(blogId, data) {
  if (typeof api.updateBlog === 'function') {
    return api.updateBlog(blogId, data)
  }
  const response = await fetch(`/api/blog/${blogId}/update`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    body: JSON.stringify(data)
  })
  const body = await response.json().catch(() => null)
  if (!response.ok || (body && typeof body.code === 'number' && body.code !== 200)) {
    throw new Error(body?.description || body?.msg || '修改文章失败')
  }
  return body?.data ?? body
}

async function deleteMyBlog(blog) {
  if (!blog?.id) return
  const confirmed = window.confirm(`确认删除《${blog.title || '未命名文章'}》吗？`)
  if (!confirmed) return
  await guarded(async () => {
    await api.deleteBlog(blog.id)
    if (selectedBlog.value?.id === blog.id) selectedBlog.value = null
    await Promise.allSettled([loadMyBlogs(), refreshBlogs(), loadHot(), loadFavorites()])
  }, '文章已删除')
}

async function loadAdminData() {
  if (!isAdmin.value) return
  const [usersResult, blogsResult] = await Promise.allSettled([
    api.adminUsers(),
    api.adminBlogs(1, 100)
  ])
  adminUsers.value = usersResult.status === 'fulfilled' && Array.isArray(usersResult.value) ? usersResult.value : []
  adminBlogs.value = blogsResult.status === 'fulfilled' && Array.isArray(blogsResult.value) ? blogsResult.value : []
}

async function toggleUserBan(user) {
  if (!isAdmin.value || !user?.id || user.id === currentUser.value?.id) return
  await guarded(async () => {
    if (Number(user.status || 0) === 1) {
      await api.unbanUser(user.id)
    } else {
      await api.banUser(user.id)
    }
    await loadAdminData()
  }, Number(user.status || 0) === 1 ? '用户已解封' : '用户已封禁')
}

async function toggleBlogPublish(blog) {
  if (!isAdmin.value || !blog?.id) return
  await guarded(async () => {
    if (Number(blog.status || 0) === 4) {
      await api.publishBlog(blog.id)
    } else {
      await api.offlineBlog(blog.id)
    }
    await Promise.allSettled([loadAdminData(), refreshBlogs(), loadHot()])
  }, Number(blog.status || 0) === 4 ? '文章已恢复发布' : '文章已下架')
}

function userStatusText(status) {
  return Number(status || 0) === 1 ? '已封禁' : '正常'
}

function blogStatusText(status) {
  const map = {
    0: '草稿',
    1: '已发布',
    2: '审核中',
    3: '未通过',
    4: '已下架'
  }
  return map[Number(status)] || '未知'
}

async function loadUnread() {
  if (!currentUser.value) return
  unreadCount.value = await api.unreadCount()
}

async function loadMessages() {
  if (!currentUser.value) {
    messages.value = []
    return
  }
  messages.value = await api.messages(1, 30)
  await loadUnread()
}

async function readMessage(message) {
  await guarded(async () => {
    await api.readMessage(message.id)
    message.isRead = 1
    await loadUnread()
  })
}

async function readAll() {
  await guarded(async () => {
    await api.readAll()
    messages.value = messages.value.map(item => ({ ...item, isRead: 1 }))
    unreadCount.value = 0
  }, '已全部标记为已读')
}

function clearUserState(blog) {
  return { ...blog, hasThumb: false, hasFavorite: false, hasShare: false }
}

function excerpt(value, length = 96) {
  if (!value) return '暂无摘要'
  const text = value.replace(/\s+/g, ' ').trim()
  return text.length > length ? `${text.slice(0, length)}...` : text
}

function splitTags(value) {
  if (!value) return []
  return String(value)
    .split(/[,，\s/|]+/)
    .map(item => item.trim())
    .filter(Boolean)
}

function markCoverBroken(blog) {
  if (blog) blog.coverBroken = true
}

function getInitial(value) {
  return String(value || 'U').slice(0, 1).toUpperCase()
}

function formatDate(value) {
  if (!value) return '刚刚'
  return new Date(value).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function number(value) {
  return Number(value || 0).toFixed(1)
}

onMounted(async () => {
  await restoreSession()
  await Promise.allSettled([refreshBlogs(), loadHot()])
  if (currentUser.value) await afterLogin()
})
</script>
