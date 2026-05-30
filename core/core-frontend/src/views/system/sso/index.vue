<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { saveSsoConfigApi, ssoConfigApi, validateSsoConfigApi } from '@/api/sso'

const loading = ref(false)
const saving = ref(false)
const form = reactive<any>({
  enabled: false,
  providerName: '统一身份认证',
  providerType: 'OIDC_GENERIC',
  clientId: '',
  clientSecret: '',
  authorizationEndpoint: '',
  tokenEndpoint: '',
  userInfoEndpoint: '',
  issuer: '',
  scope: 'openid profile email',
  redirectUri: '',
  userIdAttribute: 'sub',
  accountAttribute: 'preferred_username',
  nameAttribute: 'name',
  emailAttribute: 'email',
  unionIdAttribute: '',
  autoCreateUser: true,
  allowLocalLogin: true,
  requireHttps: true,
  logoutRedirectUrl: '',
  callbackUrl: '',
  secretConfigured: false
})

const loadConfig = async () => {
  loading.value = true
  try {
    const res = await ssoConfigApi()
    Object.assign(form, res.data || {})
    form.clientSecret = ''
  } finally {
    loading.value = false
  }
}

const save = async () => {
  saving.value = true
  try {
    await saveSsoConfigApi(form)
    ElMessage.success('配置已保存')
    await loadConfig()
  } finally {
    saving.value = false
  }
}

const validate = async () => {
  await validateSsoConfigApi(form)
  ElMessage.success('配置校验通过')
}

const copyCallback = async () => {
  await navigator.clipboard.writeText(form.callbackUrl || '')
  ElMessage.success('回调地址已复制')
}

onMounted(loadConfig)
</script>

<template>
  <div class="sso-setting" v-loading="loading">
    <p class="router-title">单点登录</p>
    <div class="setting-panel">
      <div class="panel-head">
        <div>
          <div class="setting-title">OIDC / OAuth2</div>
          <div class="setting-desc">
            通过授权码模式接入企业身份提供方，用户属性以 UserInfo 响应为准。
          </div>
        </div>
        <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
      </div>

      <el-form label-position="top" class="sso-form">
        <div class="form-grid">
          <el-form-item label="身份提供方名称" required>
            <el-input v-model.trim="form.providerName" maxlength="64" />
          </el-form-item>
          <el-form-item label="身份提供方类型" required>
            <el-select v-model="form.providerType" class="full-width">
              <el-option label="通用 OIDC" value="OIDC_GENERIC" />
              <el-option label="Casdoor" value="CASDOOR" />
              <el-option label="通用 OAuth2" value="OAUTH2_GENERIC" />
              <el-option label="飞书" value="FEISHU" />
              <el-option label="企业微信" value="WECOM" />
            </el-select>
          </el-form-item>
          <el-form-item label="Client ID" required>
            <el-input v-model.trim="form.clientId" maxlength="128" />
          </el-form-item>
          <el-form-item label="Client Secret" required>
            <el-input
              v-model.trim="form.clientSecret"
              type="password"
              show-password
              maxlength="256"
              :placeholder="form.secretConfigured ? '已保存，留空表示不变更' : '请输入客户端密钥'"
            />
          </el-form-item>
          <el-form-item label="Scope" required>
            <el-input v-model.trim="form.scope" />
          </el-form-item>
        </div>

        <el-form-item label="授权端点" required>
          <el-input v-model.trim="form.authorizationEndpoint" placeholder="https://idp.example.com/oauth2/authorize" />
        </el-form-item>
        <el-form-item label="令牌端点" required>
          <el-input v-model.trim="form.tokenEndpoint" placeholder="https://idp.example.com/oauth2/token" />
        </el-form-item>
        <el-form-item label="用户信息端点" required>
          <el-input v-model.trim="form.userInfoEndpoint" placeholder="https://idp.example.com/oauth2/userinfo" />
        </el-form-item>
        <el-form-item label="Issuer">
          <el-input v-model.trim="form.issuer" placeholder="https://idp.example.com" />
        </el-form-item>
        <el-form-item label="回调地址">
          <div class="callback-row">
            <el-input
              v-model.trim="form.redirectUri"
              placeholder="留空时按当前访问地址生成"
            />
            <el-button @click="copyCallback">复制回调地址</el-button>
          </div>
          <div class="form-tip">{{ form.callbackUrl }}</div>
        </el-form-item>

        <div class="sub-title">用户字段映射</div>
        <div class="form-grid">
          <el-form-item label="唯一标识字段" required>
            <el-input v-model.trim="form.userIdAttribute" placeholder="sub" />
          </el-form-item>
          <el-form-item label="账号字段" required>
            <el-input v-model.trim="form.accountAttribute" placeholder="preferred_username" />
          </el-form-item>
          <el-form-item label="姓名字段">
            <el-input v-model.trim="form.nameAttribute" placeholder="name" />
          </el-form-item>
          <el-form-item label="邮箱字段">
            <el-input v-model.trim="form.emailAttribute" placeholder="email" />
          </el-form-item>
          <el-form-item label="统一标识字段">
            <el-input v-model.trim="form.unionIdAttribute" placeholder="union_id / unionid，可选" />
          </el-form-item>
        </div>

        <div class="switch-grid">
          <div class="switch-item">
            <div>
              <div class="switch-title">自动创建账号</div>
              <div class="switch-desc">未匹配的外部用户将创建为普通账号。</div>
            </div>
            <el-switch v-model="form.autoCreateUser" />
          </div>
          <div class="switch-item">
            <div>
              <div class="switch-title">保留本地登录</div>
              <div class="switch-desc">关闭后登录页仅保留单点登录入口。</div>
            </div>
            <el-switch v-model="form.allowLocalLogin" />
          </div>
          <div class="switch-item">
            <div>
              <div class="switch-title">强制 HTTPS</div>
              <div class="switch-desc">开启后非本地端点必须使用 HTTPS。</div>
            </div>
            <el-switch v-model="form.requireHttps" />
          </div>
        </div>

        <el-form-item label="退出跳转地址">
          <el-input v-model.trim="form.logoutRedirectUrl" placeholder="可选" />
        </el-form-item>
      </el-form>

      <div class="actions">
        <el-button @click="validate">校验配置</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.sso-setting {
  min-height: 100%;
}
.router-title {
  margin: 0 0 16px;
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
}
.setting-panel {
  max-width: 1080px;
  padding: 24px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
}
.setting-title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}
.setting-desc,
.form-tip,
.switch-desc {
  color: #64748b;
  font-size: 13px;
}
.setting-desc {
  margin-top: 8px;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 20px;
}
.callback-row {
  display: flex;
  width: 100%;
  gap: 12px;
}
.full-width {
  width: 100%;
}
.sub-title {
  margin: 10px 0 18px;
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}
.switch-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin: 8px 0 22px;
}
.switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 82px;
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}
.switch-title {
  margin-bottom: 6px;
  color: #0f172a;
  font-weight: 700;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 6px;
}
@media (max-width: 960px) {
  .form-grid,
  .switch-grid {
    grid-template-columns: 1fr;
  }
  .callback-row {
    flex-direction: column;
  }
}
</style>
