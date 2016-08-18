#!/usr/bin/env bash
wget --no-cookies --header "Cookie: gpw_e24=xxx; oraclelicense=accept-securebackup-cookie;" \
    "http://download.oracle.com/otn-pub/java/jdk/8u101-b13/jdk-8u101-linux-x64.rpm" && \
sudo yum localinstall jdk-8u101-linux-x64.rpm && \
sudo /usr/sbin/alternatives --config java
