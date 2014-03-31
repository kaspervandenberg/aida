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
## {#param PASSWORD_OPTION_FILE}
## \\t a file containing password options, if it exists its contents is 
## \\t passed as commandline arguments to {@code ZPSC_JAR}
PASSWORD_OPTION_FILE=${PASSWORD_OPTION_FILE:-${HOME}/emdPassword}

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
	invokeZylabPatisClient "$@"
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


invokeZylabPatisClient() {
	if [ -n ${PASSWORD_OPTION_FILE} ] && 
			[ -f ${PASSWORD_OPTION_FILE} ] &&
			[ -r ${PASSWORD_OPTION_FILE} ]; then
		xargs --arg-file=${PASSWORD_OPTION_FILE} java -jar ${ZPSC_JAR} "$@" 2>&1 | tee -a ${LOG}
	else
		java -jar ${ZPSC_JAR} "$@" 2>&1 | tee -a ${LOG}
	fi
}


symlinkLatest() {
	if [ -d ${REPORT_DIR} ] && [ "$(find ${REPORT_DIR} -maxdepth 0 -! -empty)"]; then
		local LATEST=$( \
				ls --sort=time -1 ${REPORT_DIR}/results*.html |
				head --lines=1)
		if [ -n ${LATEST} ]; then
			echo "Moving symlink ${LATEST_REPORT_SYMLINK_TARGET} to point to ${LATEST}" | tee ${LOG}
			ln --force --symbolic ${LATEST} ${LATEST_REPORT_SYMLINK_TARGET} 2>&1 | tee ${LOG}
		else
			echo "WARNING: no latest results; continuing without moving symlink" | tee ${LOG}
		fi
	else
		echo "WARNING: Directory ${REPORT_DIR} does not exist or is empty; continuing without moving symlink." | tee ${LOG}
	fi
}

main "$@"

# vim: set tabstop=4 shiftwidth=4 : #


