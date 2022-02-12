package dev.ishikawa.demo.dd_flink.joinjob;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.operators.base.JoinOperatorBase;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class InnerJoinJob {
	public static void main(String[] args) throws Exception {
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

		final ParameterTool params = ParameterTool.fromArgs(args);
		env.getConfig().setGlobalJobParameters(params);

		// Read person file and generate tuples out of each string read
		//presonSet = tuple of (1  John)
		DataSet<Tuple2<Integer, String>> personSet = env
				.readTextFile(params.get("input1"))
				.map(value -> {
					String[] words = value.split(",");                                                 // words = [ {1} {John}]
					return new Tuple2<>(Integer.parseInt(words[0]), words[1]);
				});
		// Read location file and generate tuples out of each string read
		//locationSet = tuple of (1  DC)
		DataSet<Tuple2<Integer, String>> locationSet = env
				.readTextFile(params.get("input2")).
				map(value -> {
					String[] words = value.split(",");
					return new Tuple2<>(Integer.parseInt(words[0]), words[1]);
				});

		// join datasets on person_id
		// joined format will be <id, person_name, state>
		DataSet<Tuple3<Integer, String, String>> joined = personSet
				.join(locationSet, JoinOperatorBase.JoinHint.OPTIMIZER_CHOOSES)
				.where(0)
				.equalTo(0)
				.with((JoinFunction<Tuple2<Integer, String>, Tuple2<Integer, String>, Tuple3<Integer, String, String>>) (person, location) -> {
					return new Tuple3<>(person.f0, person.f1, location.f1);         // returns tuple of (1 John DC)
				});

		joined.writeAsCsv(params.get("output"), "\n", " ");

		env.execute("Join example");
	}
}
