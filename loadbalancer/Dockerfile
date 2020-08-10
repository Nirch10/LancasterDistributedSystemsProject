FROM java:8-jdk-alpine
COPY balancer.jar /balancer.jar
Expose 9090
CMD ["/usr/bin/java", "-jar", "-Dspring.profiles.active=default", "/balancer.jar"]
