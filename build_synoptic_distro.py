#!/usr/bin/python

'''
Purpose:
=========

This script puts together a self-contained distribution of
command-line synoptic. The resulting distribution does not include a
GWT/Jung interface.



Usage:
=======

- This script must be run from within the top level synoptic directory.

- This script packages synoptic from scratch, it:

  1. Runs 'ant clean' within synoptic/ project dir

  2. Runs 'ant jar' to build the jar file from synoptic sources

  3. Creates a directory named synoptic-${version}/ (e.g.,
     synoptic-0.0.5/) which will contains the self-contained
     distribution.
  
  4. Copies over all the jars into the distro dir

  5. Copies over the traces that are necessary for end-to-end tests
     into the distro dir

  6. Packages the distro dir into synoptic-${version}.tar.gz
'''


import sys
import os
import subprocess


def get_cmd_output(cmd, args):
    '''
    Returns the standard output from running:
    $ cmd args[0] args[1] .. args[n]

    Where cmd is the command name (e.g., 'svn') and args is a list of
    arguments to the command (e.g., ['help', 'log']).
    '''
    return subprocess.Popen([cmd] + args,
                            stdout=subprocess.PIPE, 
                            stderr=subprocess.STDOUT).communicate()[0]


def runcmd(s):
    '''
    Logs and runs a shell command.
    '''
    print "os.system: " + s
    os.system(s)
    

def main():
    '''
    Workhorse method to execute all the of the steps described in the file header.
    '''
    
    # Run ant clean.
    runcmd("cd synoptic/ && ant clean")

    # Remove a previously generated synoptic.jar, if it exists.
    if (os.path.exists("./lib/synoptic.jar")):
        runcmd("rm ./lib/synoptic.jar")

    # Run ant jar to generate a fresh synoptic.jar
    runcmd("cd synoptic/ && ant jar")

    # Check that ant jar created synoptic.jar
    if (not os.path.exists("./lib/synoptic.jar")):
        print "Error: ant jar failed to create synoptic.jar"
        sys.exit(-1)

    # Extract META-INF to find out the version and jar deps
    runcmd("jar -xvf lib/synoptic.jar META-INF/MANIFEST.MF");

    # Read MANIFEST.MF and lightly parse it
    f = open("./META-INF/MANIFEST.MF", "r")
    lines = f.readlines()
    jardeps = None
    synoptic_version = None
    for line in lines:
        line = line.strip()
        if line.startswith("Class-Path:"):
            pieces = line.split(" ")
            jardeps = pieces[1:]
        if line.startswith("Implementation-Version:"):
            synoptic_version = line.split(" ")[1]
    f.close()

    if jardeps == None or synoptic_version == None:
        print "Error: could not extract jardeps or synoptic version from synoptic.jar MANIFEST.MF"
        sys.exit(-1)

    # Create the distro dir
    dist_dir = "./synoptic-" + synoptic_version + "/"
    runcmd("rm -Rf " + dist_dir + " &&  mkdir " + dist_dir)

    # Create the lib/ sub-dir inside of the distro dir
    runcmd("mkdir " + dist_dir + "lib/")
    
    # Copy over the jar dependencies.
    for jardep in jardeps:
        runcmd("cp lib/" + jardep + " " + dist_dir + "lib/")

    # Copy over the synoptic.jar file
    runcmd("cp lib/synoptic.jar " + dist_dir + "lib/")

    # Copy the example traces into the distro dir
    output = get_cmd_output('/usr/bin/find', ['-L', './traces/EndToEndTests', '-name', '*.txt', '-print'])
    output = output.strip()
    for line in output.split("\n"):
        splits = line.split("/")
        trace_dir = dist_dir + "/".join(splits[1:-1])
        runcmd("mkdir -p " + trace_dir)
        runcmd("cp " + line + " " + trace_dir)

    # Copy other, non-essential files and scripts into the distro dir.
    runcmd("cp synoptic-jar.sh " + dist_dir)
    runcmd("cp synoptic-jar.bat " + dist_dir)
    runcmd("cp RELEASE-README.txt " + dist_dir + "README.txt")

    # Remove the extracted META-INF
    runcmd("rm -Rf ./META-INF")
        
    return



if __name__ == "__main__":
    main()
