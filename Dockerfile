FROM java:8-jdk

WORKDIR /usr/bin

VOLUME /root/.m2

run curl -sLO http://mirror.dkm.cz/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip && unzip apache-maven-3.3.9-bin.zip && ln -s apache-maven-3.3.9 mvn && rm apache-maven-3.3.9-bin.zip

ENV M2_HOME /usr/bin/mvn
ENV PATH $PATH:$M2_HOME/bin

WORKDIR /chobot-containerAPI
COPY target/container_api.jar /container_api.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=docker","/container_api.jar"]
