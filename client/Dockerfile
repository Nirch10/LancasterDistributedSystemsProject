FROM java:8-jdk-alpine
COPY Client.jar /Client.jar
CMD ["/usr/bin/java", "-jar", "-Dspring.profiles.active=default", "/Client.jar"]
