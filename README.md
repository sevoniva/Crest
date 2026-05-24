# Crest

Crest 是一款开源 BI 工具，面向私有化部署和企业内部数据分析场景。它提供数据源接入、数据集建模、仪表板、数据大屏、分享、导出、系统管理和字段级数据血缘能力，并内置 OceanBase Oracle 模式数据源支持。

## 项目来源与许可

Crest 是 [DataEase](https://github.com/dataease/crest.git) 2.10.22 的 GPLv3 派生项目。原项目版权归 [FIT2CLOUD 飞致云](https://fit2cloud.com/) 及其贡献者所有。

本仓库保留原项目的 [LICENSE](./LICENSE) 文件，并继续按 GNU General Public License version 3 (GPLv3) 发布。使用、分发或继续开发本仓库代码时，请遵守 GPLv3：

- 保留原版权声明和许可证声明；
- 保留无担保声明；
- 随源码或分发物提供 GPLv3 许可证副本；
- 说明项目来源于 DataEase；
- 派生版本继续按 GPLv3 发布。

## 功能

Crest 当前包含这些核心模块：

- 工作台：最近使用资源、常用入口和运行概览；
- 数据源：管理 MySQL、OceanBase Oracle 等数据连接；
- 数据集：基于数据源建模，支持字段、计算字段、预览和缓存同步；
- 仪表板：制作业务看板，支持图表、筛选、联动、跳转和分享；
- 数据大屏：制作大屏展示页面；
- 分享：为仪表板和数据大屏生成公开访问链接，可设置密码、有效期和 ticket；
- 数据导出中心：查看导出任务状态和下载结果；
- 数据血缘：查看数据源、表、字段、数据集、图表、仪表板和大屏之间的上下游关系；
- 系统管理：管理用户、基础权限、系统参数和字体。

产品边界以 BI 分析主链路为核心：数据接入、建模、可视化、分享、导出、权限和血缘分析。模板市场、帮助中心、关于页、SQLBot、消息中心、独立移动端入口、地图类图表和外部插件入口不属于当前 Crest 的运行范围。

## OceanBase Oracle

Crest 内置 `obOracle` 数据源类型，使用 OceanBase Connector/J：

```text
drivers/oceanbase-client-2.4.17.jar
```

配置项说明：

| 配置项 | 说明 |
| --- | --- |
| Host | OBServer、OBProxy 或 ODP 地址 |
| Port | OBServer 直连常见端口为 `2881`，代理端口按实际环境填写 |
| Database / Schema | 目标 Schema；留空时默认使用用户名中 `@` 或 `#` 前面的账号名，并转为大写 |
| Username | 支持 `username@tenant` 和 `username@tenant#cluster` |
| Password | 租户用户密码 |
| JDBC 参数 | 追加到 JDBC URL 的查询参数；危险 JNDI、反序列化和外部协议参数会被拦截 |

生产环境建议单独创建查询账号，只授予报表需要的 `SELECT` 权限。不要使用 DBA、DDL 或 DML 权限账号接入报表系统。

## 数据集缓存

OceanBase Oracle 数据集支持结果缓存。Crest 会按数据集建模规则生成查询 SQL，从源库读取结果并写入内部缓存表。缓存就绪后，数据预览和仪表板可以读取内部缓存，减少对业务库的直接查询压力。

当前规则：

- 仅支持单一 OceanBase Oracle 数据源的数据集；
- 不支持跨源数据集缓存；
- 缓存未就绪时仍走直连查询；
- 首次增量同步没有水位时会先执行全量同步；
- 增量字段建议选择稳定递增的时间字段或数值字段；
- 增量同步不处理源端删除和历史数据修正；
- 源端发生删除或历史修正时，需要重新执行全量同步。

内部对象：

| 对象 | 名称 |
| --- | --- |
| 缓存表 | `de_sync_dataset_<datasetGroupId>` |
| 临时缓存表 | `tmp_de_sync_dataset_<datasetGroupId>` |
| 同步任务表 | `core_dataset_sync_task` |
| 同步日志表 | `core_dataset_sync_task_log` |

## 数据血缘

数据血缘菜单位于主导航的“数据大屏”之后。它读取 Crest 已保存的元数据，形成字段级链路：

```text
数据源 -> 物理表 -> 物理字段 -> 数据集字段 -> 数据集 -> 图表字段 -> 图表 -> 仪表板/大屏
```

常用方式：

- 全局：查看整体资源依赖；
- 数据源：从数据源查看进入 Crest 后形成的字段链路；
- 数据集：反查数据集上游表字段，并查看下游图表和展示资源；
- 仪表板/大屏：从展示资源反查图表、数据集和源字段；
- 表字段：先选物理表，再选字段，查看该字段完整上下游。

这个功能适合字段口径排查、报表影响分析、数据源下线评估和数据集设计前检查。详细说明见 [docs/data-lineage.md](./docs/data-lineage.md)。

新安装环境只保留默认管理员和系统基础菜单，不预置示例数据源、示例数据集、图表或仪表板。接入数据后，数据血缘会基于已保存的数据源、数据集和图表元数据自动生成关系图。

## 本地开发

推荐工具链：

- JDK 21；
- Maven 3.9 或兼容版本；
- Node.js 22；
- pnpm 11；
- MySQL 8 或兼容数据库。

前端构建：

```bash
cd core/core-frontend
pnpm install --frozen-lockfile
pnpm run build:base
```

后端打包：

```bash
mvn -s .mvn/settings.xml -pl :core-backend -am clean package -Pstandalone -DskipTests -Dmaven.test.skip=true
```

本地启动：

```bash
./run.sh start
```

默认服务端口为 `8100`。如需连接自己的数据库，请按实际环境调整 `spring.datasource.*`。

前端开发模式：

```bash
cd core/core-frontend
pnpm dev
```

提交前建议执行：

```bash
cd core/core-frontend
pnpm run build:lite:check
```

后端涉及数据库、数据集、血缘或导出时，需要结合实际服务做接口回归。

## 镜像

本地构建主镜像：

```bash
mvn -s .mvn/settings.xml -pl :core-backend -am clean package -Pstandalone -DskipTests -Dmaven.test.skip=true
docker build -t crest:local .
```

GitHub Actions 发布入口：

```text
.github/workflows/docker-publish.yml
```

默认镜像：

```text
ghcr.io/sevoniva/crest:v2.10.22-ob
ghcr.io/sevoniva/crest:main
```

## 部署

安装入口：

```text
installer/install.sh
```

基础配置文件：

```text
installer/install.conf
```

默认安装会启动 Crest 和 MySQL：

```bash
cd installer
bash install.sh
```

安装完成后会输出访问地址、用户名和初始密码。默认账号为：

```text
用户名：admin
初始密码：admin
```

生产环境上线前建议完成这些检查：

- 修改 `installer/install.conf` 中的 MySQL 密码；
- 使用 HTTPS 或网关反向代理；
- 调整 `DE_ORIGIN_LIST` 为实际访问域名；
- 首次登录后修改管理员密码；
- 使用最小权限账号接入业务数据源；
- 确认镜像来自 `ghcr.io/sevoniva/crest`；
- 备份数据库和运行目录后再升级。

已有环境升级时，建议先备份数据库和安装目录，再只替换 Crest 服务镜像：

```bash
docker-compose up -d --no-deps crest
```

新安装默认运行目录为 `/opt/crest`，服务名为 `crest`，控制命令为 `crestctl`。

数据库初始化由后端 Flyway 迁移负责，脚本位于：

```text
core/core-backend/src/main/resources/db/migration
```

当前初始状态包含：

- 默认管理员账号 `admin`，初始密码 `admin`；
- 无内置数据源、数据集、图表和仪表板；
- 最近使用、收藏、导出任务、数据源任务日志和助手历史为空；
- 上游示例表和历史演示资源会在初始化阶段清理掉。

安装脚本里的 MySQL `init.sql` 只负责创建数据库，不承载业务元数据。当前初始化收口在 `V2.10.22.8__V1.1_initial_state.sql`，这是 Crest V1.1 的干净初始状态脚本。

## 目录结构

```text
core/core-backend                     后端服务
core/core-frontend                    前端工程
drivers                               JDBC 驱动
docs/data-lineage.md                  数据血缘说明
docs/development.md                   开发说明
installer                             安装脚本
installer/crest                       Docker 部署模板
.github/workflows/docker-publish.yml  GHCR 镜像发布 workflow
```

## 维护约定

- 不提交 `node_modules`、`target`、`dist`、运行日志、本地压测报告和临时文件；
- 不把本地账号、数据库地址、访问 token、测试报告提交到仓库；
- 不引入非公开或不可控依赖源；
- 镜像统一使用 `ghcr.io/sevoniva/crest` 命名空间；
- 前端依赖以 `core/core-frontend/pnpm-lock.yaml` 为准；
- 后端依赖应来自公开 Maven 坐标、本仓库源码或明确可再分发的文件；
- OceanBase Oracle 相关功能需要回归直连、OBProxy/ODP、表字段读取、字段备注和数据预览；
- 缓存同步相关功能需要回归全量同步、增量同步、字段结构变化和数据集删除；
- 数据血缘相关功能需要回归全局、数据源、数据集、仪表板/大屏、表字段筛选和删除影响检查；
- 涉及部署、镜像、默认账号或功能边界的内容，需要同步更新 README 和相关文档。

## License

本仓库保留原项目的 GPLv3 许可证文件：[LICENSE](./LICENSE)。

Copyright (c) 2014-2026 [FIT2CLOUD 飞致云](https://fit2cloud.com/), All rights reserved.

Licensed under The GNU General Public License version 3 (GPLv3)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

<https://www.gnu.org/licenses/gpl-3.0.html>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
