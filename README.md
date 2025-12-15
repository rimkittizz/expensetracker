# Приложение для записи личных трат
Это приложение для учёта личных расходов по 15 категориям с записью дат и возможностью просмотра детальной статистики и выгрузки отчётов в Excel
## Требования для сборки и запуска
JDK 17+\
Gradle 8+
## Сборка
### Клонировать репозиторий
https://github.com/rimkittizz/expensetracker.git
## JavaFX
### Запуск через консоль
./gradlew run
## Консольная версия
### Сборка jar-файла
./gradlew clean jar
### Запуск jar-файла через консоль
java -jar build\libs\ExpenseTracker.jar
## Генерация JavaDoc документации
./gradlew javadoc \
Документация: build/docs/javadoc/index.html
