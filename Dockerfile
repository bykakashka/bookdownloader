FROM openjdk:8-jdk-alpine
COPY ./build/libs/bookdownloader-1.0-SNAPSHOT-all.jar /usr/work/service.jar
WORKDIR /usr/work
CMD ["java", "-jar", "service.jar"]