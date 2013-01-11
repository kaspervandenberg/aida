echo 'all exports aida install might need'

#aida@aida.science.uva.nl /home/aida/dev http://aida.science.uva.nl:9999
export JAVA_HOME=/usr/java/latest
export AIDA_HOME=/home/aida/dev
export TARGET_URL=http://aida.science.uva.nl:9999
export TARGET_PORT=9999

#common stuff
export CATALINA_HOME=$AIDA_HOME/apache-tomcat-6.0.13
export PATH=$JAVA_HOME/bin:$CATALINA_HOME/bin:$ANT_HOME/bin:$PATH
export ANT_HOME=$AIDA_HOME/apache-ant-1.7.0
export INDEXDIR=/home/aida/AIDA27june2007/INDEXDIR #shared with version running on 8888
export EDITOR=gedit
export JRE_HOME=$JAVA_HOME
export CATALINA_OPTS=-Xmx1000M

echo 'skip stuff below'

#aida@ws.vl-e.nl /home/aida/AIDA27june2007 http://aida.science.uva.nl:8888/axis/servlet/AxisServlet
#export JAVA_HOME=/usr/java/latest
#export CATALINA_HOME=/home/aida/AIDA27june2007/apache-tomcat-6.0.13
#export PATH=$JAVA_HOME/bin:$CATALINA_HOME/bin:$PATH
#export AIDA_HOME=/home/aida/AIDA27june2007
#export ANT_HOME=/home/aida/AIDA27june2007/apache-ant-1.7.0
#export INDEXDIR=$AIDA_HOME/INDEXDIR

#aida@ws.vl-e.nl /home/aida/Aida6july2007 http://ws.adaptivedisclosure.org:28888
#export JAVA_HOME=/home/shared/AIDA/jdk1.6.0
#export AIDA_HOME=/home/aida/Aida6july2007
#export CATALINA_HOME=$AIDA_HOME/apache-tomcat-6.0.13
#export PATH=$JAVA_HOME/bin:$CATALINA_HOME/bin:$ANT_HOME/bin:$PATH
#export ANT_HOME=$AIDA_HOME/apache-ant-1.7.0
#export INDEXDIR=/home/shared/AIDA/INDEXDIR
#export EDITOR=kedit
#export TARGET_URL=http://ws.adaptivedisclosure.org:8080
#export TARGET_PORT=8080

#ws.vl-e.nl
#export JAVA_HOME=/home/shared/AIDA/jdk1.6.0
#export CATALINA_HOME=/home/shared/AIDA/apache-tomcat-6.0.10
#export AIDA_HOME=/home/shared/AIDA
#export ANT_HOME=$AIDA_HOME/apache-ant-1.7.0

#aida@ws.vl-e.nl /home/aida/AIDA2
#export JAVA_HOME=/home/shared/AIDA/jdk1.6.0
#export CATALINA_HOME=/home/aida/AIDA2/apache-tomcat-6.0.13
#export AIDA_HOME=/home/aida/AIDA2
#export ANT_HOME=/home/aida/AIDA2/apache-ant-1.7.0
#export INDEXDIR=/home/shared/AIDA/INDEXDIR

#u005673.science.uva.nl PRODUCTION
#export JAVA_HOME=/usr/java/latest
#export CATALINA_HOME=/tmp/aida/apache-tomcat-6.0.13
#export AIDA_HOME=/tmp/aida
#export ANT_HOME=/opt/netbeans-5.5.1/ide7/ant

#u005673.science.uva.nl TEST
#export JAVA_HOME=/usr/java/latest
#export CATALINA_HOME=/tmp/DELETEME/apache-tomcat-6.0.13
#export AIDA_HOME=/tmp/DELETEME
#export ANT_HOME=/opt/netbeans-5.5.1/ide7/ant

#frans u005673.science.uva.nl /tmp/frans/TEST_INSTALL
#export JAVA_HOME=/usr/java/latest
#export CATALINA_HOME=/tmp/frans/TEST_INSTALL/apache-tomcat-6.0.13
#export AIDA_HOME=/tmp/frans/TEST_INSTALL
#export ANT_HOME=/opt/netbeans-5.5.1/ide7/ant

#adsl-208-29.dsl.uva.nl
#export JAVA_HOME=/home/frans/jdk1.6.0
#export CATALINA_HOME=/tmp/frans/apache-tomcat-6.0.9
#export AIDA_HOME=/tmp/frans/aida
#export ANT_HOME=/home/frans/netbeans-5.5/ide7/ant

#####################

echo "JAVA_HOME=$JAVA_HOME"
echo "CATALINA_HOME=$CATALINA_HOME"
echo "AIDA_HOME=$AIDA_HOME"
echo "ANT_HOME=$ANT_HOME"

cd $AIDA_HOME
export PATH=$JAVA_HOME/bin:$PATH
export JRE_HOME=$JAVA_HOME
export CVS_RSH=ssh
export INDEXDIR=${INDEXDIR:-AIDA_HOME/INDEXDIR}
export EDITOR=${EDITOR:-gedit}


echo "INDEXDIR=$INDEXDIR"

mkdir $AIDA_HOME
mkdir $INDEXDIR
export CATALINA_BASE=$CATALINA_HOME
export HTTPD_DIR=/var/www/rdf.adaptivedisclosure.org/tmp/
export HTTPD_URL=http://rdf.adaptivedisclosure.org/tmp/
#export PWD=`pwd` #if not already set
export CATALINA_OPTS=-Xmx1000M
export SOPHIA_CP=".`find $JAVA_HOME/lib/ -type f -name '*.jar' -printf ':%p'``find $ANT_HOME/lib/ -name '*.jar' -printf ':%p'`"

java -version
javac -version

echo "start tomcat with $CATALINA_HOME/bin/startup.sh"
echo "stop tomcat with $CATALINA_HOME/bin/shutdown.sh"
