#!/bin/bash

javaClasspath=$1
javaMainClass=$2
javascriptServerPath=$3

echo "first"
echo $1
echo "second"
echo $2
echo "third"
echo $3

( java -cp "$javaClasspath" $javaMainClass ) &
( node  $javascriptServerPath )  &

