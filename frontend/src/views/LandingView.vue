<template>
  <div class="landing">
    <PublicHeader />
    <main class="hero">
      <section class="hero-copy">
        <div class="status-line">
          <span class="status-dot" />
          <span>统一考试入口</span>
          <span class="status-divider" />
          <span>本地演示已就绪</span>
        </div>
        <h1>智能考试评测运维平台</h1>
        <p class="lead">
          为教务、教师和学生提供同一套考试工作流：发布、作答、阅卷、分析与审计集中处理。
        </p>
        <div class="hero-actions">
          <router-link to="/login" class="btn btn-primary">进入系统</router-link>
          <a href="#workspace" class="btn btn-secondary">查看工作台</a>
        </div>
      </section>

      <section class="console" aria-label="平台概览">
        <div class="console-top">
          <span class="window-dot" />
          <span class="window-dot amber" />
          <span class="window-dot green" />
          <span class="console-title">ExamOps Live</span>
        </div>
        <div class="console-grid">
          <article class="console-main">
            <div class="panel-head">
              <span>当前考试</span>
              <strong>进行中</strong>
            </div>
            <div class="exam-title">人工智能通识 · 样例期末考试</div>
            <div class="progress-row">
              <span>答题进度</span>
              <span>62%</span>
            </div>
            <div class="bar"><span /></div>
            <div class="mini-stats">
              <div><strong>128</strong><span>在线</span></div>
              <div><strong>3</strong><span>切屏阈值</span></div>
              <div><strong>60</strong><span>分钟</span></div>
            </div>
          </article>
          <article class="console-side">
            <span class="side-label">阅卷队列</span>
            <strong>24</strong>
            <p>主观题等待教师处理</p>
          </article>
          <article class="console-side warm">
            <span class="side-label">平台告警</span>
            <strong>0</strong>
            <p>当前无阻断异常</p>
          </article>
        </div>
      </section>
    </main>

    <section id="workspace" class="workspace">
      <div class="section-head">
        <span class="eyebrow">Workspace</span>
        <h2>三类角色，一套界面语言</h2>
      </div>
      <div class="role-grid">
        <article v-for="role in roles" :key="role.title" class="role-card">
          <span class="role-k">{{ role.k }}</span>
          <h3>{{ role.title }}</h3>
          <p>{{ role.desc }}</p>
        </article>
      </div>
    </section>

    <PublicFooter />
  </div>
</template>

<script setup>
import PublicHeader from '../components/public/PublicHeader.vue'
import PublicFooter from '../components/public/PublicFooter.vue'

const roles = [
  { k: '01', title: '学生端', desc: '考试大厅、全屏答题、成绩趋势和错题整理集中展示。' },
  { k: '02', title: '教师端', desc: '题库、组卷、发布、统计和主观题批阅保留清晰操作路径。' },
  { k: '03', title: '教务端', desc: '用户导入、平台设置、审计与监控围绕运维效率组织。' }
]
</script>

<style scoped>
.landing {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    linear-gradient(180deg, rgba(15, 139, 141, 0.08), transparent 430px),
    var(--ds-bg);
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 0.92fr) minmax(420px, 1fr);
  gap: 36px;
  align-items: center;
  width: min(1180px, calc(100% - 40px));
  min-height: calc(100vh - 126px);
  margin: 0 auto;
  padding: 48px 0 40px;
}

.hero-copy h1 {
  margin: 18px 0 16px;
  color: var(--ds-text);
  font-size: clamp(34px, 5vw, 56px);
  font-weight: 900;
  line-height: 1.08;
  letter-spacing: 0;
}

.lead {
  max-width: 560px;
  margin: 0 0 28px;
  color: var(--ds-text-secondary);
  font-size: 17px;
  line-height: 1.75;
}

.status-line {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 32px;
  padding: 0 12px;
  border: 1px solid var(--ds-border);
  border-radius: 999px;
  color: var(--ds-text-secondary);
  background: rgba(255, 255, 255, 0.78);
  font-size: 13px;
  font-weight: 700;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--ds-success);
  box-shadow: 0 0 0 4px var(--ds-success-soft);
}

