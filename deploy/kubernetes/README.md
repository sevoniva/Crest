# Crest Kubernetes 部署

本目录提供 Crest 在 Kubernetes 上的基础部署清单。当前支持两种方式：

- `internal-mysql`：同时安装 Crest 和 MySQL；
- `external-mysql`：只安装 Crest，连接外部 MySQL。

两种方式共用 `base` 中的应用 Deployment、Service、PVC 和配置模板，差异在数据库地址、数据库 Secret 和是否创建 MySQL StatefulSet。

## 目录结构

```text
deploy/kubernetes
├── base
│   ├── configmap-app.yaml
│   ├── deployment.yaml
│   ├── kustomization.yaml
│   ├── pvc.yaml
│   ├── service.yaml
│   └── serviceaccount.yaml
├── internal-mysql
│   ├── kustomization.yaml
│   ├── mysql-init.sql
│   ├── mysql-my.cnf
│   ├── mysql.yaml
│   └── namespace.yaml
└── external-mysql
    ├── kustomization.yaml
    └── namespace.yaml
```

## 镜像

默认应用镜像：

```text
ghcr.io/sevoniva/crest:v1.3.0
```

应用镜像以 Alpine 为最终基础镜像，内置 jlink 裁剪后的 Java 21 runtime。容器以固定非 root 用户运行：

```text
uid: 10001
gid: 10001
```

默认安全上下文：

- `runAsNonRoot: true`
- `allowPrivilegeEscalation: false`
- `capabilities.drop: ALL`
- `seccompProfile: RuntimeDefault`
- 应用容器 `readOnlyRootFilesystem: true`

本地构建镜像：

```bash
mvn -s .mvn/settings.xml -pl :core-backend -am clean package -Pstandalone -DskipTests -Dmaven.test.skip=true
docker build -t ghcr.io/sevoniva/crest:main .
```

## 配置项

`crest-env` ConfigMap:

| 配置项 | 说明 |
| --- | --- |
| `CREST_CONTEXT_PATH` | 应用上下文路径，默认空 |
| `CREST_DB_HOST` | MySQL 地址 |
| `CREST_DB_PORT` | MySQL 端口 |
| `CREST_DB_NAME` | 元数据库名 |
| `CREST_DB_PARAMS` | JDBC 参数 |
| `CREST_ORIGIN_LIST` | 允许来源列表，正式环境改为实际域名 |
| `CREST_LOGIN_TIMEOUT` | 登录超时时间，默认 960 分钟 |

`crest-db-secret` Secret:

| 配置项 | 说明 |
| --- | --- |
| `CREST_DB_USERNAME` | 元数据库账号 |
| `CREST_DB_PASSWORD` | 元数据库密码 |
| `MYSQL_ROOT_PASSWORD` | 内置 MySQL 模式需要 |
| `CREST_AES_KEY` | 32 位加密 Key |
| `CREST_AES_IV` | 16 位加密 IV |
| `CREST_INITIAL_PASSWORD` | 管理员初始密码 |

部署前必须替换所有占位值。不要把生产密码提交到仓库。

## 内置 MySQL 部署

适合快速验证、单集群测试或不依赖外部数据库的环境。

部署：

```bash
kubectl apply -k deploy/kubernetes/internal-mysql
kubectl -n crest-internal rollout status statefulset/crest-mysql --timeout=180s
kubectl -n crest-internal rollout status deployment/crest --timeout=300s
kubectl -n crest-internal port-forward svc/crest 18100:8100
```

访问：

```text
http://127.0.0.1:18100/index.html
```

账号：

```text
用户名：admin
密码：internal-mysql/kustomization.yaml 中 CREST_INITIAL_PASSWORD 的值
```

内置 MySQL 默认使用：

```text
mysql:8.0
```

如果集群无法访问 Docker Hub，需要先把 MySQL 镜像同步到内部镜像仓库，再修改 `internal-mysql/mysql.yaml`。

## 外部 MySQL 部署

适合正式环境。先准备数据库：

```sql
CREATE DATABASE crest DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

修改 `deploy/kubernetes/external-mysql/kustomization.yaml`：

```yaml
configMapGenerator:
  - name: crest-env
    literals:
      - CREST_DB_HOST=mysql.example.com
      - CREST_DB_PORT=3306
      - CREST_DB_NAME=crest
      - CREST_DB_PARAMS=autoReconnect=false&useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
