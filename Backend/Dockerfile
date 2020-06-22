# Start with a base image containing Java runtime
FROM openjdk:11-jre-slim

# Add Maintainer Info
LABEL maintainer="yesilyurt.selim@metu.edu.tr"

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8083 available to the world outside this container
EXPOSE 8083

# The application's jar file
ARG JAR_FILE=target/vipassistantbackend-1.0-SNAPSHOT.jar

# Add the application's jar to the container
ADD ${JAR_FILE} vipassistantbackend.jar

# Run the jar file
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/vipassistantbackend.jar"]

#######
# sudo docker build -t vipassistantbackend .
# sudo docker run -e"SPRING_PROFILES_ACTIVE=prod" --network="host" -d --rm --name vipassistantbackend vipassistantbackend
#######