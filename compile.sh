if [ $# -lt 1 ]
then
	printf 'error, use -j for javac or -c for gcc or -all to compile everithing\n'
	exit
fi
CURDIR=$(pwd)
cd $(dirname $-1)
BASEDIR=$(pwd)/
cd $CURDIR


export RELPATH=${BASEDIR}native_src/
RELPATH_LIN_FB=${BASEDIR}
RELPATH_LIB=${RELPATH_LIN_FB}clib
C_SRC_PATH=${BASEDIR}clib
RAW_SCK_SRC_PATH=${BASEDIR}clib/raw_sock
RAW_SCK_LIB_PATH=${BASEDIR}clib
JAVA_SRC_PATH=${RELPATH_LIN_FB}src
JAVA_BUILD_PATH=${BASEDIR}build
JAVA_SRC_FILE_LIST=${BASEDIR}jfile.list

if [ $1 == '-clean' ]
then
	if [ -d $JAVA_BUILD_PATH ]
	then
		rm -f -r $JAVA_BUILD_PATH
	fi
	rm -f -r $RAW_SCK_SRC_PATH/*.o
	rm -f -r $RAW_SCK_LIB_PATH/*.h
	rm -f -r $RAW_SCK_LIB_PATH/*.so
	printf "clean complete\n"
	exit 0
fi

if [ $1 == '-all' ]
then
	if ! ./compile.sh -j
	then
		exit 1
	else
		./compile.sh -c
	fi
	exit 0
fi
if [ $1 == '-j' ]
then
	if [ -d $JAVA_BUILD_PATH ]
	then
		rm -f -r $JAVA_BUILD_PATH/*.class
	else
		mkdir $JAVA_BUILD_PATH
	fi
	find $JAVA_SRC_PATH/ -name *.java -print>$JAVA_SRC_FILE_LIST
	printf "javac -classpath %s -d %s %s\n" $BASEDIR $JAVA_BUILD_PATH "@$JAVA_SRC_FILE_LIST"
	TEMP_VAR=""
	if ! javac -classpath $BASEDIR -d $JAVA_BUILD_PATH @$JAVA_SRC_FILE_LIST
	then
		printf "Compilation error, aborting\n"
		exit 1
	fi
	rm -f $JAVA_SRC_FILE_LIST
	cd $JAVA_BUILD_PATH
	find -name __*__.class -print>$JAVA_SRC_FILE_LIST
	cd $CURDIR
	export JAVA_SRC_FILE_LIST
	export JAVA_SRC_PATH
	$BASEDIR./filter_javah.sh
	JNI_CLASSES=""
	while read a
	do
		JNI_CLASSES+="$JNI_CLASSES $a"
	done < $JAVA_SRC_FILE_LIST
	printf "javah -d %s -classpath %s/ -jni %s\n" $RAW_SCK_LIB_PATH $JAVA_BUILD_PATH $JNI_CLASSES
	if ! javah -d $RAW_SCK_LIB_PATH -classpath $JAVA_BUILD_PATH/ -jni $JNI_CLASSES
	then
		printf "Compilation error, aborting\n"
		exit 1
	fi
	rm $JAVA_SRC_FILE_LIST
	printf "Build Complete\n"
else
	if [ $1 == '-c' ]
	then
		if [ $# -gt 1 ]
		then
			make clean -C $RAW_SCK_SRC_PATH
		fi
		printf "\n\nbuilding raw_socket..\n"
		if ! make -C $RAW_SCK_SRC_PATH
		then
			printf "Compilation error, aborting\n"
			exit 1
		fi
		rm $RAW_SCK_SRC_PATH/raw_socket
		printf "\n\nbuilding shared library..\n"
		if ! make shared -C $RAW_SCK_SRC_PATH
		then
			printf "Compilation error, aborting\n"
			exit 1
		fi
		mv $RAW_SCK_SRC_PATH/libraw_socket.so $RAW_SCK_LIB_PATH
		JNI_C_FILE=$(ls $RELPATH_LIB/*.c)
		printf "\n\nbuilding JNI shared library..\n"
		printf "gcc -shared -fPIC -I /usr/lib/jvm/java-1.7.0-openjdk-1.7.=.9.x86_64/include/ -I /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.9.x86_64/include/linux/ -I%s/lib/ %s/%s %s/libraw_socket.so -o libjin_rawsocket.so\n" $RELPATH $C_SRC_PATH $JNI_C_FILE $RAW_SCK_LIB_PATH
		if ! gcc -shared -fPIC -I/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.9.x86_64/include/ -I/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.9.x86_64/include/linux/ -I$RELPATH/lib/ $JNI_C_FILE $RAW_SCK_LIB_PATH/libraw_socket.so -o libjni_rawsocket.so
		then
			printf "Compilation error, aborting\n"
			exit 1
		fi
		mv libjni_rawsocket.so $JAVA_BUILD_PATH
	else
		printf 'error, unknown option $1\n'
	fi
	rm -f -r $RAW_SCK_SRC_PATH/*.o
	rm -f -r $RAW_SCK_LIB_PATH/*.h
	printf "Build Complete\n"
fi
exit 0
