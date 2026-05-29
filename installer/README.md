# Crest 单机安装与离线包

本目录用于单机 Docker 部署和离线安装包制作。源码仓库只提交安装脚本、配置模板和说明文档；离线包、镜像归档和本地运行目录不提交到 Git。

## 文件说明

| 文件 | 说明 |
| --- | --- |
| `install.sh` | 安装或升级入口 |
| `install.conf` | 安装配置模板 |
| `uninstall.sh` | 卸载脚本 |
| `crestctl` | 安装后的运维控制命令 |
| `crest/docker-compose.yml` | Crest 应用容器模板 |
| `crest/bin/mysql/init.sql` | MySQL 初始化脚本，只负责创建数据库 |
| `crest/templates/application.yml` | 后端运行配置模板 |
| `make-offline-package.sh` | 离线包制作脚本 |
| `LICENSE` | GPLv3 许可证副本 |

## 默认部署形态

Crest 默认使用内部轻量模式：

```bash
DE_INTERNAL_LITE=true
```

该模式只启动两个容器：

- `crest`：Crest 应用；
- `mysql-crest`：元数据库。

数据血缘、导出中心、系统管理、数据源、数据集、仪表盘和数据大屏都在应用容器内运行，不需要额外服务。同步任务服务和外部截图服务默认不安装。

如需恢复同步任务或外部截图服务，可在 `install.conf` 中调整后重新制作安装包：

```bash
DE_INTERNAL_LITE=false
DE_EXTERNAL_PLAYWRIGHT=false
DE_EXTERNAL_SYNC_TASK=false
```

## 安装前检查

建议服务器满足：

| 项目 | 建议 |
| --- | --- |
| 操作系统 | Linux x86_64 或 arm64 |
| CPU | 2 核及以上 |
| 内存 | 4 GB 及以上 |
| 磁盘 | `/opt` 所在磁盘至少 20 GB 可用 |
| 端口 | 默认开放 `8100/tcp` |
| 容器运行时 | Docker 和 Docker Compose；脚本可在线安装，也可离线包内置 |

生产环境建议：

- 使用固定域名和 HTTPS 反向代理；
- 修改 `DE_ORIGIN_LIST` 为实际访问地址；
- 使用高强度 `DE_MYSQL_PASSWORD`、`DE_AES_KEY`、`DE_AES_IV` 和 `DE_INITIAL_PASSWORD`；
- 首次登录后修改管理员密码；
- 将 `/opt/crest` 纳入备份。

## 配置项

`install.conf` 中主要配置：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `DE_BASE` | `/opt` | 安装基目录，实际运行目录为 `${DE_BASE}/crest` |
| `DE_PORT` | `8100` | Web 访问端口 |
| `DE_APP_IMAGE` | `ghcr.io/sevoniva/crest:v1.3.0` | Crest 应用镜像 |
| `DE_MYSQL_IMAGE` | `mysql:8.4.5` | MySQL 镜像 |
| `DE_MYSQL_DB` | `crest` | 元数据库名 |
| `DE_MYSQL_USER` | `root` | MySQL 用户 |
| `DE_MYSQL_PASSWORD` | 空 | 留空时安装脚本自动生成 |
| `DE_AES_KEY` | 空 | 留空时自动生成 32 位值；升级时不要改 |
| `DE_AES_IV` | 空 | 留空时自动生成 16 位值；升级时不要改 |
| `DE_INITIAL_PASSWORD` | 空 | 管理员初始密码，留空时自动生成并输出 |
| `DE_ORIGIN_LIST` | `http://localhost:8100` | 允许来源，正式环境要改成实际域名 |

## 在线安装

```bash
cd installer
bash install.sh
```

安装脚本会：

1. 读取 `install.conf`；
2. 创建运行目录；
3. 生成缺省密码和加密参数；
4. 复制配置模板；
5. 安装或复用 Docker 与 Docker Compose；
6. 加载 `images/` 中的离线镜像，如果目录不存在则从镜像仓库拉取；
7. 注册 `crest.service`；
8. 启动服务；
9. 输出访问地址、账号和初始密码。

默认账号：

```text
用户名：admin
密码：安装完成后终端输出的初始密码
```

## 运维命令

安装后使用 `crestctl`：

