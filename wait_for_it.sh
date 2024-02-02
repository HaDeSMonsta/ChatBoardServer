#!/bin/sh

cmd="$@"
sleepTime=5

echo "Sleeping for $sleepTime seconds..."
sleep $sleepTime

echo "Running command: $cmd"
exec $cmd