# Stage 1: Build the application
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first to cache them (use dependency:resolve instead of go-offline to prevent fetching unneeded plugins and SSL handshake timeouts)
RUN mvn dependency:resolve -B -ntp
COPY src ./src
RUN mvn clean package -DskipTests -B -ntp

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/jlpt-backend-*.jar app.jar
EXPOSE 8080
RUN mkdir -p /app/uploads && addgroup -S spring && adduser -S spring -G spring && chown -R spring:spring /app
USER spring
ENV JAVA_OPTS="-Xmx256m -Xms128m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
