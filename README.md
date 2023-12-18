# Summary
###Technologies used:
- Java 17
- Spring boot.
- Feign client
- Kafka as a fallback mechanism
- Monitoring ( Prometheus ) and Tracing
- findbugs plugin
- Cucumber for behavioural testing, Jacoco for tests coverage.
- Wiremock
- Docker-ization
- Lomobok, mapstruct
###HOW TO RUN:
`docker-compose up -d`

This will deploy:
- Kafka, Control center for visualizing Kafka.
- 2 instances of Game-of-Three game: 1 for each player

#How to Play the game:
Each player will need to use different urls bases namely:
- localhost:8080 & localhost:8081

- Start by submitting a game to any process.`POST /submit-game`
- User get games endpoint to regularly check for game changes `GET /game`
- Take turns by using `POST /update-number`


PS: in case 1 of the player's Process was down, a message will be pushed to a Kafka topic to be handled later when the process is started!

####For further flow check _GameOfThree.feature_
