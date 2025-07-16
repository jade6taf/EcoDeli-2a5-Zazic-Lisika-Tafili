#!/bin/bash
# Development setup script for EcoDeli

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

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to setup environment file
setup_env() {
    print_step "Setting up environment file"
    
    if [ ! -f ".env" ]; then
        if [ -f ".env.example" ]; then
            cp .env.example .env
            print_success "Created .env file from template"
            print_warning "Please edit .env file with your development values"
        else
            print_error ".env.example file not found"
            return 1
        fi
    else
        print_warning ".env file already exists"
    fi
}

# Function to install Node.js dependencies
install_node_deps() {
    print_step "Installing Node.js dependencies"
    
    # User frontend
    if [ -d "ecodeli-frontend-user" ]; then
        print_step "Installing user frontend dependencies"
        cd ecodeli-frontend-user
        if command_exists npm; then
            npm install
            print_success "User frontend dependencies installed"
        else
            print_error "npm not found. Please install Node.js"
            return 1
        fi
        cd ..
    fi
    
    # Admin frontend
    if [ -d "ecodeli-frontend-admin" ]; then
        print_step "Installing admin frontend dependencies"
        cd ecodeli-frontend-admin
        npm install
        print_success "Admin frontend dependencies installed"
        cd ..
    fi
}

# Function to setup database for development
setup_database() {
    print_step "Setting up development database"
    
    # Start only the database service
    docker-compose up -d database
    
    # Wait for database to be ready
    print_step "Waiting for database to be ready"
    ./scripts/wait-for-services.sh database
    
    print_success "Development database is ready"
}

# Function to build backend
build_backend() {
    print_step "Building backend"
    
    if [ -d "ecodeli-backend" ]; then
        cd ecodeli-backend
        if command_exists mvn; then
            mvn clean compile
            print_success "Backend compiled successfully"
        elif command_exists ./mvnw; then
            ./mvnw clean compile
            print_success "Backend compiled successfully"
        else
            print_warning "Maven not found. Backend will be built in Docker container"
        fi
        cd ..
    fi
}

# Function to start development environment
start_dev() {
    print_step "Starting development environment"
    
    # Start all services
    docker-compose up -d
    
    # Wait for services
    ./scripts/wait-for-services.sh
    
    print_success "Development environment is ready"
    
    # Show access URLs
    print_step "Access URLs"
    echo "User Frontend:  http://localhost:5173"
    echo "Admin Frontend: http://localhost:5174"
    echo "Backend API:    http://localhost:8080"
    echo "Nginx Proxy:    http://localhost:8000"
    echo "Database:       localhost:3306"
}

# Function to show development commands
show_dev_commands() {
    print_step "Development Commands"
    echo "Frontend Development (with hot reload):"
    echo "  cd ecodeli-frontend-user && npm run dev"
    echo "  cd ecodeli-frontend-admin && npm run dev"
    echo ""
    echo "Backend Development:"
    echo "  cd ecodeli-backend && ./mvnw spring-boot:run"
    echo ""
    echo "Docker Commands:"
    echo "  docker-compose up -d          # Start all services"
    echo "  docker-compose down           # Stop all services"
    echo "  docker-compose logs -f        # View logs"
    echo "  ./scripts/health-check.sh     # Check service health"
    echo ""
    echo "Database Access:"
    echo "  docker-compose exec database mysql -u ecodeli -p EcoDeli"
}

# Function to clean development environment
clean_dev() {
    print_step "Cleaning development environment"
    
    # Stop all services
    docker-compose down -v
    
    # Remove node_modules (optional)
    read -p "Remove node_modules directories? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        find . -name "node_modules" -type d -exec rm -rf {} + 2>/dev/null || true
        print_success "node_modules directories removed"
    fi
    
    # Clean Maven target (optional)
    read -p "Remove Maven target directory? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        rm -rf ecodeli-backend/target
        print_success "Maven target directory removed"
    fi
    
    print_success "Development environment cleaned"
}

# Function to show usage
usage() {
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  setup               Complete development setup"
    echo "  env                 Setup environment file"
    echo "  deps                Install Node.js dependencies"
    echo "  database            Setup development database"
    echo "  backend             Build backend"
    echo "  start               Start development environment"
    echo "  commands            Show development commands"
    echo "  clean               Clean development environment"
    echo ""
    echo "Examples:"
    echo "  $0 setup            # Complete setup"
    echo "  $0 start            # Start development environment"
    echo "  $0 clean            # Clean everything"
}

# Main function for complete setup
complete_setup() {
    print_step "EcoDeli Development Setup"
    
    setup_env
    install_node_deps
    build_backend
    setup_database
    start_dev
    show_dev_commands
    
    print_success "Development setup completed!"
}

# Parse command line arguments
case ${1:-setup} in
    "setup")
        complete_setup
        ;;
    "env")
        setup_env
        ;;
    "deps")
        install_node_deps
        ;;
    "database")
        setup_database
        ;;
    "backend")
        build_backend
        ;;
    "start")
        start_dev
        ;;
    "commands")
        show_dev_commands
        ;;
    "clean")
        clean_dev
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