<template>
  <footer class="pf">
    <div class="pf-inner">
      <div class="pf-col">
        <h3 class="pf-h">智能考试平台</h3>
        <p class="pf-p">
          面向校内的稳定机考与运维能力，支持大规模并发与全流程考试管理，保障数据安全与考试公平。
        </p>
      </div>
      <div class="pf-col">
        <h3 class="pf-h">快速链接</h3>
        <a class="pf-link" href="javascript:void(0)" @click.prevent>问题反馈</a>
        <a class="pf-link" href="javascript:void(0)" @click.prevent>用户手册</a>
        <router-link to="/login" class="pf-link">登录入口</router-link>
      </div>
      <div class="pf-col">
        <h3 class="pf-h">联系我们</h3>
        <p class="pf-p">邮箱：support@example.com</p>
        <p class="pf-p">电话：123-4567-8910</p>
      </div>
    </div>
    <p class="pf-copy">© 2026 智能考试评测运维平台 · 保留所有权利</p>
  </footer>
</template>

<style scoped>
.pf {
  background: #16181f;
  color: #9ca3af;
  padding: 48px 24px 28px;
  margin-top: auto;
}
.pf-inner {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}
@media (max-width: 768px) {
  .pf-inner {
    grid-template-columns: 1fr;
  }
}
.pf-h {
  margin: 0 0 14px;
  font-size: 15px;
  font-weight: 600;
  color: #e5e7eb;
}
.pf-p {
  margin: 0 0 8px;
  font-size: 13px;
  line-height: 1.65;
  max-width: 320px;
}
.pf-link {
  display: block;
  margin-bottom: 10px;
  font-size: 14px;
  color: #d1d5db;
  text-decoration: none;
}
.pf-link:hover {
  color: #a5b4fc;
}
.pf-copy {
  text-align: center;
  margin: 36px 0 0;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 12px;
  color: #6b7280;
}
</style>
