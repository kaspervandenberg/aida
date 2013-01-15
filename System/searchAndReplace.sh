#!/bin/bash

# Array of regular expression and its replacement as supported by /bin/sed
# Order the array of patterns from most specific to most general; since 
# all matching patterns are executed in order
PATTERNS=(
	"http://localhost:8080/" "$TARGET_URL"
	"http://localhost:80/" "$TARGET_URL"
	"localhost" "$TARGET_HOST"
	"[^[:alnum:]]8080[^[:alnum:]]" "$TARGET_PORT"
	"[^[a:alnum:]]80[^[:alnum:]]" "$TARGET_PORT"
	"/home/sophijka/Tomcat/apache-tomcat-5.5.20" "$CATALINA_HOME"
	"/scratch/emij/old.jakarta" "$CATALINA_HOME"
)

FILEPATTERNS=( '*.java' '*.xml' '*.js' ) 

BACKUPEXT=.rename-bak

sedExpr() {
	INDEX=$1
	RESULT=$2
	eval "$RESULT=\"s,${PATTERNS[$INDEX]},${PATTERNS[$INDEX +1]},g\""
} 

echo "Replacing (in files ${FILEPATTERNS[@]}):"
echo "(Backup to $BACKUPEXT)"
for ((i = 0; i < ( ${#PATTERNS[@]} - 1); i+=2)); do
	echo -e "\t"${PATTERNS[$i]} "->" ${PATTERNS[$i + 1]}
done

COMMAND_LIST=""
COMMAND_LIST_FORMATTED=""

for FILEPAT in "${FILEPATTERNS[@]}"; do
	for ((i = 0; i < ( ${#PATTERNS[@]} - 1); i+=2)); do
		sedExpr $i EXPR
		COMMAND="find . -type f -name \\\"$FILEPAT\\\" -exec sed -i$BACKUPEXT \\\"$EXPR\\\" \'{}\'\'\;\';"
		COMMAND_LIST+=$COMMAND
		COMMAND_LIST_FORMATTED+=$COMMAND\\n
	done
done

echo -e $COMMAND_LIST_FORMATTED
echo Execute these replacement commands?
select yn in "Yes" "No"; do
	case $yn in
		Yes) eval $COMMAND_LIST; break;;
		No) echo No files altered; exit;
	esac
done


