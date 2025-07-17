#!/bin/bash

# Fix EKS Access for GitHub Actions User
# Usage: ./fix-eks-access.sh

set -e

USER_NAME="tuanta-jenkins-user"
CLUSTER_NAME="t24-cluster"
REGION="ap-southeast-1"
ACCOUNT_ID="711652947591"

echo "🔧 Fixing EKS access for user: $USER_NAME"

# Step 1: Create and attach IAM policy
echo "📝 Creating IAM policy..."
aws iam create-policy \
    --policy-name T24ServiceEKSPolicy \
    --policy-document file://aws-iam-policy.json \
    --description "Policy for T24 Service GitHub Actions to access EKS and ECR" \
    2>/dev/null || echo "Policy already exists"

echo "🔗 Attaching policy to user..."
aws iam attach-user-policy \
    --user-name $USER_NAME \
    --policy-arn arn:aws:iam::${ACCOUNT_ID}:policy/T24ServiceEKSPolicy

# Step 2: Add user to EKS cluster access
echo "🎯 Adding user to EKS cluster access..."
aws eks create-access-entry \
    --cluster-name $CLUSTER_NAME \
    --principal-arn arn:aws:iam::${ACCOUNT_ID}:user/$USER_NAME \
    --type STANDARD \
    --username $USER_NAME \
    2>/dev/null || echo "Access entry already exists"

aws eks associate-access-policy \
    --cluster-name $CLUSTER_NAME \
    --principal-arn arn:aws:iam::${ACCOUNT_ID}:user/$USER_NAME \
    --policy-arn arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy \
    --access-scope type=cluster \
    2>/dev/null || echo "Policy already associated"

# Step 3: Test access
echo "🧪 Testing EKS access..."
aws eks describe-cluster --name $CLUSTER_NAME --region $REGION > /dev/null
echo "✅ EKS access test successful!"

echo "🎉 EKS access fixed! You can now run your GitHub Actions workflow."
echo ""
echo "To test kubectl access, run:"
echo "aws eks update-kubeconfig --region $REGION --name $CLUSTER_NAME"
echo "kubectl get nodes" 