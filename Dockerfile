FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY tasknest/pom.xml ./pom.xml
COPY tasknest/src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/target/tasknest-0.0.1-SNAPSHOT.jar /app/tasknest.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/tasknest.jar"]