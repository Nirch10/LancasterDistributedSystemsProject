FROM java:8-jdk-alpine
COPY server.jar /server.jar
Expose 8080
CMD ["/usr/bin/java", "-jar", "-Dspring.profiles.active=default", "/server.jar"]
