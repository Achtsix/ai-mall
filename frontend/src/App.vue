<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
body { background: #f3f7fb; color: #10243a; }
/* Navigation and action labels should never enter the browser text-selection state on click. */
body, #app, #app button, #app a, #app label, #app [role='button'], #app .el-menu-item, #app .el-sub-menu__title, #app .el-tabs__item, #app .el-button, #app .el-tag { user-select: none; -webkit-user-select: none; }
#app input, #app textarea, #app .el-input__inner, #app .el-textarea__inner, #app [contenteditable='true'] { user-select: text; -webkit-user-select: text; }
body ::selection { background: transparent; color: inherit; }
body input::selection, body textarea::selection, body .el-input__inner::selection, body .el-textarea__inner::selection { background: #b8e9f2; color: inherit; }
#app button, #app a, #app .el-menu-item, #app .el-sub-menu__title, #app .el-tabs__item { -webkit-tap-highlight-color: transparent; }
.el-menu-item, .el-sub-menu__title, .el-tabs__item, .el-button { transition: none !important; }
.el-button--primary { --el-button-bg-color: #159fba; --el-button-border-color: #159fba; --el-button-hover-bg-color: #118eab; --el-button-hover-border-color: #118eab; }
.el-card { border-color: #e4edf3; }
</style>
