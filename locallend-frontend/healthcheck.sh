#!/bin/sh
# Simple healthcheck used by the frontend container
# Return 0 if Nginx is serving the site on localhost, else non-zero
set -e

URL="http://127.0.0.1/"

if command -v curl >/dev/null 2>&1; then
  if curl -fsS "$URL" >/dev/null 2>&1; then
    exit 0
  else
    exit 1
  fi
else
  # If curl isn't available, check for a local file
  if [ -f /usr/share/nginx/html/index.html ]; then
    exit 0
  fi
  exit 1
fi
