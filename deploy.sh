#!/bin/bash
# ============================================================
# HustSimulator V2 — Kubernetes Deployment Script (Minikube)
# ============================================================
# Cách dùng:
#   ./deploy.sh                  # Deploy toàn bộ
#   ./deploy.sh build            # Build + load images vào minikube
#   ./deploy.sh apply            # Chỉ apply manifests
#   ./deploy.sh status           # Xem trạng thái pods
#   ./deploy.sh delete           # Xóa toàn bộ
#   ./deploy.sh logs <service>   # Xem log của service
# ============================================================

set -euo pipefail

NAMESPACE="hustsim"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
K8S_DIR="$SCRIPT_DIR/k8s"
REGISTRY="${DOCKER_REGISTRY:-hustsim}"

# Danh sách services cần build Docker image
SERVICES=(
  "api-gateway"
  "auth-service"
  "context-service"
  "social-service"
  "streaming-service"
  "state-dissemination-service"
  "state-computation-service"
  "interest-matcher-service"
  "prediction-service"
)

# ─── Functions ─────────────────────────────────────────────

print_header() {
  echo ""
  echo "══════════════════════════════════════════════════════"
  echo "  $1"
  echo "══════════════════════════════════════════════════════"
}

check_prerequisites() {
  print_header "Kiểm tra prerequisites"
  
  for cmd in docker minikube kubectl; do
    if ! command -v $cmd &>/dev/null; then
      echo "❌ Chưa cài đặt $cmd. Vui lòng cài đặt trước."
      exit 1
    fi
  done
  
  # Kiểm tra minikube đang chạy
  if ! minikube status &>/dev/null; then
    echo "⚠️  Minikube chưa chạy. Đang khởi động..."
    minikube start --cpus=4 --memory=8192 --driver=docker
  fi
  
  echo "✅ Tất cả prerequisites đã sẵn sàng"
}

build_images() {
  print_header "Build Docker images"
  
  # Dùng Docker daemon của minikube để build trực tiếp
  # (không cần push lên registry)
  eval $(minikube docker-env)
  
  for svc in "${SERVICES[@]}"; do
    echo ""
    echo "🔨 Building $svc..."
    
    # Image name: chuẩn hóa (interest-matcher-service → interest-matcher-service)
    IMAGE_NAME="${REGISTRY}/${svc}:latest"
    
    if [ "$svc" = "api-gateway" ]; then
      BUILD_CONTEXT="$SCRIPT_DIR/$svc"
    else
      BUILD_CONTEXT="$SCRIPT_DIR"
    fi
    
    docker build \
      -t "$IMAGE_NAME" \
      -f "$SCRIPT_DIR/$svc/Dockerfile" \
      "$BUILD_CONTEXT"
    
    echo "✅ Built $IMAGE_NAME"
  done
  
  echo ""
  echo "✅ Tất cả images đã build xong"
  
  # Reset Docker env về host
  eval $(minikube docker-env -u)
}

apply_manifests() {
  print_header "Apply K8s manifests"
  
  echo "📦 Tạo namespace..."
  kubectl apply -f "$K8S_DIR/namespace.yaml"
  
  echo "🔐 Apply ConfigMap + Secrets..."
  kubectl apply -f "$K8S_DIR/configmap.yaml"
  kubectl apply -f "$K8S_DIR/secrets.yaml"
  
  echo "🗄️  Deploy infrastructure (Redis, RabbitMQ, LiveKit)..."
  kubectl apply -f "$K8S_DIR/infrastructure/" --recursive
  
  echo "⏳ Chờ infrastructure sẵn sàng..."
  kubectl wait --for=condition=ready pod -l app=redis -n "$NAMESPACE" --timeout=120s 2>/dev/null || true
  kubectl wait --for=condition=ready pod -l app=rabbitmq -n "$NAMESPACE" --timeout=120s 2>/dev/null || true
  
  echo "🚀 Deploy application services..."
  kubectl apply -f "$K8S_DIR/services/" --recursive
  
  echo "📊 Deploy monitoring stack..."
  kubectl apply -f "$K8S_DIR/monitoring/" --recursive
  
  echo ""
  echo "✅ Tất cả manifests đã được apply"
}

show_status() {
  print_header "Trạng thái hệ thống"
  
  echo "📋 Pods:"
  kubectl get pods -n "$NAMESPACE" -o wide
  
  echo ""
  echo "🌐 Services:"
  kubectl get svc -n "$NAMESPACE"
  
  echo ""
  echo "📈 HPA:"
  kubectl get hpa -n "$NAMESPACE" 2>/dev/null || echo "(Chưa có HPA hoặc metrics-server chưa cài)"
  
  echo ""
  echo "💾 PVCs:"
  kubectl get pvc -n "$NAMESPACE"
  
  echo ""
  echo "─── Truy cập ────────────────────────────────────────"
  echo ""
  
  MINIKUBE_IP=$(minikube ip 2>/dev/null || echo "localhost")
  echo "🌍 API Gateway:  http://${MINIKUBE_IP}:30080"
  echo "📊 Grafana:      http://${MINIKUBE_IP}:30300  (admin/admin)"
  echo "🎥 LiveKit:      ws://${MINIKUBE_IP}:30880"
  echo ""
  echo "Hoặc dùng: minikube service api-gateway -n $NAMESPACE"
}

delete_all() {
  print_header "Xóa toàn bộ"
  
  read -p "⚠️  Bạn chắc chắn muốn xóa toàn bộ namespace '$NAMESPACE'? (y/N): " confirm
  if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "Đã hủy."
    exit 0
  fi
  
  kubectl delete namespace "$NAMESPACE" --ignore-not-found
  # Xóa ClusterRole/ClusterRoleBinding (nằm ngoài namespace)
  kubectl delete clusterrole hustsim-prometheus hustsim-promtail --ignore-not-found
  kubectl delete clusterrolebinding hustsim-prometheus hustsim-promtail --ignore-not-found
  
  echo "✅ Đã xóa namespace $NAMESPACE"
}

show_logs() {
  local service="${1:-}"
  if [[ -z "$service" ]]; then
    echo "Usage: ./deploy.sh logs <service-name>"
    echo "Ví dụ: ./deploy.sh logs auth-service"
    exit 1
  fi
  kubectl logs -n "$NAMESPACE" -l "app=$service" --tail=100 -f
}

# ─── Main ──────────────────────────────────────────────────

case "${1:-all}" in
  build)
    check_prerequisites
    build_images
    ;;
  apply)
    apply_manifests
    show_status
    ;;
  status)
    show_status
    ;;
  delete)
    delete_all
    ;;
  logs)
    show_logs "${2:-}"
    ;;
  all)
    check_prerequisites
    build_images
    apply_manifests
    echo ""
    echo "⏳ Chờ 30s cho pods khởi động..."
    sleep 30
    show_status
    ;;
  *)
    echo "Usage: ./deploy.sh [build|apply|status|delete|logs|all]"
    exit 1
    ;;
esac
