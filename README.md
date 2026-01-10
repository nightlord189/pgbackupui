# pgbackupui

Страничка, где показываем список джоб с расписанием с возможностью отредактировать, удалить, создать новую и просмотреть логи запуска каждой джобы.
Для каждой джобы вводим данные для подключения (хост, порт, юзер, пароль), потом селектим список таблиц с полями, выводим их юзеру и он может прокликать - отметить нужные таблицы и поля. 
Затем выбираем, куда сохранять результат (в виде .sql файлов - one file (insert) per table) - локально или в s3-бакет. 
Настраиваем retention - храним только N последних бэкапов.

Используемые технологии: 
- Spring Boot
- MongoDB как хранилище самого сервиса
- PostgreSQL
- AWS S3

Что храним?
1) connections - name, type (postgresql), host, port, db, username, password
2) storages - name, type (local, s3), а дальше уже специфичные поля
3) jobs - name, connection_id, storage_id, retention_count, schedule, tables (тут и колонки опционально, либо селектим все (*))
4) job_runs - job_id, folder_name, started_at, finished_at, status, error_message, triggered_by, files_size

## Запуск

### 1. Запуск MongoDB (обязательно)
```bash
make run
# или
docker-compose up -d
```

- База: `pgbackupui`
- Пользователь: `admin`
- Пароль: `password`

### 2. Запуск тестовой PostgreSQL базы (опционально)
```bash
make run-test-db
# или
docker-compose -f docker-compose-test.yml up -d
```

Параметры подключения к тестовой БД:
- Host: `localhost`
- Port: `5433`
- Database: `testdb`
- Username: `postgres`
- Password: `postgres`
- Таблицы: `users` (3 записи), `orders` (4 записи)

### 3. Запуск
```bash
mvn spring-boot:run
```

Доступно по http://localhost:8080
