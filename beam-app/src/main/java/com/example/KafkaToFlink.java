import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.beam.runners.flink.FlinkPipelineOptions;
import org.apache.beam.runners.flink.FlinkRunner;

public class KafkaToFlink {
    public static void main(String[] args) {
        // FlinkPipelineOptions を登録
        PipelineOptionsFactory.register(FlinkPipelineOptions.class);
        FlinkPipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(FlinkPipelineOptions.class);
        options.setRunner(FlinkRunner.class);

        Pipeline pipeline = Pipeline.create(options);

        // Kafka Source -> Transform -> Kafka Sink
        pipeline.apply("ReadFromKafka", KafkaIO.<String, String>read()
                .withBootstrapServers("kafka.flink.svc.cluster.local:9092")
                .withTopic("test-topic")
                .withKeyDeserializer(StringDeserializer.class)
                .withValueDeserializer(StringDeserializer.class)
                .withoutMetadata())
                .apply("ProcessData", MapElements.into(TypeDescriptors.strings())
                        .via((KV<String, String> kv) -> kv.getValue().toUpperCase()))
                .apply("WriteToKafka", KafkaIO.<Void, String>write()
                        .withBootstrapServers("kafka.flink.svc.cluster.local:9092")
                        .withTopic("output-topic")
                        .withValueSerializer(StringSerializer.class)
                        .values());

        pipeline.run();
    }
}

