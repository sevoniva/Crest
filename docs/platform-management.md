# 平台管理与权限体系

本文说明 Crest 平台管理能力的源码现状、改造范围、数据表设计和权限口径。该能力面向企业内网部署，目标是在保留现有登录和资源模型的基础上，补齐组织、角色、菜单权限、资源权限和用户批量管理能力。

## 现状梳理

### 登录、Token 和用户上下文

当前登录入口位于 `SubstituleLoginServer`，本地账号登录使用 `/login/localLogin`。账号和密码由前端 RSA 加密，后端解密后校验 `crest_user.password_hash`。校验通过后生成 JWT，JWT 包含 `uid` 和 `oid`。

请求鉴权由 `TokenFilter` 完成。过滤器从 `X-DE-TOKEN` 读取 JWT，通过 `TokenUtils` 按用户密钥验签，并将 `TokenUserBO` 写入 `AuthUtils` 的线程上下文。后端业务代码通过 `AuthUtils.getUser()` 获取当前用户 ID 和默认组织 ID。

单点登录由 `SsoManage` 处理。SSO 用户可按配置自动创建，也可拒绝不存在用户登录。SSO 登录成功后同样生成包含 `uid` 和 `oid` 的 JWT。

### 用户、角色、组织与权限残留

社区版已有 `crest_user` 作为本地用户表，包含账号、姓名、邮箱、电话、启停状态、管理员标记、认证类型、外部身份 ID、最近登录时间等字段。

权限 API 在 `sdk/api/api-permissions` 中已经定义了组织、角色、用户、菜单权限和资源权限接口，但后端替补实现原先不完整。系统设置中已有用户管理页面，角色、组织、权限页面和后端实现需要由本次补齐。

菜单来自 `core_menu`。原系统主要依赖前端菜单渲染和少量管理员判断，缺少可配置的角色菜单授权表。

### 前端系统管理

系统设置使用统一路由和菜单结构。用户管理页面已存在，入口在系统设置中。前端请求通过 `src/config/axios` 统一带上 `X-DE-TOKEN`，菜单和路由由后端菜单接口返回结果驱动。

本次新增的组织管理、角色管理、权限管理页面放在系统设置下。前端只负责展示、勾选和隐藏，不承担最终权限判定。

### 业务资源归属

数据源、数据集、仪表盘和数据大屏已有创建人或组织字段：

| 资源 | 主要表 | 可复用字段 |
| --- | --- | --- |
| 数据源 | `core_datasource` | `create_by`、`create_time`、`update_time` |
| 数据集 | `core_dataset_group` | `create_by`、`create_time`、`last_update_time`、`node_type` |
| 仪表盘/大屏 | `data_visualization_info` | `create_by`、`org_id`、`type`、`node_type`、`delete_flag` |

数据源和数据集没有完整组织字段，因此需要统一资源索引表补齐组织归属。仪表盘和数据大屏优先使用已有 `org_id`，同时写入统一资源索引。

## 改造方案

### 设计原则

1. 复用 `crest_user`、现有登录态和 JWT 机制。
2. 复用现有资源表的创建人和组织字段。
3. 新增表只覆盖组织、用户组织关系、角色、角色菜单权限、资源索引、资源授权。
4. 用户不存在时，SSO 按配置自动创建或拒绝登录。
5. 权限判断在后端生效，前端只做菜单、按钮和表单展示。
6. 数据源、数据集、仪表盘、数据大屏的树查询接口统一加资源范围过滤。

### 后端改造点

| 能力 | 改造点 |
| --- | --- |
| 用户管理 | 新建、编辑、删除、启停、批量删除、批量导入；导入支持 `xlsx/xls/csv`；用户支持多角色 |
| 组织管理 | 支持多级组织树、创建子组织、编辑、删除；默认组织不可删除 |
| 角色管理 | 支持角色查询、创建、编辑、删除、挂载用户、组织外用户搜索 |
| 菜单权限 | 通过 `crest_role_menu_permission` 控制角色可见菜单 |
| 资源权限 | 通过 `crest_resource_index` 和 `crest_resource_permission` 控制资源可见范围 |
| 登录态 | 本地登录和 SSO 登录生成 token 时写入用户默认组织 |
| 资源过滤 | 数据源、数据集、仪表盘、大屏树查询按创建人、组织、用户授权、角色授权过滤 |

### 前端改造点

