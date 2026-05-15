<p align="center"><a href="https://dataease.cn"><img src="https://dataease.oss-cn-hangzhou.aliyuncs.com/img/dataease-logo.png" alt="DataEase" width="300" /></a></p>
<h3 align="center">人人可用的开源 BI 工具</h3>
<p align="center">
  <a href="https://www.gnu.org/licenses/gpl-3.0.html"><img src="https://img.shields.io/github/license/dataease/dataease?color=%231890FF" alt="License: GPL v3"></a>
  <a href="https://app.codacy.com/gh/dataease/dataease?utm_source=github.com&utm_medium=referral&utm_content=dataease/dataease&utm_campaign=Badge_Grade_Dashboard"><img src="https://app.codacy.com/project/badge/Grade/da67574fd82b473992781d1386b937ef" alt="Codacy"></a>
  <a href="https://github.com/dataease/dataease"><img src="https://img.shields.io/github/stars/dataease/dataease?color=%231890FF&style=flat-square" alt="GitHub Stars"></a>
  <a href="https://github.com/dataease/dataease/releases"><img src="https://img.shields.io/github/v/release/dataease/dataease" alt="GitHub release"></a>
  <a href="https://gitee.com/fit2cloud-feizhiyun/DataEase"><img src="https://gitee.com/fit2cloud-feizhiyun/DataEase/badge/star.svg?theme=gvp" alt="Gitee Stars"></a>
  <a href="https://gitcode.com/feizhiyun/DataEase"><img src="https://gitcode.com/feizhiyun/DataEase/star/badge.svg" alt="GitCode Stars"></a>
</p>
<p align="center">
  <a href="/README.md"><img alt="中文(简体)" src="https://img.shields.io/badge/中文(简体)-d9d9d9"></a>
  <a href="/docs/README.en.md"><img alt="English" src="https://img.shields.io/badge/English-d9d9d9"></a>
  <a href="/docs/README.zh-Hant.md"><img alt="中文(繁體)" src="https://img.shields.io/badge/中文(繁體)-d9d9d9"></a>
  <a href="/docs/README.ja.md"><img alt="日本語" src="https://img.shields.io/badge/日本語-d9d9d9"></a>
  <a href="/docs/README.pt-br.md"><img alt="Português (Brasil)" src="https://img.shields.io/badge/Português (Brasil)-d9d9d9"></a>
  <a href="/docs/README.ar.md"><img alt="العربية" src="https://img.shields.io/badge/العربية-d9d9d9"></a>
  <a href="/docs/README.de.md"><img alt="Deutsch" src="https://img.shields.io/badge/Deutsch-d9d9d9"></a>
  <a href="/docs/README.es.md"><img alt="Español" src="https://img.shields.io/badge/Español-d9d9d9"></a>
  <a href="/docs/README.fr.md"><img alt="français" src="https://img.shields.io/badge/français-d9d9d9"></a>
  <a href="/docs/README.ko.md"><img alt="한국어" src="https://img.shields.io/badge/한국어-d9d9d9"></a>
  <a href="/docs/README.id.md"><img alt="Bahasa Indonesia" src="https://img.shields.io/badge/Bahasa Indonesia-d9d9d9"></a>
  <a href="/docs/README.tr.md"><img alt="Türkçe" src="https://img.shields.io/badge/Türkçe-d9d9d9"></a>
</p>

------------------------------

## 项目说明

