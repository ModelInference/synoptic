#!/bin/sh
#
# Documents Synoptic's usage screen (generated with the -H option) to
# a wiki page -- 'DocsUsageScreen':
# http://code.google.com/p/synoptic/wiki/DocsUsageScreen
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
 echo "       wiki-path : path of local working copy of the synoptic wiki Hg repository"
 exit
fi 

# Assumes that the current dir is the synoptic repo.
synoptic_repo=`pwd`

# First argument is the location of the Synoptic wiki repo.
synoptic_wiki_repo=$1

# Location of the usage screen wiki page.
usagef=${synoptic_wiki_repo}/DocsCmdLineHelpScreen.wiki

# 1. Pull and update the wiki repository
cd ${synoptic_wiki_repo} && hg pull && hg up

# 2. Wipe out the old usage wiki page.
cd ${synoptic_wiki_repo} && rm $usagef

# 3. Generate the new wiki page
echo "#summary Lists the command line usage screen" >> $usagef
echo "" >> $usagef
echo "= Command Line Options =" >> $usagef
echo "{{{" >> $usagef
cd ${synoptic_repo} && ./synoptic.sh -H >> $usagef
echo "}}}" >> $usagef
echo >> $usagef

# 4. Determine the current revision for the synoptic repository
# (assumed to be the current dir)
echo "As of revision \c" >> $usagef
cd ${synoptic_repo} && hg tip --template "{node|short}" >> $usagef
echo >> $usagef
echo >> $usagef

# 5. Determine the synoptic version by running synoptic.sh (assuming
# the current dir is the synoptic repo).
cd ${synoptic_repo} && ./synoptic.sh -V >> $usagef
echo >> $usagef
echo "*Note: this page is auto-generated. Do not edit.*" >> $usagef

# 6. Commit and push the new wiki page.
cd ${synoptic_wiki_repo} && hg commit -m 'Updated usage screen docs' $usagef && hg push


