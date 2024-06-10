#!/bin/bash
source /etc/profile

# create directory if not exist
if [ ! -d "/app/service-channelunfiy" ];then
  mkdir -p /app/service-channelunfiy
fi

# clear debug log
echo Clearing debug.log ...
rm -rf logs
mkdir -p logs/ipower-channelunfiy
touch logs/ipower-channelunfiy/debug.log

# stop service
echo Stopping service-channelunfiy ...
docker compose -f service-channelunfiy.yaml down

# start service
echo Starting service-channelunfiy ...
docker compose -f service-channelunfiy.yaml up -d

# print log, auto abort when 'JVM running for' is encountered(start successfully)
tail -200f logs/ipower-channelunfiy/debug.log | sed '/JVM running for/ q'
