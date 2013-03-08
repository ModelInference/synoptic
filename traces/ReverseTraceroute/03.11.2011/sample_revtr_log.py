import sys

def extract_recs(num_wanted, fname_in, fname_out):
    '''
    num_wanted : number of unique IP addresses to extract from the log
    fname_in : input filename to grab records from
    fname_out : output filename to write the sample to
    '''

    print "Extracting with:"
    print "\tnum_wanted = " + str(num_wanted)
    print "\tfname_in = " + fname_in
    print "\tfname_out = " + fname_out

    # Mumber recorded to extract so far.
    num = 0
    observed_ips = []

    # Open input/output files.
    fin = open(fname_in, 'r')
    fout = open(fname_out, 'w')

    # First, determine the first num_wanted IPs that we can extract.
    while True:
        line = fin.readline()
        if not line:
            return
        
        revtr_src = line.split(" ")[3]
        #print line
        #print revtr_src
        fout.write(line)
        if (not revtr_src in observed_ips):
            observed_ips.append(revtr_src)
            num += 1
            if (num == num_wanted):
                break

    # Second, go through the rest of the file, continuing to extract
    # the IPs we've decided on above.
    while True:
        line = fin.readline()
        if not line:
            return
        revtr_src = line.split(" ")[3]
        if (revtr_src in observed_ips):
            fout.write(line)
    return


if __name__ == "__main__":
    if (len(sys.argv) != 4):
        usage = "Usage: " + sys.argv[0] + " [num_IPs_to_extract] [filename_in] [filename_out]"
        usage += "\n"
        usage += "Example:\n"
        usage += "\t" + sys.argv[0] + " 2 revtr-log.10k.sample revtr-log.2rec.sample"
        print usage
    else:
        num_wanted = int(sys.argv[1])
        fname_in = sys.argv[2]
        fname_out = sys.argv[3]
        extract_recs(num_wanted, fname_in, fname_out)
        print "Done."
