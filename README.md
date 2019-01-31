# TwitterStalkerBot
This is my non-commercial project TwitterStalkerBot — a Telegram Bot able to send real-time notifications about new and/or deleted tweets from chosen Twitter users.

Bot is available at [@StalkerTwitterBot]( https://t.me/StalkerTwitterBot)

# Usage
Change ```config.properties``` to run the application with appropriate credentials. Build project with Maven ```mvn clean package``` and run it with Java ```java -jar <path_to_jar>```.

# Refinements
* Change bot type from Long Polling to Web Hook
* Store and reproduce tweet's media

# Project stack
* [Telegram Bot Java Library](https://github.com/rubenlagus/TelegramBots), [Twitter4J](https://github.com/Twitter4J/Twitter4J)
* [MongoDB Java Driver](https://github.com/mongodb/mongo-java-driver), [MySQL Connector/J](https://github.com/mysql/mysql-connector-j), [Hibernate ORM](https://github.com/hibernate/hibernate-orm)
* [Mockito](https://github.com/mockito/mockito), [JUnit](https://github.com/junit-team/junit4), [Embedded MongoDB](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo)
* [Apache log4j](https://logging.apache.org/log4j/1.2/)
