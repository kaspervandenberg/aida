#!/bin/bash
echo "installing AIDA in $AIDA_HOME"
cd $AIDA_HOME

#if not done yet then source ./SOURCE_THIS.sh
#if [ -z $AIDA_HOME ] ; then echo "ERROR: env not set: source $AIDA_HOME/AIDA/System/SOURCE_THIS3_SVN.sh" ; exit ;
#else echo "env set"
#fi
if [ -z $TARGET_HOST ] ; then echo "ERROR: env not set!" ; exit ; fi

#get all code from SVN
#svn co svn+ssh://AidaUser@ilps.science.uva.nl/scratch/svn/AIDA

#tomcat
if [ -d $CATALINA_HOME ] && [ -a $CATALINA_HOME/conf/server.xml ] ; then echo "tomcat installed";
else
wget http://apache.nedmirror.nl/tomcat/tomcat-6/v6.0.13/bin/apache-tomcat-6.0.13.tar.gz
tar -zxf apache-tomcat-6.0.13.tar.gz
rm -f apache-tomcat-6.0.13.tar.gz
mkdir $CATALINA_HOME/
mv apache-tomcat-6.0.13/* $CATALINA_HOME/
#handy:
#ln ./INSTALL.sh $CATALINA_HOME/webapps/ROOT/INSTALL.sh.txt
#ln ./SOURCE_THIS.sh $CATALINA_HOME/webapps/ROOT/SOURCE_THIS.sh.txt
fi

#axis
if [ -a $CATALINA_HOME/webapps/axis/EchoHeaders.jws ] ; then echo "axis installed";
else
echo "installing axis"
wget http://apache.dsmirror.nl/ws/axis/1_4/axis-bin-1_4.tar.gz
tar -zxf axis-bin-1_4.tar.gz
cp -R axis-1_4/webapps/axis $CATALINA_HOME/webapps/.
rm axis-bin-1_4.tar.gz
rm -rf axis-1_4/
wget http://rdf.adaptivedisclosure.org/AIDA/JarsOnWS/javamail-1.4/lib/mailapi.jar
mv mailapi.jar $CATALINA_HOME/webapps/axis/WEB-INF/lib/.
wget http://xml.apache.org/security/dist/java-library/xml-security-bin-1_4_1.zip
fi

#something fishy with unzip xml-security-bin-1_4_1.zip in script, works when typed directly...
if [ -a $CATALINA_HOME/webapps/axis/WEB-INF/lib/xmlsec-1.4.1.jar ] ; then echo "xmlsec-1.4.1.jar installed";
else
echo "about to do jar -xf xml-security-bin-1_4_1.zip"
#unzip xml-security-bin-1_4_1.zip
jar -xf xml-security-bin-1_4_1.zip
cp xml-security-1_4_1/libs/xmlsec-1.4.1.jar $CATALINA_HOME/webapps/axis/WEB-INF/lib/.
rm -f xml-security-bin-1_4_1.zip
rm -rf xml-security-1_4_1/
fi

#password
if [ -a $AIDA_HOME/tomcat-users_ok ] ; then echo 'tomcat-users.xml ok'
else
echo 'starting $EDITOR $CATALINA_HOME/conf/tomcat-users.xml , do not continue with this installation if not OK'
echo 'make sure passwords are set, and not the default ones'
$EDITOR $CATALINA_HOME/conf/tomcat-users.xml & #kedit blocks; gedit not
echo 'is $CATALINA_HOME/conf/tomcat-users.xml ok ? If not control-C'
select ok in "yes" "no" ; do
if [ ${ok:-"hallo"} == "no" ] ; then exit; fi;
if [ ${ok:-"hallo"} == "yes" ] ; then break; fi;
done
echo '$CATALINA_HOME/conf/tomcat-users.xml OK'
touch tomcat-users_ok
fi
#cat $CATALINA_HOME/conf/tomcat-users.xml

#localhost:8080 no proxy
if [ -a $AIDA_HOME/server_ok ] ; then echo 'server.xml ok'
else
echo 'starting $EDITOR $CATALINA_HOME/conf/server.xml , do not continue with this installation if not OK'
echo 'make sure it runs on localhost:8080 no proxy'
echo 'if you use different servers at the same time, also check others ports'
$EDITOR $CATALINA_HOME/conf/server.xml &
echo 'is $CATALINA_HOME/conf/server.xml ok, just localhost:8080, without proxy ? If not control-C'
select ok in "yes" "no" ; do
if [ ${ok:-"hallo"} == "no" ] ; then exit; fi;
if [ ${ok:-"hallo"} == "yes" ] ; then break; fi;
done
echo '$CATALINA_HOME/conf/server.xml OK'
touch server_ok
fi
#grep port $CATALINA_HOME/conf/server.xml

#cvs aida code, download add-ons
#USING SVN NOWADAYS
#if [ -d $AIDA_HOME/OOO ] ; then echo 'cvs already done'
#else
#echo 'about to download from cvs, user=fransve, password needed'
#cvs -d :ext:fransve@gforge.vl-e.nl:/cvsroot/aid checkout search CVSROOT CollocationService Indexer Indexer_1 LearnModelService NERService OOO PassWords SRBDirectory TFIDF TFIDFMean TestModelService TomcatClient synonymWS
#NOTE: many different versions of activation.jar
#cp $AIDA_HOME/OOO/ext/activation.jar $CATALINA_HOME/webapps/axis/WEB-INF/lib/.
#fi

#cvs version of NER not ok, use her tar.gz
# if [ -a $AIDA_HOME/NERecognizer.tar.gz  ] ; then echo 'already have the latest ner code'
# else
# #wget http://aida.science.uva.nl/~frans/NERecognizer.tar.gz #'support' block on port 80 on aida machine
# wget http://aida.science.uva.nl:8888/NERecognizer.tar.gz
# tar -zxf NERecognizer.tar.gz
# wget http://aida.science.uva.nl:8888/Collocations.tar.gz
# tar -zxf Collocations.tar.gz
# wget http://aida.science.uva.nl:8888/LearnModel.tar.gz
# tar -zxf LearnModel.tar.gz
# fi

if [ -a $CATALINA_HOME/webapps/sesame/WEB-INF/system.conf ] ; then echo 'sesame installed'
else
echo "about to download sesame"
wget http://ovh.dl.sourceforge.net/sourceforge/sesame/sesame-1.2.6-bin.tar.gz
tar -zxf sesame-1.2.6-bin.tar.gz
rm -f sesame-1.2.6-bin.tar.gz
mkdir sesame ; cd sesame
jar -xf $AIDA_HOME/sesame-1.2.6/lib/sesame.war
cd ..
mv $AIDA_HOME/sesame $CATALINA_HOME/webapps/.
cp $CATALINA_HOME/webapps/sesame/WEB-INF/lib/*.jar $CATALINA_HOME/webapps/axis/WEB-INF/lib/.
cp $CATALINA_HOME/webapps/sesame/WEB-INF/system.conf.example $CATALINA_HOME/webapps/sesame/WEB-INF/system.conf
echo '$EDITOR $CATALINA_HOME/webapps/sesame/WEB-INF/system.conf &'
rm -rf $AIDA_HOME/sesame
rm -rf $AIDA_HOME/sesame-1.2.6
fi


#add ant
if [ -a $ANT_HOME/bin/ant ] ; then echo 'ant installed'
else echo "about to download ant"
#wget http://apache.dsmirror.nl/ant/binaries/apache-ant-1.7.0-bin.tar.gz
wget http://archive.apache.org/dist/ant/binaries/apache-ant-1.7.0-bin.tar.gz

tar -zxf apache-ant-1.7.0-bin.tar.gz
rm -f apache-ant-1.7.0-bin.tar.gz
mv apache-ant-1.7.0 $ANT_HOME
cp $CATALINA_HOME/lib/catalina-ant.jar $ANT_HOME/lib/.
#add more ant stuff, perhaps not really needed
wget http://www.clarkware.com/software/jdepend-2.9.zip
unzip jdepend-2.9.zip
mv jdepend-2.9/lib/jdepend-2.9.jar $ANT_HOME/lib
wget http://surfnet.dl.sourceforge.net/sourceforge/junit/junit4.3.1.zip
unzip junit4.3.1.zip
mv junit4.3.1/junit-4.3.1.jar $ANT_HOME/lib
#wget http://mirror.w3media.nl/apache/xml/commons/xml-commons-1.0.b2.tar.gz
wget ftp://mirror.nyi.net/apache/xml/commons/xml-commons-1.0.b2.tar.gz
tar -zxf xml-commons-1.0.b2.tar.gz
cp xml-commons-1.0.b2/java/build/which.jar $ANT_HOME/lib
rm -f xml-commons-1.0.b2.tar.gz
rm -rf xml-commons-1.0.b2/
#NOTE: after adding an ant jar, add it to the path and see what you've got
export SOPHIA_CP=".`find $JAVA_HOME/lib/ -type f -name '*.jar' -printf ':%p'``find $ANT_HOME/lib/ -name '*.jar' -printf ':%p'`"
#java -classpath $SOPHIA_CP -Duser.properties.file=user_properties_file org.apache.tools.ant.launch.Launcher -diagnostics
fi

if [ -f ~/build.properties ]
then echo 'watch out various build.xml might use same ~/build.properties'
#cat ~/build.properties
#same for  ~/*-build.properties # where *=<project name>
#simply move those out of the way, nothing shared between installations
echo 'I will simply move ~/build.properties ~/build.properties.movedoutoftheway'
mv ~/build.properties ~/build.properties.movedoutoftheway
fi

if [ -a ./build.properties ] ; then echo 'assuming ./build.properties are ok'
else
echo 'fill ./build.properties'
#echo 'tomcat proxy and install must match apache2 proxy (see /etc/httpd/conf.d/ws.adaptivedisclosure.org.conf)'
#grep Connector $CATALINA_HOME/conf/server.xml
#cat $CATALINA_HOME/conf/tomcat-users.xml|grep 'roles'| grep 'manager'| awk '{gsub("\"","''"); print $2 ORS $3}' > ./build.properties
cat $CATALINA_HOME/conf/tomcat-users.xml|grep 'roles'| grep 'manager'| awk '{gsub("\"","''"); print $2 ORS $3}' >> ./build.properties
echo "manager.port=$TARGET_PORT" >> ./build.properties
#looks like the web pages from OOO have problems in the build
#OOO watch out, check http://ws.adaptivedisclosure.org/OOO/ and not http://ws.adaptivedisclosure.org/OOO
echo "manager.url=$TARGET_URL/manager" >> ./build.properties
#echo "manager.url=http://localhost:8080/manager" >> ./build.properties
echo "catalina.home=$CATALINA_HOME" >> ./build.properties
echo "host.name=$TARGET_HOST" >> ./build.properties
echo "sesame.username=testuser" >> ./build.properties
echo "sesame.password=opensesame" >> ./build.properties
echo "sesame.repository=rdf-db" >> ./build.properties
#    <property name="manager.username"   value=""/>
#    <property name="manager.password"   value=""/>
fi

#start tomcat
#echo 'about to do the real stuff, not for now, quitting'
#exit
if netstat -t -l --numeric-ports | grep ":$TARGET_PORT"
then echo "tomcat seems to be running"
else echo "tomcat does not seems to be running, let me start it"
$CATALINA_HOME/bin/startup.sh
sleep 5
fi

#if netstat -t -l --numeric-ports | grep ":$TARGET_PORT"
#then echo "tomcat seems to be running"
#else echo "FATAL ERROR: tomcat does not seems to be running" ; exit
#fi

#config sesame, note the system.conf has default two times 'admin'
#pushd $CATALINA_HOME/webapps/sesame/WEB-INF/bin
#java -cp ../lib/sesame.jar:../lib/rio.jar:../lib/openrdf-model.jar:../lib/openrdf-util.jar org.openrdf.sesame.config.ui.ConfigureSesame
#popd
#java -classpath .`find $CATALINA_HOME/webapps/sesame/WEB-INF/lib -type f -name '*.jar' -printf '%p:'` org.openrdf.sesame.config.ui.ConfigureSesame
chmod +x $CATALINA_HOME/webapps/sesame/WEB-INF/bin/configSesame.sh
$CATALINA_HOME/webapps/sesame/WEB-INF/bin/configSesame.sh

echo 'about to build the code'
#find $AIDA_HOME/AIDA -name build.xml

cd AIDA
find . -type f -name "*.java" -exec grep -I -H -n 'localhost' {} \; -exec sed -i "s^localhost^$TARGET_HOST^g" {} \;
find . -type f -name "*.xml" -exec grep -I -H -n 'localhost' {} \; -exec sed -i "s^localhost^$TARGET_HOST^g" {} \;
find . -type f -name "*.java" -exec grep -I -H -n 'http://localhost:8080/' {} \; -exec sed -i "s^http://localhost:8080/^$TARGET_URL/^g" {} \;
find . -type f -name "*.xml" -exec grep -I -H -n 'http://localhost:8080/' {} \; -exec sed -i "s^http://localhost:8080/^$TARGET_URL/^g" {} \;
find . -type f -name "*.java" -exec grep -I -H -n '8080' {} \; -exec sed -i "s^8080^$TARGET_PORT^g" {} \;
find . -type f -name "*.xml" -exec grep -I -H -n '8080' {} \; -exec sed -i "s^8080^$TARGET_PORT^g" {} \;
find . -type f -name "*.xml" -exec grep -I -H -n '/home/sophijka/Tomcat/apache-tomcat-5.5.20' {} \; -exec sed -i "s^/home/sophijka/Tomcat/apache-tomcat-5.5.20^$CATALINA_HOME^g" {} \;

cd ..

cd AIDA/Storage/Services
cp $AIDA_HOME/build.properties .
$ANT_HOME/bin/ant clean compile > ant-output.txt 2>&1
if grep -H -n BUILD `pwd`/ant-output.txt |grep "SUCCESSFUL"
then
  echo -n ""
else
  echo "BUILD FAILED:"
  cat `pwd`/ant-output.txt
  exit -1
fi
touch $CATALINA_HOME/webapps/axis/WEB-INF/web.xml
cd $AIDA_HOME

sleep 20
cd AIDA/Search/Indexer/
cp $AIDA_HOME/build.properties .
$ANT_HOME/bin/ant test install > ant-output.txt 2>&1
#ERROR dir names with jakarta-tomcat-5.5.9_base
if grep -H -n BUILD `pwd`/ant-output.txt |grep "SUCCESSFUL"
then
  echo -n ""
else
  echo "BUILD FAILED:"
  cat `pwd`/ant-output.txt
  exit -1
fi
touch $CATALINA_HOME/webapps/axis/WEB-INF/web.xml
cd $AIDA_HOME

sleep 20
cd AIDA/Search/search/
cp $AIDA_HOME/build.properties .
find . -type f -name "*.java" -exec grep -I -H -n 'http://localhost:8080/' {} \; -exec sed -i "s^http://localhost:8080/^$TARGET_URL/^g" {} \;
find . -type f -name "*.xml" -exec grep -I -H -n 'http://localhost:8080/' {} \; -exec sed -i "s^http://localhost:8080/^$TARGET_URL/^g" {} \;
find . -type f -name "*.js" -exec grep -I -H -n 'http://localhost:8080/' {} \; -exec sed -i "s^http://localhost:8080/^$TARGET_URL/^g" {} \;
find . -type f -name "*.java" -exec grep -I -H -n '8080' {} \; -exec sed -i "s^8080^$TARGET_PORT^g" {} \;
find . -type f -name "*.xml" -exec grep -I -H -n '8080' {} \; -exec sed -i "s^8080^$TARGET_PORT^g" {} \;
#sed -i "s^8080^$TARGET_PORT^g" build.xml
sed -i "s^/scratch/emeij/old.jakarta^$CATALINA_HOME^g" build.xml
#java -classpath $SOPHIA_CP org.apache.tools.ant.launch.Launcher -v install > ant-output.txt 2>&1
if [ -d $CATALINA_HOME/webapps/search ];
  then $ANT_HOME/bin/ant remove clean install > ant-output.txt 2>&1
  else $ANT_HOME/bin/ant install > ant-output.txt 2>&1
fi
if grep -H -n BUILD `pwd`/ant-output.txt |grep "SUCCESSFUL"
then
  echo -n ""
else
  echo "BUILD FAILED:"
  cat `pwd`/ant-output.txt
  exit -1
fi
touch $CATALINA_HOME/webapps/axis/WEB-INF/web.xml
cd $AIDA_HOME

sleep 20
# Sophia
cd AIDA/Learning/Collocations/
cp $AIDA_HOME/build.properties .
$ANT_HOME/bin/ant install > ant-output.txt 2>&1
#ERROR dir names with jakarta-tomcat-5.5.9_base
if grep -H -n BUILD `pwd`/ant-output.txt |grep "SUCCESSFUL"
then
  echo -n ""
else
  echo "BUILD FAILED:"
  cat `pwd`/ant-output.txt
  exit -1
fi
touch $CATALINA_HOME/webapps/axis/WEB-INF/web.xml
cd $AIDA_HOME

sleep 20
# Sophia
cd AIDA/Learning/NERcrf/
cp $AIDA_HOME/build.properties .
$ANT_HOME/bin/ant install > ant-output.txt 2>&1
if grep -H -n BUILD `pwd`/ant-output.txt |grep "SUCCESSFUL"
then
  echo -n ""
else
  echo "BUILD FAILED:"
  cat `pwd`/ant-output.txt
  exit -1
fi
touch $CATALINA_HOME/webapps/axis/WEB-INF/web.xml
cd $AIDA_HOME

sleep 20
# Sophia
cd AIDA/Learning/RelationLearning/
cp $AIDA_HOME/build.properties .
$ANT_HOME/bin/ant remove install > ant-output.txt 2>&1
#ERROR dir names with jakarta-tomcat-5.5.9_base
if grep -H -n BUILD `pwd`/ant-output.txt |grep "SUCCESSFUL"
then
  echo -n ""
else
  echo "BUILD FAILED:"
  cat `pwd`/ant-output.txt
  exit -1
fi
touch $CATALINA_HOME/webapps/axis/WEB-INF/web.xml
cd $AIDA_HOME

sleep 20
cd AIDA/Learning/LearnModel
cp $AIDA_HOME/build.properties .
$ANT_HOME/bin/ant install > ant-output.txt 2>&1
#ERROR dir names with jakarta-tomcat-5.5.9_base
if grep -H -n BUILD `pwd`/ant-output.txt |grep "SUCCESSFUL"
then
  echo -n ""
else
  echo "BUILD FAILED:"
  cat `pwd`/ant-output.txt
  exit -1
fi
touch $CATALINA_HOME/webapps/axis/WEB-INF/web.xml
cd $AIDA_HOME

sleep 20
cd AIDA/Learning/NERecognizer
cp $AIDA_HOME/build.properties .
$ANT_HOME/bin/ant install > ant-output.txt 2>&1
#ERROR dir names with jakarta-tomcat-5.5.9_base
if grep -H -n BUILD `pwd`/ant-output.txt |grep "SUCCESSFUL"
then
  echo -n ""
else
  echo "BUILD FAILED:"
  cat `pwd`/ant-output.txt
  exit -1
fi
touch $CATALINA_HOME/webapps/axis/WEB-INF/web.xml
cd $AIDA_HOME

sleep 20
cd AIDA/Learning/TestModel
cp $AIDA_HOME/build.properties .
$ANT_HOME/bin/ant  install > ant-output.txt 2>&1
#ERROR dir names with jakarta-tomcat-5.5.9_base
if grep -H -n BUILD `pwd`/ant-output.txt |grep "SUCCESSFUL"
then
  echo -n ""
else
  echo "BUILD FAILED:"
  cat `pwd`/ant-output.txt
  exit -1
fi
touch $CATALINA_HOME/webapps/axis/WEB-INF/web.xml
cd $AIDA_HOME

find -name build.properties -exec rm {} \;
