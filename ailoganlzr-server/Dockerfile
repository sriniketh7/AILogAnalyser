FROM maven:3.9-eclipse-temurin-26 AS build

WORKDIR /workspace

COPY pom.xml .
COPY ailoganlzr-client ./ailoganlzr-client
COPY ailoganlzr-server ./ailoganlzr-server

RUN mvn clean package -DskipTests

FROM eclipse-temurin:26-jre

WORKDIR /app

COPY --from=build \
    /workspace/ailoganlzr-server/target/ailoganlzr-server-0.0.1-SNAPSHOT.jar \
    app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]