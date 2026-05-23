# Crest

Crest 是一个基于 DataEase 2.10.22 改造的开源 BI 项目。当前版本主要面向私有化部署场景，保留数据源、数据集、仪表板、数据大屏、分享、导出、系统管理等核心能力，并补充了 OceanBase Oracle 模式数据源和字段级数据血缘。

这个仓库不是一个全新 BI 系统，也不是商业版功能的复制。它是在 DataEase 社区版基础上的二次开发版本，代码、目录和一部分数据库表名仍会保留 DataEase 的历史命名，这是为了兼容原有结构和升级路径。

## 项目来源与许可

本仓库基于 [DataEase](https://github.com/dataease/dataease.git) fork 修改，基线版本为 DataEase 2.10.22。

原项目版权归 [FIT2CLOUD 飞致云](https://fit2cloud.com/) 及其贡献者所有。本仓库保留原项目的 [LICENSE](./LICENSE) 文件，并继续按 GNU General Public License version 3 (GPLv3) 发布。

使用、分发或继续修改本仓库代码时，请遵守 GPLv3：

- 保留原版权声明和许可证声明；
- 保留无担保声明；
- 随源码或分发物提供 GPLv3 许可证副本；
- 标明这是基于 DataEase 的修改版本；
- 派生版本继续按 GPLv3 发布。

## 当前功能边界

当前 Crest 保留的主要功能：

- 工作台；
- 仪表板；
- 数据大屏；
- 仪表板和大屏分享；
- 数据源管理；
- 数据集管理；
- 数据导出中心；
- 字体管理；
- 系统参数；
- 用户和基础权限管理；
- 字段级数据血缘；
- OceanBase Oracle 模式数据源；
- OB Oracle 数据集结果缓存同步。

当前内部轻量模式默认关闭或移除的功能：

- SQLBot；
- 模板市场；
- 帮助中心；
- 关于页面；
- 工具箱；
- 消息中心；
- 移动端独立入口；
- 商业插件加载；
- 地图类图表；
- 离线地图资源；
- 远程商业组件和非公开镜像源。

分享功能已经保留。代码中仍会看到 `xpack_share`、`XpackShare*`、`io.dataease.api.xpack.share` 等名称，它们是历史表结构、接口路径、Mapper 和旧分享链接的兼容边界，不代表重新引入商业插件。不要直接改名；如果要改，需要同时设计历史数据和旧链接迁移。

## 主要改动

与原始 DataEase 2.10.22 相比，本仓库当前维护这些改动：

- 前端品牌替换为 Crest，包括登录页、顶部 logo、浏览器图标和主要展示文案；
- 默认管理员账号为 `admin`，初始密码为 `admin`；
- 新增 `obOracle` 数据源类型，使用 OceanBase Connector/J；
- 支持 OBServer 直连和 OBProxy/ODP 代理连接；
- 支持 Oracle 模式 Schema 推断、表读取、字段读取、字段备注和数据预览；
- OB Oracle 数据源默认开启只读模式；
- OB Oracle 数据集支持结果缓存同步；
- 支持全量同步、增量同步、定时同步和手动同步；
- 数据集删除时清理对应缓存表、同步任务和同步日志；
- 新增独立菜单“数据血缘”，放在数据大屏之后；
- 血缘图支持从数据源、物理表、物理字段一路追踪到数据集、图表、仪表板和大屏；
- 支持按表选择字段，再查看字段完整上下游；
- 删除数据源或数据集前，会基于血缘关系检查下游影响；
- 部署镜像使用 `ghcr.io/sevoniva/crest` 命名空间；
- 安装模板只使用 GHCR 和公开依赖源，不保留上游私有镜像源。

## OceanBase Oracle 数据源

Crest 内置 OceanBase Oracle 模式数据源，类型为 `obOracle`。

| 配置项 | 说明 |
| --- | --- |
| Host | OBServer、OBProxy 或 ODP 地址 |
| Port | OBServer 直连常见端口为 `2881`，代理端口按实际环境填写 |
| Database / Schema | 目标 Schema；留空时默认使用用户名中 `@` 或 `#` 前面的账号名，并转为大写 |
| Username | 支持 `username@tenant` 和 `username@tenant#cluster` |
| Password | 租户用户密码 |
| JDBC 参数 | 追加到 JDBC URL 的查询参数；危险 JNDI、反序列化和外部协议参数会被拦截 |

驱动文件放在仓库内：

```text
drivers/oceanbase-client-2.4.17.jar
```

建议生产环境单独创建查询账号，只授予业务报表需要的 `SELECT` 权限。不要使用 DBA、DDL 或 DML 权限账号接入报表系统。

## 数据集缓存同步

这里实现的是“数据集结果缓存”，不是整库同步，也不是 CDC。

Crest 会按数据集建模规则生成查询 SQL，从 OB Oracle 源端读取结果，再写入内部缓存表。缓存就绪后，数据预览和仪表板可以读取内部缓存，减少对业务库的直接查询压力。

当前规则：

- 仅支持单一 OB Oracle 数据源的数据集；
- 不支持跨源数据集缓存；
- 缓存未就绪时仍走直连查询；
- 首次增量同步没有水位时会先执行全量同步；
- 增量字段建议选择稳定递增的时间字段或数值字段；
- 增量同步不处理源端删除和历史数据修正；
- 源端发生删除或历史修正时，需要重新执行全量同步。

内部对象命名：

| 对象 | 名称 |
| --- | --- |
| 缓存表 | `de_sync_dataset_<datasetGroupId>` |
| 临时缓存表 | `tmp_de_sync_dataset_<datasetGroupId>` |
| 同步任务表 | `core_dataset_sync_task` |
| 同步日志表 | `core_dataset_sync_task_log` |

## 数据血缘

数据血缘是 Crest 当前重点补充的能力。菜单名称为“数据血缘”，默认位于“数据大屏”之后。

血缘关系来自 Crest 已保存的元数据，不直接扫描业务库。当前覆盖链路：

```text
数据源 -> 物理表 -> 物理字段 -> 数据集字段 -> 数据集 -> 图表字段 -> 图表 -> 仪表板/大屏
```

页面支持这些查看方式：

- 全局：查看资源级整体依赖；
- 数据源：从某个数据源查看进入 Crest 后形成的字段链路；
- 数据集：反查数据集上游表字段，并查看下游图表和展示资源；
- 仪表板/大屏：从展示资源反查图表、数据集和源字段；
- 表字段：先选物理表，再选字段，查看该字段完整上下游。

这个功能适合做字段口径排查、报表影响分析、数据源下线评估和数据集改造前检查。更详细的设计和使用说明见 [docs/data-lineage.md](./docs/data-lineage.md)。

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

默认服务端口为 `8100`。如果使用自己的数据库，请按实际环境调整后端配置或启动参数中的 `spring.datasource.*`。

前端开发模式：

```bash
cd core/core-frontend
pnpm dev
```

提交前建议至少执行：

```bash
cd core/core-frontend
pnpm run build:lite:check
```

后端改动涉及数据库、数据集、血缘或导出时，需要结合实际服务做接口回归。

## 镜像构建

本地构建主镜像：

```bash
mvn -s .mvn/settings.xml -pl :core-backend -am clean package -Pstandalone -DskipTests -Dmaven.test.skip=true
docker build -t crest:local .
```

GitHub Actions 发布入口：

```text
.github/workflows/docker-publish.yml
```

默认发布镜像：

```text
ghcr.io/sevoniva/crest:v2.10.22-ob
ghcr.io/sevoniva/crest:main
```

如果 GHCR package 设置为 private，部署服务器需要先执行 `docker login ghcr.io`。如果设置为 public，可以直接拉取。

## 部署

安装模板位于：

```text
installer/dataease
```

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
docker-compose up -d --no-deps dataease
```

容器名、运行目录和部分脚本仍沿用 `dataease`，这是为了兼容原安装结构。对外展示和产品界面使用 Crest。

## 目录结构

```text
core/core-backend                     后端服务
core/core-frontend                    前端工程
drivers                               JDBC 驱动
docs/data-lineage.md                  数据血缘说明
docs/development.md                   二次开发说明
installer                             安装脚本
installer/dataease                    Docker 部署模板
.github/workflows/docker-publish.yml  GHCR 镜像发布 workflow
```

## 维护约定

- 不提交 `node_modules`、`target`、`dist`、运行日志、本地压测报告和临时文件；
- 不把本地账号、数据库地址、访问 token、测试报告提交到仓库；
- 不再引入非公开或不可控依赖源；
- 镜像统一使用 `ghcr.io/sevoniva/crest` 命名空间；
- 前端依赖以 `core/core-frontend/pnpm-lock.yaml` 为准；
- 后端依赖应来自公开 Maven 坐标、本仓库源码或明确可再分发的文件；
- OB Oracle 改动需要回归直连、OBProxy/ODP、表字段读取、字段备注和数据预览；
- 缓存同步改动需要回归全量同步、增量同步、字段结构变化和数据集删除；
- 数据血缘改动需要回归全局、数据源、数据集、仪表板/大屏、表字段筛选和删除影响检查；
- 涉及部署、镜像、默认账号或功能边界的改动，需要同步更新 README 和相关文档。

## License

本仓库保留原项目的 GPLv3 许可证文件：[LICENSE](./LICENSE)。

Copyright (c) 2014-2026 [FIT2CLOUD 飞致云](https://fit2cloud.com/), All rights reserved.

Licensed under The GNU General Public License version 3 (GPLv3)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

<https://www.gnu.org/licenses/gpl-3.0.html>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
