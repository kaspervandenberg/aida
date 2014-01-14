#!/bin/bash

# Array of regular expression and its replacement as supported by /bin/sed
# Order the array of patterns from most specific to most general; since 
# all matching patterns are executed in order
PATTERNS=(
	'http://localhost:8080/' "$TARGET_URL/"
	'http://localhost:80/' "$TARGET_URL/"
	'(\W):8080/(\W|sesame\b)' "\1:$TARGET_PORT/\2"
	'(\W):80/(\W|sesame\b)' "\1:$TARGET_PORT/\2"
	'/home/sophijka/Tomcat/apache-tomcat-5.5.20' "$CATALINA_HOME"
	'/scratch/emij/old.jakarta' "$CATALINA_HOME"
)


FILEPATTERNS=( '*.java' '*.js') 

BACKUPEXT=.rename-bak

grepExpr() {
	for ((i = 0; i < ( ${#PATTERNS[@]} - 1); i+=2)); do
		echo -n "(${PATTERNS[$i]})|"
	done
	echo -n "^(?!x)x"		# '^(?!x)x' is intended to never 
					# match, so that it can be OR-ed
					#  with the last clause in
					# $PATTERNS.
					# This grep expression is a
					# Perl regexp and requires
					# the grep '--perl-regexp'-
					# switch.
}

sedProgram() {
	for ((i = 0; i < ( ${#PATTERNS[@]} - 1); i+=2)); do
		echo -n "s,${PATTERNS[$i]},${PATTERNS[$i +1]},g; "
	done
}

findExpr() {
	echo -n "-type f \( "
	for FILEPAT in "${FILEPATTERNS[@]}"; do
		echo -n "-name '$FILEPAT' -o "
	done
	echo -n "-false \)"		# intentionally use '-false'-
					# expression which can be OR-ed
					# ('-o') with last clause in
					# $FILEPATTERNS.
}


echo "Replacing (in files ${FILEPATTERNS[@]}):"
echo "(Backup to $BACKUPEXT)"
for ((i = 0; i < ( ${#PATTERNS[@]} - 1); i+=2)); do
	echo -e "\t"${PATTERNS[$i]} "->" ${PATTERNS[$i + 1]}
done

COMMAND="find . `findExpr` |\
	 xargs grep --files-with-matches --perl-regexp --regexp='`grepExpr`' |\
	 xargs sed --regexp-extended --separate --in-place='$BACKUPEXT' --expression='`sedProgram`'"

echo $COMMAND
echo Execute these replacement commands?
select yn in "Yes" "No"; do
	case $yn in
		Yes) eval $COMMAND; break;;
		No) echo No files altered; exit;
	esac
done


