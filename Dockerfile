# Use official gradle image as the builder
FROM gradle:8.4-jdk20 AS build

# Set the working directory
WORKDIR /home/gradle/project

# Copy your source code into the container
COPY --chown=gradle:gradle . /home/gradle/project

# Run the gradle build
RUN gradle build --no-daemon

# Use OpenJDK for runtime
FROM openjdk:20

# Set the working directory
WORKDIR /app

# Copy the jar file from the builder to the current location
COPY --from=build /home/gradle/project/build/libs/*.jar ./ChatBoardServer_new.jar

# Run the jar file
CMD ["java", "-jar", "./ChatBoardServer_new.jar"]
