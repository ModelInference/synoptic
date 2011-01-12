#!/bin/sh
#
# Documents Synoptic's usage screen (generated with the -H option) to
# a wiki page -- 'DocsUsageScreen'
#
# This command must be run manually, and preferably whenever program
# options change.
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

# Commit the new wiki page.
cd ../wiki/ && svn commit -m 'Updated usage screen docs'
