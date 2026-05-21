# DataEase 2.10.22 OB Oracle Fork

这是一个基于 DataEase 2.10.22 的 fork 版本，主要用于支持 OceanBase Oracle 模式租户，并补充 OB Oracle 数据集缓存同步能力。

## 来源与许可证

本仓库基于 [DataEase 官方仓库](https://github.com/dataease/dataease.git) 修改，基线版本为 DataEase 2.10.22。

原项目版权归 [FIT2CLOUD 飞致云](https://fit2cloud.com/) 及其贡献者所有。本仓库保留原项目的 [LICENSE](./LICENSE) 文件，继续使用 GNU General Public License version 3 (GPLv3) 发布。

本 fork 已对修改内容做出标记和说明。使用、分发或继续修改本仓库代码时，请遵守 GPLv3：

- 保留原版权声明和许可证声明；
- 保留无担保声明；
- 随源码或分发物提供 GPLv3 许可证副本；
- 标明这是基于 DataEase 的修改版本；
- 派生版本继续按 GPLv3 发布。

## 本 Fork 的主要改动

- 新增 OceanBase Oracle 模式数据源类型 `obOracle`。
- 引入 OceanBase Connector/J，驱动类为 `com.oceanbase.jdbc.Driver`。
- 支持 OBServer 直连和 OBProxy/ODP 代理连接。
- 支持 Oracle 模式 Schema 推断、表列表、字段列表、字段备注和连通性校验。
- OB Oracle 数据源增加 `只读模式`，默认开启。
- OB Oracle 数据集支持 DataEase 原生的数据集结果缓存。
- 支持缓存全量同步、增量同步、定时同步和手动同步。
- 数据集删除时清理对应缓存表、同步任务和同步日志。
- 增加内部轻量模式，默认隐藏商业版和非内部使用入口。
- 精简内部版前端功能边界，下线 SQLBot、模板市场、工具箱、消息中心、移动端、商业插件、地图类图表、地图接口、离线地图资源和 demo 模板资源。
- 保留仪表板和数据大屏分享能力。
- 离线安装默认只部署 DataEase 与 MySQL，同步任务服务和 Playwright 按需外接。
- 部署镜像和安装模板改为使用 GitHub Container Registry。
- 构建依赖改为优先使用公共 Maven、npm 镜像源。

## OB Oracle 数据源

本 fork 适配 OceanBase 数据库 Oracle 模式租户。数据源配置方式与 DataEase 其他 JDBC 数据源保持一致。

| 配置项 | 说明 |
| --- | --- |
| Host | OBServer、OBProxy 或 ODP 地址 |
| Port | OBServer 直连常见端口为 `2881`；OBProxy/ODP 按实际代理端口填写 |
| Database / Schema | 目标 Schema；留空时默认使用用户名中 `@` 或 `#` 前面的账号名，并转为大写 |
| Username | 支持 `username@tenant` 和 `username@tenant#cluster` |
| Password | 租户用户密码 |
| 额外 JDBC 参数 | 追加到 JDBC URL 的查询参数；危险 JNDI、反序列化和外部协议参数会被拦截 |

连接示例：

```text
直连 OBServer
Driver: com.oceanbase.jdbc.Driver
URL:    jdbc:oceanbase://127.0.0.1:2881/TEST
User:   test@obora

OBProxy / ODP
Driver: com.oceanbase.jdbc.Driver
URL:    jdbc:oceanbase://127.0.0.1:2883/TEST
User:   test@obora#obdemo
```

驱动文件由本 fork 管理，位置为：

```text
drivers/oceanbase-client-2.4.17.jar
```

实际兼容范围以 OceanBase 官方 Connector/J 文档和目标 OceanBase 集群版本为准。升级驱动或数据库版本时，建议回归以下场景：

- OBServer 直连；
- OBProxy/ODP 连接；
- Schema 获取；
- 表列表获取；
- 字段备注获取；
- 数据预览；
- 全量缓存同步；
- 增量缓存同步；
- 仪表板直连查询；
- 仪表板读取缓存结果。

推荐的最小回归用例：

| 场景 | Host | Port | Username | 说明 |
| --- | --- | --- | --- | --- |
| OBServer 直连 | `127.0.0.1` | `2881` | `test@obora` | 验证直连 observer |
| OBProxy/ODP | `127.0.0.1` | `2883` | `test@obora#obdemo` | 验证代理模式和 cluster 解析 |

两种方式都需要覆盖连通性校验、表字段读取、字段备注展示、数据预览、数据集保存、缓存同步和仪表板查看。

## 只读模式

OB Oracle 数据源表单新增 `只读模式`，默认勾选。历史数据源如果没有保存过该配置，重新编辑时按开启处理。

只读模式会在源端 JDBC 连接上执行只读设置，覆盖以下链路：

- 数据源连通性校验；
- Schema、表和字段获取；
- 数据集字段解析；
- 数据集预览查询；
- OB Oracle 数据集缓存同步的源端查询。

缓存同步读取 OB Oracle 源库时始终按只读请求执行，不依赖前端开关。前端开关主要用于兼容少数驱动、代理或网关对 `Connection#setReadOnly(true)` 支持不完整的环境。

生产环境建议单独创建 DataEase 查询账号，并在 OceanBase 侧只授予所需的 `SELECT` 权限。不要使用拥有 DDL、DML 或管理员权限的业务账号。

## 字段备注

OB Oracle 数据集取字段时会读取 Oracle 模式字段注释。字段有备注时，DataEase 展示备注；没有备注时回退为字段名。

这样创建数据集和制作图表时可以直接看到业务含义，不需要用户再根据技术字段名反查含义。

## 数据集缓存同步

这里实现的是“数据集结果缓存”，不是整库同步，也不是把源端业务表完整复制到 DataEase。

DataEase 会按数据集建模规则生成查询 SQL，执行后把查询结果写入内部引擎表。缓存就绪后，数据预览和仪表板读取内部缓存表，减少对 OB Oracle 源库的直接查询压力。

适用范围：

- 仅支持单一 OB Oracle 数据源的数据集；
- 不支持跨源数据集；
- 不改变 Excel、API 和非 OB Oracle 数据集的原有逻辑；
- 数据集选择 `定时同步` 模式后才会创建同步任务；
- 首次同步成功并标记 `cache_ready = 1` 后，查询才会路由到缓存表；
- 缓存未就绪时，仍按原有直连查询路径读取。

同步方式：

- 全量更新：创建临时缓存表，写入完整数据集结果，成功后切换为正式缓存表。
- 增量更新：按用户选择的增量字段读取新增数据。
- 首次增量：没有可用缓存或水位时，自动执行一次全量更新。
- 定期校准：增量任务可按配置周期执行全量校准，降低长期增量偏差。

增量字段建议选择稳定递增的时间字段或数值字段，并在源表上建立索引。增量同步不处理源端删除，也不做通用 CDC。如果源端发生删除或历史数据修正，需要执行全量更新。

内部对象：

| 对象 | 名称 |
| --- | --- |
| 缓存表 | `de_sync_dataset_<datasetGroupId>` |
| 临时缓存表 | `tmp_de_sync_dataset_<datasetGroupId>` |
| 同步任务表 | `core_dataset_sync_task` |
| 同步日志表 | `core_dataset_sync_task_log` |

可靠性处理：

- 全量更新失败时保留上一份可用缓存；
- 增量更新失败时不推进水位；
- 字段结构变化后要求重新同步；
- 后续同步失败时，已就绪缓存仍可继续被仪表板读取；
- 数据集删除时清理同步任务、同步日志和内部缓存表。

## 镜像与部署

本 fork 默认启用内部轻量模式，用于内部二开和私有化部署：

- 后端配置项：`dataease.internal-lite.enabled=true`；
- 安装配置项：`DE_INTERNAL_LITE=true`；
- 默认入口保留工作台、仪表板、数据大屏、分享、数据准备、数据集、数据源、系统参数和字体；
- 默认安装包只启动 DataEase 与 MySQL。

本版本下线以下非核心功能：

- SQLBot；
- 模板市场；
- 工具箱；
- 消息中心；
- 移动端独立入口；
- 商业插件和远程商业组件加载；
- 地图类图表和离线地图资源；
- 模板管理和内置 demo 模板 XML。

说明：

- 分享功能保留，分享链接、访问校验和 ticket 管理接口继续注册。代码中仍会出现 `xpack_share`、`XpackShare*`、`io.dataease.api.xpack.share` 等命名，这是为了兼容原有表结构、接口路径、Mapper 和历史分享链接，不代表重新引入商业插件。不要直接改名；如需改名，必须同时提供历史数据和历史链接的迁移方案；
- 移动端独立页面不再构建，PC 端嵌入和仪表板预览继续保留；
- 地图类图表、地图接口、离线地图文件和 L7、L7Plot、Mapbox 运行时均不作为内部版支持范围。数据库实体中可能保留少量历史字段，只用于读取旧图表配置，不作为功能入口；
- 模板市场和模板管理入口已删除，内部版不再维护模板导入、模板复制和模板市场接口；
- SQLBot、模板、地图和商业插件相关接口在 `dataease.internal-lite.enabled=true` 时不注册或不再加载。

如需恢复同步任务服务或外部 Playwright，在 `installer/install.conf` 中改为按需启用：

```bash
DE_INTERNAL_LITE=false
DE_EXTERNAL_PLAYWRIGHT=false
DE_EXTERNAL_SYNC_TASK=false
```

主镜像发布到 GitHub Container Registry：

```text
ghcr.io/sevoniva/dataease-2.10.22:v2.10.22-ob
```

安装模板位于：

```text
installer/dataease
```

模板中的 DataEase、MySQL、Playwright API 和同步任务镜像均使用 `ghcr.io/sevoniva/dataease-2.10.22*` 命名空间。

从 0 开始部署时，推荐流程如下：

```bash
# 1. 准备安装目录
cp -R installer/dataease /opt/dataease
cd /opt/dataease

# 2. 按实际环境修改 install.conf
vi install.conf

# 3. 执行安装
bash install.sh
```

如果使用自己构建的镜像，只需要在 `install.conf` 或 compose 文件中把 DataEase 镜像标签改成目标版本。内部轻量版默认不启动同步任务服务和外部 Playwright；需要这些服务时再按需打开。

已有环境升级时，建议先备份数据库和安装目录，再只替换 DataEase 服务镜像：

```bash
docker-compose \
  -f docker-compose.yml \
  -f docker-compose-mysql.yml \
  -f docker-compose-doris.yml \
  up -d --no-deps dataease
```

`--no-deps dataease` 表示只重新创建 DataEase 容器，不重启 MySQL、Doris 等依赖服务。回滚时把镜像标签改回旧版本，再执行同一条命令即可。

仓库提供手动发布 workflow：

```text
.github/workflows/docker-publish.yml
```

在 GitHub Actions 页面手动运行 `Build and Publish Docker Images` 后，会构建前后端、打包主镜像并推送到 GHCR。默认主镜像标签为 `v2.10.22-ob`，也可以通过 `image_tag` 输入覆盖。

如果目标服务器需要免登录拉取镜像，请在 GHCR 中把对应 package 设置为 public。若 package 保持 private，部署前需要执行：

```bash
docker login ghcr.io
```

## 构建

推荐工具链：

- JDK 21；
- Maven 3.9 或兼容版本；
- Node.js 22；
- Docker Buildx。

依赖源：

- Maven 使用仓库内 `.mvn/settings.xml`，公共依赖走阿里云 Maven 公共镜像；
- 前端使用 `core/core-frontend/.npmrc` 中的 `registry.npmmirror.com`；
- 少量公共仓库缺失的制品放在 `third-party/maven`；
- 前端依赖以 `package-lock.json` 为准。

常用命令：

```bash
# 前端构建
cd core/core-frontend
npm ci
npm run build:base

# 严格检查或预压缩资源按需执行
npm run lint:check
npm run build:lite:check
npm run build:base:strict
npm run build:base:gzip

# 后端打包
cd ../..
mvn -s .mvn/settings.xml clean install -DskipTests -Dmaven.test.skip=true
mvn -s .mvn/settings.xml -f core/pom.xml clean package -Pstandalone -DskipTests -Dmaven.test.skip=true

# 本地镜像
docker build -t dataease-2.10.22-ob:local .
```

默认前端构建跳过 Vite 阶段的 ESLint、Stylelint 和 gzip 预压缩，减少本地与 CI 打包耗时。提交前需要完整检查时执行 `npm run lint:check` 或 `npm run build:base:strict`。构建完成后执行 `npm run build:lite:check`，用于确认源码、依赖声明和构建产物中没有重新引入地图接口、Mapbox、L7、L7Plot、Maplibre 或 PMTiles。

## 目录说明

```text
core/core-backend                  后端服务
core/core-frontend                 前端工程
drivers                            本 fork 管理的 JDBC 驱动
installer/dataease                 安装模板
third-party/maven                  本地 Maven 静态仓库
docs/development.md                二次开发说明
.github/workflows/docker-publish.yml  GHCR 镜像发布 workflow
```

## 维护说明

- 不要提交 `node_modules`、`target`、运行日志、临时包和本地数据库文件。
- OB Oracle 相关改动需要回归直连和 OBProxy 两种连接方式。
- 涉及缓存同步的改动需要回归全量同步、增量同步、字段结构变化和数据集删除。
- 涉及依赖或镜像的改动需要同步更新本 README 和 `docs/development.md`。

## License

本仓库保留原项目的 GPLv3 许可证文件：[LICENSE](./LICENSE)。

Copyright (c) 2014-2026 [FIT2CLOUD 飞致云](https://fit2cloud.com/), All rights reserved.

Licensed under The GNU General Public License version 3 (GPLv3)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

<https://www.gnu.org/licenses/gpl-3.0.html>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
