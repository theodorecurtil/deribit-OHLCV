package curtil.theodore;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;

// import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.connector.kafka.source.*;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.source.enumerator.initializer.*;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
// import org.apache.flink.core.fs.FileSystem.WriteMode;

import java.time.Duration;
// import java.time.Instant;
// import java.util.Date;

// import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroSerializationSchema;

// import org.apache.flink.api.common.functions.MapFunction;

// import org.apache.flink.types.Row;

import com.theodorecurtil.schemas.Data;

// import org.apache.flink.api.java.tuple.Tuple2;

// import java.util.regex.Matcher;
// import java.util.regex.Pattern;

// import org.apache.flink.api.common.state.MapState;
// import org.apache.flink.api.common.state.MapStateDescriptor;
// import org.apache.flink.api.common.typeinfo.TypeInformation;
// import org.apache.flink.streaming.api.datastream.BroadcastStream;
// import org.apache.flink.streaming.api.datastream.KeyedStream;

import utils.OHLCV_PWF;


public class App {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);


        // --------------------- DEFINE KAFKA SOURCES ---------------------

        KafkaSource<Data> source = KafkaSource.<Data>builder()
        .setBootstrapServers("kafka_broker:29092")
        .setTopics("TRADE")
        .setGroupId("flink-java-ohlcv")
        .setStartingOffsets(OffsetsInitializer.earliest())
        .setValueOnlyDeserializer(ConfluentRegistryAvroDeserializationSchema.forSpecific(Data.class, "http://schema-registry:8081"))
        .build();

        

        // --------------------- DEFINE DATASTREAMS ---------------------

        // TRADE STREAM
        DataStream<Data> trade_data = env
        .fromSource(source, WatermarkStrategy.noWatermarks(), "TRADE")
        // .filter(new FilterFunction<Data>() {
        //     public boolean filter(Data value) {
        //         return (value.getInstrumentName().equals("ETH-PERPETUAL"));
        //     }
        // })
        .assignTimestampsAndWatermarks(
            WatermarkStrategy
            .<Data>forBoundedOutOfOrderness(Duration.ofSeconds(10))
            .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
        );


        DataStream<String> OHLCV_data = trade_data
        .keyBy(value -> value.getInstrumentName())
        .window(TumblingEventTimeWindows.of(Time.seconds(60)))
        .process(new OHLCV_PWF());

        
        // DEFINE KAFKA SINK
        KafkaSink<String> kafka_sink_ohlcv = KafkaSink.<String>builder()
        .setBootstrapServers("kafka_broker:29092")
        .setRecordSerializer(KafkaRecordSerializationSchema.builder()
            .setTopic("OHLCV")
            .setValueSerializationSchema(new SimpleStringSchema())
            .build()
        )
        .build();

        OHLCV_data.sinkTo(kafka_sink_ohlcv);



        // --------------------- LOGIC ---------------------

        // BROADCAST PERP
        // a map descriptor to store the name of the rule (string) and the rule itself.
        // MapStateDescriptor<String, Data> perpStateDescriptor = new MapStateDescriptor<>(
        //     "PerpBroadcastState",
        //     TypeInformation.of(String.class),
        //     TypeInformation.of(Data.class));

        // // broadcast the rules and create the broadcast state
        // BroadcastStream<Data> perpBroadcastStream = perp_stream
        //                 .broadcast(perpStateDescriptor);

        // // key options per mat and strike
        // KeyedStream<Tuple2<String, Data>, String> option_partitioned_stream = option_stream
        // .keyBy(value -> value.f0);

        // DataStream<String> output = option_partitioned_stream
        //          .connect(perpBroadcastStream)
        //          .process(
        //             new straddleKBPF()
        //          );

        // output.print();

        // // DEFINE KAFKA SINK
        // KafkaSink<stockPrice> kafka_sink_stock_price = KafkaSink.<stockPrice>builder()
        // .setBootstrapServers("broker:29092")
        // .setRecordSerializer(
        //     KafkaRecordSerializationSchema
        //     .builder()
        //     .setValueSerializationSchema(ConfluentRegistryAvroSerializationSchema.forSpecific(stockPrice.class, "STOCK_PRICE-value", "http://schema-registry:8081"))
        //     .setTopic("STOCK_PRICE")
        //     .build()
        // )
        // .build();

        // stock_price.sinkTo(kafka_sink_stock_price);

        env.execute("OHLCV");
    }
}