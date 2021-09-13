#!/bin/bash
yes="`dirname "$0"`/../software"
softwareDir=`cd "$yes";pwd`

for d in ${softwareDir}/*/; do
    serviceFile=`find ${d} -name "*.service" -exec basename \{} \;`
    if [ "$serviceFile" ]; then
    	cp ${d}/${serviceFile} /etc/systemd/system/
        systemctl start ${serviceFile}
        systemctl enable ${serviceFile}
    fi
done

systemctl daemon-reload
