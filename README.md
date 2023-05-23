# Build the Flink job

Building the Flink job (which is simply building a `.jar` file) requires having the basic Java/Maven tooling installed on your machine.

The project was developed with the following version

```bash
$> java --version
openjdk 11.0.19 2023-04-18
OpenJDK Runtime Environment (build 11.0.19+7)
OpenJDK 64-Bit Server VM (build 11.0.19+7, mixed mode)

$> javac --version
javac 11.0.19

$> mvn --version
Apache Maven 3.8.7 (b89d5959fcde851dcb1c8946a785a163f14e1e29)
Maven home: /opt/maven
Java version: 11.0.19, vendor: Oracle Corporation, runtime: /usr/lib/jvm/java-11-openjdk
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "6.3.2-arch1-1", arch: "amd64", family: "unix"
```

To build the Flink job, run the following commands

```bash
git clone git@github.com:theodorecurtil/deribit-OHLCV.git

cd deribit-OHLCV

./build-job.sh
```

Will build the Flink job in `./target/OHLCV-1.0-SNAPSHOT-jar-with-dependencies.jar`.

> :warning: In case the build fails for reasons XYZ, I included anyway the `.jar` file in the `./target` folder.

Assumptions:

1. You have running Kafka/Flink clusters with Confluent Schema registry.
2. Kafka is accessible locally on `kafka_broker:29092`
3. Schema registry is accessible locally on `http://schema-registry:8081`

Copy the Flink job to the `jobmanager` container.

```bash
docker cp ./target/OHLCV-1.0-SNAPSHOT-jar-with-dependencies.jar jobmanager:/opt/flink
```

Start the Flink job

```bash
docker exec -it jobmanager bash
flink@jobmanager~$ flink run OHLCV-1.0-SNAPSHOT-jar-with-dependencies.jar
```