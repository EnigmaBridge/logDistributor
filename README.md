## Log distributor

Listens on TCP port for logstash output and redistributes loglines to multiple customer endpoints.

## Compilation on MAC with Java8

Place `gradle.properties` to the root of the project:
```
org.gradle.java.home=/Library/Java/JavaVirtualMachines/jdk1.8.0_20.jdk/Contents/Home/
```

## Running the server

Distributor needs a config file: `webServer/config/appConfig.yml`.
There is an example file which can be used for this purpose.

