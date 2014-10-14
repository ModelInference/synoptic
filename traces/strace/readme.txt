Strace is a utility that traces syscalls and signals. In this directory
are 2 experiments using strace including times of the syscalls so that
the traces can be used with pynoptic or base synoptic.

The first log is from a run of "ls". The 3 traces were running "ls" on
the same directory structure: one on a HDD with the NTFS filesystem,
another on a SSD with the NTFS filesystem, and the last on a SSD with
the ext4 filesystem. There are 3 versions of this log: ls.txt, ls_io.txt
(include only i/o syscalls), and ls_io_slim.txt (include only some very
concrete i/o syscalls).

The second log is from a run of "ping". The 3 traces were running "ping"
to google.com from the same machine, the first over a cellular data
connection, the second over a university connection, and the third over
the same university connection but pinging one of Google's IP addresses
directly without DNS involved. There are 3 versions of this log:
ping.txt, ping_net.txt (include only network syscalls), and
ping_net_slim.txt (include only some very concrete network syscalls).


To test in Eclipse, run with program arguments:
-p -o ../output/strace -c ../traces/strace/args.txt ../traces/strace/FILE.txt

(where FILE = {ls,ls_io,ls_io_slim,ping,ping_net,ping_net_slim})

To run in terminal, cd to root synoptic/ directory, and run:
synoptic.sh -p ../output/strace -c ../traces/strace/args.txt ../traces/strace/FILE.txt


The filtered versions of the logs were created using grep with arguments:

I/O:
-e access -e open -e fstat -e close -e read -e statfs -e ioctl -e openat -e getdents -e write -e poll -e stat -e fcntl

NETWORK:
-e socket -e connect -e setsockopt -e getsockopt -e getsockname -e sendmsg -e sendto -e recvmsg -e recvfrom
