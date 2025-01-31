FROM openjdk:17-jdk-alpine
WORKDIR /app

ARG JAR_FILE=target/back-duoc-json-writer-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Crear directorio para guardar los JSONs
RUN mkdir -p /app/json_data && chmod -R 777 /app/json_data

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
