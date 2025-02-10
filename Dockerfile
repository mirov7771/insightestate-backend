ARG GRADLE_DOCKER_IMAGE="gradle:8.4.0-jdk21-alpine"
ARG JDK_DOCKER_IMAGE="amazoncorretto:21-alpine-jdk"

FROM ${GRADLE_DOCKER_IMAGE} AS build
WORKDIR /home/gradle
COPY ./build.gradle.kts ./build.gradle.kts
COPY ./settings.gradle.kts ./settings.gradle.kts
COPY ./src ./src
COPY ./.git ./.git
RUN gradle clean assemble --no-daemon

FROM ${JDK_DOCKER_IMAGE}
RUN addgroup --gid 10001 javauser && adduser -s /bin/ash -G javauser -D -H -u 10001 javauser
COPY --from=build /home/gradle/build/libs/*.jar /application.jar
ENV JVM_OPTS="-XX:InitialRAMPercentage=30 -XX:MaxRAMPercentage=60"
ENV JAR_ARGS=""
RUN chown -R javauser:javauser ./application.jar
USER javauser
ENTRYPOINT java -XshowSettings:vm -XX:+PrintCommandLineFlags -Duser.timezone=GMT+3 -Dfile.encoding=UTF-8 -XX:+UseParallelGC -XX:+DisableExplicitGC -XX:+ParallelRefProcEnabled ${JVM_OPTS} -jar application.jar ${JAR_ARGS}
