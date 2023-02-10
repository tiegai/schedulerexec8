#!/bin/bash

set -e

java_opt() {
  export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS} ${1}"
}

java_default() {
  echo "$@"
  java -XX:+PrintFlagsFinal -version | grep -E "MaxRAM|MB|MaxHeap"
}

log_date() {
  date -u +"%Y-%m-%d %H:%M:%S,%s" | cut -c1-23
}

log_sys() {
  echo "$(log_date) [p=sys]: $@"
}

log_boot() {
  echo "$(log_date) [p=boot, ecsTaskId=${ONENCP_ECS_TASK_ID}, app=${ONENCP_APP_NAME}, ver=${ONENCP_APP_VER}, gitcommit=${ONENCP_COMMIT_ID}] $@"
}

get_ecstask_id() {
  echo $ECS_CONTAINER_METADATA_URI_V4 | sed 's/.*\///g' | cut -c1-32
}

get_ecstask_short_id() {
  echo $ECS_CONTAINER_METADATA_URI_V4 | sed 's/.*\///g' | cut -c1-16
}


get_commitid() {
  cat ${ONENCP_APP_VER_FILE} | head -1 | cut -c8-49
}

get_commit_short_id(){
  cat ${ONENCP_APP_VER_FILE} | head -1 | cut -c8-15

}

get_versionid(){
  cat ${ONENCP_APP_META_FILE} | grep -e "^version" | sed 's/version=//'
}


setup_java_opt() {
  java_opt "-XX:+IgnoreUnrecognizedVMOptions"
  java_opt "-Djava.security.egd=file:/dev/./urandom"
  java_opt "-Dsfx_org_name=Commerce"
  java_opt "-Donencp_ecs_pid=${ONENCP_ECS_TASK_ID}"
  java_opt "-Dspring.profiles.active=${ONENCP_APP_ENV}"
  java_opt "-server"
}


log_sys " initial system enviroment....."
export ONENCP_ECS_TASK_ID=$(get_ecstask_id)
export ONENCP_COMMIT_ID=$(get_commitid)
export ONENCP_APP_ENV="${env}"
export ONENCP_APP_NAME="${app}"
export ONENCP_APP_VER=$(get_versionid)
export ONENCP_COMMIT_SHORT_ID=$(get_commit_short_id)

log_boot " initial java enviroment....."
setup_java_opt
java_default "## java default flags config ##"

if [ ! -z "$JAVA_TIMEZONE" ]; then
    java_opt "-Duser.timezone=${JAVA_TIMEZONE}"
fi

exec /bin/sh -c "exec java -jar ${ONENCP_APP_MAIN_JAR}"