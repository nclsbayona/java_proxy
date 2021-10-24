FROM sgrio/java:jdk_15_ubuntu
RUN apt-get update && apt-get upgrade -y && apt-get install -y maven
WORKDIR /home/Documents
COPY ./pom.xml .
COPY ./src/ ./src
RUN mvn clean install
ENTRYPOINT ["mvn", "exec:java"]