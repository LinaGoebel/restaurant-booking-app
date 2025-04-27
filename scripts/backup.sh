#!/bin/bash
# Скрипт для резервного копирования базы данных и конфигурации приложения

# Настройки
APP_NAME="restaurant-booking-app"
BACKUP_DIR="/opt/backups/${APP_NAME}"
DOCKER_COMPOSE_DIR="/opt/restaurant-booking-app"
DB_CONTAINER="restaurant-db"
DB_USER="postgres"
DB_PASSWORD="postgres.2025"
DB_NAME="restaurantdb"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/${APP_NAME}_${DATE}.tar.gz"
RETENTION_DAYS=7

# Создание директории для бэкапов, если она не существует
mkdir -p "${BACKUP_DIR}"

echo "Starting backup process for ${APP_NAME} at $(date)"

# Переход в директорию с конфигурацией Docker
cd "${DOCKER_COMPOSE_DIR}" || {
    echo "ERROR: Could not change directory to ${DOCKER_COMPOSE_DIR}"
    exit 1
}

# Проверка, запущен ли контейнер с базой данных
if ! docker ps | grep -q "${DB_CONTAINER}"; then
    echo "ERROR: Database container ${DB_CONTAINER} is not running"
    exit 1
fi

# Создание временной директории для файлов бэкапа
TMP_DIR=$(mktemp -d)
echo "Created temporary directory: ${TMP_DIR}"

# Бэкап конфигурационных файлов
echo "Backing up configuration files..."
cp -r docker-compose.yml Dockerfile .env* "${TMP_DIR}/" 2>/dev/null || true
cp -r prometheus grafana alertmanager "${TMP_DIR}/" 2>/dev/null || true

# Дамп базы данных
echo "Creating database dump..."
docker exec "${DB_CONTAINER}" pg_dump -U "${DB_USER}" -d "${DB_NAME}" -F c > "${TMP_DIR}/database.dump" || {
    echo "ERROR: Failed to create database dump"
    rm -rf "${TMP_DIR}"
    exit 1
}

# Архивирование всех файлов
echo "Creating archive..."
tar -czf "${BACKUP_FILE}" -C "${TMP_DIR}" . || {
    echo "ERROR: Failed to create backup archive"
    rm -rf "${TMP_DIR}"
    exit 1
}

# Очистка временной директории
rm -rf "${TMP_DIR}"

# Проверка успешности создания бэкапа
if [ -f "${BACKUP_FILE}" ]; then
    BACKUP_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
    echo "Backup completed successfully: ${BACKUP_FILE} (${BACKUP_SIZE})"

    # Копирование в облачное хранилище (например, AWS S3)
    if command -v aws &> /dev/null; then
        echo "Uploading backup to S3..."
        aws s3 cp "${BACKUP_FILE}" "s3://your-bucket-name/${APP_NAME}/$(basename "${BACKUP_FILE}")" || echo "Warning: S3 upload failed"
    fi

    # Удаление старых бэкапов
    echo "Removing backups older than ${RETENTION_DAYS} days..."
    find "${BACKUP_DIR}" -name "${APP_NAME}_*.tar.gz" -type f -mtime +${RETENTION_DAYS} -delete
else
    echo "ERROR: Backup file was not created"
    exit 1
fi

echo "Backup process completed at $(date)"