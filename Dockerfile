FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /TicketMookingMovie
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src /TicketMookingMovie/src
RUN mvn package -DskipTests

FROM openjdk:17-jdk
COPY --from=build /TicketMookingMovie/out/artifacts/TicketMookingMovie.jar /TicketMookingMovie.jar
CMD ["java", "-jar", "/TicketMookingMovie.jar"]
