ARG CREST_JDK_IMAGE=eclipse-temurin:21-jdk-alpine
ARG CREST_BASE_IMAGE=alpine:3.22

FROM ${CREST_JDK_IMAGE} AS java-runtime
RUN "$JAVA_HOME/bin/jlink" \
    --add-modules java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.transaction.xa,java.xml,java.xml.crypto,jdk.charsets,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.localedata,jdk.management,jdk.naming.dns,jdk.unsupported,jdk.zipfs \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --compress=zip-6 \
    --output /opt/java/crest-runtime

FROM ${CREST_BASE_IMAGE}
STOPSIGNAL SIGTERM
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=java-runtime /opt/java/crest-runtime /opt/java/openjdk
RUN addgroup -S -g 10001 crest \
    && adduser -S -u 10001 -G crest -h /opt/apps crest \
    && mkdir -p /opt/apps/config \
    /opt/crest/drivers/ \
    /opt/crest/cache/ \
    /opt/crest/logs/crest/ \
    /opt/crest/tmp/ \
    /opt/crest/data/map \
    /opt/crest/data/static-resource/ \
    /opt/crest/data/appearance/ \
    /opt/crest/data/exportData/ \
    /opt/crest/data/excel/ \
    /opt/crest/data/font/ \
    /opt/crest/data/i18n/ \
    /opt/crest/data/plugin/ \
    && chown -R 10001:10001 /opt/apps /opt/crest

COPY --chown=10001:10001 drivers/ /opt/crest/drivers/
COPY --chown=10001:10001 staticResource/ /opt/crest/data/static-resource/

WORKDIR /opt/apps

COPY --chown=10001:10001 core/core-backend/target/CoreApplication.jar /opt/apps/app.jar

ENV JAVA_APP_JAR=/opt/apps/app.jar
ENV RUNNING_PORT=8100
ENV JAVA_OPTIONS="-Dfile.encoding=utf-8 -Dloader.path=/opt/apps -Dspring.config.additional-location=/opt/apps/config/"

HEALTHCHECK --interval=15s --timeout=5s --retries=20 --start-period=30s CMD nc -z 127.0.0.1 $RUNNING_PORT

USER crest

CMD ["sh", "-c", "exec java $JAVA_OPTIONS -jar $JAVA_APP_JAR"]
