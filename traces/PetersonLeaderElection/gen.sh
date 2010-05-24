#!/bin/bash

for i in $(seq 1 150); do
  python src/gen_peterson_trace.py 5 1 $i > generated_traces/peterson_trace-n5-1-s$i.txt
done
