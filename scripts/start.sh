#!/usr/bin/env bash

APP_CONFIG="/etc/logdist/appConfig.yml"
LOG_CONFIG="/etc/logdist/logback.xml"
KEYSTORE_PATH="/etc/logdist/onetimekeystore.jks"
LOG_FILE="/var/log/logdist/logdist.log"
JAR_PATH="/opt/logdist/webServer/build/libs/webServer-1.0-SNAPSHOT.jar"

# ***********************************************
# ***********************************************

ARGS=" -Dconfig.location=${APP_CONFIG} -Dlogging.logfile=${LOG_FILE} -Dlogging.config=${LOG_CONFIG} -Dkeystore.file=${KEYSTORE_PATH}"

exec java $ARGS -jar "${JAR_PATH}" $*

