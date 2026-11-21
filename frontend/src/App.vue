<template>
  <!-- 单应用双身份：所有页面由路由驱动，App 只做挂载容器 -->
  <RouterView />
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>
