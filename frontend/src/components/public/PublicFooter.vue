<template>
  <footer class="pf">
    <div class="pf-inner">
      <div class="pf-brand">
        <span class="pf-mark">E</span>
        <div>
          <div class="pf-title">智能考试评测运维平台</div>
          <p class="pf-desc">校内考试、阅卷、学情与审计统一入口。</p>
        </div>
      </div>
      <div class="pf-links">
        <router-link to="/login">登录入口</router-link>
        <router-link to="/">平台总览</router-link>
      </div>
      <div class="pf-meta">
        <span>服务状态：本地演示环境</span>
        <span>© 2026 Exam Operations</span>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.pf {
  margin-top: auto;
  padding: 24px 28px;
  background: #111719;
  color: rgba(255, 255, 255, 0.72);
}

.pf-inner {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) auto auto;
  align-items: center;
  gap: 24px;
  max-width: 1180px;
  margin: 0 auto;
}

.pf-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pf-mark {
  display: inline-flex;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  border-radius: var(--ds-radius);
  background: var(--ds-primary);
  color: #fff;
  font-weight: 850;
}

.pf-title {
  color: #fff;
  font-size: 14px;
  font-weight: 800;
}

.pf-desc {
  margin: 4px 0 0;
  color: rgba(255, 255, 255, 0.56);
  font-size: 12px;
}

.pf-links {
  display: flex;
  gap: 14px;
}

.pf-links a {
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
  text-decoration: none;
}

.pf-links a:hover {
  color: #fff;
}

.pf-meta {
  display: grid;
  justify-items: end;
  gap: 4px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

@media (max-width: 860px) {
  .pf-inner {
    grid-template-columns: 1fr;
    align-items: start;
  }

  .pf-meta {
    justify-items: start;
  }
}
</style>
