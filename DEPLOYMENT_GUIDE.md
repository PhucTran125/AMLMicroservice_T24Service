# T24 Service - Automatic Deployment Setup Guide

This guide explains how to set up automatic deployment from GitHub Actions to Amazon EKS when you push code to the master branch.

## 🏗️ Architecture Overview

```
GitHub Push → GitHub Actions → Build Docker Image → Push to ECR → Deploy to EKS → Service Available
```

## 📋 Prerequisites

1. **AWS Account** with appropriate permissions
2. **EKS Cluster** already created
3. **ECR Repository** already created
4. **GitHub Repository** with secrets configured
5. **Domain name** (optional, for ingress)
6. **SSL Certificate** (optional, for HTTPS)

## 🔧 Setup Instructions

### 1. Create EKS Cluster (if not done already)

```bash
# Create EKS cluster using eksctl
eksctl create cluster \
  --name your-eks-cluster-name \
  --region ap-southeast-1 \
  --nodegroup-name t24-nodes \
  --node-type m5.large \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 4 \
  --managed
```

### 2. Install AWS Load Balancer Controller

```bash
# Create IAM OIDC provider
eksctl utils associate-iam-oidc-provider --region=ap-southeast-1 --cluster=your-eks-cluster-name --approve

# Create IAM service account
eksctl create iamserviceaccount \
  --cluster=your-eks-cluster-name \
  --namespace=kube-system \
  --name=aws-load-balancer-controller \
  --role-name=AmazonEKSLoadBalancerControllerRole \
  --attach-policy-arn=arn:aws:iam::aws:policy/ElasticLoadBalancingFullAccess \
  --approve

# Install AWS Load Balancer Controller
helm repo add eks https://aws.github.io/eks-charts
helm repo update
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=your-eks-cluster-name \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller
```

### 3. Configure GitHub Secrets

Add the following secrets to your GitHub repository (Settings → Secrets and variables → Actions):

| Secret Name             | Description                    | Example                                      |
| ----------------------- | ------------------------------ | -------------------------------------------- |
| `AWS_ACCESS_KEY_ID`     | AWS Access Key ID              | `AKIAXXXXXXXXXXXXXXXX`                       |
| `AWS_SECRET_ACCESS_KEY` | AWS Secret Access Key          | `xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`   |
| `AWS_ACCOUNT_ID`        | Your AWS Account ID            | `123456789012`                               |
| `SSL_CERT_ARN`          | SSL Certificate ARN (optional) | `arn:aws:acm:region:account:certificate/xxx` |

### 4. Update Configuration Files

#### Update `.github/workflows/deploy-to-ecr.yml`:

```yaml
# Change these values in the workflow file:
EKS_CLUSTER_NAME: your-actual-eks-cluster-name
```

#### Update `k8s/deployment.yaml`:

```yaml
# Update the database and Kafka connection details in the secret
stringData:
  database-url: "jdbc:postgresql://your-rds-endpoint:5432/t24db"
  database-username: "your-db-username"
  database-password: "your-db-password"
  kafka-servers: "your-kafka-broker:9092"
```

#### Update `k8s/ingress.yaml`:

```yaml
# Update the host in the ingress rules
rules:
- host: t24-service.your-domain.com  # Replace with your actual domain
```

### 5. Set Up Database and Kafka

#### RDS PostgreSQL Setup:

```sql
-- Create database
CREATE DATABASE t24db;

-- Create user
CREATE USER t24user WITH ENCRYPTED PASSWORD 'your-secure-password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE t24db TO t24user;
```

#### Amazon MSK (Managed Kafka) or EC2 Kafka Setup:

- Create Kafka topics: `account-opening`, `transaction`, `aml-result`
- Ensure security groups allow connections from EKS

### 6. Configure IAM Permissions

