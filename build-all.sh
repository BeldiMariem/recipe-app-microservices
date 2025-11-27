#!/bin/bash

echo "Building Recipe App Microservices..."

# Array of services to build
services=("service-discovery" "api-gateway" "user-service")

for service in "${services[@]}"; do
    echo "==========================================="
    echo "Building $service..."
    echo "==========================================="
    
    cd "$service"
    
    # Build with Maven
    mvn clean package -DskipTests
    
    # Check if build was successful
    if [ $? -eq 0 ]; then
        echo "✅ $service built successfully!"
    else
        echo "❌ $service build failed!"
        exit 1
    fi
    
    cd ..
done

echo "==========================================="
echo "🎉 All services built successfully!"
echo "==========================================="

# Build Docker images
echo "Building Docker images..."
docker-compose build

echo "🎉 Docker images built successfully!"
echo "You can now run: docker-compose up"