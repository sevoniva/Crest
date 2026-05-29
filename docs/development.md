# Crest 开发说明

本文面向参与 Crest 开发、部署和发布的人，说明仓库结构、构建方式、数据库迁移、公开仓库规则和提交前检查。

Crest 基于 DataEase 2.10.22 开源版本继续开发，并合入 2.10.23 相关安全加固和依赖升级。项目继续按 GPLv3 发布。开发时要保留上游版权和许可证声明，不引入不能公开分发的依赖、驱动或数据。

## 目录结构

| 路径 | 说明 |
| --- | --- |
| `core/core-backend` | Spring Boot 后端服务，包含接口、业务实现、Flyway 迁移和最终 JAR 打包入口 |
| `core/core-frontend` | Vue 3.3、Vite、TypeScript、Element Plus、Pinia 和 vxe-table 前端工程 |
| `sdk/api` | 对内 API、DTO、VO 和接口契约 |
| `sdk/common` | 认证、通用模型、工具类、异常处理和 Spring 配置 |
| `sdk/extensions/extensions-datasource` | 数据源扩展、JDBC 数据源定义和方言能力 |
| `drivers` | 随仓库维护的 JDBC 驱动，当前跟踪 `oceanbase-client-2.4.17.jar` |
| `installer` | 单机安装脚本、Docker Compose 模板、控制脚本和离线包制作脚本 |
| `deploy/kubernetes` | Kubernetes 清单，支持内置 MySQL 和外部 MySQL |
| `docs` | 产品能力、开发、血缘和研发效能大屏说明 |
| `.github/workflows/docker-publish.yml` | GHCR 镜像构建和发布流程 |

## 产品边界

当前 Crest 保留并维护以下主链路：

- 数据源、数据集、数据集缓存同步；
- 图表编辑、仪表盘、数据大屏；
- 字段级数据血缘；
- 分享页、导出中心；
- 登录、用户、系统参数、站点设置和字体管理；
- OceanBase Oracle 模式数据源；
- 零售经营和研发效能演示资源。

当前不提供 SQLBot、模板市场、工具箱、消息中心、独立移动端入口、地图类图表、地图运行时、地图 API、帮助中心、关于页和外部插件入口。新增入口前必须确认产品边界和部署依赖，不要把不可运行或未维护的菜单暴露出来。

分享模块保留 `core_share`、`CoreShare*`、`ShareTicket` 和 `de-link` 等历史兼容名称。这些名称关联数据库表、接口路径、Mapper 语句和旧分享链接。调整前必须先设计迁移、回滚和链接兼容方案。

## 工具链

| 工具 | 建议版本 |
| --- | --- |
| JDK | 21 |
| Maven | 3.9 或兼容版本 |
| Node.js | 22 |
| pnpm | 11 |
| MySQL | 8.0 或兼容版本 |
| Docker | 20.10+ |
| Docker Buildx | 多架构镜像发布时需要 |

Maven 使用仓库内 `.mvn/settings.xml`。前端依赖使用 `core/core-frontend/pnpm-lock.yaml`，`flushbonading` 子包使用自己的 `package-lock.json`。

不要引入私有制品仓库、个人代理源、无法公开访问的镜像仓库或授权不清的二进制文件。确需新增第三方驱动或前端包时，需要在提交说明中写明用途和许可证。

## 常用命令

编译数据源扩展相关模块：

```bash
mvn -s .mvn/settings.xml -pl sdk/extensions/extensions-datasource -am \
  -DskipTests -Dmaven.test.skip=true -Dmaven.antrun.skip=true \
  test-compile
```

安装前端依赖：

```bash
cd core/core-frontend
pnpm install --frozen-lockfile
```

构建前端：

```bash
cd core/core-frontend
pnpm run build:base
```

前端轻量构建检查：

```bash
cd core/core-frontend
pnpm run build:lite:check
```

打包后端：

```bash
mvn -s .mvn/settings.xml -pl :core-backend -am clean package -Pstandalone -DskipTests -Dmaven.test.skip=true
```

构建本地镜像：

```bash
docker build -t ghcr.io/sevoniva/crest:local .
```

源码方式启动后端：

```bash
export CREST_DB_HOST=127.0.0.1
export CREST_DB_PORT=3306
export CREST_DB_NAME=crest
export CREST_DB_USERNAME=root
export CREST_DB_PASSWORD='<mysql-password>'
export CREST_AES_KEY='<32-character-aes-key>'
export CREST_AES_IV='<16-character-aes-iv>'
export CREST_INITIAL_PASSWORD='<admin-initial-password>'
./run.sh start
```

前端开发服务：

```bash
cd core/core-frontend
pnpm dev
```

## 运行时配置

后端主要配置来自 Spring 配置文件和环境变量。单机安装时由 `installer/install.sh` 根据 `installer/install.conf` 生成运行目录配置；Kubernetes 部署时由 `ConfigMap` 和 `Secret` 注入。

