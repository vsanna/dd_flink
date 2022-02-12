package dev.ishikawa.demo.dd_flink.wordcount;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.UnsortedGrouping;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;

import java.util.Objects;

public class WordCountJob {
    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        // ex: java -jar
        ParameterTool params = ParameterTool.fromArgs(args);

        env.getConfig().setGlobalJobParameters(params);

//        DataSet<String> text = env.readTextFile(params.get("input"));
        DataSet<String> text = env.readTextFile("file:///var/data/input/wordcount.txt");

        DataSet<String> filtered = text.filter((FilterFunction<String>) value -> value.startsWith("N"));

        DataSet<Tuple2<String, Integer>> tokenized = filtered.map(new Tokenizer());

        UnsortedGrouping<Tuple2<String, Integer>> grouped = tokenized.groupBy(0);
        DataSet<Tuple2<String, Integer>> counts = grouped.sum(1);

        String outputPath = "file:///var/data/output/wordcount.txt";
//        String outputPath = params.has("output");
        if (!Objects.isNull(outputPath)) {
            counts.writeAsCsv(outputPath, "\n", " ")
                    .name("CSV output");

            env.execute("WordCount Example");
        }
    }

    public static final class Tokenizer
            implements MapFunction<String, Tuple2<String, Integer>> {
        public Tuple2<String, Integer> map(String value) {
            return new Tuple2(value, Integer.valueOf(1));
        }
    }
}