```

替换 `crest-db-secret` 中的账号、密码和加密参数后部署：

```bash
kubectl apply -k deploy/kubernetes/external-mysql
kubectl -n crest-external rollout status deployment/crest --timeout=300s
kubectl -n crest-external port-forward svc/crest 18101:8100
```

访问：

```text
http://127.0.0.1:18101/index.html
```

## 持久化

`base/pvc.yaml` 创建 `crest-data` PVC，用于保存：

- 静态资源；
- 外观配置；
- 导出文件；
- Excel 文件；
- 字体文件；
- i18n 文件。

应用日志和缓存默认使用 `emptyDir`：

| Volume | 类型 | 说明 |
| --- | --- | --- |
| `crest-cache` | `emptyDir`，内存 | 应用缓存 |
| `crest-logs` | `emptyDir`，内存 | 容器内日志 |
| `crest-tmp` | `emptyDir`，内存 | 临时文件 |
| `crest-data` | PVC | 业务持久化数据 |

正式环境如需长期保存应用日志，建议接入集群日志采集系统，或按现场规范改为持久化卷。

## 本地 kind 验证

构建并加载镜像：

```bash
mvn -s .mvn/settings.xml -pl :core-backend -am -DskipTests -Dmaven.test.skip=true package
docker build -t ghcr.io/sevoniva/crest:main .
kind load docker-image ghcr.io/sevoniva/crest:main --name crest-kind
kind load docker-image mysql:8.0 --name crest-kind
```

如果 `kind load` 后 MySQL 名称不是 `docker.io/library/mysql:8.0`，可以在 kind 节点里补标签：

```bash
docker exec crest-kind-control-plane \
  ctr --namespace=k8s.io images tag docker.io/crest-kind/mysql:8.0 docker.io/library/mysql:8.0
```

部署内置 MySQL 模式：

```bash
kubectl apply -k deploy/kubernetes/internal-mysql
kubectl -n crest-internal get pods
```

外部 MySQL 模式在 Docker Desktop 上可使用宿主机地址 `host.docker.internal`。例如本机 MySQL 映射到 `13306`：

```bash
docker exec crest-mysql-local mysql -uroot -p'<mysql-root-password>' \
  -e "DROP DATABASE IF EXISTS crest_k8s_external; CREATE DATABASE crest_k8s_external DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
```

## 验证清单

部署后检查：

```bash
kubectl -n crest-internal get pods
kubectl -n crest-internal logs deploy/crest --tail=200
kubectl -n crest-internal describe pod -l app.kubernetes.io/name=crest
```

功能验证：

- Pod 全部 Ready，应用容器重启次数为 0；
- `/index.html` 返回 200；
- `/doc.html` 返回 200；
- `/v3/api-docs` 返回 200；
- `/v3/api-docs/5-relation` 返回 200；
- 使用 `CREST_INITIAL_PASSWORD` 配置的初始管理员密码可以登录；
- `de_standalone_version` 最新迁移成功，包含 `1.5:sanitize user display names`；
- 初始状态包含零售经营和研发效能演示数据源、数据集、图表和数据大屏；
- 工作台、数据源、数据集、仪表盘、数据大屏、数据血缘、系统设置和导出中心能打开；
- 应用日志没有持续的 `WARN`、`ERROR` 或 `Exception`。

## 升级

升级前：

1. 备份外部 MySQL 或内置 MySQL PVC；
2. 记录当前镜像标签；
3. 准备新镜像并确认架构；
4. 在测试命名空间完成一次迁移验证。

更新镜像后：

```bash
kubectl -n crest-internal set image deployment/crest crest=ghcr.io/sevoniva/crest:v1.3.0
kubectl -n crest-internal rollout status deployment/crest --timeout=300s
```

如需回滚：

```bash
kubectl -n crest-internal rollout undo deployment/crest
```

数据库迁移由 Flyway 自动执行。迁移失败时不要反复重启覆盖现场，先保存日志和数据库备份。

## 清理

```bash
kubectl delete ns crest-internal
kubectl delete ns crest-external
```

删除命名空间会删除命名空间内的 PVC。正式环境清理前先确认备份。

## 公开仓库注意事项

清单中的 Secret 当前使用占位值，部署前由运维替换。不要提交真实密码、内网地址、证书、kubeconfig、镜像仓库凭据或生产导出的 YAML。
