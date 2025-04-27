#!/bin/bash
# Скрипт для настройки crontab для автоматического резервного копирования

# Настройки
BACKUP_SCRIPT="/opt/restaurant-booking-app/scripts/backup.sh"
LOG_DIR="/var/log/restaurant-backup"

# Создание директории для логов
mkdir -p "${LOG_DIR}"
chmod 755 "${LOG_DIR}"

# Проверка существования скрипта резервного копирования
if [ ! -f "${BACKUP_SCRIPT}" ]; then
    echo "ERROR: Backup script not found: ${BACKUP_SCRIPT}"
    exit 1
fi

# Установка прав на исполнение для скрипта
chmod +x "${BACKUP_SCRIPT}"

# Создание временного файла для crontab
TEMP_CRON=$(mktemp)

# Экспорт текущего crontab
crontab -l > "${TEMP_CRON}" 2>/dev/null || echo "# New crontab" > "${TEMP_CRON}"

# Проверка, существует ли уже задание для бэкапа
if grep -q "${BACKUP_SCRIPT}" "${TEMP_CRON}"; then
    echo "Backup cron job already exists"
    rm "${TEMP_CRON}"
    exit 0
fi

# Добавление записи для ежедневного бэкапа в 2:00 ночи
echo "# Ежедневное резервное копирование ресторанного приложения в 2:00" >> "${TEMP_CRON}"
echo "0 2 * * * ${BACKUP_SCRIPT} > ${LOG_DIR}/backup_\$(date +\%Y\%m\%d).log 2>&1" >> "${TEMP_CRON}"

# Установка нового crontab
crontab "${TEMP_CRON}"
rm "${TEMP_CRON}"

echo "Crontab setup completed successfully"
echo "Daily backup will run at 2:00 AM and log to ${LOG_DIR}"