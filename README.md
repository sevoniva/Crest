# Crest

Crest 是面向私有化部署的开源 BI 平台，覆盖数据源接入、数据集建模、仪表盘、数据大屏、分享、导出、系统管理和字段级数据血缘。当前封版版本为 `v1.3.0`。

Crest 基于 DataEase 2.10.22 开源版本继续开发，并合入 2.10.23 相关安全加固和依赖升级。仓库保留上游版权和 GPLv3 许可声明，品牌、部署脚本、演示资源、OceanBase Oracle 数据源、数据血缘和研发效能分析能力按 Crest 当前产品边界维护。

## 项目定位

Crest 适合放在企业内网或专有云环境中，用于内部经营分析、研发效能分析、数据资产查看和报表共享。项目关注 BI 主链路，不包含模板市场、SQLBot、消息中心、独立移动端入口、地图类图表运行时、外部插件市场、帮助中心和关于页等非当前运行范围的能力。

当前主功能：

| 模块 | 说明 |
| --- | --- |
| 工作台 | 展示当前用户资源概览、收藏资源、最近使用资源和快捷创建入口 |
| 数据源 | 管理 MySQL、OceanBase Oracle 等连接；生产环境建议使用只读账号 |
| 数据集 | 基于数据源建模，支持字段管理、计算字段、预览和缓存同步 |
| 仪表盘 | 通过图表、筛选、联动、跳转和分享形成业务看板 |
| 数据大屏 | 面向展示墙、驾驶舱和主题分析场景的可视化页面 |
| 分享 | 为仪表盘和数据大屏生成访问链接，可配置密码、有效期和 ticket |
| 导出中心 | 查看导出任务状态并下载导出文件 |
| 数据血缘 | 展示数据源、表、字段、数据集、图表和展示资源之间的字段级依赖 |
| 系统管理 | 管理用户、系统参数、站点设置和字体 |

## 许可

Crest 按 GNU General Public License version 3 (GPLv3) 发布。使用、修改、部署或再分发时请遵守以下要求：

- 保留原项目版权声明和许可证声明；
- 保留无担保声明；
- 随源码或分发物提供 GPLv3 许可证副本；
- 说明项目来源于 DataEase；
- 派生版本继续按 GPLv3 发布。

上游项目信息：

- DataEase: <https://github.com/dataease/dataease>
- FIT2CLOUD 飞致云: <https://fit2cloud.com/>

## 架构概览

Crest 采用前后端分离开发、后端统一打包部署的结构。

```text
浏览器
  -> Vue 3 / Vite 前端静态资源
  -> Spring Boot 后端接口
  -> Crest 元数据库 MySQL
  -> 外部业务数据源
```

核心目录：

| 目录 | 作用 |
| --- | --- |
| `core/core-backend` | Spring Boot 后端、接口实现、Flyway 迁移、最终 JAR 打包入口 |
| `core/core-frontend` | Vue 3.3、Vite、TypeScript、Element Plus、Pinia 和 vxe-table 前端工程 |
| `sdk/api` | 对内 API、DTO、VO 和接口契约 |
| `sdk/common` | 公共模型、认证、工具类和 Spring 配置 |
| `sdk/extensions/extensions-datasource` | JDBC 数据源扩展定义 |
| `drivers` | 随仓库分发的 JDBC 驱动，当前包含 OceanBase Connector/J |
| `installer` | 单机 Docker 安装脚本、控制脚本和离线包制作脚本 |
| `deploy/kubernetes` | Kubernetes 部署清单，支持内置 MySQL 和外部 MySQL |
| `docs` | 数据血缘、研发效能大屏和开发说明 |

运行时只需要 Crest 应用容器和 MySQL。应用启动时由 Flyway 执行数据库迁移，初始化必要表结构、管理员账号、系统参数、演示数据源、演示数据集、演示图表和演示大屏。

## 默认端口和账号

| 项目 | 默认值 |
| --- | --- |
| Web 端口 | `8100` |
| 默认管理员账号 | `admin` |
| 单机安装初始密码 | 安装脚本随机生成，并在安装完成后输出 |
| Kubernetes 初始密码 | `CREST_INITIAL_PASSWORD` Secret 中配置 |
| 本地源码运行初始密码 | 需要显式设置 `CREST_INITIAL_PASSWORD` 环境变量 |

安装完成后请立即修改管理员密码。不要在生产环境使用公开示例密码。

## 快速安装

单机 Docker 安装入口位于 `installer`：

```bash
cd installer
bash install.sh
```

安装脚本会完成以下工作：

1. 读取 `install.conf`；
2. 生成 MySQL 密码、AES Key、AES IV 和管理员初始密码；
3. 创建 `/opt/crest` 运行目录；
4. 安装或复用 Docker 和 Docker Compose；
5. 加载离线镜像目录 `images/` 中的镜像，或从镜像仓库拉取；
6. 创建 `crest.service`；
7. 启动 MySQL 和 Crest。

常用配置项：

