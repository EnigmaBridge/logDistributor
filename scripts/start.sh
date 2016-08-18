#!/usr/bin/env bash

PRODUCT="logdist"
APP_CONFIG="/etc/${PRODUCT}/appConfig.yml"
LOG_CONFIG="/etc/${PRODUCT}/logback.xml"
KEYSTORE_PATH="/etc/${PRODUCT}/onetimekeystore.jks"
LOG_FILE="/var/log/${PRODUCT}/${PRODUCT}.log"
JAR_PATH="/opt/logdist/webServer/build/libs/webServer-1.0-SNAPSHOT.jar"

# ***********************************************
# ***********************************************

ARGS=" -Dconfig.location=${APP_CONFIG} -Dlogging.logfile=${LOG_FILE} -Dlogging.config=${LOG_CONFIG} -Dkeystore.file=${KEYSTORE_PATH}"

exec java $ARGS -jar "${JAR_PATH}" $*

