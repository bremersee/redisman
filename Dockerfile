FROM openjdk:11-jdk-slim
MAINTAINER Christian Bremer <bremersee@googlemail.com>
ARG JAR_FILE
ADD target/${JAR_FILE} /opt/app.jar
ADD docker/entrypoint.sh /opt/entrypoint.sh
RUN chmod 755 /opt/entrypoint.sh
RUN mkdir /opt/log
RUN mkdir /opt/tmp
ENTRYPOINT ["/opt/entrypoint.sh"]
