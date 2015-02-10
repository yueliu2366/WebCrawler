#!/bin/sh
#Program: This is the program of cs5700 project 1
# 2014/01/15

port=27993
host=""
nuID=""

if [ "$1" = "-p" ];then
shift
port=$1
shift
if [ "$1" = "-s" ];then
shift
host=$1
nuID=$2
else
host=$1
shift
nuID=$1
fi
elif [ "$1" = "-s" ];then
shift
host=$1
nuID=$2
else
host=$1
shift
nuID=$1
fi

echo $host $nuID
if [ -z $host ] || [ -z $nuID ];then
echo "host or nuID is empty"
exit 1
fi

javac Client.java
java Client $host $port $nuID

