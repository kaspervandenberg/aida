#!/bin/bash

## Generates reports about patient's trial eligibility status

##
## {@param AIDA_VAR_BASE_DIR} 
## \\t Base directory, by default the log directory and index directory are subdirectories of this
AIDA_VAR_BASE_DIR=${AIDA_VAR_BASE_DIR:-${INDEXDIR:-/var/local/aida/indexes}/..}

##
## {@param INDEXDIR}
## \\t Directory where indexer.Indexer stores the index named as configured in
## \\t {@code zpsc-config.xml}.
#
# The mutual reference between AIDA_VAR_BASE_DIR and INDEXDIR might look like 
# a code smell, but this allows setting either one and the other being derived
# by default.
# Since incrementalIndexing.sh is designed to be executed via cron, we cannot 
# expect INDEXDIR to be set.
export INDEXDIR=${INDEXDIR:-${AIDA_VAR_BASE_DIR}/indexes}

##
## {@param REPORT_LOG_DIR}
## \\t Directory where log output of generating the report goes
REPORT_LOG_DIR=${REPORT_LOG_DIR:-${AIDA_VAR_BASE_DIR}/log}

##
## {@param ZPSC_JAR}
## \\t Jar to invoke to generate eligibility report
ZPSC_JAR=${ZPSC_JAR:-/opt/aida/bin/zylabPatisClient.jar}

##
## {@param REPORT_DIR}
## \\t directory where reports are written to.
## \\t must be equal to the one configured in META_INF/beans.xml in the jar.
REPORT_DIR=${REPORT_DIR:-/var/www/zpsc}

##
## {@param LATEST_REPORT_SYMLINK_TARGET}
## \\t name of generated symlink to latest report
LATEST_REPORT_SYMLINK_TARGET=${LATEST_REPORT_SYMLINK_TARGET:-${REPORT_DIR}/LATEST}


# end of configuration
DATE_PATTERN='+%Y%m%d_%H%M%S'
PROG=$0
LOG=${REPORT_LOG_DIR}/report-$(date ${DATE_PATTERN})


main() {
	if echo "$@" | egrep -q -e '(-h)|(--help)'; then
		doHelp
		exit
	fi

	initDirectories
	echo "Started report generation at $(date)" | tee -a ${LOG}
	java -jar ${ZPSC_JAR} "$@" | tee -a ${LOG}
	echo "Finished report generation at $(date)" | tee -a ${LOG}
}



# Print a help message
# doHelp uses lines starting with ## to create the output
# the tags {@param ...} and {@code ...} colorize words
doHelp() {
	grep '^##' "${PROG}" | 
	sed -e 's/##//' \
		-e 's/{@param \([^}]*\)}/\\\\E[32;40m\1\\\\E[37;40m/g' \
		-e 's/{@code \([^}]*\)}/\\\\E[36;40m\1\\\\E[37;40m/g' |
	while read line; do
		echo -e "${line}";
	done
	java -jar $ZPSC_JAR --help
}


initDirectories() {
	mkdir --parents ${REPORT_LOG_DIR}
}


symlinkLatest() {
	if [ -d ${REPORT_DIR} ] && [ "$(find ${REPORT_DIR} -maxdepth 0 -! -empty)"]; then
		local LATEST=$( \
				ls --sort-time -1 ${REPORT_DIR}/results*.html |
				head --lines=1)
		if [ -n ${LATEST} ]; then
			ln --force --symbolic ${LATEST} ${LATEST_REPORT_SYMLINK_TARGET}
		fi
	fi
}

main "$@"

# vim: set tabstop=4 shiftwidth=4 : #