| 配置项 | 说明 |
| --- | --- |
| `DE_BASE` | 安装基目录，默认 `/opt`，实际运行目录为 `/opt/crest` |
| `DE_PORT` | Web 端口，默认 `8100` |
| `DE_APP_IMAGE` | Crest 应用镜像，默认 `ghcr.io/sevoniva/crest:v1.3.0` |
| `DE_MYSQL_IMAGE` | MySQL 镜像，默认 `mysql:8.4.5` |
| `DE_MYSQL_PASSWORD` | MySQL root 密码，留空时自动生成 |
| `DE_AES_KEY` / `DE_AES_IV` | 加密参数，留空时自动生成；升级已有环境时不要修改 |
| `DE_INITIAL_PASSWORD` | 管理员初始密码，留空时自动生成 |
| `DE_ORIGIN_LIST` | 允许访问来源，生产环境应改成实际域名 |

安装后使用：

```bash
crestctl status
crestctl start
crestctl stop
crestctl restart
crestctl clear-logs
```

升级前请备份元数据库和 `/opt/crest` 目录。已有环境只替换应用镜像时，可在运行目录执行：

```bash
docker-compose up -d --no-deps crest
```

## 离线包

离线包制作脚本：

```bash
cd installer
bash make-offline-package.sh v1.3.0 linux-amd64
bash make-offline-package.sh v1.3.0 linux-arm64
```

正式包名：

```text
crest-offline-v1.3.0-linux-amd64.tar.gz
crest-offline-v1.3.0-linux-arm64.tar.gz
```

包内包含安装脚本、配置模板、Docker Compose 文件、正式说明文档和 `images/` 镜像归档。离线服务器解压后直接执行：

```bash
tar -zxf crest-offline-v1.3.0-linux-amd64.tar.gz
cd crest-offline-v1.3.0-linux-amd64
bash install.sh
```

离线包产物属于发布物，不提交到源码仓库。源码仓库只保留制作脚本和说明。

## Kubernetes 部署

Kubernetes 清单位于 `deploy/kubernetes`：

- `deploy/kubernetes/internal-mysql`：同时安装 Crest 和 MySQL StatefulSet；
- `deploy/kubernetes/external-mysql`：只安装 Crest，连接外部 MySQL。

部署前必须替换 Secret 中的占位值：

```text
CREST_DB_PASSWORD
MYSQL_ROOT_PASSWORD
CREST_AES_KEY
CREST_AES_IV
CREST_INITIAL_PASSWORD
```

内置 MySQL 快速验证：

```bash
kubectl apply -k deploy/kubernetes/internal-mysql
kubectl -n crest-internal rollout status statefulset/crest-mysql --timeout=180s
kubectl -n crest-internal rollout status deployment/crest --timeout=300s
kubectl -n crest-internal port-forward svc/crest 18100:8100
```

访问地址：

```text
http://127.0.0.1:18100/index.html
```

详细说明见 [deploy/kubernetes/README.md](./deploy/kubernetes/README.md)。

## Docker 镜像

默认镜像：

```text
ghcr.io/sevoniva/crest:v1.3.0
ghcr.io/sevoniva/crest:main
```

本地构建：

```bash
mvn -s .mvn/settings.xml -pl :core-backend -am clean package -Pstandalone -DskipTests -Dmaven.test.skip=true
docker build -t ghcr.io/sevoniva/crest:local .
```

镜像使用 Alpine 作为最终基础镜像，内置 jlink 裁剪后的 Java 21 runtime，以非 root 用户 `10001:10001` 运行。

GitHub Actions 发布入口：

```text
.github/workflows/docker-publish.yml
```

## 本地开发

推荐工具链：

| 工具 | 版本 |
| --- | --- |
| JDK | 21 |
| Maven | 3.9 或兼容版本 |
| Node.js | 22 |
| pnpm | 11 |
| MySQL | 8.0 或兼容版本 |
| Docker | 20.10+，多架构镜像需要 Buildx |

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

源码方式启动后端前，需要准备 MySQL，并设置必要环境变量：

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

前端开发模式：

```bash
cd core/core-frontend
pnpm dev
```

常用验证：

```bash
cd core/core-frontend
pnpm run build:lite:check
```

涉及后端、数据库迁移、数据血缘、数据集缓存、导出或部署脚本时，需要结合实际服务做接口和安装回归。

开发约定见 [docs/development.md](./docs/development.md)。

## OceanBase Oracle 数据源

Crest 内置 `obOracle` 数据源类型，使用 OceanBase Connector/J：

```text
drivers/oceanbase-client-2.4.17.jar
```

配置项：

| 配置项 | 说明 |
| --- | --- |
| Host | OBServer、OBProxy 或 ODP 地址 |
| Port | OBServer 直连常见端口为 `2881`，代理端口按实际环境填写 |
| Database / Schema | 目标 Schema；留空时默认使用用户名中 `@` 或 `#` 前面的账号名，并转为大写 |
| Username | 支持 `username@tenant` 和 `username@tenant#cluster` |
| Password | 租户用户密码 |
| JDBC 参数 | 追加到 JDBC URL 的查询参数；危险 JNDI、反序列化和外部协议参数会被拦截 |