关键环境变量：

| 变量 | 说明 |
| --- | --- |
| `CREST_DB_HOST` | MySQL 地址 |
| `CREST_DB_PORT` | MySQL 端口 |
| `CREST_DB_NAME` | 元数据库名 |
| `CREST_DB_USERNAME` | 元数据库账号 |
| `CREST_DB_PASSWORD` | 元数据库密码 |
| `CREST_INITIAL_PASSWORD` | 管理员初始密码，不能为空 |
| `CREST_AES_KEY` | 32 位加密 Key |
| `CREST_AES_IV` | 16 位加密 IV |
| `CREST_SQLBOT_ENCRYPT` | SQLBot 对外返回连接信息时是否加密，默认 `false` |
| `CREST_SQLBOT_AES_KEY` / `CREST_SQLBOT_AES_IV` | 仅在开启 SQLBot 加密时配置，系统不提供默认密钥 |
| `CREST_ORIGIN_LIST` | 允许来源列表 |
| `CREST_LOGIN_TIMEOUT` | 登录超时时间，默认 960 分钟 |

源码启动时如果 `CREST_INITIAL_PASSWORD` 为空，后端会拒绝启动。安装脚本会自动生成该值，Kubernetes 清单要求部署前在 Secret 中填写。

## 数据库迁移

后端使用 Flyway 管理数据库结构和初始化数据：

```text
core/core-backend/src/main/resources/db/migration
```

当前迁移脚本：

| 脚本 | 说明 |
| --- | --- |
| `V1.1__initial_schema.sql` | 创建运行所需表结构、默认管理员、基础菜单、系统参数、内置驱动和主题配置 |
| `V1.2__demo_retail_dashboard.sql` | 创建 `crest_demo_retail` 零售经营演示库，写入演示数据源、数据集、图表和大屏 |
| `V1.3__demo_engineering_efficiency.sql` | 写入研发效能主题数据、指标视图、图表和大屏 |
| `V1.4__sso_integration.sql` | 增加单点登录配置、菜单和用户认证来源字段 |

迁移规则：

- 已发布脚本不直接改写，后续变化新增 `Vx.y__description.sql`；
- 初始化数据必须环境无关，不能写入本地 IP、个人账号、外部库密码或临时资源；
- 新增演示资源要能说明来源字段、指标口径、下钻字段和展示目的；
- 数据结构变化要兼容已有安装，必要时提供回滚说明；
- 迁移脚本应能在空库首次安装时一次执行成功。

空库验证建议：

```bash
docker exec crest-mysql-local mysql -uroot -p'<mysql-root-password>' \
  -e "DROP DATABASE IF EXISTS crest_verify; CREATE DATABASE crest_verify DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
```

验证点：

- `de_standalone_version` 包含最新迁移；
- 只有一个默认管理员账号；
- `core_opt_recent`、`core_export_task` 等运行残留为空；
- 零售经营和研发效能演示资源存在；
- `sso.` 配置项存在，默认不开启单点登录；
- 演示数据源连接信息不含本地环境地址；
- `/index.html`、`/doc.html`、`/v3/api-docs` 返回 200；
- 数据血缘能从演示数据源追踪到字段、数据集、图表和大屏。

## 前端开发约定

前端工程说明见 [../core/core-frontend/README.md](../core/core-frontend/README.md)。这里列出跨页面需要遵守的约定：

- 顶部导航、Logo、字体、主题色和常用列表样式应复用现有组件或公共样式，不在单页重复写一套；
- 业务文案沿用现有产品词，不新增营销式口号；
- “仪表盘”是标准称呼，不再使用旧称；
- 通用图表组件必须从数据集中读取维度和阶段，不把阶段、角色或状态写死在代码里；
- 修改大屏编辑器或大屏运行态时，要确认已有大屏展示不被破坏；
- 页面切换和操作反馈保持克制，不使用影响管理系统效率的夸张动效；
- 涉及数据血缘、数据集、数据源、工作台和系统管理页面时，要在宽屏、普通桌面和较窄窗口下检查布局。

## 后端开发约定

- 控制器、接口 DTO 和 VO 要带清楚的 OpenAPI 注解；
- 数据库访问优先沿用已有 Mapper、Service 和管理类结构；
- 异常信息面向用户可理解，不泄露数据库密码、完整连接串或内部堆栈；
- 外部数据源连接参数必须做危险参数过滤；
- 删除数据源、数据集、图表和展示资源前，要保持影响范围检查；
- 涉及元数据关系的改动，需要同步检查数据血缘；
- 导出、缓存和同步任务要避免在请求线程里做长时间阻塞。

## 接口文档

运行后访问：

```text
/doc.html
/v3/api-docs
```

配置入口：

