#!/bin/bash

for i in $(seq 1 5); do
  python src/gen_peterson_trace.py 5 0 $i > generated_traces/peterson_trace-rounds-0-s$i.txt
done
