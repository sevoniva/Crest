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
<p align="center">
  <a href="https://trendshift.io/repositories/1563" target="_blank"><img src="https://trendshift.io/api/badge/repositories/1563" alt="dataease%2Fdataease | Trendshift" style="width: 250px; height: 55px;" width="250" height="55"/></a>
</p>

------------------------------

## Fork 说明

本仓库是基于 [DataEase 官方仓库](https://github.com/dataease/dataease.git) 的 fork 版本，当前代码基线为 DataEase 2.10.22，并在此基础上进行 OceanBase Oracle 模式数据源适配。

本 fork 保留原项目的版权声明、开源协议与许可证文件。DataEase 原项目版权归 [FIT2CLOUD 飞致云](https://fit2cloud.com/) 及其贡献者所有；本仓库中的修改内容同样遵循原项目的 GNU General Public License version 3 (GPLv3) 协议发布。使用、分发或二次修改本仓库代码时，请继续遵守 [LICENSE](./LICENSE) 中的 GPLv3 条款。

**本 fork 的主要变更：**

-   新增 OceanBase Oracle 模式数据源类型 `obOracle`；
-   引入 OceanBase Connector/J 驱动 `oceanbase-client-2.4.17.jar`；
-   支持 OceanBase Oracle 租户的数据源连接、Schema 获取和连通性校验；
-   支持直连 OBServer 与通过 OBProxy/ODP 访问的用户名格式：
    -   直连 OBServer：`username@tenant`
    -   OBProxy/ODP：`username@tenant#cluster`
-   数据源列表与编辑页增加 OceanBase Oracle 图标与配置入口。

## OceanBase Oracle 适配说明

本 fork 适配的是 OceanBase 数据库 Oracle 模式租户，连接信息与 DataEase 其他 JDBC 数据源保持一致：

-   Host：OceanBase OBServer 或 OBProxy/ODP 地址；
-   Port：直连 OBServer 常见端口为 `2881`，OBProxy/ODP 按实际代理端口填写；
-   Database / Schema：可填写目标 Schema；如留空，默认按用户名中的账号部分转为大写作为 Schema；
-   Username：支持 `username@tenant` 和 `username@tenant#cluster`；
-   Password：租户用户密码。

驱动使用 OceanBase 官方 Connector/J，类名为 `com.oceanbase.jdbc.Driver`。实际兼容范围以 OceanBase 官方 Connector/J 文档和目标 OceanBase 集群版本为准。

## 镜像发布说明

本 fork 的部署镜像统一发布到 GitHub Container Registry (GHCR)，安装模板中的 DataEase、MySQL、APISIX、ETCD、Playwright API 和同步任务镜像均已改为 `ghcr.io/sevoniva/dataease-2.10.22*` 命名空间，避免部署时依赖原项目的镜像仓库。

仓库内提供自动发布 GitHub Actions：提交到 `main` 分支后会自动同步运行依赖镜像、构建前后端、打包 DataEase 主镜像并推送到 GHCR。主镜像默认发布为 `ghcr.io/sevoniva/dataease-2.10.22:v2.10.22-ob`，同时发布 `main` 和 `sha-<commit>` 标签。

如需在未登录 GitHub 的环境直接拉取镜像，请在 GHCR 中将对应 package 设置为 public；如保持 private，则目标服务器需要先执行 `docker login ghcr.io`。

前端依赖保留 npm 公网生态，`core/core-frontend/.npmrc` 使用 `registry.npmmirror.com`，并跟踪 `package-lock.json` 以保证构建依赖可复现。Maven 依赖保留原项目公开仓库配置。

## 什么是 DataEase？

DataEase 是开源的 BI 工具，帮助用户快速分析数据并洞察业务趋势，从而实现业务的改进与优化。DataEase 支持丰富的数据源连接，能够通过拖拉拽方式快速制作图表，并可以方便的与他人分享。

- 👉 观看视频：[DataEase 两分钟介绍视频 ](https://www.bilibili.com/video/BV1Y8dAYLErb/)
- 👉 观看PPT：[DataEase PPT 材料](https://fit2cloud.com/dataease/download/introduce-dataease_2026.pdf)
- 👉 购买图书：[《DataEase 数据可视化分析与实践》](https://item.jd.com/10207058297099.html)

**DataEase 的优势：**

-   开源开放：零门槛，线上快速获取和安装，按月迭代；
-   简单易用：极易上手，通过鼠标点击和拖拽即可完成分析；
-   全场景支持：多平台安装和多样化嵌入支持；
-   安全分享：支持多种数据分享方式，确保数据安全；
-   AI 加持：无缝集成 [SQLBot](https://github.com/dataease/SQLBot) 实现智能问数。

**DataEase 支持的数据源：**

-   OLTP 数据库： MySQL、Oracle、OceanBase Oracle、SQL Server、PostgreSQL、MariaDB、Db2、TiDB、MongoDB-BI 等；
-   OLAP 数据库： ClickHouse、Apache Doris、Apache Impala、StarRocks 等；
-   数据仓库/数据湖： Amazon RedShift 等；
-   数据文件： Excel、CSV 等；
-   API 数据源。

## 快速开始

```
# 准备一台 2 核 4G 以上的 Linux 服务器，并以 root 用户运行以下一键安装脚本：

curl -sSL https://dataease.oss-cn-hangzhou.aliyuncs.com/quick_start_v2.sh | bash

# 用户名: admin
# 密码: DataEase@123456
```

如果是用于生产环境，推荐使用 [离线安装包方式](https://dataease.io/docs/v2/installation/offline_INSTL_and_UPG/) 进行安装部署。

如你有更多问题，可以查看在线文档，或者通过论坛和交流群与我们交流。

-   [在线文档](https://dataease.cn/docs/v2/)
-   [社区论坛](https://bbs.fit2cloud.com/c/de/6)
-   微信交流群

  <img width="150" height="150" alt="image" src="https://github.com/user-attachments/assets/a8e4cd48-ed0f-4754-ba34-d047063b1633" />


## UI 展示

<table style="border-collapse: collapse; border: 1px solid black;">
  <tr>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/dataease/dataease/assets/41712985/8dbed4e1-39f0-4392-aa8c-d1fd83ba42eb" alt="DataEase 工作台"   /></td>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/dataease/dataease/assets/41712985/7c54cb07-51ef-4bb6-a931-8a95c64c7e11" alt="DataEase 仪表板"   /></td>
  </tr>

  <tr>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/dataease/dataease/assets/41712985/ffa79361-a7b3-4486-b14a-f3fd3a28f01a" alt="DataEase 数据源"   /></td>
    <td style="padding: 5px;background-color:#fff;"><img src= "https://github.com/dataease/dataease/assets/41712985/bb28f4e4-636e-4ab0-85c5-1dfbd7a5397e" alt="DataEase 模板中心"   /></td>
  </tr>
</table>

## 技术栈

-   前端：[Vue.js](https://vuejs.org/)、[Element](https://element.eleme.cn/)
-   图库：[AntV](https://antv.vision/zh)
-   后端：[Spring Boot](https://spring.io/projects/spring-boot)
-   数据库：[MySQL](https://www.mysql.com/)
-   数据处理：[Apache Calcite](https://github.com/apache/calcite/)、[Apache SeaTunnel](https://github.com/apache/seatunnel)
-   基础设施：[Docker](https://www.docker.com/)

## 飞致云的其他明星项目

- [1Panel](https://github.com/1panel-dev/1panel/) - 现代化、开源的 Linux 服务器运维管理面板
- [MaxKB](https://github.com/1panel-dev/MaxKB/) - 基于 LLM 大语言模型的开源知识库问答系统
- [JumpServer](https://github.com/jumpserver/jumpserver/) - 广受欢迎的开源堡垒机
- [Cordys CRM](https://github.com/1Panel-dev/CordysCRM) - 新一代的开源 AI CRM 系统
- [Halo](https://github.com/halo-dev/halo/) - 强大易用的开源建站工具
- [MeterSphere](https://github.com/metersphere/metersphere/) - 新一代的开源持续测试工具

## License

Copyright (c) 2014-2026 [FIT2CLOUD 飞致云](https://fit2cloud.com/), All rights reserved.

Licensed under The GNU General Public License version 3 (GPLv3)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

<https://www.gnu.org/licenses/gpl-3.0.html>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
