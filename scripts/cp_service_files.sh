#!/bin/bash
yes="`dirname "$0"`/../software"
softwareDir=`cd "$yes";pwd`

for d in ${softwareDir}/*/; do
    serviceFile=`find ${d} -name "*.service" -exec basename \{} \;`
    cp ${d}/serviceFile /etc/systemd/system
    systemctl restart $serviceFile
done
