FROM gradle:8.4-jdk17-alpine AS build

WORKDIR /home/gradle/project

COPY --chown=gradle:gradle build.gradle.kts /home/gradle/project

RUN gradle dependencies --no-daemon

COPY --chown=gradle:gradle . /home/gradle/project

RUN gradle bootJar --no-daemon --parallel

FROM openjdk:17-alpine

WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/ChatBoardServer_new.jar ./ChatBoardServer_new.jar

COPY ./wait_for_it.sh /wait_for_it.sh

RUN chmod 770 /wait_for_it.sh