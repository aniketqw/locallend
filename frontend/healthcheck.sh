#!/bin/sh
# Health check script for LocalLend Frontend container

# Check if Nginx is running and serving content
if wget --quiet --tries=1 --spider http://localhost:80/; then
    echo "Health check passed: Frontend is responding"
    exit 0
else
    echo "Health check failed: Frontend is not responding"
    exit 1
fi