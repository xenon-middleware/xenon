#
# docker build -t nlesc/xenon-test .
#
FROM nlesc/xenon-phusion-base
MAINTAINER Stefan Verhoeven "s.verhoeven@esciencecenter.nl"

RUN apt-get update && apt-get install -y openjdk-7-jdk && \
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
mkdir /code && mkdir -p /etc/my_init.d && touch /etc/service/sshd/down

# ssh keys are already installed in base image, config is needed by Xenon
RUN setuser xenon touch /home/xenon/.ssh/config

# Add globus cerficates
ADD xenon-gridftp/files/ca-cerficates /etc/grid-security/certificates
ADD xenon-gridftp/files/users/xenon /home/xenon/.globus
RUN chown -R xenon.xenon /home/xenon/.globus

# Tests will be run by xenon user which has uid taken from MYUID environment var
ADD run-tests.sh /bin/run-tests.sh

VOLUME ["/code"]
WORKDIR /code

ENTRYPOINT ["/bin/run-tests.sh"]
CMD ["./gradlew", "check"]
