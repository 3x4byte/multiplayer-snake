#!/bin/bash

javaClasspath=$1
javaMainClass=$2
javascriptServerPath=$3

( java -cp "$javaClasspath" $javaMainClass ) &
( node  $javascriptServerPath )  &