| 页面 | 改造点 |
| --- | --- |
| 系统设置 / 用户管理 | 角色单选改为多选，角色从后端读取 |
| 系统设置 / 组织管理 | 新增组织树表格，支持创建子组织、编辑、删除 |
| 系统设置 / 角色管理 | 新增角色列表和编辑表单 |
| 系统设置 / 权限管理 | 新增角色选择、菜单权限树、资源权限树 |

## 数据库变更

迁移脚本：`V1.13__platform_management_rbac.sql`

新增表：

| 表名 | 作用 |
| --- | --- |
| `crest_org` | 组织树 |
| `crest_role` | 角色 |
| `crest_user_org` | 用户与组织关系，包含默认组织 |
| `crest_user_role` | 用户与角色关系 |
| `crest_role_menu_permission` | 角色菜单权限 |
| `crest_resource_index` | 统一资源索引，补齐资源类型、组织、创建人 |
| `crest_resource_permission` | 资源授权，支持用户、角色、组织 |

初始化数据：

- 默认组织：`默认组织`
- 内置角色：`系统管理员`、`普通用户`
- 系统设置菜单：组织管理、角色管理、权限管理
- 现有用户绑定默认组织和角色
- 现有菜单授权给系统管理员
- 现有数据源、数据集、仪表盘、大屏写入资源索引

## 影响文件

后端主要文件：

- `core/core-backend/src/main/java/io/crest/substitute/permissions/login/SubstituleLoginServer.java`
- `core/core-backend/src/main/java/io/crest/system/manage/SsoManage.java`
- `core/core-backend/src/main/java/io/crest/substitute/permissions/user/CrestUserManage.java`
- `core/core-backend/src/main/java/io/crest/substitute/permissions/user/SubstituteUserServer.java`
- `core/core-backend/src/main/java/io/crest/substitute/permissions/org/CrestOrgManage.java`
- `core/core-backend/src/main/java/io/crest/substitute/permissions/org/SubstituleOrgServer.java`
- `core/core-backend/src/main/java/io/crest/substitute/permissions/role/CrestRoleManage.java`
- `core/core-backend/src/main/java/io/crest/substitute/permissions/role/SubstituteRoleServer.java`
- `core/core-backend/src/main/java/io/crest/substitute/permissions/auth/PlatformPermissionManage.java`
- `core/core-backend/src/main/java/io/crest/substitute/permissions/auth/SubstituleAuthServer.java`
- `core/core-backend/src/main/java/io/crest/menu/manage/MenuManage.java`
- `core/core-backend/src/main/java/io/crest/datasource/manage/DataSourceManage.java`
- `core/core-backend/src/main/java/io/crest/dataset/manage/DatasetGroupManage.java`
- `core/core-backend/src/main/java/io/crest/visualization/manage/CoreVisualizationManage.java`

前端主要文件：

- `core/core-frontend/src/views/system/user/index.vue`
- `core/core-frontend/src/views/system/org/index.vue`
- `core/core-frontend/src/views/system/role/index.vue`
- `core/core-frontend/src/views/system/permission/index.vue`

迁移与文案：

- `core/core-backend/src/main/resources/db/migration/V1.13__platform_management_rbac.sql`
- `core/core-backend/src/main/resources/i18n/core_zh_CN.properties`
- `core/core-backend/src/main/resources/i18n/core_zh_TW.properties`
- `core/core-backend/src/main/resources/i18n/core_en_US.properties`

## 权限口径

资源可见条件满足任一项即可：

1. 当前用户是系统管理员；
2. 当前用户是资源创建人；
3. 资源归属当前用户默认组织；
4. 资源显式授权给当前用户；
5. 资源显式授权给当前用户拥有的角色；
6. 资源显式授权给当前组织。

菜单可见条件：

1. 系统管理员拥有全部菜单；
2. 非系统管理员只返回角色授权菜单；
3. 前端根据返回菜单渲染导航，后端接口仍需做权限判断。

## 验证清单

上线前需要完成以下验证：

1. Flyway 迁移成功，`V1.13` 状态为成功。
2. 本地登录和 SSO 登录 token 均包含正确 `oid`。
3. 系统设置能看到组织管理、角色管理、权限管理。
4. 用户可分配多个角色，保存后再次打开仍保持一致。
5. 角色菜单权限保存后，非管理员只能看到授权菜单。
6. 数据源、数据集、仪表盘、大屏树接口按权限过滤。
7. 批量导入模板为 xlsx，导入成功用户具备默认组织和普通用户角色。
8. 后端编译、前端类型检查、前端构建、Docker 镜像构建均通过。
