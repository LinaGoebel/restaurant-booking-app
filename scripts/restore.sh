#!/bin/bash
# Скрипт для восстановления из резервной копии

# Проверка наличия аргумента с путем к файлу бэкапа
if [ $# -ne 1 ]; then
    echo "Usage: $0 <backup_file>"
    echo "Example: $0 /opt/backups/restaurant-booking-app/restaurant-booking-app_20250427_120000.tar.gz"
    exit 1
fi

BACKUP_FILE="$1"

# Проверка существования файла бэкапа
if [ ! -f "${BACKUP_FILE}" ]; then
    echo "ERROR: Backup file not found: ${BACKUP_FILE}"
    exit 1
fi

# Настройки
APP_NAME="restaurant-booking-app"
DOCKER_COMPOSE_DIR="/opt/restaurant-booking-app"
DB_CONTAINER="restaurant-db"
DB_USER="postgres"
DB_PASSWORD="postgres.2025"
DB_NAME="restaurantdb"

echo "Starting restoration process from ${BACKUP_FILE} at $(date)"

# Создание временной директории для распаковки бэкапа
TMP_DIR=$(mktemp -d)
echo "Created temporary directory: ${TMP_DIR}"

# Распаковка архива
echo "Extracting backup archive..."
tar -xzf "${BACKUP_FILE}" -C "${TMP_DIR}" || {
    echo "ERROR: Failed to extract backup archive"
    rm -rf "${TMP_DIR}"
    exit 1
}

# Проверка наличия файла дампа базы данных
if [ ! -f "${TMP_DIR}/database.dump" ]; then
    echo "ERROR: Database dump not found in backup archive"
    rm -rf "${TMP_DIR}"
    exit 1
fi

# Проверка, запущен ли контейнер с базой данных
if ! docker ps | grep -q "${DB_CONTAINER}"; then
    echo "ERROR: Database container ${DB_CONTAINER} is not running"
    echo "Starting Docker containers..."
    cd "${DOCKER_COMPOSE_DIR}" || {
        echo "ERROR: Could not change directory to ${DOCKER_COMPOSE_DIR}"
        rm -rf "${TMP_DIR}"
        exit 1
    }
    docker-compose up -d db || {
        echo "ERROR: Failed to start database container"
        rm -rf "${TMP_DIR}"
        exit 1
    }

    # Ожидание запуска базы данных
    echo "Waiting for database to start..."
    sleep 30
fi

# Остановка приложения, оставив базу данных работающей
echo "Stopping application container..."
cd "${DOCKER_COMPOSE_DIR}" || {
    echo "ERROR: Could not change directory to ${DOCKER_COMPOSE_DIR}"
    rm -rf "${TMP_DIR}"
    exit 1
}
docker-compose stop restaurant-app || echo "Warning: Failed to stop application container"

# Восстановление конфигурационных файлов
echo "Restoring configuration files..."
cp -r "${TMP_DIR}"/docker-compose.yml "${TMP_DIR}"/Dockerfile "${TMP_DIR}"/.env* "${DOCKER_COMPOSE_DIR}"/ 2>/dev/null || true
mkdir -p "${DOCKER_COMPOSE_DIR}/prometheus" "${DOCKER_COMPOSE_DIR}/grafana" "${DOCKER_COMPOSE_DIR}/alertmanager" 2>/dev/null || true
cp -r "${TMP_DIR}"/prometheus/* "${DOCKER_COMPOSE_DIR}/prometheus"/ 2>/dev/null || true
cp -r "${TMP_DIR}"/grafana/* "${DOCKER_COMPOSE_DIR}/grafana"/ 2>/dev/null || true
cp -r "${TMP_DIR}"/alertmanager/* "${DOCKER_COMPOSE_DIR}/alertmanager"/ 2>/dev/null || true

# Восстановление базы данных
echo "Restoring database..."
# Сначала создаем пустую базу данных (с удалением старой, если она существует)
docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '${DB_NAME}';" postgres || true
docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -c "DROP DATABASE IF EXISTS ${DB_NAME};" postgres || true
docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -c "CREATE DATABASE ${DB_NAME};" postgres || true

# Восстановление данных из дампа
cat "${TMP_DIR}/database.dump" | docker exec -i "${DB_CONTAINER}" pg_restore -U "${DB_USER}" -d "${DB_NAME}" || {
    echo "ERROR: Failed to restore database from dump"
    rm -rf "${TMP_DIR}"
    exit 1
}

# Очистка временной директории
rm -rf "${TMP_DIR}"

# Запуск приложения
echo "Starting application..."
docker-compose up -d || {
    echo "ERROR: Failed to start application"
    exit 1
}

echo "Restoration process completed at $(date)"
echo "Please verify that the application is working correctly."