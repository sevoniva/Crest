# Crest 离线安装包制作工程

本工程用来制作 Crest 离线安装包。

Crest 安装包由当前仓库构建产出，请以仓库 Release 或发布物为准。

## 内部轻量安装

Crest 默认启用 `DE_INTERNAL_LITE=true`，离线包安装后只启动 Crest 与 MySQL。同步任务服务默认不安装，Playwright 建议按需外接，适合内部报表和数据分析场景。

安装后的默认入口保留工作台、仪表板、数据大屏、分享、数据准备、数据集、数据源、数据血缘、系统参数和字体管理。数据血缘不需要额外服务，读取 Crest 元数据库里的数据源、数据集、字段、图表和仪表板配置生成关系图。

如需恢复同步任务或外部截图服务，可在 `install.conf` 中调整以下配置后重新制作安装包：

```bash
DE_INTERNAL_LITE=false
DE_EXTERNAL_PLAYWRIGHT=false
DE_EXTERNAL_SYNC_TASK=false
```

## 正式离线包

发布离线包按 CPU 架构分别制作，命名格式为 `crest-offline-v1.3.0-linux-amd64.tar.gz` 和 `crest-offline-v1.3.0-linux-arm64.tar.gz`。包内只包含安装脚本、Docker Compose 配置、正式说明文档和 `images/` 镜像归档。

```bash
bash make-offline-package.sh v1.3.0 linux-amd64
bash make-offline-package.sh v1.3.0 linux-arm64
```
