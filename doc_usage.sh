#!/bin/sh
#
# Documents Synoptic's usage screen (generated with the -H option) to
# a wiki page -- 'DocsUsageScreen'
#
# This script must be run manually, and will only work if executed
# from within trunk, i.e. /svn/trunk/
#
# Preferably this script will be run whenever synoptic command
# line options change.
#

# Location of the usage screen wiki page.
usagef=../wiki/DocsUsageScreen.wiki

# Wipe out the old wiki page content.
echo > $usagef

# Generate the new wiki page.
echo "= Command Line Options =" >> $usagef
echo "{{{" >> $usagef
./synoptic.sh -H >> $usagef
echo "}}}" >> $usagef
echo >> $usagef

echo "Last Updated r\c" >> $usagef
rev=`cd ../wiki/ && svn up | grep 'At revision' | awk '{print $3}' | tr -d '.'`
echo $(( rev+1 )) >> $usagef

syn_vers=`./synoptic.sh -V`
echo ${syn_vers} >> $usagef
echo >> $usagef
echo "*Note: this page is auto-generated. Do not edit.*" >> $usagef

# Commit the new wiki page.
cd ../wiki/ && svn commit -m 'Updated usage screen docs'
