#!/bin/bash
#
# Documents usage screens (generated with the -H option) for all of
# the projects hosted by the repo --- Synoptic, CSight,
# InvariMint. These are written to wiki pages (e.g.,
# 'DocsSynopticCmdLineHelpScreen') and can be found online, e.g.,:
# http://code.google.com/p/synoptic/wiki/DocsSynopticCmdLineHelpScreen
#
# This script must be run manually, and will only work if executed
# from within the default synoptic code branch. This script assume
# that the working copy is at the tip.
#
# Preferably this script will be run whenever synoptic command
# line options change.
#

if [ -z "$1" ]
then
 echo "Usage: ./doc_usage.sh [wiki-path]"
 echo "       wiki-path : path of local working copy of the wiki hg repository"
 exit
fi 

# Assumes that the current dir is the synoptic repo.
code_repo=`pwd`

# First argument is the location of the Synoptic wiki repo.
wiki_repo=$1

# 1. Pull and update the wiki repository
cd ${wiki_repo} && hg pull && hg up


###########################################################
function gen_wiki_page() {
    local usagef=$1
    local cmd=$2
    local prj=$3
    local escapeName=$4

    # 2. Wipe out the old usage wiki page.
    cd ${wiki_repo} && rm $usagef

    # 3. Generate the new wiki page
    echo "#summary Lists the $prj command line usage screen" >> $usagef
    echo "" >> $usagef
    if [ "$escapeName" = true ]; then
	echo "= !$prj Command Line Options =" >> $usagef
    else
	echo "= $prj Command Line Options =" >> $usagef
    fi
    echo "{{{" >> $usagef
    cd ${code_repo} && $cmd >> $usagef
    echo "}}}" >> $usagef
    echo >> $usagef

    # 4. Determine the current revision for the synoptic repository
    # (assumed to be the current dir)
    echo "As of revision: " >> $usagef
    cd ${code_repo} && hg tip --template "{node|short}" >> $usagef
    echo >> $usagef
    echo >> $usagef

    echo "*Note: this page is auto-generated. Do not edit.*" >> $usagef
}
###########################################################

# Location of the usage screen wiki pages.
syn_usagef=${wiki_repo}/DocsSynopticCmdLineHelpScreen.wiki
dyn_usagef=${wiki_repo}/DocsCSightCmdLineHelpScreen.wiki
invmint_usagef=${wiki_repo}/DocsInvariMintCmdLineHelpScreen.wiki

############################## Synoptic usage
# Generate the synoptic usage:
gen_wiki_page $syn_usagef "./synoptic.sh -H" "Synoptic" false;

############################## CSight usage
# Generate the csight usage:
gen_wiki_page $dyn_usagef "./csight.sh -H" "CSight" false;

############################## InvariMint usage
# Generate the invarimint usage:
gen_wiki_page $invmint_usagef "./invarimint.sh -H" "InvariMint" true;


# 5. (An extra step for synoptic) Determine the synoptic version by
# running synoptic.sh (assuming the current dir is the synoptic repo).
# cd ${code_repo} && ./synoptic.sh -V >> $syn_usagef
# echo >> $syn_usagef


# 6. Commit and push the edited wiki pages
cd ${wiki_repo} && hg commit -m 'Updated usage screen docs for all projects' $syn_usagef $dyn_usagef $invmint_usagef && hg push


