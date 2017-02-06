#
# docker build -t nlesc/xenon-test .
#
FROM nlesc/xenon-phusion-base
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN apt-get update && apt-get install -y python-software-properties && \
add-apt-repository ppa:webupd8team/java -y && apt-get update && \
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
apt-get install -y  oracle-java8-installer oracle-java8-set-default expect && \
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
mkdir /code && mkdir -p /etc/my_init.d && touch /etc/service/sshd/down

# ssh keys are already installed in base image, config is needed by Xenon
RUN setuser xenon touch /home/xenon/.ssh/config

# Tests will be run by xenon user which has uid taken from MYUID environment var
ENV MYUID 1000
ADD run-tests.sh /bin/run-tests.sh
ADD ssh-fillpass-xenon /bin/ssh-fillpass-xenon

VOLUME ["/code"]
WORKDIR /code

ENTRYPOINT ["/bin/run-tests.sh"]
CMD ["./gradlew", "-Pxenon.test.properties=src/integrationTest/docker/xenon.test.properties.docker", "--project-cache-dir", "/home/xenon/gradle-cache", "integrationTest"]
