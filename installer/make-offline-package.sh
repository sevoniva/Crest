#!/bin/bash
set -euo pipefail

INSTALLER_DIR=$(
  cd "$(dirname "$0")"
  pwd
)
REPO_DIR=$(cd "${INSTALLER_DIR}/.." && pwd)

VERSION=${1:?"Usage: $0 <version> <linux-amd64|linux-arm64> [output_dir]"}
TARGET_PLATFORM=${2:?"Usage: $0 <version> <linux-amd64|linux-arm64> [output_dir]"}
OUTPUT_DIR=${3:-"${REPO_DIR}/offline-packages"}
MYSQL_IMAGE=${MYSQL_IMAGE:-"mysql:8.4.5"}

case "${TARGET_PLATFORM}" in
  amd64 | x86_64 | linux-amd64)
    TARGET_PLATFORM="linux-amd64"
    TARGET_ARCH="amd64"
    ;;
  arm64 | aarch64 | linux-arm64)
    TARGET_PLATFORM="linux-arm64"
    TARGET_ARCH="arm64"
    ;;
  *)
    echo "Unsupported target platform: ${TARGET_PLATFORM}" >&2
    echo "Use linux-amd64 or linux-arm64." >&2
    exit 1
    ;;
esac

APP_IMAGE=${APP_IMAGE:-"ghcr.io/sevoniva/crest:${VERSION}-${TARGET_ARCH}"}
SOURCE_APP_IMAGE=${SOURCE_APP_IMAGE:-"${APP_IMAGE}"}
PACKAGE_NAME="crest-offline-${VERSION}-${TARGET_PLATFORM}"
PACKAGE_DIR="${OUTPUT_DIR}/${PACKAGE_NAME}"

log() {
  echo "[offline-package] $*"
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

safe_image_file_name() {
  echo "$1" | sed 's#/#_#g; s#:#_#g'
}

image_arch() {
  docker image inspect --format '{{.Architecture}}' "$1" 2>/dev/null | head -n 1
}

require_cmd docker
require_cmd tar

if ! docker image inspect "${SOURCE_APP_IMAGE}" >/dev/null 2>&1; then
  echo "Source app image not found: ${SOURCE_APP_IMAGE}" >&2
  echo "Build or tag the app image first, or set SOURCE_APP_IMAGE." >&2
  exit 1
fi

if ! docker image inspect "${MYSQL_IMAGE}" >/dev/null 2>&1; then
  echo "MySQL image not found locally: ${MYSQL_IMAGE}" >&2
  echo "Pull or load it first, or set MYSQL_IMAGE." >&2
  exit 1
fi

if [ "$(image_arch "${SOURCE_APP_IMAGE}")" != "${TARGET_ARCH}" ]; then
  echo "Source app image architecture mismatch: ${SOURCE_APP_IMAGE} is $(image_arch "${SOURCE_APP_IMAGE}"), expected ${TARGET_ARCH}." >&2
  exit 1
fi

if [ "$(image_arch "${MYSQL_IMAGE}")" != "${TARGET_ARCH}" ]; then
  echo "MySQL image architecture mismatch: ${MYSQL_IMAGE} is $(image_arch "${MYSQL_IMAGE}"), expected ${TARGET_ARCH}." >&2
  exit 1
fi

rm -rf "${PACKAGE_DIR}"
mkdir -p "${PACKAGE_DIR}/images"

log "Copy installer files"
cp "${INSTALLER_DIR}/LICENSE" "${PACKAGE_DIR}/"
cp "${INSTALLER_DIR}/README.md" "${PACKAGE_DIR}/"
cp "${INSTALLER_DIR}/install.conf" "${PACKAGE_DIR}/"
cp "${INSTALLER_DIR}/install.sh" "${PACKAGE_DIR}/"
cp "${INSTALLER_DIR}/uninstall.sh" "${PACKAGE_DIR}/"
cp "${INSTALLER_DIR}/crestctl" "${PACKAGE_DIR}/"
cp -R "${INSTALLER_DIR}/crest" "${PACKAGE_DIR}/crest"
mkdir -p "${PACKAGE_DIR}/images"

log "Pin install.conf image names"
sed -i.bak \
  -e "s#^DE_APP_IMAGE=.*#DE_APP_IMAGE=${APP_IMAGE}#g" \
  -e "s#^DE_MYSQL_IMAGE=.*#DE_MYSQL_IMAGE=${MYSQL_IMAGE}#g" \
  "${PACKAGE_DIR}/install.conf"
rm -f "${PACKAGE_DIR}/install.conf.bak"

log "Tag app image ${SOURCE_APP_IMAGE} -> ${APP_IMAGE}"
docker tag "${SOURCE_APP_IMAGE}" "${APP_IMAGE}"

APP_ARCHIVE="${PACKAGE_DIR}/images/$(safe_image_file_name "${APP_IMAGE}").tar.gz"
MYSQL_ARCHIVE="${PACKAGE_DIR}/images/$(safe_image_file_name "${MYSQL_IMAGE}").tar.gz"

log "Save ${APP_IMAGE}"
docker save "${APP_IMAGE}" | gzip -c > "${APP_ARCHIVE}"

log "Save ${MYSQL_IMAGE}"
docker save "${MYSQL_IMAGE}" | gzip -c > "${MYSQL_ARCHIVE}"

cat > "${PACKAGE_DIR}/OFFLINE-README.md" <<EOF
# Crest 离线部署包 ${VERSION} (${TARGET_PLATFORM})

本目录包含 Crest 应用镜像、MySQL 镜像、Docker Compose 配置和安装脚本，可在无公网网络的服务器上直接安装。

## 安装

\`\`\`bash
tar -zxf ${PACKAGE_NAME}.tar.gz
cd ${PACKAGE_NAME}
bash install.sh
\`\`\`

安装前可按需调整 \`install.conf\`：

- \`DE_PORT\`：Web 访问端口，默认 8100
- \`DE_RUN_BASE\`：数据、日志和配置持久化目录
- \`DE_APP_IMAGE\`：Crest 应用镜像，已固定为本包内镜像
- \`DE_MYSQL_IMAGE\`：MySQL 镜像，已固定为本包内镜像
- \`DE_INITIAL_PASSWORD\`：管理员初始密码；留空时安装脚本会随机生成并在安装完成后输出

## 镜像

- 应用镜像：\`${APP_IMAGE}\`
- MySQL 镜像：\`${MYSQL_IMAGE}\`
- 目标架构：\`${TARGET_PLATFORM}\`

安装脚本会自动执行 \`docker load\` 加载 \`images/\` 下的镜像文件。

## 默认账号

- 用户名：\`admin\`
- 密码：安装完成后终端输出的初始密码

请在首次登录后修改默认密码。
EOF

log "Create archive"
tar -zcf "${OUTPUT_DIR}/${PACKAGE_NAME}.tar.gz" -C "${OUTPUT_DIR}" "${PACKAGE_NAME}"

log "Package ready: ${OUTPUT_DIR}/${PACKAGE_NAME}.tar.gz"
