Задание 1 
Модель проекта:
Сущность Client:

- id
- Три поля ФИО
- clientId (Long или UUID)

Сущность Account со следующими атрибутами:

- id клиента
- Дебетовый счет или кредитный (enum)
- Баланс

Сущность Transaction со следующими атрибутами:

- id счета
- Сумма транзакции
- Время транзакции

Cущность DataSourceErrorLog:

- Текст стектрейса исключения
- Сообщение
- Сигнатура метода

1.1 Контроллеры к Account и Transaction

2. Сгенерировал набор данных по сущностям, написав генератор

3. Разработан аспект:

3.1 Аспект @LogDataSourceErrorAspect логирующий сообщения об исключениях в проекте путем создания в БД новой записи DataSourceErrorLog в случае, если в результате CRUD-операций над сущностями возникла ошибка

Задание 2 
Разработать аспект @Metric:

- Измеряет время работы метода

- Если время работы метода выше лимита, создает запись в БД в таблицу time_limit_exceed_log

- Лимит выполнения устанавливается в application.yml

Разработать аспект @Cached

- Кэширует записи из БД

- Перед выполнением запроса в БД кэш должен проверяться на наличие нужной записи

- Если таковой нет - сделать запрос

- Время хранения записи в кэше задать в application.yml

- По истечению времени запись должна удаляться из кэша

Задание 3
Изменить аспект @Metric:

- Если аннотированный метод превышает лимит времени работы N, то сообщение должно отправляться в топик Kafka t1_demo_metrics

- В заголовке сообщения должен указываться тип ошибки METRICS

- В случае недоступности топика или неудачной отправки сообщения - создать запись в БД.

Разработать аспект @LogDatasourceError

- В первую очередь аспект должен отсылать сообщение в топик t1_demo_metrics.

- В заголовке должен указываться тип ошибки: DATA_SOURCE;

- В случае, если отправка не удалась - записать в таблицу БД data_source_error_log

Задание 4
1. В основном модуле:

1.1 Сущности Transaction:

- добавить статус [ACCECPTED, REJECTED, BLOCKED, CANCELLED, REQUESTED]

- добавить уникальный transactionId

- добавить timestamp

1.2 Сущности Account:

- добавить статус [ARRESTED, BLOCKED, CLOSED, OPEN]

- добавить уникальный accountId

- добавить поле frozenAmount;

1.3 Сущности Client добавить уникальный clientId

2. Сервис при получении сообщения из топика t1_demo_transactions:

- проверяет статус счета: если статус OPEN, то:

- сохраняет транзакцию в БД со статусом REQUESTED

- изменяет счет клиента на сумму транзакции, отправляет сообщение в топик t1_demo_transaction_accept с информацией {clientId, accountId, transactionId, timestamp, transaction.amount, account.balance}

3. Разработать сервис 2 (приложение, новый модуль):

3.1 Сервис слушает топик t1_demo_transaction_accept

При получении сообщения:

- Если транзакции по одному и тому же клиенту и счету приходят больше N раз в Т времени (настраивается в конфиге) и timestamp транзакции попадает в этот период, то N транзакциям присвоить статус BLOCKED, сообщение со статусом, id счета и id транзакции отправить в топик t1_demo_transaction_result

- Если сумма списания в транзакции больше, чем баланс счета - отправить сообщение со статусом REJECTED

- Если всё ок, то статус ACCECPTED

4. Сервис 1 теперь слушает еще и топик t1_demo_transaction_result:

- При получении сообщения со статусом ACCECPTED - обновляет статус транзакции в БД

- При получении BLOCKED - обновляет транзакциям статусы в БД и выставляет счёту статус BLOCKED. Баланс счёта меняется следующим образом: счет корректируется на сумму заблокированных транзакций, сумма записывается в поле frozenAmount

- При получении REJECTED - обновляет статус транзакции, и на сумму транзакции изменяет баланс