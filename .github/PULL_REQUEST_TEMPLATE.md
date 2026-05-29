## 变更内容

说明这次 PR 解决什么问题，涉及哪些模块。

## 验证结果

请列出已经执行的命令或手工验证步骤，例如：

```text
pnpm run build:lite:check
mvn -s .mvn/settings.xml -pl :core-backend -am clean package -Pstandalone -DskipTests -Dmaven.test.skip=true
```

## 影响范围

- [ ] 前端页面或组件
- [ ] 后端接口或服务
- [ ] 数据库迁移或初始化数据
- [ ] 安装脚本、Docker 或 Kubernetes
- [ ] 文档
- [ ] 其他：

## 提交前确认

- [ ] 没有提交 `.env`、密码、token、私钥、证书、备份、日志、离线包或真实业务数据。
- [ ] 文档已按实际行为更新。
- [ ] 数据库迁移脚本没有直接修改已发布基线。
- [ ] UI 改动已检查常用分辨率。
- [ ] 提交信息清楚，便于回溯。
