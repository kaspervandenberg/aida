#!/bin/bash

source ~/aida.git/System/setEnvironment.sh

INDEXNAME=Zylab_test
INDEXCONFIG=${AIDA_HOME}/System/indexconfig.xml
TMP=~/tmp



INDEXERCLASSPATH=\
$(JARS=(\
${AIDA_HOME}/Search/Indexer/dist/Indexer.jar \
${AIDA_HOME}/Search/Indexer/lib/*.jar); \
 IFS=:; \
echo "${JARS[*]}")

PROMPT_MOVE="Move away & preserve" 
PROMPT_PURGE="Purge" 
PROMPT_KEEP="Keep"

INFORM_EXISTS="Index exists"
INFORM_NOTEXISTS="Index ${INDEXDIR}/${INDEXNAME} does not exist"
INFORM_MOVING="Moving index — ${INDEXNAME} — to ${TMP}"
INFORM_PURGE="Purging ${INDEXNAME}; use AIDA::Indexer to recreate it."
INFORM_KEEP="No changes made"
INFORM_RESTORE=\
"Restoring ${INDEXNAME} from ${TMP}; \\n\
moving it back to ${INDEXDIR}"
INFORM_RELOAD_DISAPPEAR=\
"Reload http://vocab.maastro.nl/search in webbrowser \\n\
and see ${INDEXNAME} not being there anymore."
INFORM_RELOAD_APPEAR=\
"Reload http://vocab.maastro.nl/search in webbrowser \\n\
and see ${INDEXNAME} is available."



function remove {
	if [ -e ${INDEXDIR}/${INDEXNAME} ]; then
		echo -e ${INFORM_EXISTS}
		select answer in "${PROMPT_MOVE}" "${PROMPT_PURGE}" "${PROMPT_KEEP}"; do
		case $answer in
			"${PROMPT_MOVE}")
				echo -e ${INFORM_MOVING}
				mv ${INDEXDIR}/${INDEXNAME} ${TMP}
				echo -e ${INFORM_RELOAD_DISAPPEAR}
				;;

			"${PROMPT_PURGE}")
				echo -e ${INFORM_PURGE}
				rm -rf ${INDEXDIR}/${INDEXNAME}
				echo -e ${INFORM_RELOAD_DISAPPEAR}
				;;
	
			*)
				echo ${INFORM_KEEP}
		esac
		break
		done

	else
		echo ${INFORM_NOTEXISTS}
	fi
}

function create {
	if [ ! -e ${INDEXDIR}/${INDEXNAME} ]; then
		if [ -e ${TMP}/${INDEXNAME} ]; then
			echo -e ${INFORM_RESTORE}
			mv ${TMP}/${INDEXNAME} ${INDEXDIR}/${INDEXNAME}
			echo -e ${INFORM_RELOAD_APPEAR}
		else
			java -cp "${INDEXERCLASSPATH}" indexer.Indexer "${INDEXCONFIG}"
			echo -e ${INFORM_RELOAD_APPEAR}
		fi
	else
		echo ${INFORM_EXISTS}
	fi
}

echo $0
if [[ "$0" == *"createPatientIndex.sh" ]]; then
	create
elif [[ "$0" == *"removePatientIndex.sh" ]]; then
	remove
else
	echo "Something is wrong with script name. Ask Kasper"
fi

# vim: set tabstop=4 shiftwidth=4 :
