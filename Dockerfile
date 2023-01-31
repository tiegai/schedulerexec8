FROM artifactory.nike.com:9002/cafi/nike-debian-slim-java11-jre-foundation:latest

VOLUME /tmp

WORKDIR /opt/app
COPY build/libs/*.jar /opt/app/app.jar

EXPOSE 8080 9080

#TODO , add JVM params
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Dsfx_org_name=Commerce", "-Dspring.profiles.active=${CLOUD_ENVIRONMENT}",  "-jar","/opt/app/app.jar"]