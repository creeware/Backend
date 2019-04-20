FROM java:8 

# Install maven
RUN apt-get update
RUN apt-get install -y maven

WORKDIR /backend

# Prepare by downloading dependencies
ADD pom.xml /code/pom.xml
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]

# Adding source, compile and package into a fat jar
ADD src /backend/src
RUN ["mvn", "package"]

EXPOSE 4567
CMD ["java", "-jar", "target/backend-1.0-jar-with-dependencies.jar"]
