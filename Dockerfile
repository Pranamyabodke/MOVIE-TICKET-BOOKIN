# Multi-stage Dockerfile for CineShow Movie Ticket Booking System
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app
COPY . .

# Compile all Java sources
RUN mkdir -p bin && \
    javac -encoding UTF-8 -d bin $(find src/main/java -name "*.java")

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /app/bin ./bin
COPY --from=build /app/src/main/resources ./src/main/resources
COPY --from=build /app/data ./data

# Expose web server port
EXPOSE 8080

# Run CineShow Web Server
CMD ["java", "-cp", "bin", "com.movieticket.Main", "8080"]

