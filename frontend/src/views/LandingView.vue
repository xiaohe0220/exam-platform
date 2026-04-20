<template>
  <div class="landing">
    <PublicHeader />
    <main class="hero-wrap">
      <div class="hero-bg" aria-hidden="true" />
      <section class="hero">
        <h1 class="hero-title">智能考试评测运维平台</h1>
        <p class="hero-sub">
          支持高并发在线答题，覆盖日常小测、期中/期末考、补考等全场景
        </p>
        <router-link to="/login" class="hero-cta">立即登录</router-link>
      </section>
    </main>
    <PublicFooter />
  </div>
</template>

<script setup>
import PublicHeader from '../components/public/PublicHeader.vue'
import PublicFooter from '../components/public/PublicFooter.vue'
</script>

<style scoped>
.landing {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
}
.hero-wrap {
  position: relative;
  flex: 1;
  min-height: calc(100vh - 56px - 280px);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(125deg, #3b6cff 0%, #6366f1 38%, #7c3aed 72%, #5b21b6 100%);
  opacity: 1;
}
.hero-bg::after {
  content: '';
  position: absolute;
  inset: -40%;
  background: radial-gradient(ellipse 80% 50% at 20% 40%, rgba(255, 255, 255, 0.15), transparent 55%),
    radial-gradient(ellipse 60% 40% at 80% 60%, rgba(255, 255, 255, 0.08), transparent 50%);
  pointer-events: none;
}
.hero {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 48px 24px 64px;
  max-width: 880px;
}
.hero-title {
  margin: 0 0 16px;
  font-size: clamp(26px, 5vw, 40px);
  font-weight: 800;
  color: #fff;
  letter-spacing: 0.04em;
  text-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}
.hero-sub {
  margin: 0 auto 32px;
  font-size: clamp(14px, 2.2vw, 17px);
  line-height: 1.75;
  color: rgba(255, 255, 255, 0.92);
  max-width: 640px;
}
.hero-cta {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 36px;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  text-decoration: none;
  background: rgba(49, 27, 108, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(8px);
  transition: background 0.2s ease, transform 0.15s ease;
}
.hero-cta:hover {
  background: rgba(49, 27, 108, 0.65);
  transform: translateY(-1px);
}
</style>
