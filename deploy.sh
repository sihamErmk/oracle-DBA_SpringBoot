#!/bin/bash
# Deployment script for Oracle Spring Boot application

set -e

echo "🚀 Starting deployment process..."

# Step 1: Create infrastructure with Terraform
echo "📦 Creating AWS infrastructure..."
cd terraform
terraform init
terraform plan
terraform apply -auto-approve
cd ..

# Step 2: Configure kubectl
echo "⚙️  Configuring kubectl..."
aws eks update-kubeconfig --region us-west-2 --name oracle-spring-cluster

# Step 3: Deploy to Kubernetes
echo "🎯 Deploying to Kubernetes..."
kubectl apply -f k8s/

# Step 4: Wait for deployments
echo "⏳ Waiting for deployments to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/oracle-db -n oracle-spring-app
kubectl wait --for=condition=available --timeout=300s deployment/spring-app -n oracle-spring-app

# Step 5: Get service information
echo "✅ Deployment complete!"
echo "📋 Service information:"
kubectl get services -n oracle-spring-app

echo "🌐 Application URL:"
kubectl get service spring-app-service -n oracle-spring-app -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'