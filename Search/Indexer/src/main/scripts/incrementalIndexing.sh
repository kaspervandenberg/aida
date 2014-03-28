#!/bin/bash

## Indexes files from {@code DATA_DIR} that where not indexed before

## Environment variables used:
## {@param DATA_DIR}
## \\t directory where all files to index are put by an other process
## \\t (e.g. by Zylab document scanning)
DATA_DIR=${DATA_DIR:-/mnt/zylab/XmlFields}

##
## {@param AIDA_VAR_BASE_DIR} 
## \\t Base directory, by default the staging directories are subdirectories of this
AIDA_VAR_BASE_DIR=${AIDA_VAR_BASE_DIR:-${INDEXDIR:-/var/local/aida/indexes}/..}

##
## {@param INDEXDIR}
## \\t Directory where indexer.Indexer stores the index named as configured in
## \\t {@code INDEXCONFIG_XML}.
#
# The mutual reference between AIDA_VAR_BASE_DIR and INDEXDIR might look like 
# a code smell, but this allows setting either one and the other being derived
# by default.
# Since incrementalIndexing.sh is designed to be executed via cron, we cannot 
# expect INDEXDIR to be set.
export INDEXDIR=${INDEXDIR:-${AIDA_VAR_BASE_DIR}/indexes}

##
## {@param INDEXCONFIG_XML}
## \\t Configuration used by {@code indexer.Indexer}
INDEXCONFIG_XML=${INDEXCONFIG_XML:-${AIDA_VAR_BASE_DIR}/conf/indexconfig.xml}

##
## {@param AIDA_SRC_DIR}
## \\t Directory containing the soruces to run the indexer from
AIDA_SRC_DIR=${AIDA_SRC_DIR:-/home/administrator/aida.git}

##
## {@param INDEX_STAGING_DIR}
## \\t Directory where this script copies the files to index
## \\t this should be the same path as configured in {@code INDEXCONFIG_XML}
INDEX_STAGING_DIR=${INDEX_STAGING_DIR:-${AIDA_VAR_BASE_DIR}/index_staging}

##
## {@param OLD_STAGING_PREFIX_DIR}
## \\t Prefix of temporary directory to where this script moves the 
## \\t previous staging directory
OLD_STAGING_PREFIX_DIR=${OLD_STAGING_PREFIX_DIR:-${AIDA_VAR_BASE_DIR}/previous-staging}

##
## {@param INDEXING_TIMESTAMP_DIR}
## \\t Directory containing timestamps of previous incremental indexing runs
INDEXING_TIMESTAMP_DIR=${INDEXING_TIMESTAMP_DIR:-${AIDA_VAR_BASE_DIR}/timestamps}


##
## {@param INDEXING_LOG_DIR}
## \\t Directory where output of the indexing goes
INDEXING_LOG_DIR=${INDEXING_LOG_DIR:-${AIDA_VAR_BASE_DIR}/log}

# end of configuration
DATE_PATTERN='+%Y%m%d_%H%M%S'
PROG=$0
LOG=${INDEXING_LOG_DIR}/$(date ${DATE_PATTERN})

main() {
	if echo "$@" | egrep -q -e '(-h)|(--help)'; then
		echo HELP
		doHelp
		exit
	fi

	initDirectories
	echo "Started indexing at $(date)" | tee -a ${LOG}
	movePreviousStaging
	createCurrentTimestamp
	stageFiles
	invokeIndexer
	echo "Finished indexing at $(date)" | tee -a ${LOG}
}


# Print a help message
# doHelp uses lines starting with ## to create the output
# the tags {@param ...} and {@code ...} colorize words
doHelp() {
	grep '^##' "${PROG}" | 
	sed -e 's/##//' \
		-e 's/{@param \(.*\)}/\\\\E[32;40m\1\\\\E[37;40m/' \
		-e 's/{@code \(.*\)}/\\\\E[36;40m\1\\\\E[37;40m/' |
	while read line; do
		echo -e "${line}";
	done
}


# Ensure directories that this script uses exist
initDirectories() {
	mkdir --parents ${INDEXING_LOG_DIR}
	mkdir --parents ${INDEX_STAGING_DIR}
	mkdir --parents ${OLD_STAGING_PREFIX_DIR}
	mkdir --parents ${INDEXING_TIMESTAMP_DIR}
}


