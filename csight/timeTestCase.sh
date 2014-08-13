#!/bin/sh 

# Times a specified test method inside a class by running the test 10
# times and computing the average runtime.
#
# Usage:
# ./timeTestCase.sh class-path method
#
# Example:
# ./timeTestCase.sh csight.main.CSightMainTests runSimpleConcurrencyString2SuccessParallel

echo "Running class[$1] method[$2] 10 times...";

total=0;

for i in $(seq 10)
do
    # Use ant to actually invoke the test-case.
    var=$(ant -Djunit.class=$1 -Djunit.case=$2 test-case | grep 'elapsed' | awk '{print $13}');
    echo "run $i: $var seconds";
    total=$(echo "$total+$var" | bc);
    # Sleep for 5 seconds to let processes terminate.
    sleep 5; 
done;

average=$(echo "scale=3;$total/10" | bc);
echo "Average rutime is: $average seconds";

