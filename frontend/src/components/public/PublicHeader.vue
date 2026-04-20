<template>
  <header class="ph">
    <router-link to="/" class="ph-brand">
      <span class="ph-cap" aria-hidden="true">🎓</span>
      <span class="ph-title">智能考试评测运维平台</span>
    </router-link>
    <nav class="ph-nav">
      <router-link to="/login" class="ph-login">登录</router-link>
    </nav>
  </header>
</template>

<style scoped>
.ph {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}
.ph-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: #111827;
  font-weight: 700;
  font-size: 17px;
  letter-spacing: 0.02em;
}
.ph-cap {
  font-size: 22px;
  line-height: 1;
}
.ph-login {
  font-size: 15px;
  color: #374151;
  text-decoration: none;
  font-weight: 500;
}
.ph-login:hover {
  color: #4f46e5;
}
</style>