本仓库 fork 自 [DataEase 官方仓库](https://github.com/dataease/dataease.git)，代码基线为 DataEase 2.10.22。这个 fork 主要面向 OceanBase Oracle 模式租户的使用场景，补充了 OB Oracle 数据源接入、字段备注读取、数据集缓存同步和源端只读保护。

原项目版权归 [FIT2CLOUD 飞致云](https://fit2cloud.com/) 及其贡献者所有。本仓库保留原项目的版权声明、许可证文件和 GPLv3 许可证引用；本 fork 中新增或修改的代码也按 GNU General Public License version 3 发布。使用、分发或二次修改本仓库代码时，请遵守 [LICENSE](./LICENSE) 中的 GPLv3 条款。

二次开发请先阅读 [开发说明](./docs/development.md)，其中记录了目录约定、依赖源、常用构建命令和本 fork 的维护规则。

## 主要变更

- 新增 OceanBase Oracle 模式数据源类型 `obOracle`。
- 引入 OceanBase Connector/J 驱动，驱动类为 `com.oceanbase.jdbc.Driver`。
- 支持 OBServer 直连和 OBProxy/ODP 代理连接。
- 支持 Oracle 模式 Schema 推断、库表获取、字段获取和连通性校验。
- 创建数据集时优先展示字段备注，便于业务人员识别字段含义。
- OB Oracle 数据源增加 `只读模式`，默认开启。
- OB Oracle 数据集支持 DataEase 原生的定时同步缓存，仪表板可读取缓存结果。
- 部署模板中的镜像地址已调整到 GitHub Container Registry。
- Maven 和 npm 构建默认使用公共镜像源；少量公共仓库缺失的制品放在 `third-party/maven`。

## OB Oracle 数据源

本 fork 适配 OceanBase 数据库 Oracle 模式租户。连接表单与 DataEase 其他 JDBC 数据源保持一致：

| 配置项 | 说明 |
| --- | --- |
| Host | OBServer、OBProxy 或 ODP 地址 |
| Port | 直连 OBServer 常见端口为 `2881`；OBProxy/ODP 按实际代理端口填写 |
| Database / Schema | 目标 Schema；留空时默认取用户名中 `@` 或 `#` 之前的账号部分并转为大写 |
| Username | 支持 `username@tenant` 和 `username@tenant#cluster` |
| Password | 租户用户密码 |
| 额外 JDBC 参数 | 追加到 JDBC URL 的查询参数；危险 JNDI、反序列化和外部协议参数会被拦截 |

两种连接方式都支持：

```text
直连 OBServer
Driver: com.oceanbase.jdbc.Driver
URL:    jdbc:oceanbase://127.0.0.1:2881/TEST
User:   test@tenant

OBProxy / ODP
Driver: com.oceanbase.jdbc.Driver
URL:    jdbc:oceanbase://127.0.0.1:2883/TEST
User:   test@tenant#cluster
```

驱动文件由本 fork 管理，位置为 `drivers/oceanbase-client-2.4.17.jar`。实际兼容范围以 OceanBase 官方 Connector/J 文档和目标 OceanBase 集群版本为准；本 fork 不在 README 中锁定数据库服务端版本。升级驱动时应同时回归直连、OBProxy、Schema 获取、字段备注、数据预览和数据集同步。

## 只读模式

OB Oracle 数据源表单中新增 `只读模式`，默认勾选。老数据源如果没有保存过这个配置，重新编辑时也会按开启处理。

只读模式会在以下链路中对源端连接执行 JDBC 只读设置：

- 数据源连通性校验；
- Schema、表和字段获取；
- 数据集字段解析和预览查询；
- OB Oracle 数据集缓存同步的源端查询。

数据集缓存同步读取 OB Oracle 源库时始终按只读请求执行，不依赖前端开关。前端开关主要用于兼容少数驱动、代理或网关对 `Connection#setReadOnly(true)` 支持不完整的环境。

只读模式不是数据库权限控制的替代品。生产环境建议给 DataEase 使用单独账号，并在 OceanBase 侧只授予查询所需权限，例如目标 Schema 下的 `SELECT` 权限。不要使用拥有 DDL、DML 或管理员权限的业务账号。

## 字段备注

OB Oracle 数据集取字段时会补充 Oracle 模式字段注释。字段有备注时，DataEase 中展示业务备注；没有备注时回退为字段名。这个处理和 MySQL 数据源的使用体验保持一致，避免数据集建模时只看到技术字段名。

## 数据集缓存同步

本 fork 增加的是“数据集结果缓存”，不是“业务库整库同步”，也不是把源端业务表复制到 DataEase。DataEase 会按数据集建模规则生成查询 SQL，执行后把结果写入内部引擎表。仪表板和数据预览在缓存就绪后读取内部表。

适用范围如下：

- 仅支持单一 OB Oracle 数据源的数据集。
- 不支持跨源数据集。
- 不影响 Excel、API 和非 OB Oracle 数据集的原有逻辑。
- 只有数据集选择 `定时同步` 模式并完成首次同步后，查询才会路由到缓存表。
- 首次同步未完成或缓存不可用时，仍按原有直连查询路径读取。

同步方式：

- 全量更新：创建临时缓存表，写入完整数据集结果，成功后切换为正式缓存表。
- 增量更新：按用户选择的增量字段读取新数据，水位大于上次保存值的数据会进入缓存。
- 首次增量更新：如果没有可用缓存或水位，会自动走一次全量更新。
- 定期校准：增量任务可按配置周期执行全量校准，降低长期增量带来的偏差。

增量字段建议选择稳定递增的时间字段或数值字段，并在 OB 源表上建立索引。增量同步不处理源端删除，也不做通用 CDC；如果源端发生删除或历史数据修正，需要执行全量更新来刷新缓存结果。

内部表和任务状态：

- 缓存表名：`de_sync_dataset_<datasetGroupId>`。
- 临时表名：`tmp_de_sync_dataset_<datasetGroupId>`。
- 任务状态表：`core_dataset_sync_task`。
- 任务日志表：`core_dataset_sync_task_log`。

缓存路由有明确保护：

- 只有首次同步成功并标记 `cache_ready = 1` 后，预览和仪表板才读取缓存。
- 字段结构发生变化时，会要求重新同步，避免旧缓存字段和新数据集模型不一致。
- 全量更新失败时保留上一份可用缓存。
- 增量更新失败时不会推进水位。
- 数据集删除时，会清理同步任务、日志和内部缓存表。

## 镜像发布

部署镜像发布到 GitHub Container Registry：

```text
ghcr.io/sevoniva/dataease-2.10.22:v2.10.22-ob
```

安装模板中的 DataEase、MySQL、APISIX、ETCD、Playwright API 和同步任务镜像也已切到 `ghcr.io/sevoniva/dataease-2.10.22*` 命名空间。模板位于 `installer/dataease`。

仓库提供手动发布 workflow：`Build and Publish Docker Images`。在 GitHub Actions 页面手动运行后，会构建前后端、打包 DataEase 主镜像并推送到 GHCR。默认主镜像标签为 `v2.10.22-ob`，也可以在运行 workflow 时填写 `image_tag`。

如果目标服务器需要免登录拉取镜像，请在 GHCR 中把对应 package 设置为 public。若 package 保持 private，部署前需要执行：

```bash
docker login ghcr.io
```

## 构建说明

推荐工具链：

- JDK 21；
- Maven 3.9 或兼容版本；
- Node.js 22；
- Docker Buildx。

Maven 默认使用仓库内 `.mvn/settings.xml`，公共依赖走阿里云 Maven 公共镜像。前端使用 `core/core-frontend/.npmrc` 中的 `registry.npmmirror.com`，并跟踪 `package-lock.json`。

常用命令：

```bash
# 前端构建
cd core/core-frontend
npm ci
npm run build:base

# 后端打包
cd ../..
mvn -s .mvn/settings.xml clean install -DskipTests -Dmaven.test.skip=true
mvn -s .mvn/settings.xml -f core/pom.xml clean package -Pstandalone -DskipTests -Dmaven.test.skip=true

# 本地镜像
docker build -t dataease-2.10.22-ob:local .
```

## DataEase 原项目信息

DataEase 是开源 BI 工具，支持连接数据库、数据仓库、文件和 API 数据源，通过拖拽方式制作图表、仪表板和数据大屏。

官方资料：

- [DataEase 官方网站](https://dataease.cn/)
- [DataEase 在线文档](https://dataease.cn/docs/v2/)
- [DataEase 官方仓库](https://github.com/dataease/dataease)
- [社区论坛](https://bbs.fit2cloud.com/c/de/6)

支持的数据源类型包括：

- OLTP 数据库：MySQL、Oracle、OceanBase Oracle、SQL Server、PostgreSQL、MariaDB、Db2、TiDB、MongoDB-BI 等；
- OLAP 数据库：ClickHouse、Apache Doris、Apache Impala、StarRocks 等；
- 数据仓库和数据湖：Amazon RedShift 等；
- 文件：Excel、CSV 等；
- API 数据源。

## UI 展示

<table style="border-collapse: collapse; border: 1px solid black;">
  <tr>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/dataease/dataease/assets/41712985/8dbed4e1-39f0-4392-aa8c-d1fd83ba42eb" alt="DataEase 工作台" /></td>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/dataease/dataease/assets/41712985/7c54cb07-51ef-4bb6-a931-8a95c64c7e11" alt="DataEase 仪表板" /></td>
  </tr>

  <tr>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/dataease/dataease/assets/41712985/ffa79361-a7b3-4486-b14a-f3fd3a28f01a" alt="DataEase 数据源" /></td>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/dataease/dataease/assets/41712985/bb28f4e4-636e-4ab0-85c5-1dfbd7a5397e" alt="DataEase 模板中心" /></td>
  </tr>
</table>

## 技术栈

- 前端：[Vue.js](https://vuejs.org/)、[Element](https://element.eleme.cn/)
- 图库：[AntV](https://antv.vision/zh)
- 后端：[Spring Boot](https://spring.io/projects/spring-boot)
- 数据库：[MySQL](https://www.mysql.com/)
- 数据处理：[Apache Calcite](https://github.com/apache/calcite/)、[Apache SeaTunnel](https://github.com/apache/seatunnel)
- 基础设施：[Docker](https://www.docker.com/)

## 飞致云的其他开源项目

- [1Panel](https://github.com/1panel-dev/1panel/) - Linux 服务器运维管理面板
- [MaxKB](https://github.com/1panel-dev/MaxKB/) - 开源知识库问答系统
- [JumpServer](https://github.com/jumpserver/jumpserver/) - 开源堡垒机
- [Cordys CRM](https://github.com/1Panel-dev/CordysCRM) - 开源 CRM 系统
- [Halo](https://github.com/halo-dev/halo/) - 开源建站工具
- [MeterSphere](https://github.com/metersphere/metersphere/) - 持续测试工具

## License

Copyright (c) 2014-2026 [FIT2CLOUD 飞致云](https://fit2cloud.com/), All rights reserved.

Licensed under The GNU General Public License version 3 (GPLv3)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

<https://www.gnu.org/licenses/gpl-3.0.html>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
