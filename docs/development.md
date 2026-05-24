# Crest 开发说明

Crest 是 DataEase 2.10.22 的 GPLv3 派生项目。仓库保留上游许可和版权声明，产品品牌使用 Crest，并包含 OceanBase Oracle 模式数据源和字段级数据血缘能力。

## 目录

- `core/core-backend`：Spring Boot 后端服务和打包入口；
- `core/core-frontend`：Vue 前端工程，依赖锁定在 `pnpm-lock.yaml`；
- `sdk/extensions/extensions-datasource`：数据源扩展接口和 JDBC 数据源定义；
- `drivers`：随仓库维护的 JDBC 驱动，目前跟踪 `oceanbase-client-2.4.17.jar`；
- `installer`：安装脚本和 Crest Docker 部署模板；
- `docs/data-lineage.md`：字段级数据血缘说明；
- `.github/workflows/docker-publish.yml`：GHCR 镜像构建和发布流程。

## 工具链

- JDK 21；
- Maven 3.9 或兼容版本；
- Node.js 22；
- pnpm 11；
- Docker 与 Buildx。

## 依赖来源

Maven 使用仓库内 `.mvn/settings.xml`，通过公开 Maven 仓库解析依赖。后端依赖应来自公开 Maven 坐标、本仓库源码模块或明确可再分发的文件。

前端依赖使用 `core/core-frontend/.npmrc` 和 `registry.npmjs.org`。主前端依赖以 `core/core-frontend/pnpm-lock.yaml` 为准，`flushbonading` 子包以自己的 `package-lock.json` 为准。

不要引入私有制品仓库、不可控镜像源或无法公开再分发的依赖。

## 常用命令

编译数据源扩展相关模块：

```bash
mvn -pl sdk/extensions/extensions-datasource -am \
  -DskipTests -Dmaven.test.skip=true -Dmaven.antrun.skip=true \
  test-compile
```

构建前端：

```bash
cd core/core-frontend
pnpm install --frozen-lockfile
pnpm run build:base
```

打包后端：

```bash
mvn -pl :core-backend -am clean package -Pstandalone -DskipTests -Dmaven.test.skip=true
```

构建本地镜像：

```bash
docker build -t crest:local .
```

通过 GitHub Actions 发布镜像：

1. 打开 GitHub 仓库的 `Actions`；
2. 选择 `Build and Publish Docker Images`；
3. 手动运行 workflow，并按需填写 `image_tag`。

## OceanBase Oracle

数据源类型为 `obOracle`，驱动类为：

```text
com.oceanbase.jdbc.Driver
```

支持的用户名格式：

- OBServer 直连：`username@tenant`
- OBProxy/ODP：`username@tenant#cluster`

Schema 留空时，系统默认使用用户名中 `@` 或 `#` 前面的账号名，并按 Oracle 风格转为大写。

相关功能至少覆盖这些回归场景：

- OBServer 直连，例如 `test@obora` + `2881`；
- OBProxy/ODP，例如 `test@obora#obdemo` + `2883`；
- 表和字段元数据读取，包含字段备注；
- 数据集预览、全量缓存同步、增量缓存同步和仪表板查询。

## 产品边界

Crest 保持 BI 主链路清晰：

- 数据源、数据集、数据集缓存同步；
- 字段级数据血缘；
- 图表编辑、仪表板、数据大屏；
- 分享页、导出中心；
- 登录、用户、角色、系统参数；
- OceanBase Oracle 模式数据源。

当前不提供 SQLBot、模板市场、工具箱、消息中心、独立移动端入口、地图类图表、地图运行时、地图 API、演示模板资源、帮助中心、关于页和外部插件入口。

分享模块保留 `core_share`、`CoreShare*` 和 `io.crest.api.share` 等历史兼容名称。这些名称关联数据库表、接口路径、Mapper 语句和旧分享链接。调整前必须先设计迁移、回滚和链接兼容方案。

## 部署命名

新安装默认使用 Crest 命名：

- 运行目录：`/opt/crest`
- 控制命令：`crestctl`
- systemd 服务：`crest.service`
- Docker 服务和容器：`crest`
- Docker 网络：`crest-network`
- 默认数据库：`crest`

安装脚本仍识别旧运行目录和旧控制命令，用于存量环境升级。不要在同一个版本里移除这些兼容判断。

## 数据库迁移与初始数据

后端使用 Flyway 管理数据库结构和初始化数据，脚本目录为：

