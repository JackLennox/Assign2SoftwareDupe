# Use OpenJDK runtime as the parent image
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the project JAR file into the container
COPY target/musicFinder-0.0.1-SNAPSHOT.jar app.jar

# Expose the port that the app runs on
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]