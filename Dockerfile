FROM artifactory.nike.com:9002/cafi/nike-debian-slim-java11-jre-foundation:latest


ENV ONENCP_HOME /opt/app
WORKDIR ${ONENCP_HOME}

ENV ONENCP_APP_MAIN_JAR ${ONENCP_HOME_BIN}/app.jar
ENV ONENCP_APP_VER_FILE ${ONENCP_HOME_BIN}/conf/git.log
ENV ONENCP_APP_META_FILE ${ONENCP_HOME_BIN}/conf/meta.properties

ENV JAVA_TIMEZONE Asia/Shanghai
# ENV JAVA_TOOL_OPTIONS ""

ADD ./docker-entrypoint.sh /docker-entrypoint.sh
ADD build/libs/*.jar ${ONENCP_APP_MAIN_JAR}
ADD ./build/git.log ${ONENCP_APP_VER_FILE}
ADD ./gradle.properties ${ONENCP_APP_META_FILE}


EXPOSE 8080 9080

ENTRYPOINT ["/docker-entrypoint.sh"]