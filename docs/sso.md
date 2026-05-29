# 单点登录

Crest 支持通用 OIDC / OAuth2 Authorization Code 单点登录。实现不绑定具体厂商，适用于 Keycloak、Authing、Azure AD、Okta、企业微信开放平台网关、自建统一认证中心等能够提供标准授权端点、令牌端点和 UserInfo 端点的身份提供方。

## 支持范围

当前支持：

- Authorization Code 模式；
- `client_secret` 后端换取 token；
- 通过 UserInfo 端点读取用户身份；
- 账号自动创建；
- 已有本地账号按账号名绑定外部身份；
- 管理员应急本地登录；
- 用户管理中查看认证来源和最近登录时间。

当前不做：

- SAML；
- PKCE 公共客户端模式；
- 自动读取 `.well-known/openid-configuration`；
- 自动同步组织、角色和部门；
- 自动登出身份提供方。

如需接入企业目录，建议先完成 OIDC 对接，再按本企业组织模型补充用户组或角色映射。

## 数据存储

SSO 配置保存到 `core_sys_setting`，键名前缀为 `sso.`。

| 配置项 | 说明 |
| --- | --- |
| `sso.enabled` | 是否启用单点登录 |
| `sso.providerName` | 登录页展示的身份提供方名称 |
| `sso.clientId` | OIDC / OAuth2 Client ID |
| `sso.clientSecret` | 加密保存的 Client Secret |
| `sso.authorizationEndpoint` | 授权端点 |
| `sso.tokenEndpoint` | 令牌端点 |
| `sso.userInfoEndpoint` | 用户信息端点 |
| `sso.issuer` | Issuer，留作审计和对接说明 |
| `sso.scope` | 默认 `openid profile email` |
| `sso.redirectUri` | 固定回调地址；留空时按当前访问域名生成 |
| `sso.userIdAttribute` | 外部用户唯一标识字段，默认 `sub` |
| `sso.accountAttribute` | Crest 账号字段，默认 `preferred_username` |
| `sso.nameAttribute` | 姓名字段，默认 `name` |
| `sso.emailAttribute` | 邮箱字段，默认 `email` |
| `sso.autoCreateUser` | 外部用户首次登录时是否自动创建 |
| `sso.allowLocalLogin` | 普通登录页是否允许本地账号登录 |
| `sso.requireHttps` | 是否要求 HTTPS 端点，localhost 调试除外 |
| `sso.logoutRedirectUrl` | 退出后跳转地址，当前保留配置 |

用户表 `crest_user` 增加字段：

| 字段 | 说明 |
| --- | --- |
| `auth_type` | `LOCAL` 或 `SSO` |
| `external_id` | 身份提供方返回的唯一用户标识 |
| `last_login_time` | 最近一次登录成功时间 |

## 登录流程

```text
浏览器 -> /sso/login
后端生成 state + nonce，缓存 10 分钟
浏览器跳转到身份提供方授权端点
身份提供方回调 /sso/callback
后端校验 state，用 code 换 token
后端用 access_token 请求 UserInfo
后端按字段映射创建或更新 Crest 用户
后端生成一次性 ticket，缓存 60 秒
浏览器回到 /#/login?ssoTicket=...
前端用 ticket 换取 Crest token
前端进入原目标页面
```

Crest 不把登录 token 直接放在回调 URL 中。回调只带一次性 ticket，ticket 被换取后立即失效。

## 用户映射规则

1. 先用 `auth_type = SSO` 且 `external_id` 相同的用户匹配；
2. 找不到时，用映射后的账号匹配现有用户；
3. 命中现有用户时，将该用户绑定到当前外部身份；
4. 仍找不到且开启自动创建时，创建普通用户；
5. 仍找不到且未开启自动创建时，拒绝登录。

SSO 用户的账号由身份提供方维护，用户管理中不能改账号。SSO 用户不展示“重置密码”，也不能在修改密码页修改本地密码。

账号字段限制为 64 位以内的字母、数字、点、下划线、横线和 `@`。建议身份提供方返回员工号、邮箱或稳定登录名，不要返回姓名。

## 配置步骤

1. 在身份提供方创建 Web 应用；
2. 将 Crest 页面中展示的回调地址填入身份提供方的 Redirect URI，默认格式为 `https://<host>/de2api/sso/callback`；
3. 在 Crest 的“系统设置 / 单点登录”中填写 Client ID、Client Secret、授权端点、令牌端点和用户信息端点；
4. 按身份提供方返回的字段调整映射；
5. 先保持“允许本地账号登录”开启；
6. 保存后在无痕窗口验证 SSO 登录；
7. 验证管理员应急入口 `/#/admin-login` 可用；
8. 再按安全要求决定是否关闭普通登录页的本地账号登录。

## 加密要求

Client Secret 使用运行时 AES 配置加密保存：

```bash
CREST_AES_KEY=<16/24/32-character-key>
CREST_AES_IV=<16-character-iv>
```

已有环境升级时不要修改这两个值。修改后，已保存的数据源密码、SSO Client Secret 等密文无法解密，需要重新配置。

## HTTPS 和代理

生产环境建议：

- 对外访问统一走 HTTPS；
- 反向代理透传 `X-Forwarded-Proto`、`X-Forwarded-Host` 和 `X-Forwarded-Port`；
- 在身份提供方中登记外部访问域名下的回调地址；
- 开启 `sso.requireHttps`。

本地调试 `localhost` 和 `127.0.0.1` 可以使用 HTTP。

## 应急登录

当 `sso.allowLocalLogin=false` 时，普通登录页会隐藏本地账号登录。管理员仍可通过：

```text
/#/admin-login
```

使用本地管理员账号登录。后端只允许管理员账号走该应急路径，普通用户仍需使用单点登录。

## 接口

| 接口 | 说明 |
| --- | --- |
| `GET /sso/public/status` | 登录页读取公开状态 |
| `GET /sso/login` | 发起单点登录 |
| `GET /sso/callback` | 身份提供方回调 |
| `GET /sso/token/{ticket}` | 前端用一次性票据换 Crest token |
| `GET /sso/config` | 管理员读取配置 |
| `POST /sso/config` | 管理员保存配置 |
| `POST /sso/validate` | 管理员校验配置完整性 |

管理接口需要管理员权限。公开接口仅处理登录流程，不返回敏感配置。
