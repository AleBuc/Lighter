Lighter
=======

**Lighter** is a tool to consult Kafka events into an embedded database.

Prerequisites 
-------------
To run the application, you need to possess on your device JRE or JDK 21. You can find it on [Adoptium website](https://adoptium.net/fr/temurin/releases).

Configuration
-------------
Add a file `config.yml` in the same folder as the .jar file.

Fill it with connection settings as following: 
```yaml
kafka:
  group-id: lighter
  server:
    address: http://localhost:29092
    username: 
    password: 
  schema-registry:
    address: http://localhost:8085
    username: 
    password: 
```

Don't provide schema registry configuration if you don't have one.

Run the program
---------------


Use the following command: `java -jar lighter.jar`

A Spring shell will start. The connection string to access to the embedded MongoDB database will be displayed.

Use this connection string in a software like MongoDB Compass or Robo3T to access to the embedded cluster.

You will find in it a database called "Lighter". Collections will be added in it with the topic names when you will set up there consumption.

Commands
--------

### `consume {topicName} [-k] [-v]`

Use `consume` command with the topic name to begin the consumption of events.
By default, the deserializer is set for `string` type. 
To modify the key or value type, add `-k {type}` or `-v {type}` and replace `{type}` by `integer` or `avro`.
The avro type will only work if the schema registry configuration is filled.

### `stop [topicName]`

Use `stop` command to end a topic consumption. If you do this due to a type configuration mistake, remove the collection with the topic name before to re-run it with the new configuration.

If no given topic name, all consumers will stop.

### `exit` or `quit`
When you have finished with your monitoring, use one of these commands to close the application.
If you force to stop, by using `Ctrl+C` or for closing your terminal, the embedded database and the consumer will continue to run during some minutes before to stop due to missing connection.



