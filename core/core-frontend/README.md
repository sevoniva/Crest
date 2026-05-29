# Crest 前端工程

这是 Crest 的 Vue 3.3、TypeScript 和 Vite 前端工程。前端负责工作台、数据源、数据集、数据血缘、仪表盘、数据大屏、分享、导出中心、修改密码和系统管理等页面。

## 技术栈

| 技术 | 用途 |
| --- | --- |
| Vue 3.3 | 页面和组件开发 |
| Vite | 开发服务和构建 |
| TypeScript | 类型约束 |
| Element Plus Secondary | 基础 UI 组件 |
| Pinia | 状态管理 |
| vxe-table | 表格 |
| ECharts / AntV / S2 | 图表和可视化 |
| pnpm | 依赖管理 |

## 常用命令

安装依赖：

```bash
pnpm install --frozen-lockfile
```

开发模式：

```bash
pnpm dev
```

快速开发模式，跳过部分 Vite 检查：

```bash
pnpm dev:fast
```

构建前端：

```bash
pnpm run build:base
```

轻量构建检查：

```bash
pnpm run build:lite:check
```

严格构建：

```bash
pnpm run build:base:strict
```

Lint：

```bash
pnpm run lint:check
pnpm run lint:stylelint
```

后端打包时会把前端 `dist` 拷贝到后端静态资源目录。只改前端后，如果要在 `http://localhost:8100` 的打包服务里看到效果，需要重新执行前端构建和后端打包。

## 产品边界

当前前端主路径：

- 工作台；
- 数据源；
- 数据集；
- 数据血缘；
- 仪表盘；
- 数据大屏；
- 分享；
- 数据导出中心；
- 修改密码；
- 系统参数、分享管理、站点设置、用户管理和字体管理。

当前不提供模板市场、工具箱、消息中心、独立移动端入口、地图类图表、地图运行时、外部插件入口、帮助中心和关于页。不要新增不可运行的菜单入口。

分享功能属于稳定能力。`core_share`、`ShareTicket`、`de-link` 等名称与数据库表、接口路径和历史分享链接有关，调整前必须先设计迁移和兼容方案。

## 主题和视觉约定

当前 Crest 管理端采用浅色工作台风格：

| Token | 值 |
| --- | --- |
| 主色 | `#3B82F6` |
| ink | `#0F172A` |
| ink2 | `#334155` |
| mute | `#64748B` |
| subt | `#94A3B8` |
| line | `#E2E8F0` |
| soft | `#F1F5F9` |
| bg | `#F8FAFC` |
| card | `#FFFFFF` |

类型色：

| 类型 | 颜色 |
| --- | --- |
| 仪表盘 | `#3B82F6` |
| 数据大屏 | `#10B981` |
| 数据集 | `#8B5CF6` |
| 数据源 | `#F59E0B` |

约定：

- 顶栏使用统一 AppHeader；
- Logo 点击返回工作台；
- 主导航激活态使用蓝色下划线；
- 列表、搜索、分页、类型 tag、头像和相对时间优先复用已有样式；
- 系统管理、数据准备、数据血缘、仪表盘和数据大屏列表页保持同一套浅色管理端风格；
- 大屏运行态和编辑画布本身不套管理端卡片样式，避免破坏大屏展示；
- 页面切换动效保持轻量，不能影响 BI 管理系统的操作效率。

## 品牌资源

Crest 品牌资源：

| 资源 | 路径 |
| --- | --- |
| 顶部导航 Logo | `src/assets/img/crest-logo-horizontal-dark-192h.png` |
| 登录页 Logo | `src/assets/img/crest-logo-horizontal-192h.png` |
| SVG Logo | `src/assets/svg/logo.svg` |
| 浏览器图标 | `public/favicon.svg`、`public/favicon.ico` |
| Apple touch icon | `public/apple-touch-icon.png` |

横版 Logo 原图比例约为 `4.67:1`。导航中应使用 `object-fit: contain`，不要用会拉伸图片比例的固定宽高。

## 数据血缘前端

入口页面：

```text
src/views/visualized/data/lineage/index.vue
```

图组件：

```text
src/components/relation-chart/GraphView.vue
```

接口封装：

```text
src/api/relation/index.ts
```

页面默认选择 `数据源` 范围，并优先选中名称包含 `Crest`、`内置` 或 `演示` 的数据源。字段筛选按“先表、后字段”工作：表下拉来自当前图里的物理表节点，字段下拉来自 `table -> table_field` 关系。

字段级过滤在前端完成。后端返回当前资源图，前端从选中字段出发，沿血缘边收集上游和下游节点，再把图收敛后交给 ECharts 渲染。

大图渲染保护：

- 节点超过 220 或边超过 420 时关闭动画和拖拽；
- 大图下隐藏部分字段标签；
- 图容器使用稳定尺寸，避免首次打开时被压成方形；
- 右侧资源和依赖表格使用统一浅色表格样式。

功能说明见：

```text
../../docs/data-lineage.md
```

## 工作台数据约定

工作台展示的数据来自后端接口和元数据库，不在前端写死创建人、最近编辑人、收藏或最近使用数据。

资源列表中：

- 创建人和最近编辑人头像按接口返回的用户信息生成；
- 相对时间由实际更新时间计算；
- 左侧资源统计从资源数量接口读取；
- 统计线条应根据近 7 天资源变化或接口趋势数据绘制，不能固定为静态线段；
- 操作按钮默认展示，不依赖 hover 才出现。

## 数据大屏和仪表盘页面约定

资源管理页面可以使用统一管理端风格；大屏编辑画布和大屏运行态不能被统一样式覆盖。

修改以下区域时要特别检查：

- 资源列表左侧边栏收起/展开；
- 数据准备下拉菜单；
- 数据导出中心抽屉；
- 大屏运行态全屏、缩放和导出按钮；
- 图表下钻、明细弹窗和导出动作。

## CodeMirror 组件命名

项目依赖包里有 `codemirror`。本地组件不要命名为 `CodeMirror.vue`，否则在 macOS 默认大小写不敏感文件系统上，构建产物容易和依赖 chunk 撞名，导致运行时动态加载 404。

当前 SQL 编辑组件命名为：

```text
src/views/visualized/data/dataset/form/SqlCodeEditor.vue
```

新增引用继续使用这个文件名。

## 提交前检查

前端改动提交前按范围检查：

| 改动 | 检查 |
| --- | --- |
| 页面样式或组件 | `pnpm run build:lite:check` |
| 图表组件 | 打开对应图表，检查空数据、加载、错误和下钻状态 |
| 工作台、系统管理、数据血缘 | 检查宽屏、普通桌面和较窄窗口布局 |
| 资源管理列表 | 检查分页、搜索、筛选、收起边栏和操作按钮 |
| 大屏相关 | 同时检查编辑态和运行态 |

不要提交：

- `node_modules/`
- `dist/`
- `.stylelintcache`
- `components.d.ts`
- 浏览器截图和临时验证目录
