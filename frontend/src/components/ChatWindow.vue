<template>
  <div class="chat-window">
    <slot />
  </div>
</template>

<script setup lang="ts">
// ChatWindow is a wrapper component for the chat area
// Main logic is handled in Main.vue
</script>

<style scoped>
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
}
</style>
