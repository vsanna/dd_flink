cd $HOME/repos/dd_flink/proto/

current_dir=$(pwd)
script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

package=demo
libdir=$current_dir/out/$package

# Generate
rm -rf $libdir
mkdir -p $libdir/kotlin $libdir/java
protoc --proto_path=$script_dir/src/proto --java_out=$libdir/java --kotlin_out=$libdir/kotlin $(find $script_dir/src/proto/$package -iname "*.proto")

# Build
mkdir $libdir/src/main $libdir/buildSrc
cp -r $libdir/kotlin $libdir/java $libdir/src/main/
cd $libdir
cat ../../scripts/templates/buildSrc.build.gradle.kts.template >| buildSrc/build.gradle.kts
cat ../../scripts/templates/build.gradle.kts.template | sed s/{{GROUP_NAME}}/dev.ishikawa.demo.dd_flink.proto/ | sed s/{{VERSION_NAME}}/0.1.0/ >| build.gradle.kts
touch settings.gradle.kts
echo "rootProject.name = \"$package\"" >| settings.gradle.kts

gradle wrapper
#./gradlew check assemble

# Publish
#./gradlew check assemble publishMavenPublicationToNexusRepository || true
./gradlew check assemble publishToMavenLocal || true