# Move any existing staging directory out of the way
movePreviousStaging() {
	if [ -d ${INDEX_STAGING_DIR} ] && [ "$(find ${INDEX_STAGING_DIR} -maxdepth 0 -! -empty )" ]; then
		echo Moving \'${INDEX_STAGING_DIR}\' to \'$(getPreviousStagingDirName)\' | tee -a ${LOG}
		mv ${INDEX_STAGING_DIR} $(getPreviousStagingDirName)
	fi
}


# Copy files to index from ${DATA_DIR} to ${INDEX_STAGING_DIR}
stageFiles() {
	cd ${DATA_DIR}
	if isZylabXmlFieldsDir; then
		# optimize to use the hash bins in Zylabs XmlFields directory
		for hashbin in $(find . -maxdepth 1 -newer $(getPreviousTimestampFile)); do
			cd ${hashbin}
			stageAllFilesInCWD ${INDEX_STAGING_DIR}/${hashbin}
		done
	else
		stageAllFilesInCWD ${INDEX_STAGING_DIR}
	fi
}


invokeIndexer() {
	mvn -f ${AIDA_SRC_DIR}/Search/Indexer/pom.xml \
		compile exec:java \
		-Dexec.mainClass=indexer.Indexer -Dexec.args=${INDEXCONFIG_XML} |
	tee -a ${LOG}
}


stageAllFilesInCWD() {
	local TARGET="$1"
	while IFS= read -r -d '' line; do
		stageFile "${TARGET}" "${line}"
	done < <(find . -type f -newer $(getPreviousTimestampFile) -print0)
}

# Copy a single file to ${TARGET_DIR} when it doesn't exist in
# $(getPreviousStagingDirName)
#
# parameter $1 target (a.k.a. destination) directory
# parameter $2 file to copy
stageFile() {
	local TARGET_DIR="$1"
	local FILE="$2"

	if [ ! -e $(getPreviousStagingDirName)/"${FILE}" ]; then
		cp --parents --target-directory="${TARGET_DIR}" "${FILE}"
	fi
}

# Create a timestamp file with the current date and time
createCurrentTimestamp() {
	# previous timestamp must be determined BEFORE creating a new timestamp file
	getPreviousTimestampFile > /dev/null

	touch ${INDEXING_TIMESTAMP_DIR}/ts-$(date ${DATE_PATTERN})
}


# get the name of the directory where the previous staging files 
# are or will be stored
getPreviousStagingDirName() {
	if [ -z "${PREVIOUS_STAGING_DIR}" ]; then
		PREVIOUS_STAGING_DIR=${OLD_STAGING_PREFIX_DIR}/$(date --reference=$(getPreviousTimestampFile) ${DATE_PATTERN})
	fi
	echo ${PREVIOUS_STAGING_DIR}
}


# Find the most recent existing timestamp in ${INDEXING_TIMESTAMP_DIR}
getPreviousTimestampFile() {
	if [ -z ${PREVIOUS_TIMESTAMP} ]; then
		if [ -d ${INDEXING_TIMESTAMP_DIR} ] && [ "$(find ${INDEXING_TIMESTAMP_DIR} -maxdepth 0 -! -empty)" ]; then
			PREVIOUS_TIMESTAMP=${INDEXING_TIMESTAMP_DIR}/$(ls --sort=time -1 ${INDEXING_TIMESTAMP_DIR} | head --lines=1)
		else
			touch -t 197001010000 ${INDEXING_TIMESTAMP_DIR}/oldestTime
			PREVIOUS_TIMESTAMP=${INDEXING_TIMESTAMP_DIR}/oldestTime
		fi
	fi
	echo ${PREVIOUS_TIMESTAMP}
}


# Check whether $1 looks like Zylabs XmlFields directory
isZylabXmlFieldsDir() {
	if [ -z ${IS_ZYLAB_XML_FIELDS_DIR} ]; then
		if [ -d ${DATA_DIR} ] &&
			[ "$(find ${DATA_DIR} -maxdepth 0 -! -empty)" ] &&
			[ $(echo ${DATA_DIR} | grep -q 'XmlFields') ] &&
			[ "$(ls ${DATA_DIR})" == "$(ls ${DATA_DIR} | grep -e '^[0-9A-F][0-9A-F]$')" ];
		then
			IS_ZYLAB_XML_FIELDS_DIR=0
		else
			IS_ZYLAB_XML_FIELDS_DIR=1
		fi
	fi
	return ${IS_ZYLAB_XML_FIELDS_DIR}
}


main $@


# vim: set tabstop=4 shiftwidth=4 : #

