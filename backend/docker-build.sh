#!/bin/bash

##############################################################################
# LocalLend 2.0 - Docker Build Script
##############################################################################
# This script builds the LocalLend 2.0 Docker image with proper tagging
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

IMAGE_NAME="locallend2.0"
VERSION="2.0.0"
LATEST_TAG="latest"

##############################################################################
# Main Build Process
##############################################################################

print_header "LocalLend 2.0 - Docker Build"

# Check if we're in the right directory
if [ ! -f "Dockerfile" ]; then
    print_error "Dockerfile not found. Please run this script from the backend directory."
    exit 1
fi

if [ ! -f "pom.xml" ]; then
    print_error "pom.xml not found. Please run this script from the backend directory."
    exit 1
fi

print_info "Building Docker image: ${IMAGE_NAME}"
print_info "Version tags: ${VERSION}, ${LATEST_TAG}"
echo ""

# Build the Docker image with multiple tags
print_info "Running Docker build..."
echo ""

docker build \
    -t "${IMAGE_NAME}:${VERSION}" \
    -t "${IMAGE_NAME}:${LATEST_TAG}" \
    --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
    --build-arg VERSION="${VERSION}" \
    .

if [ $? -eq 0 ]; then
    echo ""
    print_success "Docker image built successfully!"
    echo ""

    # Display image information
    print_header "Image Information"
    docker images | grep "${IMAGE_NAME}"
    echo ""

    # Display image size
    IMAGE_SIZE=$(docker images "${IMAGE_NAME}:${LATEST_TAG}" --format "{{.Size}}")
    print_info "Image size: ${IMAGE_SIZE}"
    echo ""

    # Display next steps
    print_header "Next Steps"
    echo ""
    print_info "Run the container:"
    echo "  ./docker-run.sh"
    echo ""
    print_info "Or use docker-compose:"
    echo "  cd .."
    echo "  docker-compose up -d"
    echo ""
    print_info "Push to registry (if configured):"
    echo "  docker tag ${IMAGE_NAME}:${VERSION} your-registry/${IMAGE_NAME}:${VERSION}"
    echo "  docker push your-registry/${IMAGE_NAME}:${VERSION}"
    echo ""
else
    print_error "Docker build failed!"
    exit 1
fi

print_success "Build complete!"
