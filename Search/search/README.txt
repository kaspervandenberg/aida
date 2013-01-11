Readme file for VL-e search interface (Query Construction Kit)

1. Prerequisites
First, install Tomcat (tested with 5.0.28) and Axis (tested with 1.3 Final) (and Ant, tested with version 1.6.5). 
The search interface has been tested on Linux, as well as Windows XP (Professional SP2).

2. Installation
Edit build.xml, specifically your configuration-specific parameters (located at 
the top), such as the location of your Tomcat files (catalina.home), the port 
number to connect to and the login credentials of the server administrator.

2.1 The Environment
The interface, as well as the search-related webservices, use the 
environment variable INDEXDIR to find the directory, containing all directories 
with Lucene indexes in them. E.g.

/INDEXDIR
/INDEXDIR/LUCENE_INDEX_1
/INDEXDIR/LUCENE_INDEX_2
/INDEXDIR/LUCENE_INDEX_3
etc...

When (re)starting tomcat, make sure you have the INDEXDIR environment variable 
set. This differs per OS, but for example for unix/linux (bash):

export INDEXDIR=[PATH__TO__INDEXDIR]

Windows:

set INDEXDIR=[PATH__TO__INDEXDIR]

The easiest is ofcourse to modify catalina.(bat|sh), so starting Tomcat always 
gets the correct environment variable. 

2.2 The actual installation
Then, to install, type 'ant install' in the /search/ folder. 

3. Updating or removing the interface
To remove the application, type 'ant remove', followed by 'ant clean'. When any 
sourcecode has been edited, type 'ant run' (or 'ant remove clean install') 
to reload. 

4. Additional Tasks
There are two additional targets defined in build.xml, to acquire synonyms and 
spelling suggestions offline. Use the following syntax:

4.1 Spelling checker (spellCheck) 
To build a language model for the spelling checker for a Lucene index:

ant createModel -Dindex.location=<PATH_TO_LUCENE_INDEX> -Dindex.field=<LUCENE_FIELD_TO_SCAN>

The compiled language model file will be placed in a subdirectory 'spellCheck', 
relative to <PATH_TO_LUCENE_INDEX>.

4.2 Synonym service
To extract acronyms from a Lucene index:

ant UpdateSynonyms -Dindex.location=<PATH_TO_LUCENE_INDEX>

The stored synonym files will be placed in a subdirectory 'synonyms', 
relative to <PATH_TO_LUCENE_INDEX>.

5. Further assistance
If you have any questions, errors or bugs, feel free to e-mail: emeij@science.uva.nl
