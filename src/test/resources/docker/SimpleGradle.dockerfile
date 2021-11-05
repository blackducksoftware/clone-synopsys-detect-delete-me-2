FROM gradle:5.2.0-jdk8-slim

ENV SRC_DIR=/opt/project/src
ENV JAVA_TOOL_OPTIONS="-Dhttps.protocols=TLSv1.2"

USER root

# Install git
RUN apt-get update
RUN apt-get install -y git

# Set up the test project
RUN mkdir -p ${SRC_DIR}

RUN git clone --depth 1 https://github.com/jabedhasan21/java-hello-world-with-gradle ${SRC_DIR}

RUN cd ${SRC_DIR} \
   && ./gradlew build