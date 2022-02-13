cd $HOME/repos/dd_flink/custom-protobuf-format-factory/

current_dir=$(pwd)

gradle wrapper

gradle shadowJar

docker cp $current_dir/build/libs/custom-protobuf-format-factory-1.0-SNAPSHOT-all.jar dd_flink_taskmanager1_1:/opt/flink

