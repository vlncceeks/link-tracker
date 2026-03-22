# LinkTracker

LinkTracker – Telegram-бот, который отслеживает изменения на веб-страницах и оперативно информирует пользователя о них.

Перед запуском необходимо добавить переменные окружения:
- TELEGRAM_TOKEN,
- GITHUB_TOKEN, STACKOVERFLOW_KEY,
- STACKOVERFLOW_ACCESS_KEY,
- POSTGRES_DB,
- POSTGRES_USER,
- POSTGRES_PASSWORD
- ACCESS_TYPE

Для локального запуска бота либо запустить сервисы через интерфейс IDE, либо выполнить команды:

```shell
mvn spring-boot:run -pl bot
mvn spring-boot:run -pl scrapper
```

Команда для поднятия базы данных:

```shell
docker-compose -f docker-compose-local.yml up -d
```

Команда, чтобы полностью развернуть проект в докере:

```shell
docker-compose -f docker-compose.yml up -d
```

Полезную для разработки проекта информацию вы можете найти в файле [HELP.md](./HELP.md).
