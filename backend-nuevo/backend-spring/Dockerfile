FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
# Copiar solo código Java primero
COPY src/main/java ./src/main/java
# Compilar sin resources
RUN mvn compile -DskipTests
# Ahora copiar resources
COPY src/main/resources ./src/main/resources
# Empaquetar sin procesar resources
RUN mvn package -DskipTests -Dmaven.resources.filtering=false

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]