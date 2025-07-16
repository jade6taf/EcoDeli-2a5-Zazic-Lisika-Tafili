#!/bin/bash
# Build script for EcoDeli Docker images

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="ecodeli"
VERSION=${1:-latest}
BUILD_ARGS=""

# Function to print colored output
print_step() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Function to build a single service
build_service() {
    local service=$1
    local context=$2
    local dockerfile=${3:-Dockerfile}
    
    print_step "Building $service"
    
    if [ -f "$context/$dockerfile" ]; then
        docker build $BUILD_ARGS -t "${PROJECT_NAME}-${service}:${VERSION}" -f "$context/$dockerfile" "$context"
        print_success "$service image built successfully"
    else
        print_error "Dockerfile not found: $context/$dockerfile"
        return 1
    fi
}

# Function to build all services
build_all() {
    print_step "Building all EcoDeli services"
    
    # Build backend
    build_service "backend" "ecodeli-backend"
    
    # Build user frontend
    build_service "user-frontend" "ecodeli-frontend-user"
    
    # Build admin frontend
    build_service "admin-frontend" "ecodeli-frontend-admin"
    
    # Build nginx proxy
    build_service "nginx" "nginx"
    
    print_success "All services built successfully"
}

# Function to build using docker-compose
build_compose() {
    local env=${1:-dev}
    
    print_step "Building with docker-compose ($env environment)"
    
    case $env in
        "prod"|"production")
            docker-compose -f docker-compose.yml -f docker-compose.prod.yml build --no-cache
            ;;
        "dev"|"development"|*)
            docker-compose build --no-cache
            ;;
    esac
    
    print_success "Docker Compose build completed"
}

# Function to clean up old images
cleanup() {
    print_step "Cleaning up old images"
    
    # Remove dangling images
    if docker images -f "dangling=true" -q | grep -q .; then
        docker rmi $(docker images -f "dangling=true" -q)
        print_success "Removed dangling images"
    else
        print_warning "No dangling images to remove"
    fi
    
    # Remove old versions (keep last 3)
    for service in backend user-frontend admin-frontend nginx; do
        old_images=$(docker images "${PROJECT_NAME}-${service}" --format "table {{.Repository}}:{{.Tag}}" | tail -n +2 | tail -n +4)
        if [ -n "$old_images" ]; then
            echo "$old_images" | xargs -r docker rmi
            print_success "Cleaned up old $service images"
        fi
    done
}

# Function to show usage
usage() {
    echo "Usage: $0 [OPTIONS] [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  all                 Build all services individually"
    echo "  compose [env]       Build using docker-compose (env: dev|prod)"
    echo "  backend             Build only backend service"
    echo "  user-frontend       Build only user frontend service"
    echo "  admin-frontend      Build only admin frontend service"
    echo "  nginx               Build only nginx service"
    echo "  cleanup             Clean up old images"
    echo ""
    echo "Options:"
    echo "  -v, --version       Set image version (default: latest)"
    echo "  -h, --help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 all                    # Build all services"
    echo "  $0 compose prod           # Build for production"
    echo "  $0 -v 1.0.0 backend       # Build backend with version 1.0.0"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -v|--version)
            VERSION="$2"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        all)
            build_all
            exit 0
            ;;
        compose)
            build_compose "$2"
            exit 0
            ;;
        backend)
            build_service "backend" "ecodeli-backend"
            exit 0
            ;;
        user-frontend)
            build_service "user-frontend" "ecodeli-frontend-user"
            exit 0
            ;;
        admin-frontend)
            build_service "admin-frontend" "ecodeli-frontend-admin"
            exit 0
            ;;
        nginx)
            build_service "nginx" "nginx"
            exit 0
            ;;
        cleanup)
            cleanup
            exit 0
            ;;
        *)
            print_error "Unknown command: $1"
            usage
            exit 1
            ;;
    esac
done

# Default action if no command specified
print_step "EcoDeli Build Script"
echo "No command specified. Use -h for help."
echo "Running default build (all services)..."
build_all