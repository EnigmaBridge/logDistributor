#!/usr/bin/env bash

APP_CONFIG="/etc/logdist/appConfig.yml"
LOG_FILE="/var/log/logdist/logdist.log"
LOG_CONFIG="/etc/logdist/logback.xml"
JAR_PATH="/opt/logdist/webServer/build/libs/webServer-1.0-SNAPSHOT.jar"
KEYSTORE_PATH="/etc/logdist/onetimekeystore.jks"

# ***********************************************
# ***********************************************

ARGS=" -Dconfig.location=${APP_CONFIG} -Dlogging.logfile=${LOG_FILE} -Dlogback.configurationFile=${LOG_CONFIG} -Dkeystore.file=${KEYSTORE_PATH}"

exec java $ARGS -jar "${JAR_PATH}" $*

