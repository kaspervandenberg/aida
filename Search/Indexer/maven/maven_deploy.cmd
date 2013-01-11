mvn deploy:deploy-file -DgroupId=org.vle.aid \
        -DartifactId=indexer \
        -Dversion=0.65 \
        -Dpackaging=jar \
        -Dfile=/home/ilps/AID/AIDA/Search/Indexer/dist/Indexer.jar \
        -DrepositoryId=AID \
        -Durl=file:///home/emeij/public_html/maven

mvn deploy:deploy-file -DgroupId=org.vle.aid \
        -DartifactId=jargon \
        -Dversion=1.4.20 \
        -Dpackaging=jar \
        -Dfile=/home/emeij/Java/AID/AIDA/Search/Indexer/lib/jargon_v1.4.20.jar \
        -DrepositoryId=AID \
        -Durl=file:///home/emeij/public_html/maven

mvn deploy:deploy-file -DgroupId=org.vle.aid \
        -DartifactId=lucene-core \
        -Dversion=2.1.0 \
        -Dpackaging=jar \
        -Dfile=/home/emeij/Java/AID/AIDA/Search/Indexer/lib/lucene-core-2.1.0.jar \
        -DrepositoryId=AID \
        -Durl=file:///home/emeij/public_html/maven

mvn deploy:deploy-file -DgroupId=org.vle.aid \
        -DartifactId=jsr \
        -Dversion=1.7.3 \
        -Dpackaging=jar \
        -Dfile=/home/emeij/Java/AID/AIDA/Search/Indexer/lib/jsr173_api.jar \
        -DrepositoryId=AID \
        -Durl=file:///home/emeij/public_html/maven

mvn deploy:deploy-file -DgroupId=org.vle.aid \
        -DartifactId=activation \
        -Dversion=1.0 \
        -Dpackaging=jar \
        -Dfile=/home/emeij/Java/AID/AIDA/Search/Indexer/lib/activation.jar \
        -DrepositoryId=AID \
        -Durl=file:///home/emeij/public_html/maven

mvn deploy:deploy-file -DgroupId=org.vle.aid \
        -DartifactId=jaxb-impl \
        -Dversion=2.0 \
        -Dpackaging=jar \
        -Dfile=/home/emeij/Java/AID/AIDA/Search/Indexer/lib/jaxb-impl.jar \
        -DrepositoryId=AID \
        -Durl=file:///home/emeij/public_html/maven

mvn deploy:deploy-file -DgroupId=org.vle.aid \
        -DartifactId=jaxb-api \
        -Dversion=2.0 \
        -Dpackaging=jar \
        -Dfile=/home/emeij/Java/AID/AIDA/Search/Indexer/lib/jaxb-api.jar \
        -DrepositoryId=AID \
        -Durl=file:///home/emeij/public_html/maven

mvn deploy:deploy-file -DgroupId=org.vle.aid \
        -DartifactId=jaxb-xjc \
        -Dversion=2.0 \
        -Dpackaging=jar \
        -Dfile=/home/emeij/Java/AID/AIDA/Search/Indexer/lib/jaxb-xjc.jar \
        -DrepositoryId=AID \
        -Durl=file:///home/emeij/public_html/maven



