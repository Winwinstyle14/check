FROM openjdk:11.0.13-jre

RUN addgroup --gid 1001 --system spring && adduser --system --uid 1001 -gid 1001 spring
USER spring:spring

ARG JAR_FILE=target/*.jar

WORKDIR /u01/app

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
