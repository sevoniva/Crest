# Crest 开发说明

Crest 是 DataEase 2.10.22 的 GPLv3 派生项目。仓库保留上游许可和版权声明，产品品牌使用 Crest，并包含 OceanBase Oracle 模式数据源、字段级数据血缘和内置演示看板能力。

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

Crest 保持 BI 主链路明确：

- 数据源、数据集、数据集缓存同步；
- 字段级数据血缘；
- 图表编辑、仪表板、数据大屏；
- 分享页、导出中心；
- 登录、用户、角色、系统参数；
- OceanBase Oracle 模式数据源。

当前不提供 SQLBot、模板市场、工具箱、消息中心、独立移动端入口、地图类图表、地图运行时、地图 API、帮助中心、关于页和外部插件入口。

分享模块保留 `core_share`、`CoreShare*` 和 `io.crest.api.share` 等历史兼容名称。这些名称关联数据库表、接口路径、Mapper 语句和旧分享链接。调整前必须先设计迁移、回滚和链接兼容方案。

## 接口文档

接口文档由 Springdoc 和 Knife4j 在运行时生成：

```text
/doc.html
/v3/api-docs
```

文档配置在：

```text
sdk/common/src/main/java/io/crest/doc/SwaggerConfig.java
```

添加后端接口时，同步检查三件事：

- 所在包是否已经进入对应的 `GroupedOpenApi` 分组；
- 控制器是否有清楚的 `@Tag` 和 `@Operation`；
- 请求和响应对象是否有必要的 `@Schema` 说明。

当前分组以运行模块为准：可视化、图表、数据集、数据源、数据血缘、导出中心、系统管理、权限管理和同步管理。移除的功能不再出现在接口文档分组里。

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

当前迁移目录包含 Crest 的初始化基线和公开演示资源：

- `V1.1__initial_schema.sql`：创建当前运行所需的全部表结构，写入默认管理员、基础菜单、系统参数、内置驱动和必要主题配置；
- `V1.2__demo_retail_dashboard.sql`：创建 `crest_demo_retail` 零售演示库，写入演示数据源、数据集、图表和数据大屏。

演示数据只用于新用户理解产品主链路，必须保持环境无关：不能写入本地 IP、个人账号、压测数据、外部库连接串或临时资源。应用启动时会根据当前元数据库连接信息同步演示数据源地址，避免在 SQL 里写死容器名或宿主机地址。

后续版本涉及数据库结构或必要初始化数据时，在已发布脚本之后添加新的迁移脚本，不直接改已发布基线。脚本内容要保持可审计：只处理产品运行必需的数据和公开演示资源。

每次调整迁移脚本后，都要至少做一次空库安装验证：

```bash
docker exec crest-mysql-local mysql -uroot -pPassword123@mysql \
  -e "DROP DATABASE IF EXISTS crest_simplify; CREATE DATABASE crest_simplify DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
launchctl kickstart -k gui/$(id -u)/com.crest.local
```

验证点：

- `de_standalone_version` 最新迁移成功，包含 `1.2:demo retail dashboard`；
- 只有一个默认管理员账号；
- `core_opt_recent`、`core_export_task` 等运行残留为空；
- 演示数据源、数据集、图表和数据大屏存在，且不包含本地环境连接串；
- `index.html` 和 `doc.html` 返回 200；
- `/v3/api-docs` 和 `/v3/api-docs/5-relation` 返回 200；
- 演示大屏能打开，图表明细弹窗能看到数据；
- 数据血缘能从演示数据源追踪到字段、数据集、图表和数据大屏；
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
