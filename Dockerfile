# Use an image with Java 17
FROM openjdk:17-jdk AS build

# Set the working directory inside the container
WORKDIR /app

# Download Maven 3.2.1
RUN wget --no-verbose -O /tmp/apache-maven-3.2.1.tar.gz http://archive.apache.org/dist/maven/maven-3/3.2.1/binaries/apache-maven-3.2.1-bin.tar.gz

# Extract Maven
RUN tar xzf /tmp/apache-maven-3.2.1.tar.gz -C /opt/

# Set up Maven environment variables
ENV MAVEN_HOME /opt/apache-maven-3.2.1
ENV PATH $MAVEN_HOME/bin:$PATH

# Copy the pom.xml file
COPY pom.xml .

# Copy the source code
COPY src ./src

# Build the application
RUN /opt/apache-maven-3.2.1/bin/mvn package

# Use a lightweight base image to run the application
FROM openjdk:17-jdk-slim AS final

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the build stage to the final stage
COPY --from=build /app/target/*.jar app.jar

# Command to run the application
CMD ["java", "-jar", "app.jar"]
