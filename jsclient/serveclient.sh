#!/bin/sh
DEVICE="$1" # eth0, wlan0, etc...
PORT="$2"
IP="$(ip addr | grep 'inet.*'"${DEVICE}" | sed 's#.*inet \([0-9]*\.[0-9]*\.[0-9]*\.[0-9]*\).*#\1#')"
TMPFILE=$(tempfile)
sed 's#localhost#'"${IP}"'#g' jsclient.html > ${TMPFILE}
./httpserver "${PORT}" "${TMPFILE}"
rm -- "${TMPFILE}"
