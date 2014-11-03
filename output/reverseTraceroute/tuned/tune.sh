#!/bin/bash

cat $1 | grep -v '/\*' | sed 's/, weight.......//' | sed 's/ .+ seems to be//g' | sed 's/.+ for traceroute/for TR/g' | sed 's/Rev Seg is/Rev Seg found/g' | sed 's/No adjacents to/No Adjacents found/g' | sed 's/*/\\n/g' > up$1
#sed 's/Issuing//g' | 
dot -Tpng up$1 -O
