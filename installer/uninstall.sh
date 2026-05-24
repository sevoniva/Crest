#!/bin/bash

DE_BASE=/opt

echo "如需备份 Crest 数据，请执行 crestctl backup 进行备份。如您的控制脚本不支持 backup 命令，请升级版本。"
read -r -p "即将卸载 Crest 服务，包括删除运行目录、数据及相关镜像，是否继续? [Y/n] " input

case $input in
   [yY][eE][sS]|[yY])
      echo "Yes"
      ;;
   [nN][oO]|[nN])
      echo "No"
      exit 1
      ;;
   *)
      echo "无效输入..."
      exit 1
      ;;
esac

echo "停止 Crest 服务"
service crest stop >/dev/null 2>&1

echo "移除 Crest 服务"
if which chkconfig >/dev/null 2>&1;then
   chkconfig crest >/dev/null
   if [ $? -eq 0 ]; then
      chkconfig --del crest >/dev/null 2>&1
   fi
fi

if [ -f /etc/systemd/system/crest.service ];then
   systemctl disable crest >/dev/null 2>&1
   rm -f /etc/systemd/system/crest.service
   systemctl daemon-reload
fi
if [[ -f /etc/init.d/crest ]];then
   rm -f /etc/init.d/crest
fi

if [ -f /usr/bin/crestctl ]; then
   # 获取已安装的 Crest 的运行目录
   DE_BASE=$(grep "^DE_BASE=" /usr/bin/crestctl | cut -d'=' -f2)
fi

# 清理 Crest 相关镜像
if test ! -z "$(docker images -f dangling=true -q)"; then
   echo "清理虚悬镜像"
   docker rmi $(docker images -f dangling=true -q)
fi

if test -n "$(docker images | grep -E 'ghcr.io/sevoniva/crest')"; then
   echo "清理 Crest 镜像"
   docker rmi $(docker images | grep -E "ghcr.io/sevoniva/crest" | awk -F' ' '{print $1":"$2}')
fi

# 清理 Crest 运行目录及命令行工具
rm -rf ${DE_BASE}/crest /usr/bin/crestctl /usr/local/bin/crestctl

echo "Crest 服务卸载完成"
