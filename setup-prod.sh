#!/bin/bash
# ============================================================
# setup-prod.sh — Script de configuração inicial para produção
# Execute ONCE no servidor antes de subir a aplicação.
# ============================================================

set -e

echo "============================================"
echo "  Saúde Ocupacional — Setup de Produção"
echo "============================================"

# 1) Gerar JWT_SECRET forte
JWT_SECRET=$(openssl rand -hex 64 2>/dev/null || python3 -c "import secrets; print(secrets.token_hex(64))")
echo ""
echo "[1/5] JWT_SECRET gerado:"
echo "  JWT_SECRET=$JWT_SECRET"

# 2) Solicitar senha do banco
echo ""
echo "[2/5] Informe a senha do banco de dados PostgreSQL (deixe em branco para gerar):"
read -s -p "DB_PASSWORD: " DB_PASSWORD
if [ -z "$DB_PASSWORD" ]; then
    DB_PASSWORD=$(openssl rand -base64 24)
    echo "(senha gerada automaticamente)"
fi

# 3) Solicitar senha do admin
echo ""
echo "[3/5] Informe a senha inicial do administrador:"
read -s -p "ADMIN_PASSWORD: " ADMIN_PASSWORD
if [ -z "$ADMIN_PASSWORD" ]; then
    echo "ERRO: ADMIN_PASSWORD não pode ser vazia!"
    exit 1
fi

# 4) Solicitar IP/hostname do servidor
echo ""
echo "[4/5] IP ou hostname do servidor (ex: 192.168.1.100):"
read -p "SERVER_IP: " SERVER_IP

# 5) Gerar .env de produção
echo ""
echo "[5/5] Gerando arquivo .env de produção..."

cat > .env << EOF
# ============================================================
# SAÚDE OCUPACIONAL — PRODUÇÃO (gerado em $(date))
# NÃO versionar este arquivo!
# ============================================================

DB_HOST=localhost
DB_PORT=5432
DB_NAME=saude_ocupacional
DB_USER=saude_user
DB_PASSWORD=${DB_PASSWORD}

SERVER_PORT=8080

JWT_SECRET=${JWT_SECRET}
JWT_EXPIRATION=28800000

CORS_ORIGINS=http://${SERVER_IP}:8080

ADMIN_PASSWORD=${ADMIN_PASSWORD}

SPRING_PROFILES_ACTIVE=prod

LOGS=/var/log/saude-ocupacional
EOF

echo ""
echo "✅ .env gerado com sucesso!"
echo ""
echo "Próximos passos:"
echo "  1. Crie o banco PostgreSQL:"
echo "     sudo -u postgres psql -c \"CREATE USER saude_user WITH PASSWORD '${DB_PASSWORD}';\""
echo "     sudo -u postgres psql -c \"CREATE DATABASE saude_ocupacional OWNER saude_user;\""
echo "  2. Suba a aplicação:"
echo "     docker-compose up --build -d"
echo "     # OU sem Docker:"
echo "     java -jar target/saude-ocupacional-1.0.0.jar"
echo "  3. Acesse: http://${SERVER_IP}:8080"
echo "  4. Login: admin / (a senha que você informou)"
echo "  5. TROQUE A SENHA DO ADMIN imediatamente após o primeiro login!"
echo ""
