# 单点登录

Crest 支持基于 OIDC / OAuth2 授权码模式的单点登录。该能力不绑定具体厂商，适用于 Keycloak、Authing、Azure AD、Okta、企业微信开放平台网关以及企业自建认证中心等身份提供方。

## 能力边界

已支持：

- 授权码模式；
- 后端使用 Client Secret 换取访问令牌；
- 通过 UserInfo 端点读取用户属性；
- 按外部唯一标识绑定账号；
- 按配置自动创建普通账号；
- 本地管理员应急登录；
- 用户管理中查看认证来源和最近登录时间。

暂不支持：

- SAML；
- PKCE 公共客户端模式；
- 自动读取 `.well-known/openid-configuration`；
- 自动同步组织、角色和部门；
- 退出时同步登出身份提供方。

组织、角色和部门通常由企业目录定义。建议先完成 OIDC 登录闭环，再按本企业权限模型扩展映射规则。

## 配置存储

SSO 配置保存到 `core_sys_setting`，键名前缀为 `sso.`。

| 配置项 | 说明 |
| --- | --- |
| `sso.enabled` | 是否启用单点登录 |
| `sso.providerName` | 登录页展示名称 |
| `sso.clientId` | OIDC / OAuth2 Client ID |
| `sso.clientSecret` | 加密保存的 Client Secret |
| `sso.authorizationEndpoint` | 授权端点 |
| `sso.tokenEndpoint` | 令牌端点 |
| `sso.userInfoEndpoint` | 用户信息端点 |
| `sso.issuer` | Issuer，用于对接记录 |
| `sso.scope` | 默认 `openid profile email` |
| `sso.redirectUri` | 固定回调地址；留空时按当前访问地址生成 |
| `sso.userIdAttribute` | 外部用户唯一标识字段，默认 `sub` |
| `sso.accountAttribute` | Crest 账号字段，默认 `preferred_username` |
| `sso.nameAttribute` | 用户姓名字段，默认 `name` |
| `sso.emailAttribute` | 邮箱字段，默认 `email` |
| `sso.autoCreateUser` | 未匹配用户是否自动创建为普通账号 |
| `sso.allowLocalLogin` | 登录页是否保留本地账号入口 |
| `sso.requireHttps` | 非本地端点是否必须使用 HTTPS |
| `sso.logoutRedirectUrl` | 退出跳转地址，当前仅保存配置 |

用户表 `crest_user` 增加字段：

| 字段 | 说明 |
| --- | --- |
| `auth_type` | `LOCAL` 或 `SSO` |
| `external_id` | 身份提供方返回的唯一用户标识 |
| `last_login_time` | 最近登录成功时间 |

## 登录流程

```text
浏览器访问 /sso/login
后端生成 state 和 nonce，缓存 10 分钟
浏览器跳转到身份提供方授权端点
身份提供方回调 /sso/callback
后端校验 state，并使用 code 换取访问令牌
后端使用 access_token 请求 UserInfo
后端按字段映射查找、绑定或创建 Crest 用户
后端生成一次性票据，缓存 60 秒
浏览器回到 /#/login?ssoTicket=...
前端使用票据换取 Crest token
前端进入目标页面
```

Crest token 不直接出现在回调 URL 中。一次性票据完成兑换后立即失效。

## 用户映射

匹配顺序：

1. 优先匹配 `auth_type = SSO` 且 `external_id` 相同的用户；
2. 未命中时，按映射后的账号匹配现有用户；
3. 命中现有用户时，将该用户绑定到当前外部身份；
4. 仍未命中且开启自动创建时，创建普通账号；
5. 仍未命中且未开启自动创建时，拒绝登录。

SSO 用户的账号由身份提供方维护，用户管理中不能修改账号。SSO 用户不展示“重置密码”，也不能通过修改密码页变更本地密码。

字段限制：

- 账号只支持 64 位以内的字母、数字、点、下划线、横线和 `@`；
- 姓名不超过 64 个字符，不能包含 HTML 标签或控制字符；
- 建议账号字段使用员工号、邮箱或稳定登录名，不建议使用姓名。

## 配置步骤

1. 在身份提供方创建 Web 应用；
2. 将 Crest 页面展示的回调地址登记为 Redirect URI，默认格式为 `https://<host>/de2api/sso/callback`；
3. 在“系统设置 / 单点登录”中填写 Client ID、Client Secret、授权端点、令牌端点和用户信息端点；
4. 根据 UserInfo 返回结构配置字段映射；
5. 保存配置，并先保留本地登录入口；
6. 使用无痕窗口验证 SSO 登录；
7. 验证管理员应急入口 `/#/admin-login`；
8. 确认登录链路稳定后，再按安全要求关闭本地登录入口。

## 加密配置

Client Secret 使用运行时 AES 配置加密保存：

```bash
CREST_AES_KEY=<16/24/32-character-key>
CREST_AES_IV=<16-character-iv>
```

已有环境升级时不要修改这两个值。修改后，已保存的数据源密码和 SSO Client Secret 等密文无法解密，需要重新配置。

## HTTPS 和代理

生产环境建议：

- 对外访问统一使用 HTTPS；
- 反向代理透传 `X-Forwarded-Proto`、`X-Forwarded-Host` 和 `X-Forwarded-Port`；
- 在身份提供方登记外部访问域名下的回调地址；
- 开启 `sso.requireHttps`。

本地调试时，`localhost` 和 `127.0.0.1` 可以使用 HTTP。

## 应急登录

当 `sso.allowLocalLogin=false` 时，普通登录页隐藏本地账号入口。管理员仍可通过以下地址处理应急运维：

```text
/#/admin-login
```

后端只允许管理员账号使用该入口。普通用户仍需通过单点登录进入系统。

## 接口

| 接口 | 说明 |
| --- | --- |
| `GET /sso/public/status` | 登录页读取公开状态 |
| `GET /sso/login` | 发起单点登录 |
| `GET /sso/callback` | 身份提供方回调 |
| `GET /sso/token/{ticket}` | 使用一次性票据换取 Crest token |
| `GET /sso/config` | 管理员读取配置 |
| `POST /sso/config` | 管理员保存配置 |
| `POST /sso/validate` | 管理员校验配置完整性 |

管理接口需要管理员权限。公开接口仅处理登录流程，不返回敏感配置。
