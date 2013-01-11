#!/bin/bash

# Source this shellscript to set the Environment variables needed to 
# build the AIDA Components and to start Tomcat on vocab.maastro.nl

export CATALINA_OPTS="-Xms4256m -Xmx4512m"
export JAVA_OPTS="-Xms4256m -Xmx4512m"
export CATALINA_HOME=/usr/local/tomcat
export CATALINA_BASE=/usr/local/tomcat
export INDEXDIR=/var/local/aida/indexes
export JAVA_HOME=/usr/lib/jvm/jdk1.7.0
