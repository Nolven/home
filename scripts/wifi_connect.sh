while [ $(nmcli radio wifi) != "enabled" ]; do
    nmcli dev wifi connect RT-GPON-C01A password qazwsxedc
    sleep 10
done
