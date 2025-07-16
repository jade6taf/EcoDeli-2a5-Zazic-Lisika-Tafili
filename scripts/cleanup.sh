#!/bin/bash
# Cleanup script for EcoDeli Docker resources

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Function to confirm action
confirm() {
    local message=$1
    local default=${2:-N}
    
    if [ "$default" = "Y" ]; then
        read -p "$message (Y/n): " -n 1 -r
        echo
        [[ $REPLY =~ ^[Nn]$ ]] && return 1 || return 0
    else
        read -p "$message (y/N): " -n 1 -r
        echo
        [[ $REPLY =~ ^[Yy]$ ]] && return 0 || return 1
    fi
}

# Function to stop and remove containers
cleanup_containers() {
    print_step "Cleaning up containers"
    
    # Stop all EcoDeli containers
    if docker-compose ps -q | grep -q .; then
        print_step "Stopping EcoDeli containers"
        docker-compose down
        print_success "Containers stopped"
    else
        print_warning "No running containers found"
    fi
    
    # Remove all EcoDeli containers (including stopped ones)
    local containers=$(docker ps -a --filter "name=ecodeli" -q)
    if [ -n "$containers" ]; then
        if confirm "Remove all EcoDeli containers?"; then
            docker rm -f $containers
            print_success "All EcoDeli containers removed"
        fi
    else
        print_warning "No EcoDeli containers found"
    fi
}

# Function to remove images
cleanup_images() {
    print_step "Cleaning up images"
    
    # Remove EcoDeli images
    local images=$(docker images --filter "reference=ecodeli-*" -q)
    if [ -n "$images" ]; then
        if confirm "Remove all EcoDeli images?"; then
            docker rmi -f $images
            print_success "EcoDeli images removed"
        fi
    else
        print_warning "No EcoDeli images found"
    fi
    
    # Remove dangling images
    local dangling=$(docker images -f "dangling=true" -q)
    if [ -n "$dangling" ]; then
        if confirm "Remove dangling images?"; then
            docker rmi $dangling
            print_success "Dangling images removed"
        fi
    else
        print_warning "No dangling images found"
    fi
}

# Function to remove volumes
cleanup_volumes() {
    print_step "Cleaning up volumes"
    
    # Remove EcoDeli volumes
    local volumes=$(docker volume ls --filter "name=ecodeli" -q)
    if [ -n "$volumes" ]; then
        print_warning "This will delete all data including database and uploads!"
        if confirm "Remove all EcoDeli volumes? THIS WILL DELETE ALL DATA!"; then
            docker volume rm $volumes
            print_success "EcoDeli volumes removed"
        fi
    else
        print_warning "No EcoDeli volumes found"
    fi
    
    # Remove unused volumes
    local unused=$(docker volume ls -f dangling=true -q)
    if [ -n "$unused" ]; then
        if confirm "Remove unused volumes?"; then
            docker volume rm $unused
            print_success "Unused volumes removed"
        fi
    else
        print_warning "No unused volumes found"
    fi
}

# Function to remove networks
cleanup_networks() {
    print_step "Cleaning up networks"
    
    # Remove EcoDeli network
    if docker network ls --filter "name=ecodeli" -q | grep -q .; then
        if confirm "Remove EcoDeli network?"; then
            docker network rm ecodeli-network 2>/dev/null || true
            print_success "EcoDeli network removed"
        fi
    else
        print_warning "No EcoDeli network found"
    fi
    
    # Remove unused networks
    if confirm "Remove unused networks?"; then
        docker network prune -f
        print_success "Unused networks removed"
    fi
}

# Function to clean build cache
cleanup_build_cache() {
    print_step "Cleaning up build cache"
    
    if confirm "Remove Docker build cache?"; then
        docker builder prune -f
        print_success "Build cache removed"
    fi
}

# Function to show disk usage
show_disk_usage() {
    print_step "Docker Disk Usage"
    docker system df
}

# Function to complete cleanup
complete_cleanup() {
    print_step "Complete EcoDeli Cleanup"
    print_warning "This will remove ALL EcoDeli Docker resources!"
    
    if confirm "Are you sure you want to proceed with complete cleanup?"; then
        cleanup_containers
        cleanup_images
        cleanup_volumes
        cleanup_networks
        cleanup_build_cache
        
        print_success "Complete cleanup finished"
        show_disk_usage
    else
        print_warning "Cleanup cancelled"
    fi
}

# Function to safe cleanup (keeps data)
safe_cleanup() {
    print_step "Safe EcoDeli Cleanup (preserves data)"
    
    cleanup_containers
    cleanup_images
    cleanup_networks
    cleanup_build_cache
    
    print_success "Safe cleanup finished"
    print_warning "Data volumes preserved"
    show_disk_usage
}

# Function to show usage
usage() {
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  all                 Complete cleanup (removes everything including data)"
    echo "  safe                Safe cleanup (preserves data volumes)"
    echo "  containers          Remove containers only"
    echo "  images              Remove images only"
    echo "  volumes             Remove volumes only (DELETES DATA)"
    echo "  networks            Remove networks only"
    echo "  cache               Remove build cache only"
    echo "  usage               Show Docker disk usage"
    echo ""
    echo "Examples:"
    echo "  $0 safe             # Safe cleanup preserving data"
    echo "  $0 all              # Complete cleanup including data"
    echo "  $0 containers       # Remove containers only"
}

# Parse command line arguments
case ${1:-safe} in
    "all"|"complete")
        complete_cleanup
        ;;
    "safe")
        safe_cleanup
        ;;
    "containers")
        cleanup_containers
        ;;
    "images")
        cleanup_images
        ;;
    "volumes")
        cleanup_volumes
        ;;
    "networks")
        cleanup_networks
        ;;
    "cache")
        cleanup_build_cache
        ;;
    "usage")
        show_disk_usage
        ;;
    "help"|"-h"|"--help")
        usage
        ;;
    *)
        print_error "Unknown command: $1"
        usage
        exit 1
        ;;
esac