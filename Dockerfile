FROM eclipse-temurin:21-jre-alpine
STOPSIGNAL SIGTERM
RUN apk add --no-cache netcat-openbsd \
    && addgroup -S crest \
    && adduser -S -G crest -h /opt/apps crest \
    && mkdir -p /opt/apps/config \
    /opt/crest/drivers/ \
    /opt/crest/cache/ \
    /opt/crest/data/map \
    /opt/crest/data/static-resource/ \
    /opt/crest/data/appearance/ \
    /opt/crest/data/exportData/ \
    /opt/crest/data/excel/ \
    /opt/crest/data/i8n/ \
    /opt/crest/data/plugin/ \
    && chown -R crest:crest /opt/apps /opt/crest

ADD drivers/* /opt/crest/drivers/
ADD staticResource/ /opt/crest/data/static-resource/

WORKDIR /opt/apps

ADD core/core-backend/target/CoreApplication.jar /opt/apps/app.jar
RUN chown crest:crest /opt/apps/app.jar

ENV JAVA_APP_JAR=/opt/apps/app.jar
ENV RUNNING_PORT=8100
ENV JAVA_OPTIONS="-Dfile.encoding=utf-8 -Dloader.path=/opt/apps -Dspring.config.additional-location=/opt/apps/config/"

HEALTHCHECK --interval=15s --timeout=5s --retries=20 --start-period=30s CMD nc -zv 127.0.0.1 $RUNNING_PORT

USER crest

CMD ["sh", "-c", "exec java $JAVA_OPTIONS -jar $JAVA_APP_JAR"]
