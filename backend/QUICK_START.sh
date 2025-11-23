#!/bin/bash

##########################################################################
# LocalLend Backend - Quick Start Script
# This script automates the setup and running of the LocalLend backend
##########################################################################

set -e  # Exit on any error

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colored output
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_header() {
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}================================================${NC}"
}

##########################################################################
# Check Prerequisites
##########################################################################

check_prerequisites() {
    print_header "Checking Prerequisites"

    # Check Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 17 ]; then
            print_success "Java $JAVA_VERSION installed"
        else
            print_error "Java 17 or higher required. Current: $JAVA_VERSION"
            exit 1
        fi
    else
        print_error "Java not found. Please install Java 17 or higher"
        exit 1
    fi

    # Check Maven
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version | grep "Apache Maven" | awk '{print $3}')
        print_success "Maven $MVN_VERSION installed"
    else
        print_error "Maven not found. Please install Maven 3.6 or higher"
        exit 1
    fi

    # Check MongoDB
    if command -v mongod &> /dev/null; then
        MONGO_VERSION=$(mongod --version | grep "db version" | awk '{print $3}')
        print_success "MongoDB $MONGO_VERSION installed"
    else
        print_warning "MongoDB not found. Continuing anyway (might be running in Docker)"
    fi

    echo ""
}

##########################################################################
# Setup MongoDB
##########################################################################

setup_mongodb() {
    print_header "Setting Up MongoDB"

    # Check if MongoDB is running
    if pgrep -x "mongod" > /dev/null; then
        print_success "MongoDB is already running"
    else
        print_info "Starting MongoDB..."

        # Try different methods to start MongoDB
        if command -v brew &> /dev/null; then
            # macOS with Homebrew
            brew services start mongodb-community &> /dev/null || true
            print_success "Started MongoDB via Homebrew"
        else
            # Try direct command
            mongod --fork --logpath /tmp/mongodb.log --dbpath /usr/local/var/mongodb &> /dev/null || \
            mongod --fork --logpath /tmp/mongodb.log --dbpath ~/data/db &> /dev/null || \
            print_warning "Could not start MongoDB automatically. Please start it manually."
        fi

        # Wait for MongoDB to be ready
        sleep 2
    fi

    # Test MongoDB connection
    if command -v mongosh &> /dev/null; then
        mongosh --eval "db.version()" --quiet > /dev/null 2>&1 && \
        print_success "MongoDB connection verified" || \
        print_warning "MongoDB might not be accessible"
    fi

    echo ""
}

##########################################################################
# Setup Environment Variables
##########################################################################

setup_environment() {
    print_header "Setting Up Environment"

    # Check if .env file exists
    if [ -f .env ]; then
        print_info "Loading existing .env file"
        export $(cat .env | xargs)
        print_success "Environment variables loaded"
    else
        print_info "Creating default .env file"
        cat > .env << EOF
# MongoDB Configuration
MONGODB_URI=mongodb://localhost:27017/locallend

# JWT Configuration
JWT_SECRET=$(openssl rand -base64 32)
JWT_EXPIRATION_MS=86400000

# Server Configuration
SERVER_PORT=8080

# Feature Flags (New Architecture)
USE_COMMAND_PATTERN=false
USE_STATE_PATTERN=false
USE_EVENT_DRIVEN=false
ENABLE_ALL_PATTERNS=false
EOF
        print_success "Created .env file with secure JWT secret"
    fi

    # Export key variables
    export MONGODB_URI="${MONGODB_URI:-mongodb://localhost:27017/locallend}"
    export JWT_SECRET="${JWT_SECRET:-change-this-in-production}"
    export SERVER_PORT="${SERVER_PORT:-8080}"

    echo ""
}

##########################################################################
# Build Project
##########################################################################

