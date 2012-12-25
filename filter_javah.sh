sed -i 's_\./__g' $JAVA_SRC_FILE_LIST
sed -i 's/.class//g' $JAVA_SRC_FILE_LIST
sed -i 's/\//./g' $JAVA_SRC_FILE_LIST
