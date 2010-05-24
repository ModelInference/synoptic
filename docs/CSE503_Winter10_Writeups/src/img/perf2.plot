set terminal postscript enhanced eps color
set output "perf2.eps"
set ylabel "Time (ms)"
set xlabel "Total number of messages M"
set key top left

set size 0.6,0.5

set xtics 100

plot [0:][0:6500] \
"perf_gk_scalable_inv_var_M.data" using 1:2 with lines lw 2 title "Scalable GK-Tail with Inv", \
"perf_gk_trivial_noinv_var_M.data" using 1:2 with lines lw 2 lt 3 title "Trivial GK-Tail", \
"perf_gk_scalable_noinv_var_M.data" using 1:2 with lines lw 2 lt 4 title "Scalable GK-Tail"


