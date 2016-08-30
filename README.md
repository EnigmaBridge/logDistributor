# Log distributor

Listens on the TCP port for Logstash output and redistributes log lines to multiple customer endpoints
based on current configuration.

Logdistributor currently supports forwarding to Splunk. 

In our logging scheme, each log producing host have a single Domain. Domain defines UserObject ID space. 
Logdistributor builds the following routing table in-memory: `domain -> UOID -> client`

Client defines how log line should be treated, e.g. contains Splunk forwarding configuration.

## Configuring Logstash
If Logstash and Logdistributor run on the same machine, the following configuration can be
used for Logstash to forward logs in form of new-line separated JSON to TCP socket 
where logdistributor is listening:

```
output {
  tcp{
   host => "localhost"
   port => 8998
   codec => "json_lines"
  }
}
```

## Compilation on MAC with Java8

Place `gradle.properties` to the root of the project:
```
org.gradle.java.home=/Library/Java/JavaVirtualMachines/jdk1.8.0_20.jdk/Contents/Home/
```

## Running the Logdistributor

### Configuration
Distributor needs a config file: `webServer/config/appConfig.yml`.
There is an example file which can be used for this purpose.

### Database
Logdistributor uses MySQL database to store the configuration.
Connection parameters are set in `appConfig.yml`. To initialize the database `scripts/initdb.sql`
can be used.

## Local development
If you want to develop/debug Logdistributor locally, while having MySQL database on the 
remote server SSH port forwarding can be used to forward traffic between your host and remote server.

```
ssh -R 8998:localhost:8998 -L 3306:localhost:3306 eblog-server
```

With this your host listens on port 3306 - enables to use MySQL locally.
`logdistributor -> your-host:3306 -> ssh -> server:3306 -> mysqld`
 
And makes remote server to listen on port 8998 and forward it to your host.
`logstash -> server:8998 -> ssh -> your-host:8998 -> logdistributor`

## REST interface
Logdistributor provides a REST interface for configuration / management.
Most REST calls require Authentication token to be present in the request. 

### Dump current database
Dumps the whole database in JSON format.
```
curl  --insecure -H 'X-Auth-Token: a' 'https://localhost:8444/api/v1/client/list'
```

### Dump current statistics 
Dumps current statistics since server started (e.g., number of lines received, forwarded, number of fails, TCP connections)
```
curl  --insecure -H 'X-Auth-Token: a' 'https://localhost:8444/api/v1/stats'
```

### Reload settings
Causes reloading of the routing table from the database backend. Lazy parameter defines
whether to reload also components not affected by the change. E.g., splunk connectors are
destroyed and built again.
```
curl  --insecure -H 'X-Auth-Token: a' 'https://localhost:8444/api/v1/reload?lazy=false'
```