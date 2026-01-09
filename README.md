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
3) jobs - name, connection_id, storage_id, retention_count, schedule, tables (тут и колонки)
4) job_runs - job_id, started_at, finished_at, status, error_message, triggered_by, files_size