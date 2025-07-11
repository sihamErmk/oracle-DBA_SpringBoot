@echo off
REM Local Kubernetes deployment script for Windows

echo 🏠 Starting local deployment...

REM Step 1: Build Docker image locally
echo 🔨 Building Docker image...
cd spring-oracle
docker build -t spring-oracle:local .
cd ..

REM Step 2: Apply Kubernetes manifests
echo 🚀 Deploying to local Kubernetes...
kubectl apply -f k8s/

REM Step 3: Wait for deployments
echo ⏳ Waiting for deployments...
kubectl wait --for=condition=available --timeout=300s deployment/oracle-db -n oracle-spring-app
kubectl wait --for=condition=available --timeout=300s deployment/spring-app -n oracle-spring-app

REM Step 4: Show status
echo ✅ Deployment complete!
echo 📋 Pod status:
kubectl get pods -n oracle-spring-app

echo 🌐 Access your application at:
echo Spring Boot: http://localhost:30080
echo Oracle DB: localhost:31521

echo 📊 To check logs:
echo kubectl logs -f deployment/spring-app -n oracle-spring-app