#This tests a customer issue from IDETECT-2714
FROM maven:3-jdk-8-alpine

# Do not change SRC_DIR, value is expected by tests
ENV SRC_DIR=/opt/project/src

# Install git: https://github.com/nodejs/docker-node/issues/586
RUN apk update && apk upgrade && \
    apk add --no-cache bash git openssh

#Install SBT
WORKDIR /home/app
RUN wget -q "https://github.com/sbt/sbt/releases/download/v1.5.2/sbt-1.5.2.tgz"
RUN tar -xzf sbt-1.5.2.tgz
RUN rm sbt-1.5.2.tgz
ENV PATH="/home/app/sbt/bin:${PATH}"

# Set up the test project
RUN mkdir -p ${SRC_DIR}

RUN git clone --depth 1 https://github.com/aiyanbo/sbt-simple-project.git ${SRC_DIR}

RUN cd ${SRC_DIR} \
   && sbt compile