<template>
  <header class="ph">
    <router-link to="/" class="brand" aria-label="智能考试评测运维平台">
      <span class="brand-mark" aria-hidden="true">E</span>
      <span class="brand-copy">
        <span class="brand-title">智能考试评测运维平台</span>
        <span class="brand-sub">Exam Operations</span>
      </span>
    </router-link>
    <nav class="nav" aria-label="公共导航">
      <router-link to="/" class="nav-link" :class="{ active: route.path === '/' }">总览</router-link>
      <router-link to="/login" class="nav-link nav-cta" :class="{ active: route.path === '/login' }">进入系统</router-link>
    </nav>
  </header>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<style scoped>
.ph {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 28px;
  background: rgba(255, 255, 255, 0.88);
  border-bottom: 1px solid var(--ds-border);
  backdrop-filter: blur(18px);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  color: var(--ds-text);
  text-decoration: none;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: var(--ds-radius);
  color: #fff;
  background: linear-gradient(135deg, var(--ds-primary), var(--ds-info));
  box-shadow: 0 10px 22px rgba(15, 139, 141, 0.24);
  font-size: 17px;
  font-weight: 850;
  letter-spacing: 0;
}

.brand-copy {
  display: grid;
  gap: 2px;
}

.brand-title {
  overflow: hidden;
  color: var(--ds-text);
  font-size: 16px;
  font-weight: 850;
  line-height: 1.15;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.brand-sub {
  color: var(--ds-text-secondary);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.nav {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: var(--ds-radius-sm);
  color: var(--ds-text-secondary);
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
  transition: var(--ds-transition);
}

.nav-link:hover,
.nav-link.active {
  color: var(--ds-primary);
  background: var(--ds-primary-soft);
}

.nav-cta {
  color: #fff;
  background: var(--ds-text);
  box-shadow: 0 8px 18px rgba(27, 36, 48, 0.16);
}

.nav-cta:hover,
.nav-cta.active {
  color: #fff;
  background: var(--ds-primary);
}

@media (max-width: 640px) {
  .ph {
    height: 58px;
    padding: 0 14px;
  }

  .brand-sub {
    display: none;
  }

  .brand-title {
    max-width: 170px;
  }

  .nav-link {
    padding: 0 10px;
    font-size: 13px;
  }
}
</style>
