release: ./mvnw flyway:migrate
web: java $JAVA_OPTS -jar target/dependency/webapp-runner.jar --port $PORT target/simple-rest-1.1-SNAPSHOT.war