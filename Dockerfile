  GNU nano 7.2                                           Dockerfile                                                     # Etapa 1: build con Maven + JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /Nebula

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: imagen ligera de ejecución
FROM eclipse-temurin:21-jdk
WORKDIR /Nebula
COPY --from=build /Nebula/target/*.jar Nebula.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "Nebula.jar"]