set terminal postscript enhanced eps color
set output "perf3.eps"
set ylabel "Time (ms)"
set xlabel "Number of partitions n"
set key top right

set size 0.6,0.35

set xtics 1

plot [0:][0:110] \
"perf_bikon_noinv_var_n.data" using 1:2 with lines lw 2 title "Bikon"

