# Crest 前端工程

这是 Crest 的 Vue 3、TypeScript 和 Vite 前端工程。前端负责工作台、仪表板、数据大屏、数据准备、数据血缘、分享、导出中心和系统管理等页面。

## 产品边界

当前前端主路径：

- 工作台；
- 仪表板；
- 数据大屏；
- 分享；
- 数据准备；
- 数据集；
- 数据源；
- 数据血缘；
- 系统参数和字体管理。

当前不提供模板市场、工具箱、消息中心、独立移动端入口、地图类图表、地图运行时、外部插件入口、帮助中心和关于页。

分享功能属于 Crest 的稳定能力。`core_share`、`ShareTicket`、`de-link` 等名称与数据库表、接口路径和历史分享链接有关，调整前必须先设计迁移和兼容方案。

## 常用命令

安装依赖：

```bash
pnpm install --frozen-lockfile
```

构建前端：

```bash
pnpm run build:base
```

常规检查：

```bash
pnpm run lint:check
pnpm run build:lite:check
```

后端打包时会把前端 `dist` 拷贝到后端静态资源目录。只改前端后，如果要在 `http://localhost:8100` 的打包服务里看到效果，需要重新执行前端构建和后端打包。

## 品牌资源

Crest 品牌资源：

- 顶部导航 logo：`src/assets/img/crest-logo-horizontal-dark-192h.png`
- 登录页 logo：`src/assets/img/crest-logo-horizontal-192h.png`
- 浏览器图标：`public/favicon.svg`、`public/favicon.ico`
- Apple touch icon：`public/apple-touch-icon.png`

横版 logo 原图比例约为 `4.67:1`。顶部导航里使用 `158px x 34px`，并设置 `object-fit: contain`，不要改成会压缩图片比例的固定宽高。

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

页面默认选择 `数据源` 范围，并优先选中名称包含 `Demo`、`Crest` 或 `内置` 的数据源。字段筛选按“先表、后字段”工作：表下拉来自当前图里的物理表节点，字段下拉来自 `table -> table_field` 关系。

字段级过滤在前端完成。后端返回当前资源图，前端从选中字段出发，沿血缘边收集上游和下游节点，再把图收敛后交给 ECharts 渲染。

大图渲染有保护：节点超过 220 或边超过 420 时，图组件会关闭动画和拖拽，隐藏部分字段标签，减少页面卡顿。

功能说明见：

```text
../../docs/data-lineage.md
```

## CodeMirror 组件命名

项目依赖包里有 `codemirror`。本地组件不要命名为 `CodeMirror.vue`，否则在 macOS 默认大小写不敏感文件系统上，构建产物容易和依赖 chunk 撞名，导致运行时动态加载 404。

当前本地 SQL 编辑组件命名为：

```text
src/views/visualized/data/dataset/form/SqlCodeEditor.vue
```

添加引用继续使用这个文件名。
