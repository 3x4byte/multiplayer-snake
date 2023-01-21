#!/bin/bash

javaClasspath=$1
javaMainClass=$2
javascriptServerPath=$3

( java -cp "$javaClasspath" $javaMainClass ) &
( node  $javascriptServerPath )  &

sleep 3
echo $(pgrep -f "java -cp $javaClasspath $javaMainClass") > process_ids.txt
echo $(pgrep -f "node $javascriptServerPath") >> process_ids.txt
