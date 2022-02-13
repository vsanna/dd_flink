cd $HOME/repos/dd_flink/proto/

current_dir=$(pwd)

cd ./out/demo

gradle wrapper

docker cp $current_dir/out/demo/build/libs/demo-0.1.0.jar dd_flink_taskmanager1_1:/opt/flink