build_project() {
    print_header "Building Project"

    print_info "Running Maven clean install..."

    if mvn clean install -DskipTests > /tmp/maven-build.log 2>&1; then
        print_success "Build successful!"
    else
        print_error "Build failed. Check /tmp/maven-build.log for details"
        tail -20 /tmp/maven-build.log
        exit 1
    fi

    echo ""
}

##########################################################################
# Run Application
##########################################################################

run_application() {
    print_header "Starting Application"

    # Check for feature flags
    local EXTRA_ARGS=""

    if [ "${ENABLE_ALL_PATTERNS}" = "true" ]; then
        EXTRA_ARGS="--feature.use-command-pattern=true --feature.use-state-pattern=true --feature.use-event-driven=true"
        print_info "🆕 Starting with ALL new patterns enabled"
    elif [ "${USE_COMMAND_PATTERN}" = "true" ]; then
        EXTRA_ARGS="--feature.use-command-pattern=true"
        print_info "🆕 Starting with Command Pattern enabled"
    else
        print_info "Starting in legacy mode (new patterns disabled)"
    fi

    print_info "Server will start on http://localhost:${SERVER_PORT}"
    print_info "Press Ctrl+C to stop"
    echo ""

    # Run the application
    if [ -n "$EXTRA_ARGS" ]; then
        mvn spring-boot:run -Dspring-boot.run.arguments="$EXTRA_ARGS"
    else
        mvn spring-boot:run
    fi
}

##########################################################################
# Main Menu
##########################################################################

show_menu() {
    clear
    print_header "LocalLend Backend - Quick Start"
    echo ""
    echo "1) Check prerequisites only"
    echo "2) Setup MongoDB"
    echo "3) Setup environment"
    echo "4) Build project"
    echo "5) Run application (legacy mode)"
    echo "6) Run with Command Pattern enabled"
    echo "7) Run with ALL new patterns enabled"
    echo "8) Full setup and run (recommended for first time)"
    echo "9) Exit"
    echo ""
    read -p "Select option [1-9]: " choice

    case $choice in
        1) check_prerequisites ;;
        2) setup_mongodb ;;
        3) setup_environment ;;
        4) build_project ;;
        5)
            export ENABLE_ALL_PATTERNS=false
            export USE_COMMAND_PATTERN=false
            run_application
            ;;
        6)
            export USE_COMMAND_PATTERN=true
            export ENABLE_ALL_PATTERNS=false
            run_application
            ;;
        7)
            export ENABLE_ALL_PATTERNS=true
            run_application
            ;;
        8)
            check_prerequisites
            setup_mongodb
            setup_environment
            build_project
            run_application
            ;;
        9)
            print_success "Goodbye!"
            exit 0
            ;;
        *)
            print_error "Invalid option"
            sleep 1
            show_menu
            ;;
    esac
}

##########################################################################
# Script Entry Point
##########################################################################

# Change to backend directory if not already there
if [ ! -f "pom.xml" ]; then
    if [ -f "../pom.xml" ]; then
        cd ..
    elif [ -f "backend/pom.xml" ]; then
        cd backend
    else
        print_error "Cannot find pom.xml. Please run this script from the backend directory"
        exit 1
    fi
fi

# If no arguments, show menu
if [ $# -eq 0 ]; then
    show_menu
else
    # Command line arguments
    case "$1" in
        --full|-f)
            check_prerequisites
            setup_mongodb
            setup_environment
            build_project
            run_application
            ;;
        --build|-b)
            build_project
            ;;
        --run|-r)
            run_application
            ;;
        --patterns|-p)
            export ENABLE_ALL_PATTERNS=true
            run_application
            ;;
        --help|-h)
            echo "Usage: $0 [option]"
            echo ""
            echo "Options:"
            echo "  --full, -f       Full setup and run"
            echo "  --build, -b      Build project only"
            echo "  --run, -r        Run application only"
            echo "  --patterns, -p   Run with all new patterns enabled"
            echo "  --help, -h       Show this help message"
            echo ""
            echo "No option: Show interactive menu"
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
fi
