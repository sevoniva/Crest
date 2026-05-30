#!/bin/bash

# 冒烟测试脚本 - 验证安全修复
# 用法: ./smoke-test-security-fixes.sh [BASE_URL]
# 默认: http://localhost:8104

BASE_URL="${1:-http://localhost:8104}"
TOKEN=""
PASS=0
FAIL=0

echo "=========================================="
echo "安全修复冒烟测试"
echo "目标: $BASE_URL"
echo "=========================================="

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 获取 Token（需要先登录）
get_token() {
    echo -e "\n${YELLOW}[准备] 获取认证 Token...${NC}"
    # 这里需要根据实际情况获取 token
    # TOKEN=$(curl -s -X POST "$BASE_URL/de2api/login/localLogin" ...)
    echo "请手动设置 TOKEN 变量"
}

# 测试函数
test_case() {
    local name="$1"
    local expected="$2"
    local actual="$3"

    if [ "$actual" = "$expected" ]; then
        echo -e "${GREEN}[PASS]${NC} $name"
        PASS=$((PASS + 1))
    else
        echo -e "${RED}[FAIL]${NC} $name"
        echo "  期望: $expected"
        echo "  实际: $actual"
        FAIL=$((FAIL + 1))
    fi
}

# ==========================================
# 测试 1: 敏感端点应需要认证
# ==========================================
echo -e "\n${YELLOW}[测试 1] 敏感端点应需要认证${NC}"

# 测试 /dekey
resp=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/de2api/dekey" 2>/dev/null)
test_case "GET /de2api/dekey 应返回 401" "401" "$resp"

# 测试 /symmetricKey
resp=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/de2api/symmetricKey" 2>/dev/null)
test_case "GET /de2api/symmetricKey 应返回 401" "401" "$resp"

# ==========================================
# 测试 2: SSRF 防护
# ==========================================
echo -e "\n${YELLOW}[测试 2] SSRF 防护${NC}"

if [ -n "$TOKEN" ]; then
    # 测试内部地址
    resp=$(curl -s -X POST "$BASE_URL/de2api/datasource/loadRemoteFile" \
        -H "X-DE-TOKEN: $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"url":"http://127.0.0.1/","type":"json"}' 2>/dev/null | grep -o '"code":[0-9]*' | cut -d: -f2)
    test_case "SSRF 应阻止 127.0.0.1" "40001" "$resp"

    # 测试元数据地址
    resp=$(curl -s -X POST "$BASE_URL/de2api/datasource/loadRemoteFile" \
        -H "X-DE-TOKEN: $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"url":"http://169.254.169.254/","type":"json"}' 2>/dev/null | grep -o '"code":[0-9]*' | cut -d: -f2)
    test_case "SSRF 应阻止 169.254.169.254" "40001" "$resp"
else
    echo -e "${YELLOW}[跳过] 需要 Token${NC}"
fi

# ==========================================
# 测试 3: 安全头
# ==========================================
echo -e "\n${YELLOW}[测试 3] HTTP 安全头${NC}"

headers=$(curl -s -I "$BASE_URL/" 2>/dev/null)

# X-Frame-Options
if echo "$headers" | grep -q "X-Frame-Options: DENY"; then
    test_case "X-Frame-Options 头存在" "true" "true"
else
    test_case "X-Frame-Options 头存在" "true" "false"
fi

# X-Content-Type-Options
if echo "$headers" | grep -q "X-Content-Type-Options: nosniff"; then
    test_case "X-Content-Type-Options 头存在" "true" "true"
else
    test_case "X-Content-Type-Options 头存在" "true" "false"
fi

# X-XSS-Protection
if echo "$headers" | grep -q "X-XSS-Protection"; then
    test_case "X-XSS-Protection 头存在" "true" "true"
else
    test_case "X-XSS-Protection 头存在" "true" "false"
fi

# Content-Security-Policy
if echo "$headers" | grep -q "Content-Security-Policy"; then
    test_case "Content-Security-Policy 头存在" "true" "true"
else
    test_case "Content-Security-Policy 头存在" "true" "false"
fi

# Strict-Transport-Security
if echo "$headers" | grep -q "Strict-Transport-Security"; then
    test_case "Strict-Transport-Security 头存在" "true" "true"
else
    test_case "Strict-Transport-Security 头存在" "true" "false"
fi

# Cache-Control
if echo "$headers" | grep -q "Cache-Control: no-store"; then
    test_case "Cache-Control 头存在" "true" "true"
else
    test_case "Cache-Control 头存在" "true" "false"
fi

# ==========================================
# 测试 4: 登录端点仍可用
# ==========================================
echo -e "\n${YELLOW}[测试 4] 登录端点可用性${NC}"

resp=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/de2api/login/localLogin" 2>/dev/null)
test_case "POST /de2api/login/localLogin 应可访问" "405" "$resp"

# ==========================================
# 测试结果汇总
# ==========================================
echo -e "\n=========================================="
echo "测试结果汇总"
echo "=========================================="
echo -e "通过: ${GREEN}$PASS${NC}"
echo -e "失败: ${RED}$FAIL${NC}"
echo "总计: $((PASS + FAIL))"

if [ $FAIL -eq 0 ]; then
    echo -e "\n${GREEN}所有测试通过！${NC}"
    exit 0
else
    echo -e "\n${RED}有 $FAIL 个测试失败${NC}"
    exit 1
fi
