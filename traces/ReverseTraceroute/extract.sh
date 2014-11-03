#!/bin/bash

for file in `ls rt_raw`; do
  python parse_rt.py rt_raw/$file > rt_parsed_rich/$file
done
