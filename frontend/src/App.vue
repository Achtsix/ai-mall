<template>
  <router-view />
</template>

<script setup>
</script>

<style>
:root {
  --brand: #f2553d;
  --brand-hover: #dd3f2c;
  --brand-soft: #fff0ed;
  --brand-border: #ffc8be;
  --ink: #20242b;
  --muted: #78808c;
  --panel: #ffffff;
  --page: #f5f6f8;
  --line: #e6e8ec;
  --tech: #171b21;
  --tech-soft: #232932;
  --ai-cyan: #19a7b8;
  --el-color-primary: #f2553d;
  --el-color-primary-light-3: #f67f6d;
  --el-color-primary-light-5: #f9a99c;
  --el-color-primary-light-7: #fcd0c8;
  --el-color-primary-light-8: #fde2dd;
  --el-color-primary-light-9: #fff0ed;
  --el-color-primary-dark-2: #cf3d2a;
  --el-border-radius-base: 6px;
}
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
body { background: var(--page); color: var(--ink); }
/* Navigation and action labels should never enter the browser text-selection state on click. */
body, #app, #app button, #app a, #app label, #app [role='button'], #app .el-menu-item, #app .el-sub-menu__title, #app .el-tabs__item, #app .el-button, #app .el-tag { user-select: none; -webkit-user-select: none; }
#app input, #app textarea, #app .el-input__inner, #app .el-textarea__inner, #app [contenteditable='true'] { user-select: text; -webkit-user-select: text; }
body ::selection { background: transparent; color: inherit; }
body input::selection, body textarea::selection, body .el-input__inner::selection, body .el-textarea__inner::selection { background: #ffd4cc; color: inherit; }
#app button, #app a, #app .el-menu-item, #app .el-sub-menu__title, #app .el-tabs__item { -webkit-tap-highlight-color: transparent; }
.el-menu-item, .el-sub-menu__title, .el-tabs__item, .el-button { transition: none !important; }
.el-button--primary { --el-button-bg-color: var(--brand); --el-button-border-color: var(--brand); --el-button-hover-bg-color: var(--brand-hover); --el-button-hover-border-color: var(--brand-hover); }
.el-card { border-color: var(--line); border-radius: 8px; }
.el-input__wrapper.is-focus, .el-select__wrapper.is-focused { box-shadow: 0 0 0 1px var(--brand) inset !important; }
.el-pagination.is-background .el-pager li.is-active { background: var(--brand); }
.el-tabs__item.is-active, .el-tabs__item:hover { color: var(--brand); }
.el-tabs__active-bar { background: var(--brand); }
.el-switch.is-checked .el-switch__core { border-color: var(--brand); background: var(--brand); }
.el-tag:not(.el-tag--success):not(.el-tag--danger):not(.el-tag--warning):not(.el-tag--info) { --el-tag-text-color: var(--brand); --el-tag-bg-color: var(--brand-soft); --el-tag-border-color: var(--brand-border); }
</style>
