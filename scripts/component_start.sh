service_pids=()

function cleanup() {
  for p in $service_pids; do
    kill -7 $p
  done
  exit
}
trap cleanup SIGINT

for d in ${1}/*/; do
    if test -f ${d}start.sh; then
        # pass dir path to start
        sudo ${d}start.sh &
        echo ${d}" started with pid "$!
        service_pids+=($!)
    fi
done

while true; do
  sleep 1
done
