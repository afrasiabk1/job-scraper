FROM openjdk:17
EXPOSE 8080
ADD target/jobs-scraper.jar jobs-scraper.jar
ENTRYPOINT ["java", "-jar", "/jobs-scraper.jar"]