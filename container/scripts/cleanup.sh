#!/bin/bash

echo "Removing deployment..."

kubectl delete deployment trade-loan-service --ignore-not-found=true
kubectl delete service trade-loan-service --ignore-not-found=true
kubectl delete secret trade-loan-service-secrets --ignore-not-found=true
kubectl delete configmap trade-loan-service-config --ignore-not-found=true
kubectl delete serviceaccount trade-loan-service --ignore-not-found=true
kubectl delete role trade-loan-service-role --ignore-not-found=true
kubectl delete rolebinding trade-loan-service-rolebinding --ignore-not-found=true

echo "✓ Cleanup complete"