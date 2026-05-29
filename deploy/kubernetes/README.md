# Kubernetes 部署

这个目录提供 Crest 在 Kubernetes 上的两种安装方式：

- `internal-mysql`：同时安装 Crest 和一个 MySQL StatefulSet；
- `external-mysql`：只安装 Crest，数据库使用外部 MySQL。

两种方式共用 `base` 里的应用 Deployment、Service、PVC 和应用配置模板。差异主要在 `crest-env` ConfigMap：

| 配置项 | 内置 MySQL | 外部 MySQL |
| --- | --- | --- |
| `CREST_DB_HOST` | `crest-mysql` | 外部数据库地址 |
| `CREST_DB_PORT` | `3306` | 外部数据库端口 |
| `CREST_DB_NAME` | `crest` | 外部数据库名 |
| `CREST_DB_PARAMS` | MySQL JDBC 参数 | MySQL JDBC 参数 |

数据库账号密码放在 `crest-db-secret`。生产环境不要使用示例密码。

## 镜像

应用镜像默认使用：

```text
ghcr.io/sevoniva/crest:v1.3.0
```

Dockerfile 默认使用 JDK Alpine 生成裁剪运行时，并以 Alpine 作为最终基础镜像：

```text
构建阶段：eclipse-temurin:21-jdk-alpine
运行阶段：alpine:3.22
```

最终镜像只保留运行应用需要的 Java runtime、应用包、驱动和静态资源，不包含 JDK 工具链。容器以固定非 root 用户 `10001:10001` 运行，Kubernetes 清单默认开启：

- `runAsNonRoot`
- `allowPrivilegeEscalation: false`
- `capabilities.drop: ALL`
- `seccompProfile: RuntimeDefault`

默认构建：

```bash
docker build -t ghcr.io/sevoniva/crest:main .
```

如果构建机无法访问 Docker Hub，但本地已经有基础镜像，可以临时关闭 BuildKit 避免重新拉取 metadata：

```bash
DOCKER_BUILDKIT=0 docker build -t ghcr.io/sevoniva/crest:main .
```

## 内置 MySQL

适合单集群、快速安装或测试环境：

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

默认账号：

```text
admin / admin
```

内置 MySQL 默认使用 `mysql:8.0`。如果集群不能访问 Docker Hub，先把镜像同步到自己的镜像仓库，再修改 `internal-mysql/mysql.yaml` 里的镜像地址。

## 外部 MySQL

适合正式环境。先创建数据库：

```sql
CREATE DATABASE crest DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

然后修改 `deploy/kubernetes/external-mysql/kustomization.yaml`：

```yaml
configMapGenerator:
  - name: crest-env
    literals:
      - CREST_DB_HOST=mysql.example.com
      - CREST_DB_PORT=3306
      - CREST_DB_NAME=crest
```

修改 `crest-db-secret` 里的账号密码后部署：

```bash
kubectl apply -k deploy/kubernetes/external-mysql
kubectl -n crest-external rollout status deployment/crest --timeout=300s
kubectl -n crest-external port-forward svc/crest 18101:8100
```

访问：

```text
http://127.0.0.1:18101/index.html
```

## 本地 kind 验证

构建并加载镜像：

```bash
mvn -s .mvn/settings.xml -pl :core-backend -am -DskipTests -Dmaven.test.skip=true package
docker build -t ghcr.io/sevoniva/crest:main .
kind load docker-image ghcr.io/sevoniva/crest:main --name devops-kind
kind load docker-image mysql:8.0 --name devops-kind
```

如果 `kind load` 后 MySQL 名称不是 `docker.io/library/mysql:8.0`，可以在 kind 节点里补一个标签：

```bash
docker exec devops-kind-control-plane \
  ctr --namespace=k8s.io images tag docker.io/devops-kind/mysql:8.0 docker.io/library/mysql:8.0
```

外部 MySQL 模式在 Docker Desktop 上可使用宿主机地址 `host.docker.internal`。例如本机 MySQL 映射到 `13306` 时：

```bash
docker exec crest-mysql-local mysql -uroot -p'<mysql-root-password>' \
  -e "DROP DATABASE IF EXISTS crest_k8s_external; CREATE DATABASE crest_k8s_external DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
```

验证点：

- Pod 全部 Ready，应用容器重启次数为 0；
- `/index.html`、`/doc.html`、`/v3/api-docs`、`/v3/api-docs/5-relation` 返回 200；
- 使用 `CREST_INITIAL_PASSWORD` 配置的初始管理员密码可以登录；
- `de_standalone_version` 最新迁移成功，包含 `1.3:demo engineering efficiency`；
- 初始状态包含零售经营和研发效能演示数据源、数据集、图表和数据大屏；
- 应用日志没有 `WARN`、`ERROR`、`Exception`。

清理：

```bash
kubectl delete ns crest-internal crest-external
```
