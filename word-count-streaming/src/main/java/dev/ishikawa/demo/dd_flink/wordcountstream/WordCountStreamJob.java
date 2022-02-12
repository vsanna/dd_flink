package dev.ishikawa.demo.dd_flink.wordcountstream;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Objects;

public class WordCountStreamJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // ex: java -jar
        ParameterTool params = ParameterTool.fromArgs(args);

        env.getConfig().setGlobalJobParameters(params);

//        DataSet<String> text = env.readTextFile(params.get("input"));
        DataStreamSource<String> text = env.readTextFile("/var/data/input/wordcount.txt");


        SingleOutputStreamOperator<String> filtered = text.filter((FilterFunction<String>) value -> value.startsWith("N"));

        SingleOutputStreamOperator<Tuple2<String, Integer>> tokenized = filtered.map(new Tokenizer());

        String outputPath = "/var/data/output/wordcountstream.txt";

        env.execute("WordCount Example");
    }

    public static final class Tokenizer
            implements MapFunction<String, Tuple2<String, Integer>> {
        public Tuple2<String, Integer> map(String value) {
            return new Tuple2(value, Integer.valueOf(1));
        }
    }
}
