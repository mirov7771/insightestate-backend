ssh root@77.238.232.18
# Обновляем пакеты
sudo apt update && sudo apt upgrade -y
# Состояние диска
df -h
# Просмотр топ процессов
htop
# Просмотр открытых портов
netstat -pnltu

# Просмотр диска
df -h
# Просмотр больших директорий
du -hs * | sort -rh | head -5
# Просмотр больших файлов
find -type f -exec du -Sh {} + | sort -rh | head -n 5
# Просмотр занятого места docker
docker system df --verbose

# Генерация сертификатов
# Тестовая
sudo docker compose --file /home/nemodev/docker/docker-compose.yml run --rm certbot certonly --webroot --webroot-path /var/www/certbot/ --dry-run --email viktorusnak@mail.ru -d admin.barnhouse-village.ru -d www.admin.barnhouse-village.ru --logs-dir /var/www/certbot/
# Боевая
sudo docker compose --file /home/nemodev/docker/docker-compose.yml run --rm certbot certonly --webroot --webroot-path /var/www/certbot/ --email viktorusnak@mail.ru -d admin.barnhouse-village.ru -d www.admin.barnhouse-village.ru --logs-dir /var/www/certbot/