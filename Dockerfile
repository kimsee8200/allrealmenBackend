FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew
RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
RUN mkdir -p /uploads && chmod 777 /uploads
COPY --from=build /workspace/app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"] 