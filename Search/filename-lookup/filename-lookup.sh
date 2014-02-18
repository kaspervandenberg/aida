#!/bin/bash

# Script that invokes filename_lookup via the mvn exec plugin
# This script assumes by default the configuration of clinisearch.ad.maastro.nl:
# 	you can select other maven profiles (defined in 'pom.xml') 
#	via the environment variable 'PROFILE' .

# Useage ./filename-lookup.sh {patisnr}...
# alternative: PROFILE={profile} ./filename-lookup.sh {patisnr}...


FILELOOKUP_CLASS=nl.maastro.eureca.aida.search.filename_lookup.Main
PROFILE=${PROFILE:-maastro_clinisearch}

ZYLAB_MOUNT=${ZYLAB_MOUNT:-/mnt/zylab}
INCORRECT_DATA_PATH=file:/media/medical//

QUIET_MAVEN=${QUIET_MAVEN:-yes}
if [ "${QUIET_MAVEN}" == "yes" ]; then
	QUIET_OPT=-q
else
	QUIET_OPT=""
fi

mvn ${QUIET_OPT} -P ${PROFILE} \
	compile --also-make \
	exec:java \
		-Dexec.mainClass=${FILELOOKUP_CLASS} \
		-Dexec.args="$*" \
| \
sed -e "s,${INCORRECT_DATA_PATH},${ZYLAB_MOUNT}/,"


# vim:set tabstop=4 shiftwidth=4 :

