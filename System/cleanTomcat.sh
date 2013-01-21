#!/bin/bash

# This script removes changes made by AIDA install from Tomcat webapps 
# and AXIS: it removes the directories $AXIS_HOME, $SEARCH_HOME, and 
# $SERVICES_HOME, installs a fresh copy of AXIS, copies xml-security 
# and javamail into the AXIS classpath. 

# Path where Tomcat is installed
CATALINA_HOME=${CATALINA_HOME-/usr/local/tomcat}

# Path of Axis servlet in Tomcat
AXIS_HOME=${AXIS_HOME-$CATALINA_HOME/webapps/axis}

# Path of AIDA::Search servlet in Tomcat
SEARCH_HOME=${SEARCH_HOME-$CATALINA_HOME/webapps/search}

# Path of AIDA::Search.war in Tomcat
SEARCH_WAR=${SEARCH_WAR-$CATALINA_HOME/webapps/search.war}

# Path of AIDA::Storage Services servlet in Tomcat
SERVICES_HOME=${SERVICES_HOME-$CATALINA_HOME/webapps/Services}

# Binary distribution of AXIS;
# default is v1.4 in ~/download.
AXIS_DIST=${AXIS_DIST-~/download/axis-bin-1_4.tar.gz}

# Binary distribution of xml-security;
# default is v1.5.3 in ~/download.
XMLSEC_DIST=${XMLSEC_DIST-~/download/xml-security-bin-1_5_3.zip}

#Binary distribution of javamail;
#default is v1.4.5 in ~/download.
JAVAMAIL_DIST=${JAVAMAIL_DIST-~/download/javamail1_4_5.zip}

### END OF Configuration section

extract_tar () {
	ARCHIVE=$1
	WHITELIST=${*:2}

	tar --extract --auto-compress --directory=$EXTRACTDIR \
		--wildcards \
		--file=$ARCHIVE $WHITELIST
}

extract_zip () {
	ARCHIVE=$1
	WHITELIST=${*:2}

	unzip -d $EXTRACTDIR $ARCHIVE $WHITELIST
}

extract () {
	ARCHIVE=$1
	WHITELIST=${*:2}

	if [[ $ARCHIVE == *.tar ]] || [[ $ARCHIVE == *.tgz ]] || \
			[[ $ARCHIVE == *.tar.gz ]] || [[ $ARCHIVE == *.tar.bz2 ]]; then
		extract_tar $ARCHIVE $WHITELIST;
	elif [[ $ARCHIVE == *.zip ]]; then
		extract_zip $ARCHIVE $WHITELIST;
	fi
}
echo "About to remove $AXIS_HOME, $SEARCH_HOME, and $SERVICES_HOME. Continue?"
select yn in "Yes" "No"; do
	case $yn in
		"Yes") break;;
		*) exit;;
	esac
done

if [ -n "$AXIS_HOME" ]; then
	rm -rf $AXIS_HOME
fi

if [ -n "$SEARCH_HOME" ]; then
	sudo rm -rf $SEARCH_HOME
	sudo rm $SEARCH_WAR
fi

if [ -n "$SERVICES_HOME" ]; then
	rm -rf $SERVICES_HOME
fi

EXTRACTDIR=`mktemp -d`
extract $AXIS_DIST axis\*/webapps
extract $XMLSEC_DIST xml-security-\*/libs/xmlsec-\*
extract $JAVAMAIL_DIST javamail\*/mail.jar javamail\*/lib/\*.jar

mkdir $AXIS_HOME
cp --recursive --target-directory=$AXIS_HOME \
	$EXTRACTDIR/axis*/webapps/axis/*
cp --recursive --target-directory=$AXIS_HOME/WEB-INF/lib \
	$EXTRACTDIR/xml-security-*/libs/xmlsec-* \
	$EXTRACTDIR/javamail*/mail.jar $EXTRACTDIR/javamail*/lib/*.jar

echo "About to remove temporary directory $EXTRACTDIR. Continue?"
select yn in "Yes" "No"; do
	case $yn in
		"Yes") break;;
		*) exit;;
	esac
done
rm -rf $EXTRACTDIR

# vim: set tabstop=4 shiftwidth=4 fo=cqwan textwidth=70 spell 
# spl=en_gb:
