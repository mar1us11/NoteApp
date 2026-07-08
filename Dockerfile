FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre

ENV APP_HOME=/app
ENV JAVA_OPTS=""

WORKDIR ${APP_HOME}

RUN addgroup --system spring && adduser --system --ingroup spring spring

COPY --from=build /workspace/target/noteAppToy-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

USER spring

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
