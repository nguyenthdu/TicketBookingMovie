# Stage 1: Build
FROM maven:3.9.4-eclipse-temurin-17-alpine as build
WORKDIR /app
COPY . .
RUN mvn clean package -Dmaven.test.failure.ignore=true

# Stage 2: Run
FROM openjdk:17-jdk-alpine

# Set the timezone to Vietnam
ENV TZ=Asia/Ho_Chi_Minh
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime \
    && echo "Asia/Ho_Chi_Minh" > /etc/timezone \
    && apk del tzdata

COPY --from=build /app/target/TicketBookingMovie-0.0.1-SNAPSHOT.jar app.jar

# Run the application with the specified timezone
ENTRYPOINT [ "java", "-Duser.timezone=Asia/Ho_Chi_Minh", "-jar", "app.jar" ]
