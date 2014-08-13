#!/bin/sh
#performs timing on the specified test-class and test-method by running
#the test 10 times and calculating the average time taken.
#Run with arguments: full.path.to.class testMethod

echo "Running 10 tests from class: $1, method: $2...";

total=0;

for i in $(seq 10)
do
 var=$(ant -Djunit.class=$1 -Djunit.case=$2 test-case | grep 'elapsed' | awk '{print $13}');
 echo "$var seconds";
 total=$(echo "$total+$var" | bc);
 sleep 5; #sleep for 5 seconds to let unterminated processes terminate
done;

average=$(echo "scale=3;$total/10" | bc);
echo "Average is: $average seconds";

