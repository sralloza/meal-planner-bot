FROM openjdk:11-jre-slim AS build

WORKDIR /home/gradle

COPY src/ /home/gradle/src/
COPY gradle/ /home/gradle/gradle/
COPY build.gradle settings.gradle gradlew /home/gradle/

RUN ./gradlew test --scan
RUN ./gradlew fat -i

FROM openjdk:11-jre-slim

RUN mkdir /app

COPY --from=build /home/gradle/build/libs/*.jar /app/meal-planner-bot.jar

ENTRYPOINT [ "java", "-jar", "/app/meal-planner-bot.jar" ]
