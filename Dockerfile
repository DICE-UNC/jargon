# OPEN JDK 11 test harness, meant for use with base servers in docker-compose under docker-test-framework. This image will load the source tree,
# build it, and run the unit tests against those docker-compose images
# docker build -t diceunc/jargon-test:latest .
# docker run -it -v /Users/conwaymc/Documents/workspace-niehs-rel/jargon:/var/jargon diceunc/jargon-test:latest 
FROM adoptopenjdk/openjdk11
RUN apt-get update
RUN apt-get install wget unzip -y
RUN wget http://apache.cs.utah.edu/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.zip
RUN mkdir /var/maven
RUN mkdir /var/irodsscratch
RUN unzip apache-maven-3.6.3-bin.zip -d /var/maven
RUN mkdir /var/jargon
WORKDIR /var/jargon

ENTRYPOINT ["sh"]

# /var/maven/apache-maven-3.6.3/bin/mvn
# docker run -it -v /Users/conwaymc/Documents/workspace-niehs-rel/jargon:/var/jargon --network="4-2_irodsnet" diceunc/jargon-test:latest 