FROM openjdk:11.0.13

RUN addgroup --gid 1001 --system spring && adduser --system --uid 1001 -gid 1001 spring
USER spring:spring

ARG JAR_FILE=target/*.jar
ARG RESOURCE_FILE=build/resources/main/rsa/

WORKDIR /u01/app

RUN mkdir -p resources/rsa

COPY ${RESOURCE_FILE} resources/rsa/
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
