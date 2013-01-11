#!/bin/bash

LIST="javac.classpath="
CNT=1
echo "" > javadoc.properties

for JAR in `find -name "*.jar"`
do
    LIST="$LIST\${file.reference.$CNT}:"
    echo "file.reference.$CNT=$JAR" >> javadoc.properties
    CNT=`expr $CNT + 1`
done

LENGTH=`echo $LIST | grep -o "." | wc -l | sed s/\ //g`-1
echo ${LIST:0:$LENGTH} >> javadoc.properties
