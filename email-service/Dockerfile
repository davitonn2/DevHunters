FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# copy pom and download deps
COPY pom.xml .
RUN mvn dependency:go-offline -B

# copy source
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