```bash
crestctl status
crestctl start
crestctl stop
crestctl restart
crestctl clear-logs
crestctl backup
crestctl restore crest-backup-YYYYMMDD_HHMMSS.tar.gz
crestctl version
```

备份建议：

- 重要升级前先停服备份；
- 备份文件不要放在 `/opt/crest` 目录内；
- 备份文件可能包含数据库内容、导出文件和业务配置，不提交到 Git。

## 升级

升级前：

```bash
crestctl backup stop
```

替换镜像标签后，在运行目录执行：

```bash
cd /opt/crest
docker-compose up -d --no-deps crest
```

升级后检查：

```bash
crestctl status
docker logs crest --tail 200
```

确认点：

- `crest` 容器健康；
- Flyway 迁移成功；
- 登录、工作台、数据源、数据集、仪表盘、数据大屏、数据血缘和导出中心可打开；
- 日志中没有持续的 `ERROR` 或 `Exception`。

## 离线包制作

发布包按 CPU 架构分别制作：

```bash
bash make-offline-package.sh v1.3.0 linux-amd64
bash make-offline-package.sh v1.3.0 linux-arm64
```

默认输出目录：

```text
../offline-packages
```

正式包名：

```text
crest-offline-v1.3.0-linux-amd64.tar.gz
crest-offline-v1.3.0-linux-arm64.tar.gz
```

制作前需要本机已有对应架构镜像：

```text
ghcr.io/sevoniva/crest:v1.3.0-amd64
ghcr.io/sevoniva/crest:v1.3.0-arm64
mysql:8.4.5
```

也可以通过环境变量指定来源镜像：

```bash
SOURCE_APP_IMAGE=ghcr.io/sevoniva/crest:v1.3.0-amd64 \
APP_IMAGE=ghcr.io/sevoniva/crest:v1.3.0-amd64 \
MYSQL_IMAGE=mysql:8.4.5 \
bash make-offline-package.sh v1.3.0 linux-amd64
```

脚本会校验应用镜像和 MySQL 镜像架构，架构不一致会直接停止。

## 离线安装

在目标服务器执行：

```bash
tar -zxf crest-offline-v1.3.0-linux-amd64.tar.gz
cd crest-offline-v1.3.0-linux-amd64
vi install.conf
bash install.sh
```

离线包内的 `images/` 目录会被安装脚本自动加载：

```bash
docker load -i images/<image>.tar.gz
```

安装完成后访问：

```text
http://服务器IP:8100
```

## 数据持久化

默认运行目录：

```text
/opt/crest
```

重要目录：

| 路径 | 内容 |
| --- | --- |
| `/opt/crest/.env` | 安装时生成的环境变量 |
| `/opt/crest/conf` | 应用配置 |
| `/opt/crest/data/mysql` | MySQL 数据 |
| `/opt/crest/data/exportData` | 导出文件 |
| `/opt/crest/data/static-resource` | 静态资源 |
| `/opt/crest/data/font` | 字体文件 |
| `/opt/crest/logs` | 应用和容器日志 |
| `/opt/crest/cache` | 应用缓存 |

备份和迁移时至少保留 `/opt/crest` 目录和数据库数据。

## 故障排查

查看服务：

```bash
crestctl status
docker ps
docker logs crest --tail 200
docker logs mysql-crest --tail 200
```

查看应用日志：

```bash
tail -f /opt/crest/logs/crest/info.log
tail -f /opt/crest/logs/crest/error.log
```

常见问题：

| 现象 | 检查点 |
| --- | --- |
| 页面打不开 | 端口是否开放、`crest` 容器是否健康、反向代理是否转发到 `8100` |
| 登录失败 | 初始密码是否来自安装输出，是否已经被修改 |
| 数据库连接失败 | `mysql-crest` 是否健康，`DE_MYSQL_PASSWORD` 是否和 `.env` 一致 |
| 升级后启动失败 | Flyway 迁移日志、数据库备份是否可恢复、镜像架构是否正确 |
| 导出文件不可下载 | `/opt/crest/data/exportData` 权限和磁盘空间 |

## 不提交的内容

以下内容只存在本机或发布流程，不进入 Git：

- `offline-packages/`
- `images/`
- `install.log`
- `/opt/crest` 运行目录备份
- `.env`
- 备份压缩包
- 镜像导出包
- 含真实连接串或账号的数据文件
