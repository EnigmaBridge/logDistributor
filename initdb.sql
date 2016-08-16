CREATE DATABASE logdist;
CREATE USER 'logdist'@'localhost' IDENTIFIED BY 'logdist';
GRANT ALL PRIVILEGES ON logdist . * TO 'logdist'@'localhost';
FLUSH PRIVILEGES;
