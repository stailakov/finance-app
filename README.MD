## CRUD для учета финансов
### Возможности
#### Просмотр и редактирование данных
- Просмотр с постраничным выводом
- Добавлять (кнопка New)
- Удалять (выделить строку с помощью чекбокса. Появится кнопка DELETE. Доступно удаление нескольких строк)
- Редактировать (выделить строку с помощью чекбокса. Появится кнопка UPDATE. Доступно обновление нескольких строк)

#### Статистика
- Реализован график трат с группировкой трат по месяцам. График перестраивается без перезагрузки страницы.
- Виджет затрат текущего месяца.

### Стек
#### Frontend
- ClojureScript
- reagent + material UI
- react-vis
- cljs.ajax

#### Backend
- Clojure
- next.jdbc
- honeqsql
- ring
- compojure

### Запуск
#### local dev
- Необходимо наличие postgres и выполнение скрипта finance-server/db/init.sql
- Затем запустить backend
```bash
cd finance-server
lein ring server
```
- и frontend
```bash
cd ../finance-ui
npm install react react-dom react-vis
shadow-cljs watch app
```

#### docker-compose
- Необходимо изменить адрес подключения к бд в файле finance-server/src/finance_server/data.clj на сервис БД указанный в docker-compose (db)
То есть конфигурация подключения будет выглядеть так
```

(def db-config
  {:dbtype "postgresql"
   :dbname "finance"
   :host "db"
   :user "postgres"
   :password "postgres"})

```
- Затем запустить
```bash
docker-compose up
```
