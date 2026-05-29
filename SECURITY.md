# Security Policy

本文说明 Crest 的安全报告方式、公开仓库检查项和部署加固建议。

## Supported Versions

当前维护版本：

| Version | Status |
| --- | --- |
| `v1.3.x` | Supported |

更早的本地构建和实验分支不作为公开安全维护对象。正式部署建议使用 GitHub Release 或 `ghcr.io/sevoniva/crest` 中带版本号的镜像。

## Reporting a Vulnerability

如果发现安全问题，请优先通过 GitHub Security Advisory 提交。也可以在仓库 Issue 中报告，但不要在公开 Issue 中粘贴可直接利用的攻击代码、真实 token、生产连接串、内网地址、客户数据或完整日志。

报告时建议包含：

- Crest 版本和镜像标签；
- 部署方式，单机 Docker 或 Kubernetes；
- 影响模块；
- 复现步骤；
- 期望行为和实际行为；
- 已做的临时缓解措施；
- 脱敏后的日志或截图。

维护者确认后会评估影响范围、修复版本和披露节奏。

## Public Repository Rules

不要提交以下内容：

- `.env`、数据库密码、访问 token、私钥、证书、kubeconfig；
- 真实客户、员工、供应商、合同或生产业务数据；
- 内网地址、内部镜像仓库账号、私有制品仓库凭据；
- 本地离线包、镜像导出包、数据库备份、运行日志和压测报告。

提交前建议执行：

```bash
git status --short --ignored
rg -n "(BEGIN .*PRIVATE KEY|AKIA[0-9A-Z]{16}|secret_access_key|password\\s*[:=]|token\\s*[:=])" .
```

命中代码里的字段名、配置模板或脱敏示例时可以保留；命中真实值时必须移除或改为占位符。

## Deployment Hardening

生产环境建议：

- 使用 HTTPS 或可信网关反向代理；
- 将 `DE_ORIGIN_LIST` / `CREST_ORIGIN_LIST` 设置为实际访问域名；
- 管理员初始密码使用随机强密码，首次登录后立即修改；
- `CREST_AES_KEY` 和 `CREST_AES_IV` 使用部署环境独立生成的值，升级已有环境时不要更换；
- 如开启 SQLBot 连接信息加密，显式配置 `CREST_SQLBOT_AES_KEY` 和 `CREST_SQLBOT_AES_IV`；
- MySQL、业务数据源和 OceanBase 使用最小权限账号；
- 不把 Crest 直接暴露到公网管理面；
- 限制 `/doc.html` 和 `/v3/api-docs` 的访问范围；
- 定期备份 `/opt/crest` 和元数据库；
- 升级前先在测试环境验证 Flyway 迁移；
- Kubernetes 部署使用 Secret 管理密码，不在清单中写真实值。

## Dependency and Image Checks

发布前建议检查：

- Maven 依赖来自公开仓库或仓库内源码模块；
- npm 依赖与 `pnpm-lock.yaml` 一致；
- Docker 镜像来源清楚，架构匹配；
- 最终镜像以非 root 用户运行；
- Dockerfile 和 Kubernetes 清单未新增特权容器、宿主机敏感路径挂载或不必要的 Linux capability。

## Data Source Safety

Crest 会保存数据源连接配置。接入生产库时：

- 使用只读账号；
- 限制访问库表范围；
- 不在 JDBC 参数中传入危险 JNDI、反序列化或外部协议参数；
- 不把 DBA、DDL 或 DML 权限账号接入报表系统；
- 下线数据源前通过数据血缘确认影响范围。
