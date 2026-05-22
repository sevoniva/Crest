#!/bin/bash
# DataEase 简化启动脚本
# 用法: ./run.sh [start|stop|status]

APP_NAME="dataease"
JAR_FILE="core/core-backend/target/CoreApplication.jar"
LOG_DIR="logs"
PID_FILE="dataease.pid"
JAVA_OPTS="-Dfile.encoding=utf-8 -Xms1g -Xmx2g"

start() {
    if [ -f "$PID_FILE" ] && kill -0 $(cat "$PID_FILE") 2>/dev/null; then
        echo "$APP_NAME 已经在运行 (PID: $(cat $PID_FILE))"
        exit 0
    fi

    if [ ! -f "$JAR_FILE" ]; then
        echo "错误: 找不到 $JAR_FILE"
        echo "请先执行: mvn clean install"
        exit 1
    fi

    mkdir -p "$LOG_DIR"
    nohup java $JAVA_OPTS -jar "$JAR_FILE" > "$LOG_DIR/stdout.log" 2>&1 &
    echo $! > "$PID_FILE"
    echo "$APP_NAME 已启动 (PID: $(cat $PID_FILE))"
    echo "日志: tail -f $LOG_DIR/stdout.log"
}

stop() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            kill "$PID"
            echo "$APP_NAME 已停止 (PID: $PID)"
        else
            echo "进程 $PID 不存在"
        fi
        rm -f "$PID_FILE"
    else
        echo "$APP_NAME 未运行"
    fi
}

status() {
    if [ -f "$PID_FILE" ] && kill -0 $(cat "$PID_FILE") 2>/dev/null; then
        echo "$APP_NAME 运行中 (PID: $(cat $PID_FILE))"
    else
        echo "$APP_NAME 未运行"
    fi
}

case "${1:-start}" in
    start) start ;;
    stop) stop ;;
    restart) stop; sleep 2; start ;;
    status) status ;;
    *) echo "用法: $0 [start|stop|restart|status]" ;;
esac
