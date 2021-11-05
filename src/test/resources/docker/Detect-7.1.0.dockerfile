FROM gradle:5.2.0-jdk11-slim

ENV SRC_DIR=/opt/project/src
ENV JAVA_TOOL_OPTIONS="-Dhttps.protocols=TLSv1.2"

USER root

# Install git
RUN apt-get update
RUN apt-get install -y git

# Set up the test project
RUN mkdir -p ${SRC_DIR}

RUN git clone --depth 1 -b 7.1.0 https://github.com/blackducksoftware/synopsys-detect ${SRC_DIR}

RUN cd ${SRC_DIR} \
   && ./gradlew build