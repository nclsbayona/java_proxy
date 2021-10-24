FROM sgrio/java:jdk_15_ubuntu
RUN apt-get update && apt-get upgrade -y && apt-get install -y maven
WORKDIR /usr/local/service
COPY pom.xml ./pom.xml
COPY src ./src
RUN mvn package
ENTRYPOINT ["mvn","exec:java"]