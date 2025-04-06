FROM --platform=linux/amd64 amazoncorretto:21-alpine-jdk
WORKDIR /home/app
RUN addgroup --gid 10001 javauser && adduser -s /bin/ash -G javauser -D -H -u 10001 javauser
COPY /build/libs/*.jar /home/app/application.jar
ENV JVM_OPTS="-Xmx768m"
ENV JAR_ARGS=""
RUN chown -R javauser:javauser /home/app/application.jar
USER javauser
ENTRYPOINT java -XshowSettings:vm -XX:+PrintCommandLineFlags -Duser.timezone=GMT+3 -Dfile.encoding=UTF-8 -XX:+UseParallelGC -XX:+DisableExplicitGC -XX:+ParallelRefProcEnabled ${JVM_OPTS} -jar application.jar ${JAR_ARGS}
