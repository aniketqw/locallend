#!/bin/bash

##############################################################################
# LocalLend 2.0 - Docker Run Script
##############################################################################
# This script runs the LocalLend 2.0 Docker container with proper configuration
##############################################################################

set -e  # Exit on any error

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print functions
print_success() {
    echo -e "${GREEN} $1${NC}"
}

print_info() {
    echo -e "${BLUE}9 $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}  $1${NC}"
}

print_error() {
    echo -e "${RED} $1${NC}"
}

print_header() {
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}================================================${NC}"
}

##############################################################################
# Configuration
##############################################################################

CONTAINER_NAME="locallend2.0"
IMAGE_NAME="locallend2.0:latest"
PORT="8080"

# Default environment variables (can be overridden)
MONGODB_URI="${MONGODB_URI:-mongodb://host.docker.internal:27017/locallend}"
JWT_SECRET="${JWT_SECRET:-change-this-secret-in-production-use-long-random-string}"
JWT_EXPIRATION_MS="${JWT_EXPIRATION_MS:-86400000}"

##############################################################################
# Main Run Process
##############################################################################

print_header "LocalLend 2.0 - Docker Run"

# Check if image exists
if ! docker images | grep -q "${IMAGE_NAME}"; then
    print_error "Docker image '${IMAGE_NAME}' not found!"
    print_info "Please build the image first using: ./docker-build.sh"
    exit 1
fi

# Check if container is already running
if docker ps | grep -q "${CONTAINER_NAME}"; then
    print_warning "Container '${CONTAINER_NAME}' is already running!"
    print_info "Stopping and removing existing container..."
    docker stop "${CONTAINER_NAME}" > /dev/null 2>&1
    docker rm "${CONTAINER_NAME}" > /dev/null 2>&1
    print_success "Stopped and removed existing container"
elif docker ps -a | grep -q "${CONTAINER_NAME}"; then
    print_info "Removing stopped container..."
    docker rm "${CONTAINER_NAME}" > /dev/null 2>&1
    print_success "Removed stopped container"
fi

# Check if .env file exists
if [ -f ".env" ]; then
    print_info "Loading environment variables from .env file"
    ENV_FILE="--env-file .env"
else
    print_warning "No .env file found. Using default environment variables."
    ENV_FILE=""
fi

# Run the container
print_info "Starting container: ${CONTAINER_NAME}"
print_info "Port: ${PORT}"
echo ""

docker run -d \
    --name "${CONTAINER_NAME}" \
    -p "${PORT}:8080" \
    -e MONGODB_URI="${MONGODB_URI}" \
    -e JWT_SECRET="${JWT_SECRET}" \
    -e JWT_EXPIRATION_MS="${JWT_EXPIRATION_MS}" \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e USE_COMMAND_PATTERN=true \
    -e USE_STATE_PATTERN=true \
    -e USE_EVENT_DRIVEN=true \
    -e ENABLE_ALL_PATTERNS=true \
    ${ENV_FILE} \
    --restart unless-stopped \
    "${IMAGE_NAME}"

if [ $? -eq 0 ]; then
    echo ""
    print_success "Container started successfully!"
    echo ""

    # Wait for container to be healthy
    print_info "Waiting for application to start..."
    sleep 5

    # Check container status
    print_header "Container Status"
    docker ps | grep "${CONTAINER_NAME}"
    echo ""

    # Display next steps
    print_header "Next Steps"
    echo ""
    print_info "View logs:"
    echo "  docker logs -f ${CONTAINER_NAME}"
    echo ""
    print_info "Access the application:"
    echo "  Backend API: http://localhost:${PORT}"
    echo "  Health Check: http://localhost:${PORT}/actuator/health"
    echo ""
    print_info "Stop the container:"
    echo "  docker stop ${CONTAINER_NAME}"
    echo ""
    print_info "Remove the container:"
    echo "  docker rm ${CONTAINER_NAME}"
    echo ""

    # Check health endpoint
    print_info "Checking health endpoint (waiting 10 seconds for startup)..."
    sleep 10

    if curl -s -f http://localhost:${PORT}/actuator/health > /dev/null 2>&1; then
        print_success "Health check passed! Application is running."
    else
        print_warning "Health check failed. Application may still be starting up."
        print_info "Check logs with: docker logs -f ${CONTAINER_NAME}"
    fi
    echo ""
else
    print_error "Failed to start container!"
    exit 1
fi

print_success "LocalLend 2.0 is now running!"