.status-divider {
  width: 1px;
  height: 14px;
  background: var(--ds-border);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  padding: 0 20px;
  border-radius: var(--ds-radius);
  font-size: 15px;
  font-weight: 800;
  text-decoration: none;
  transition: var(--ds-transition);
}

.btn:hover {
  transform: translateY(-1px);
}

.btn-primary {
  color: #fff;
  background: var(--ds-primary);
  box-shadow: 0 12px 24px rgba(15, 139, 141, 0.24);
}

.btn-secondary {
  color: var(--ds-text);
  background: #fff;
  border: 1px solid var(--ds-border);
}

.console {
  overflow: hidden;
  border: 1px solid var(--ds-border);
  border-radius: var(--ds-radius);
  background: #111719;
  box-shadow: var(--ds-shadow-lg);
}

.console-top {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 42px;
  padding: 0 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.64);
  font-size: 12px;
  font-weight: 800;
}

.window-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #d94b4b;
}

.window-dot.amber {
  background: var(--ds-warning);
}

.window-dot.green {
  background: var(--ds-success);
}

.console-title {
  margin-left: 8px;
}

.console-grid {
  display: grid;
  grid-template-columns: 1.4fr 0.8fr;
  gap: 14px;
  padding: 16px;
}

.console-main,
.console-side {
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: var(--ds-radius);
  background: rgba(255, 255, 255, 0.055);
}

.console-main {
  grid-row: span 2;
  padding: 18px;
}

.panel-head,
.progress-row,
.mini-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-head span,
.progress-row,
.side-label,
.console-side p,
.mini-stats span {
  color: rgba(255, 255, 255, 0.58);
  font-size: 12px;
}

.panel-head strong {
  color: var(--ds-success);
  font-size: 12px;
}

.exam-title {
  margin: 26px 0 22px;
  color: #fff;
  font-size: 24px;
  font-weight: 850;
  line-height: 1.35;
}

.bar {
  height: 10px;
  margin-top: 10px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.09);
}

.bar span {
  display: block;
  width: 62%;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--ds-primary), var(--ds-accent));
}

.mini-stats {
  gap: 10px;
  margin-top: 26px;
}

.mini-stats div {
  flex: 1;
  padding: 12px;
  border-radius: var(--ds-radius-sm);
  background: rgba(255, 255, 255, 0.06);
}

.mini-stats strong {
  display: block;
  color: #fff;
  font-size: 20px;
}

.console-side {
  padding: 16px;
}

.console-side strong {
  display: block;
  margin-top: 16px;
  color: #fff;
  font-size: 34px;
  line-height: 1;
}

.console-side p {
  margin: 12px 0 0;
}

.console-side.warm strong {
  color: var(--ds-warning);
}

.workspace {
  width: min(1180px, calc(100% - 40px));
  margin: 0 auto 56px;
}

.section-head {
  margin-bottom: 18px;
}

.eyebrow {
  color: var(--ds-primary);
  font-size: 12px;
  font-weight: 850;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.section-head h2 {
  margin: 8px 0 0;
  color: var(--ds-text);
  font-size: 26px;
  font-weight: 900;
}

.role-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.role-card {
  min-height: 166px;
  padding: 20px;
  border: 1px solid var(--ds-border);
  border-radius: var(--ds-radius);
  background: #fff;
  box-shadow: var(--ds-shadow-sm);
}

.role-k {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 26px;
  border-radius: var(--ds-radius-sm);
  color: var(--ds-primary);
  background: var(--ds-primary-soft);
  font-size: 12px;
  font-weight: 850;
}

.role-card h3 {
  margin: 18px 0 8px;
  color: var(--ds-text);
  font-size: 17px;
  font-weight: 850;
}

.role-card p {
  margin: 0;
  color: var(--ds-text-secondary);
  font-size: 14px;
  line-height: 1.65;
}

@media (max-width: 980px) {
  .hero {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .console {
    max-width: 680px;
  }
}

@media (max-width: 720px) {
  .console-grid,
  .role-grid {
    grid-template-columns: 1fr;
  }

  .console-main {
    grid-row: auto;
  }

  .mini-stats {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
