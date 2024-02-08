FROM gradle:8.4-jdk17-alpine AS build

ENV GRADLE_USER_HOME /.gradle

RUN mkdir /.gradle && chmod 777 /.gradle

WORKDIR /home/gradle/project

COPY --chown=gradle:gradle build.gradle.kts /home/gradle/project
COPY --chown=gradle:gradle settings.gradle.kts /home/gradle/project/

RUN gradle dependencies --no-daemon

COPY --chown=gradle:gradle . /home/gradle/project

RUN gradle bootJar --no-daemon --parallel

FROM openjdk:17-alpine

WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/ChatBoardServer.jar ./ChatBoardServer.jar

COPY ./wait_for_it.sh /wait_for_it.sh

RUN chmod 770 /wait_for_it.sh