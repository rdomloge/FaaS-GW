FROM ubuntu

RUN apt-get update
RUN apt-get install -y net-tools
RUN apt-get install -y iputils-ping
RUN apt-get install -y iproute2

RUN apt-get install -y git

RUN apt-cache search maven
RUN apt-get install -y maven

RUN git clone git://github.com/rdomloge/FaaS-DTO.git
RUN mvn -f FaaS-DTO/pom.xml install

RUN git clone -b master git://github.com/rdomloge/FaaS-GW.git
RUN mvn -f FaaS-GW/pom.xml package

RUN echo "#!/bin/bash" >> run-gw.sh
RUN echo "java -jar FaaS-GW/target/faas-gw-0.0.1-SNAPSHOT.war \
--routing-key=ramsay \
--RMQ_USER=faas-gw \
--RMQ_PASSWORD=0pen5esame" \
>> run-gw.sh
RUN chmod a+x run-gw.sh

EXPOSE 8080