#!/usr/bin/env bash
sudo git clone https://github.com/EnigmaBridge/logDistributor.git /opt/logdist && \
sudo mkdir /etc/logdist && \
sudo cp /opt/logdist/webServer/config/appConfig.yml.example /etc/logdist && \
sudo mkdir /var/log/logdist && \
ln -s /opt/logdist/scripts/logdist /etc/init.d/logdist && \
echo "OK"