```text
core/core-backend/src/main/resources/db/migration
```

当前迁移从 `V2.0` 开始，新增脚本必须追加在现有最高版本之后。不要在已有版本之后补一个 `V1.x` 脚本，除非整个迁移历史重新基线化；否则空库安装时脚本执行顺序会早于建表脚本，升级环境也容易出现不可控差异。

新安装的初始状态由这些脚本共同保证：

- `V2.6__ddl.sql`：创建官方茶饮示例表和基础数据集；
- `V2.10.22.8__clean_initial_metadata.sql`：清理运行残留，重置默认管理员为 `admin/admin`；
- `V2.10.22.9__seed_crest_lineage_demo.sql`：创建 Crest 茶饮经营血缘样例。

初始化脚本可以添加演示数据和必要元数据，但不能删除用户真实资源。清理类迁移只处理运行日志、最近使用、收藏、导出任务、临时任务记录和默认账号这类安装态数据。

每次调整迁移脚本后，都要至少做一次空库安装验证：

```bash
docker exec dataease-mysql-local mysql -uroot -pPassword123@mysql \
  -e "DROP DATABASE IF EXISTS crest_simplify; CREATE DATABASE crest_simplify DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
launchctl kickstart -k gui/$(id -u)/com.crest.local
```

验证点：

- `de_standalone_version` 最新版本成功；
- 只有一个默认管理员账号；
- `core_opt_recent`、`core_export_task` 等运行残留为空；
- `Demo` 数据源存在；
- `茶饮经营全链路分析` 能被数据集血缘接口读取；
- `index.html` 和 `doc.html` 返回 200；
- `error.log` 为空。

## 数据血缘

数据血缘属于 Crest 主功能，不作为外部插件加载。

后端入口：

- `core/core-backend/src/main/java/io/crest/relation/server/RelationServer.java`
- `core/core-backend/src/main/java/io/crest/relation/manage/RelationManage.java`
- `core/core-backend/src/main/java/io/crest/relation/dto/*`

前端入口：

- `core/core-frontend/src/api/relation/index.ts`
- `core/core-frontend/src/views/visualized/data/lineage/index.vue`
- `core/core-frontend/src/components/relation-chart/GraphView.vue`

菜单迁移：

- `core/core-backend/src/main/resources/db/migration/V2.10.22.5__data_lineage_menu.sql`
- `core/core-backend/src/main/resources/db/desktop/V2.10.22.5__data_lineage_menu.sql`

血缘图只使用 Crest 元数据，打开页面时不连接业务数据源。当前链路模型：

```text
datasource -> table -> table_field -> dataset_field -> dataset -> chart_field -> chart -> dv
```

后端、前端和文档保持同一组节点类型：

- `datasource`
- `table`
- `table_field`
- `dataset_field`
- `dataset`
- `chart_field`
- `chart`
- `dv`

`RelationManage` 同时实现 `RelationApi`，数据源和数据集的下线检查复用同一套图谱逻辑：

- `getDsResource(id)`：检查数据源下游使用情况；
- `getDatasetResource(id)`：检查数据集下游图表和展示资源。

相关功能至少覆盖这些回归场景：

- 全局概览能加载；
- 数据源级字段血缘能加载；
- 数据集级字段血缘能加载；
- 仪表板和数据大屏血缘能加载；
- 表选择器列出当前图里的全部物理表；
- 字段选择器跟随所选表更新；
- 选择字段后图谱收敛到上下游节点；
- 关键字搜索能命中字段和资源；
- 被使用的数据源或数据集下线前能给出影响范围；
- 仪表板和数据大屏页面能正常打开。

用户文档是 `docs/data-lineage.md`。血缘语义、接口、节点类型或边界发生变化时，同步更新该文档。

## 工作区

不要提交本地构建或运行输出：

- `node_modules`
- Maven `target` 目录
- `.flattened-pom.xml`
- `runtime`
- 运行日志、pid、压缩包、浏览器测试输出和 IDE 文件

清理被忽略文件前先预览：

```bash
git clean -fdXn
```

确认后再执行：

```bash
git clean -fdX
```

## 提交前检查

- 代码范围保持清楚，避免把无关内容混在同一个提交；
- 优先沿用现有工程模式；
- 根据影响范围执行 Maven、前端构建或服务级验证；
- 涉及构建、部署、依赖、OceanBase、默认账号、镜像或数据血缘时，同步更新 README 或对应文档。
