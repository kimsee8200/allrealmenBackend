FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew
RUN ./gradlew build -x test

RUN ls -l /workspace/app/build/libs/ && \
    THE_JAR=$(find /workspace/app/build/libs/ -name '*.jar' -type f -print -quit) && \
    if [ -z "$THE_JAR" ]; then echo "Error: No JAR file found in /workspace/app/build/libs/" && exit 1; fi && \
    echo "Found JAR file: $THE_JAR" && \
    mkdir -p /workspace/app/build/dependency && \
    echo "Extracting $THE_JAR to /workspace/app/build/dependency/" && \
    (cd /workspace/app/build/dependency && jar -xf "$THE_JAR") && \
    echo "Contents of /workspace/app/build/dependency after extraction:" && \
    ls -R /workspace/app/build/dependency && \
    if [ ! -d "/workspace/app/build/dependency/BOOT-INF/classes" ]; then echo "Error: BOOT-INF/classes not found in /workspace/app/build/dependency after extraction. Check JAR structure and extraction logs." && exit 1; fi

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/build/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.example.allrealmen.AllrealmenApplication"] 