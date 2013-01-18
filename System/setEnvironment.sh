#!/bin/bash

# Source this shellscript to set the Environment variables needed to 
# build the AIDA Components and to start Tomcat on vocab.maastro.nl

export CATALINA_OPTS="-Xms4256m -Xmx4512m"
export JAVA_OPTS="-Xms4256m -Xmx4512m"
export CATALINA_HOME=/usr/local/tomcat
export CATALINA_BASE=/usr/local/tomcat
export INDEXDIR=/var/local/aida/indexes
export JAVA_HOME=/usr/lib/jvm/jdk1.7.0
export JRE_HOME=$JAVA_HOME
export AIDA_HOME=/home/aida/
export ANT_HOME=/usr/share/ant
export TARGET_URL=http://vocab.maastro.nl:80/
export TARGET_HOST=vocab.maastro.nl
export TARGET_PORT=80

export PATH=$JAVA_HOME/bin:$CATALINA_HOME/bin:$ANT_HOME/bin:$PATH
export EDITOR=nano
