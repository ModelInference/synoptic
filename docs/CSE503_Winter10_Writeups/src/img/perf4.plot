set terminal postscript enhanced eps color
set output "perf4.eps"
set ylabel "Time (ms)"
set xlabel "Total number of messages M"
set key top right

set size 0.6,0.35

set xtics 100

plot [0:][0:110] \
"perf_bikon_noinv_var_M.data" using 1:2 with lines lw 2 title "Bikon"
