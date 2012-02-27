#!/bin/bash

for i in $(seq 1 10); do
  python src/gen_stenning_trace.py 10 0.5 0.0 $i > generated_traces/t-10-0.5-0-s$i.txt
done