```text
sdk/common/src/main/java/io/crest/doc/SwaggerConfig.java
```

新增接口时检查：

- 所在包是否进入对应 `GroupedOpenApi` 分组；
- 控制器是否有清楚的 `@Tag` 和 `@Operation`；
- 请求和响应对象是否有必要的 `@Schema`；
- 删除或隐藏的功能不要出现在接口文档分组里。

## OceanBase Oracle 回归

数据源类型为 `obOracle`，驱动类：

```text
com.oceanbase.jdbc.Driver
```

用户名格式：

- OBServer 直连：`username@tenant`
- OBProxy/ODP：`username@tenant#cluster`

Schema 留空时，系统默认使用用户名中 `@` 或 `#` 前面的账号名，并按 Oracle 风格转为大写。

相关变更至少覆盖：

- OBServer 直连，例如 `test@obora` + `2881`；
- OBProxy/ODP，例如 `test@obora#obcluster` + `2883`；
- 表和字段元数据读取，包含字段备注；
- 数据集预览；
- 全量缓存同步；
- 增量缓存同步；
- 仪表盘查询。

## 数据血缘回归

数据血缘属于 Crest 主功能，不作为外部插件加载。

后端入口：

- `core/core-backend/src/main/java/io/crest/relation/server/RelationServer.java`
- `core/core-backend/src/main/java/io/crest/relation/manage/RelationManage.java`
- `core/core-backend/src/main/java/io/crest/relation/dto/*`

前端入口：

- `core/core-frontend/src/api/relation/index.ts`
- `core/core-frontend/src/views/visualized/data/lineage/index.vue`
- `core/core-frontend/src/components/relation-chart/GraphView.vue`

链路模型：

```text
datasource -> table -> table_field -> dataset_field -> dataset -> chart_field -> chart -> dv
```

节点类型：

- `datasource`
- `table`
- `table_field`
- `dataset_field`
- `dataset`
- `chart_field`
- `chart`
- `dv`

回归场景：

- 全局概览能加载；
- 数据源级字段血缘能加载；
- 数据集级字段血缘能加载；
- 仪表盘和数据大屏血缘能加载；
- 表选择器列出当前图里的全部物理表；
- 字段选择器跟随所选表更新；
- 选择字段后图谱收敛到上下游节点；
- 关键字搜索能命中字段和资源；
- 被使用的数据源或数据集下线前能给出影响范围；
- 数据源、数据集、仪表盘和数据大屏页面能正常打开。

用户文档是 [data-lineage.md](./data-lineage.md)。血缘语义、接口、节点类型或边界发生变化时，同步更新该文档。

## 部署命名

新安装默认使用 Crest 命名：

| 项目 | 名称 |
| --- | --- |
| 运行目录 | `/opt/crest` |
| 控制命令 | `crestctl` |
| systemd 服务 | `crest.service` |
| Docker 服务和容器 | `crest` |
| Docker 网络 | `crest-network` |
| 默认数据库 | `crest` |

安装脚本仍识别旧运行目录和旧控制命令，用于存量环境升级。不要在同一个版本里移除这些兼容判断。

## 公开仓库规则

不要提交：

- `.env`、账号密码、token、私钥、证书和真实连接串；
- 本地运行目录、离线包、镜像导出包、日志、pid、压测报告和浏览器测试输出；
- `node_modules`、Maven `target`、前端 `dist`、`.flattened-pom.xml`；
- 真实客户、员工、供应商、合同、内网地址或生产库数据；
- 私有制品仓库、私有 npm 源或内部镜像地址。

可以提交：

- 开源源码、配置模板、迁移脚本、脱敏样例和公开演示数据；
- 公开可再分发的驱动文件；
- 构建、部署、验证和排障文档。

清理被忽略文件前先预览：

```bash
git clean -fdXn
```

确认后再执行：

```bash
git clean -fdX
```

## 提交前检查

按改动范围选择检查项：

| 改动 | 建议检查 |
| --- | --- |
| 前端页面、样式、组件 | `pnpm run build:lite:check`，必要时再跑 `pnpm run build:base` |
| 后端接口、数据源、导出、血缘 | Maven 打包或相关模块编译，结合服务做接口回归 |
| 数据库迁移 | 空库启动，检查 Flyway 版本和初始化数据 |
| 安装脚本或 Dockerfile | 本地镜像构建，单机安装或离线包验证 |
| Kubernetes 清单 | `kubectl apply -k` 到测试命名空间并检查 rollout |
| 文档 | 检查版本号、镜像名、端口、账号和路径是否与代码一致 |

提交要求：

- 一个提交只解决一类问题；
- commit message 使用简洁的英文 Conventional Commit 风格；
- 代码范围保持清楚，避免把无关格式化混入；
- 修改部署、镜像、默认账号、功能边界或数据口径时，同步更新 README 和对应文档。
