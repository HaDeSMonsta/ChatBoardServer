# Use official gradle image as the builder
FROM gradle:8.4-jdk17 AS build

# Set the working directory
WORKDIR /home/gradle/project

# Copy your source code into the container
COPY --chown=gradle:gradle . /home/gradle/project

# Run the gradle build
RUN gradle bootJar --no-daemon

# Use OpenJDK for runtime
FROM openjdk:17

# Set the working directory
WORKDIR /app

# Copy the jar file from the builder to the current location
COPY --from=build /home/gradle/project/build/libs/ChatBoardServer_new.jar ./ChatBoardServer_new.jar

# Copy the wait_for_me.sh script
COPY ./wait_for_it.sh /wait_for_it.sh
RUN chmod 770 /wait_for_it.sh