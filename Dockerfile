FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/myapp.jar /myapp/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/myapp/app.jar"]