生产环境建议创建报表只读账号，只授予需要的 `SELECT` 权限。

## 数据集缓存

OceanBase Oracle 数据集支持结果缓存。Crest 会按数据集建模规则生成查询 SQL，从源库读取结果并写入内部缓存表。缓存就绪后，数据预览和仪表盘可以读取内部缓存，减少对业务库的直接查询压力。

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

数据血缘读取 Crest 已保存的元数据，形成字段级链路：

```text
数据源 -> 物理表 -> 物理字段 -> 数据集字段 -> 数据集 -> 图表字段 -> 图表 -> 仪表盘/大屏
```

使用场景：

- 字段口径排查；
- 报表影响分析；
- 数据源或数据集下线前检查；
- 展示资源反查上游来源；
- 演示环境验证完整数据链路。

数据血缘不会在打开页面时扫描业务库，只读取 Crest 元数据库中已经保存的数据源、数据集、字段、图表和展示资源配置。说明见 [docs/data-lineage.md](./docs/data-lineage.md)。

## 内置演示资源

新安装环境会自动初始化两组演示资源：

| 资源 | 用途 |
| --- | --- |
| 零售经营演示库 `crest_demo_retail` | 展示数据源、数据集、图表、大屏和字段血缘的完整链路 |
| 研发效能分析资源 | 展示研发经营、需求流动、人力容量、工程活动和质量风险等主题大屏 |

数据库迁移脚本：

| 脚本 | 内容 |
| --- | --- |
| `V1.1__initial_schema.sql` | 创建运行所需表结构、默认管理员、基础菜单、系统参数、内置驱动和主题配置 |
| `V1.2__demo_retail_dashboard.sql` | 创建零售经营演示库和对应的演示资源 |
| `V1.3__demo_engineering_efficiency.sql` | 创建研发效能主题数据、指标视图、图表和数据大屏 |

演示数据保持环境无关，不写入本地 IP、个人账号、压测数据、外部库连接串或临时资源。应用启动时会根据当前元数据库连接信息同步演示数据源地址。

研发效能大屏口径见 [docs/engineering-efficiency-dashboard-solution.md](./docs/engineering-efficiency-dashboard-solution.md)。

## 接口文档

服务启动后访问：

```text
http://<host>:8100/doc.html
http://<host>:8100/v3/api-docs
```

接口按当前运行模块分组，包括可视化管理、图表管理、数据集管理、数据源管理、数据血缘、导出中心、系统管理、权限管理和同步管理。生产环境建议通过网关限制接口文档访问范围。

## 公开仓库边界

可以提交：

- 源码、配置模板、迁移脚本、公开演示数据、部署清单、构建脚本和文档；
- 可以公开再分发的驱动文件；
- 复现问题所需的脱敏样例。

不要提交：

- `.env`、本地数据库密码、访问 token、私钥、证书和账号清单；
- `node_modules`、Maven `target`、前端 `dist`、静态打包目录和运行日志；
- 本地离线包、镜像导出包、压测报告、浏览器测试输出和临时验证目录；
- 含真实客户、供应商、员工或内网地址的数据文件。

提交前可以预览被忽略文件：

```bash
git status --short --ignored
git clean -fdXn
```

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [docs/development.md](./docs/development.md) | 仓库结构、开发工具链、构建命令、迁移规则和提交前检查 |
| [docs/data-lineage.md](./docs/data-lineage.md) | 字段级数据血缘的入口、口径、接口和边界 |
| [docs/engineering-efficiency-dashboard-solution.md](./docs/engineering-efficiency-dashboard-solution.md) | 研发效能大屏体系、指标口径、数据来源和治理要求 |
| [installer/README.md](./installer/README.md) | 单机安装、离线包制作、升级、备份和故障排查 |
| [deploy/kubernetes/README.md](./deploy/kubernetes/README.md) | Kubernetes 部署、Secret 配置、验证和运维说明 |
| [core/core-frontend/README.md](./core/core-frontend/README.md) | 前端工程结构、构建命令、品牌资源和页面约定 |
| [SECURITY.md](./SECURITY.md) | 安全报告、公开仓库检查和部署加固建议 |
| [CONTRIBUTING.md](./CONTRIBUTING.md) | 贡献流程、分支、提交、测试和文档要求 |

## 版本与变更

当前版本：`v1.3.0`。

变更记录见 [CHANGELOG.md](./CHANGELOG.md)。

## License

本仓库保留原项目的 GPLv3 许可证文件：[LICENSE](./LICENSE)。

Copyright (c) 2014-2026 [FIT2CLOUD 飞致云](https://fit2cloud.com/), All rights reserved.

Licensed under The GNU General Public License version 3 (GPLv3) (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at <https://www.gnu.org/licenses/gpl-3.0.html>.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
