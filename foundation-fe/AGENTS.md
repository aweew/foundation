# 前端开发规范

## Vue 单文件组件结构

- Vue 单文件组件统一按 `<template>` → `<script setup lang="ts">` → `<style scoped>` 的顺序组织
- 页面结构放在最前面，页面逻辑放在中间，页面样式放在最后
- 新增或修改 Vue 文件时遵循此顺序；没有逻辑或样式时可省略对应区块，不添加空区块

```vue
<template>
  <!-- 页面结构 -->
</template>

<script setup lang="ts">
// 页面逻辑
</script>

<style scoped>
/* 页面样式 */
</style>
```

## Vue 模板排版

- 使用 2 个空格缩进，嵌套层级清晰对齐
- 包含多个子元素的容器，开始标签、各子元素、结束标签分别独占一行，避免将多个标签挤在同一行
- 同一层级连续出现多个相同的独立区块标签时，例如 `el-form-item`、`el-card`，各区块之间空一行，方便区分表单项或卡片
- 简短文本或简单插值与其所属标签保持同一行，例如 `<strong>{{ auth.menus.length }}</strong>`；超过行宽时再换行
- `<script>`、`<style>` 的开始和结束标签独占一行
- 使用项目 Prettier 配置格式化代码

```vue
<el-card>
  <span class="stat-label">可访问菜单</span>
  <strong>{{ auth.menus.length }}</strong>
  <small>当前账号权限范围</small>
</el-card>

<el-card>
  <span class="stat-label">权限数量</span>
  <strong>{{ auth.permissions.length }}</strong>
  <small>按钮与接口权限</small>
</el-card>
```

## API 调用与接口类型分离

- 按业务模块组织 API 目录
- 每个模块使用 `src/api/<模块>/index.ts` 定义 API 调用，使用同目录下的 `types.ts` 定义请求参数、响应数据及相关业务接口类型
- `index.ts` 只定义 API 调用，不定义或转导出业务 `interface`、`type`
- 类型统一使用 `import type` 引入；调用方从模块目录导入 API 函数，从模块的 `types.ts` 导入类型
- 通用响应包装、分页响应等公共类型放在 `src/types/api.ts`，业务类型放在所属模块的 `types.ts`
- 新增或调整 API 时同步更新相关类型及调用方引用

```ts
import { login } from '@/api/auth'
import type { LoginRequest } from '@/api/auth/types'
```

## API 请求方法写法

- API 函数统一使用箭头函数、显式函数体和 `return request<T>({ ... })`，不使用 `request.get()`、`request.post()` 等快捷方法
- 请求配置分行书写，显式指定 `url` 和小写的 `method`；查询参数使用 `params`，请求体使用 `data`
- 保留明确的响应泛型类型，确保调用方获得正确的类型提示
- 每个 API 函数添加 JSDoc，说明业务用途；有参数时使用 `@param` 说明参数含义

```ts
/**
 * 用户登录
 * @param payload 登录参数
 */
export const login = (payload: LoginRequest) => {
  return request<ApiResult<LoginResponse>>({
    url: '/auth/login',
    method: 'post',
    data: payload,
  })
}
```
