# Build the Flink job

```bash
git clone git@github.com:theodorecurtil/deribit-OHLCV.git

cd deribit-OHLCV

./build-job.sh
```

Will build the Flink job in `./target/OHLCV-1.0-SNAPSHOT-jar-with-dependencies.jar`.

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