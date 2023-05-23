<br>

[![Button Acosom]][LinkAcosom] [![Button Twitter]][LinkTwitter] [![Button Linkedin]][LinkLinkedin]

<br>

This repo contains the code to build a Flink job that computes real-time OHLCV from a Kafka topic containing flowing Deribit trades. In this README you will find all instructions to

1. Build the Flink job
2. Launch the infrastructure to run the actual Flink job

# Build the Flink Job

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

# Run the Flink Job

The easiest way to have this Flink job running is to follow the following process.

## Launch the local Infrastructure

To run the Flink job, you need:

1. You have running Kafka/Flink clusters with Confluent Schema registry.
2. Kafka is accessible locally on `kafka_broker:29092`
3. Schema registry is accessible locally on `http://schema-registry:8081`

To have that, type the following commands

```bash
git clone git@github.com:theodorecurtil/flink_sql_job.git

cd flink_sql_job

docker-compose up -d
```

Then wait about 1 minute that the whole infrastructure starts locally. You should be able to access:

1. Flink UI on [localhost:18081](http://localhost:18081/#/overview)
2. Kafka UI on [localhost:9021](http://localhost:9021/clusters)

## Run the Deribit Producer

Next step is to get some data flowing into Kafka. In this example, we will get live trade data from [Deribit](https://www.deribit.com/), a crypto derivatives exchange for options, futures and perpetuals.

To get this data to flow into our local Kafka, run the following command

```bash
docker pull theodorecurtil/deribit-trade-connector:latest

docker run --rm --network host --name deribit-trades theodorecurtil/deribit-trade-connector:latest
```

You should see some data flowing in the `TRADES` topic almost immediately.

## Start the Flink Job

The Flink job that we created is a simple OHLCV job, performing real-time computations of OHLCV over a 1-minute window (you can tweak that in the source code to be whatever time duration you want).

To run the job on the local Flink cluster, follow the following steps.

Copy the Flink job to the `jobmanager` container.

```bash
docker cp ./target/OHLCV-1.0-SNAPSHOT-jar-with-dependencies.jar jobmanager:/opt/flink
```

Start the Flink job

```bash
docker exec -it jobmanager bash

flink@jobmanager~$ flink run OHLCV-1.0-SNAPSHOT-jar-with-dependencies.jar
```

:tada: After about 1 minute (remember, it is a 1-minute time aggregation) you should start seeing some OHLCV data flowing into the Kafka cluster inside the `OHLCV` topic. Congrats!

:warning: If it is not the case, please reach out to me via email at [theodore.curtil@icloud.com](mailto:theodore.curtil@icloud.com) or [theodore.curtil@acosom.com](mailto:theodore.curtil@acosom.com). I will help you.

<!---------------------------------------------------------------------------->

[Button Acosom]: https://img.shields.io/badge/Acosom-Read%20blog%20post-orange
[Button Email]: https://img.shields.io/badge/-theodore.curtil@icloud.com-grey?style=social&logo=gmail
[Button Linkedin]: https://img.shields.io/badge/LinkedIn-Follow%20Acosom-blue

[LinkAcosom]: https://acosom.com/en/?utm_source=github&utm_medium=social&utm_campaign=ohlcv-repo 'Contact us!'
[LinkEmail]: mailto:theodore.curtil@icloud.com 'Send me an email'
[LinkLinkedin]: https://ch.linkedin.com/company/acosom 'Follow us on LinkedIn :)'