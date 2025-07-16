#!/bin/bash
# Deployment script for EcoDeli application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="ecodeli"
ENVIRONMENT=${1:-development}
COMPOSE_FILES=""

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

# Function to check prerequisites
check_prerequisites() {
    print_step "Checking prerequisites"
    
    # Check if Docker is running
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running"
        exit 1
    fi
    
    # Check if docker-compose is available
    if ! command -v docker-compose &> /dev/null; then
        print_error "docker-compose is not installed"
        exit 1
    fi
    
    # Check if .env file exists for production
    if [ "$ENVIRONMENT" = "production" ] && [ ! -f ".env" ]; then
        print_warning ".env file not found. Using .env.example as template"
        if [ -f ".env.example" ]; then
            cp .env.example .env
            print_warning "Please edit .env file with your production values before deploying"
            read -p "Press Enter to continue or Ctrl+C to abort..."
        else
            print_error ".env.example file not found"
            exit 1
        fi
    fi
    
    print_success "Prerequisites check passed"
}

# Function to set compose files based on environment
set_compose_files() {
    case $ENVIRONMENT in
        "production"|"prod")
            COMPOSE_FILES="-f docker-compose.yml -f docker-compose.prod.yml"
            ;;
        "development"|"dev")
            COMPOSE_FILES="-f docker-compose.yml"
            ;;
        *)
            print_error "Unknown environment: $ENVIRONMENT"
            print_error "Supported environments: development, production"
            exit 1
            ;;
    esac
}

# Function to deploy the application
deploy() {
    print_step "Deploying EcoDeli ($ENVIRONMENT environment)"
    
    # Pull latest images if in production
    if [ "$ENVIRONMENT" = "production" ]; then
        print_step "Pulling latest images"
        docker-compose $COMPOSE_FILES pull
    fi
    
    # Build images
    print_step "Building images"
    docker-compose $COMPOSE_FILES build
    
    # Start services
    print_step "Starting services"
    docker-compose $COMPOSE_FILES up -d
    
    # Wait for services to be ready
    print_step "Waiting for services to be ready"
    ./scripts/wait-for-services.sh
    
    # Run health checks
    print_step "Running health checks"
    ./scripts/health-check.sh
    
    print_success "Deployment completed successfully"
    
    # Show service status
    print_step "Service Status"
    docker-compose $COMPOSE_FILES ps
}

# Function to stop the application
stop() {
    print_step "Stopping EcoDeli services"
    docker-compose $COMPOSE_FILES down
    print_success "Services stopped"
}

# Function to restart the application
restart() {
    print_step "Restarting EcoDeli services"
    stop
    deploy
}

# Function to show logs
logs() {
    local service=${2:-}
    if [ -n "$service" ]; then
        docker-compose $COMPOSE_FILES logs -f "$service"
    else
        docker-compose $COMPOSE_FILES logs -f
    fi
}

# Function to show service status
status() {
    print_step "EcoDeli Service Status"
    docker-compose $COMPOSE_FILES ps
    
    print_step "Health Status"
    ./scripts/health-check.sh
}

# Function to clean up
cleanup() {
    print_step "Cleaning up EcoDeli deployment"
    
    # Stop and remove containers
    docker-compose $COMPOSE_FILES down -v --remove-orphans
    
    # Remove images
    docker-compose $COMPOSE_FILES down --rmi all
    
    # Clean up unused volumes (be careful with this)
    read -p "Do you want to remove all unused volumes? This will delete all data! (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker volume prune -f
        print_warning "All unused volumes removed"
    fi
    
    print_success "Cleanup completed"
}

# Function to backup data
backup() {
    print_step "Creating backup"
    
    local backup_dir="backups/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$backup_dir"
    
    # Backup database
    if docker-compose $COMPOSE_FILES ps database | grep -q "Up"; then
        print_step "Backing up database"
        docker-compose $COMPOSE_FILES exec -T database mysqldump -u root -p${DB_ROOT_PASSWORD:-rootpassword} --all-databases > "$backup_dir/database.sql"
        print_success "Database backup created"
    fi
    
    # Backup uploads
    if docker volume ls | grep -q ecodeli-uploads; then
        print_step "Backing up uploads"
        docker run --rm -v ecodeli-uploads:/data -v $(pwd)/$backup_dir:/backup alpine tar czf /backup/uploads.tar.gz -C /data .
        print_success "Uploads backup created"
    fi
    
    print_success "Backup completed: $backup_dir"
}

# Function to show usage
usage() {
    echo "Usage: $0 [ENVIRONMENT] [COMMAND]"
    echo ""
    echo "Environments:"
    echo "  development, dev    Deploy in development mode (default)"
    echo "  production, prod    Deploy in production mode"
    echo ""
    echo "Commands:"
    echo "  deploy              Deploy the application (default)"
    echo "  stop                Stop all services"
    echo "  restart             Restart all services"
    echo "  status              Show service status"
    echo "  logs [service]      Show logs (optionally for specific service)"
    echo "  cleanup             Stop and remove all containers, images, and volumes"
    echo "  backup              Create backup of database and uploads"
    echo ""
    echo "Examples:"
    echo "  $0                          # Deploy in development mode"
    echo "  $0 production deploy        # Deploy in production mode"
    echo "  $0 dev logs backend         # Show backend logs in development"
    echo "  $0 prod backup              # Create backup in production"
}

# Parse command line arguments
case $1 in
    "development"|"dev"|"production"|"prod")
        ENVIRONMENT=$1
        shift
        ;;
    "help"|"-h"|"--help")
        usage
        exit 0
        ;;
esac

# Set compose files based on environment
set_compose_files

# Parse command
COMMAND=${1:-deploy}

case $COMMAND in
    "deploy")
        check_prerequisites
        deploy
        ;;
    "stop")
        stop
        ;;
    "restart")
        check_prerequisites
        restart
        ;;
    "status")
        status
        ;;
    "logs")
        logs "$@"
        ;;
    "cleanup")
        cleanup
        ;;
    "backup")
        backup
        ;;
    "help"|"-h"|"--help")
        usage
        ;;
    *)
        print_error "Unknown command: $COMMAND"
        usage
        exit 1
        ;;
esac