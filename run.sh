CURDIR=$(pwd)
cd $(dirname $0)
BASEDIR=$(pwd)/
cd $CURDIR

RELPATH_LIN_FB=${BASEDIR}
JAVA_SRC_PATH=${RELPATH_LIN_FB}src
JAVA_BUILD_PATH=${BASEDIR}build

cd $JAVA_SRC_PATH
TEST=$(grep -l -r " void main" | sed 's_.java__g')
cd $JAVA_BUILD_PATH
echo "java -Djava.library.path=$JAVA_BUILD_PATH $TEST"
su -c "java -Djava.library.path=$JAVA_BUILD_PATH $TEST"
