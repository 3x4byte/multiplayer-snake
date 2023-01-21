#!/bin/bash

# read the process IDs from the file
while read pid; do
  # check if the process is still running
  if kill -0 $pid 2>/dev/null; then
    # if it is, send the kill signal
    echo "Killing process $pid"
    kill $pid
  else
    echo "Process $pid is not running"
  fi
done < process_ids.txt
# delete the files contents
> process_ids.txt
