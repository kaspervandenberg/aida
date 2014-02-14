#!/bin/bash

# Script that invokes filename_lookup via the mvn exec plugin
# This script assumes by default the configuration of clinisearch.ad.maastro.nl:
# 	you can select other maven profiles (defined in 'pom.xml') 
#	via the environment variable 'PROFILE' .

# Useage ./filename-lookup.sh {patisnr}...
# alternative: PROFILE={profile} ./filename-lookup.sh {patisnr}...


FILELOOKUP_CLASS=nl.maastro.eureca.aida.search.filename_lookup.Main
PROFILE=${PROFILE:-maastro_clinisearch}

mvn -P ${PROFILE} \
	compile --also-make \
	exec:java \
		-Dexec.mainClass=${FILELOOKUP_CLASS} \
		-Dexec.args="$*"

