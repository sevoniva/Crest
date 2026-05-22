# DataEase 离线安装包制作工程

本工程用来制作 DataEase 离线安装包。

本 fork 的安装包由当前仓库构建产出，请以仓库 Release 或内部发布物为准。

## 内部轻量安装

本 fork 默认启用 `DE_INTERNAL_LITE=true`，离线包安装后只启动 DataEase 与 MySQL。同步任务服务默认不安装，Playwright 建议按需外接，适合内部报表和数据分析场景。

安装后的默认入口保留工作台、仪表板、数据大屏、分享、数据准备、数据集、数据源、数据血缘、系统参数和字体管理。数据血缘不需要额外服务，读取 DataEase 元数据库里的数据源、数据集、字段、图表和仪表板配置生成关系图。

如需恢复同步任务或外部截图服务，可在 `install.conf` 中调整以下配置后重新制作安装包：

```bash
DE_INTERNAL_LITE=false
DE_EXTERNAL_PLAYWRIGHT=false
DE_EXTERNAL_SYNC_TASK=false
```
