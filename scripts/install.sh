#!/usr/bin/env bash

# Settings
PRODUCT=logdist

# Installation
sudo git clone https://github.com/EnigmaBridge/logDistributor.git "/opt/${PRODUCT}" && \
    sudo mkdir -p "/var/log/${PRODUCT}" && \
    sudo mkdir -p "/etc/${PRODUCT}" && \
    sudo cp "/opt/${PRODUCT}/webServer/config/appConfig.yml.example" "/etc/${PRODUCT}" && \
    sudo cp "/opt/${PRODUCT}/webServer/config/logback.xml.example" "/etc/${PRODUCT}" && \
    sudo ln -s "/opt/${PRODUCT}/scripts/${PRODUCT}" "/etc/init.d/${PRODUCT}" && \
    cd "/etc/${PRODUCT}" && \
    sudo bash "/opt/${PRODUCT}/createKeystore.sh" && \
    cd "/opt/${PRODUCT}" && \
    bash buildBoot.sh && \
    echo "OK"



