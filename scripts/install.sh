#!/usr/bin/env bash
sudo git clone https://github.com/EnigmaBridge/logDistributor.git /opt/logdist && \
sudo mkdir -p /etc/logdist && \
sudo cp /opt/logdist/webServer/config/appConfig.yml.example /etc/logdist && \
sudo cp /opt/logdist/webServer/config/logback.xml.example /etc/logdist && \
sudo mkdir -p /var/log/logdist && \
ln -s /opt/logdist/scripts/logdist /etc/init.d/logdist && \
echo "OK"



