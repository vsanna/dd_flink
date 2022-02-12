package dev.ishikawa.demo.dd_flink.joinjob;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;

public class RightOuterJoinJob {
	public static void main(String[] args) throws Exception {
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		final ParameterTool params = ParameterTool.fromArgs(args);
		env.getConfig().setGlobalJobParameters(params);

		DataSet<Tuple2<Integer, String>> personSet = env
				.readTextFile(params.get("input1"))
				.map(value -> {
					String[] words = value.split(",");
					return new Tuple2<>(Integer.parseInt(words[0]), words[1]);
				});

		DataSet<Tuple2<Integer, String>> locationSet = env
				.readTextFile(params.get("input2"))
				.map(value -> {
					String[] words = value.split(",");
					return new Tuple2<>(Integer.parseInt(words[0]), words[1]);
				});


		// right outer join datasets on person_id
		// joined format will be <id, person_name, state>
		DataSet<Tuple3<Integer, String, String>> joined = personSet.rightOuterJoin(locationSet)
				.where(0)
				.equalTo(0)
				.with((JoinFunction<Tuple2<Integer, String>, Tuple2<Integer, String>, Tuple3<Integer, String, String>>) (person, location) -> {
					// joinされた者同士が個々に来る. right joinなのでlocationは常に存在し, personがnullable
					// check for nulls
					if (person == null) {
						return new Tuple3<>(location.f0, "NULL", location.f1);
					}

					return new Tuple3<>(person.f0, person.f1, location.f1);
				});//.collect();

		joined.writeAsCsv(params.get("output"), "\n", " ");

		env.execute("Right Outer Join Example");
	}

}
