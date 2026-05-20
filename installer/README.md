# DataEase 离线安装包制作工程

本工程用来制作 DataEase 离线安装包。

如需下载 DataEase 安装包，请移步：https://community.fit2cloud.com/#/products/dataease/downloads

## 内部轻量安装

本 fork 默认启用 `DE_INTERNAL_LITE=true`，离线包安装后只启动 DataEase 与 MySQL。APISIX、Playwright 和同步任务服务默认不安装，适合内部报表和数据分析场景。

如需恢复完整服务，可在 `install.conf` 中调整以下配置后重新制作安装包：

```bash
DE_INTERNAL_LITE=false
DE_EXTERNAL_APISIX=false
DE_EXTERNAL_PLAYWRIGHT=false
DE_EXTERNAL_SYNC_TASK=false
```