Your GitHub Actions user needs the following AWS permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage",
                "ecr:GetAuthorizationToken",
                "ecr:PutImage",
                "ecr:InitiateLayerUpload",
                "ecr:UploadLayerPart",
                "ecr:CompleteLayerUpload"
            ],
            "Resource": "*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "eks:DescribeCluster",
                "eks:ListClusters"
            ],
            "Resource": "*"
        }
    ]
}
```

## 🚀 Deployment Process

### Automatic Deployment

1. **Push to Master**: Push your code to the `master` branch
2. **GitHub Actions Trigger**: The workflow automatically starts
3. **Build & Push**: Docker image is built and pushed to ECR
4. **Deploy**: Application is deployed to EKS
5. **Verify**: Health checks ensure the deployment is successful

### Manual Deployment

```bash
# Apply configurations manually
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml

# Check deployment status
kubectl get pods -n t24-namespace
kubectl get svc -n t24-namespace
kubectl get ingress -n t24-namespace
```

## 🔍 Monitoring and Troubleshooting

### Health Checks

The application exposes the following health check endpoints:

- **Liveness**: `http://your-service/actuator/health/liveness`
- **Readiness**: `http://your-service/actuator/health/readiness`
- **General Health**: `http://your-service/actuator/health`

### Viewing Logs

```bash
# View application logs
kubectl logs -f deployment/t24-service -n t24-namespace

# View previous logs
kubectl logs deployment/t24-service -n t24-namespace --previous

# View logs from specific pod
kubectl logs pod-name -n t24-namespace
```

### Common Troubleshooting

```bash
# Check pod status
kubectl describe pod pod-name -n t24-namespace

# Check service endpoints
kubectl get endpoints -n t24-namespace

# Check ingress status
kubectl describe ingress t24-service-ingress -n t24-namespace

# Check deployment events
kubectl get events -n t24-namespace --sort-by=.metadata.creationTimestamp
```

## 🌐 Accessing Your Application

### Using Load Balancer

```bash
# Get Load Balancer URL
kubectl get svc t24-service -n t24-namespace

# Access using the EXTERNAL-IP
http://your-load-balancer-url
```

### Using Ingress (with domain)

```bash
# Get ALB hostname
kubectl get ingress t24-service-ingress -n t24-namespace

# Access using your domain
https://t24-service.your-domain.com
```

## 🔄 Rollback Strategy

### Rollback to Previous Version

```bash
# View deployment history
kubectl rollout history deployment/t24-service -n t24-namespace

# Rollback to previous version
kubectl rollout undo deployment/t24-service -n t24-namespace

# Rollback to specific revision
kubectl rollout undo deployment/t24-service --to-revision=2 -n t24-namespace
```

## 📊 Demo Applications

Once deployed, access your real-time demos:

- **Home Page**: `https://your-domain.com/`
- **Account Opening Demo**: `https://your-domain.com/account-opening-demo.html`
- **Transaction Demo**: `https://your-domain.com/transaction-demo.html`

## 🔒 Security Considerations

1. **Use SSL/TLS**: Configure HTTPS with proper certificates
2. **Network Policies**: Implement Kubernetes network policies
3. **RBAC**: Configure role-based access control
4. **Secrets Management**: Use AWS Secrets Manager or Kubernetes secrets
5. **Image Scanning**: Enable ECR image scanning
6. **Pod Security**: Use security contexts and pod security policies

## 📈 Scaling

### Horizontal Pod Autoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: t24-service-hpa
  namespace: t24-namespace
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: t24-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

## 🎯 Next Steps

1. **Set up monitoring** with CloudWatch or Prometheus
2. **Configure alerts** for application and infrastructure metrics
3. **Implement blue-green deployments** for zero-downtime updates
4. **Set up staging environment** for testing before production
5. **Configure backup strategies** for your database and persistent volumes

This setup provides a complete CI/CD pipeline that automatically builds, tests, and deploys your T24 service to EKS whenever you push code to the master branch!
