

Grid ftp uses certificates for server and users.
To create the certificates a certificate authority is needed.
The `ca` directory contains a Dockerfile that manage the ca.

The grid servers need to be run with predefined hostnames.

# Create a ca and certificates

## Build the ca container

```
docker build -t nlesc/xenon-gridftp-ca ca
```

## Create certificates

Start the container

```
docker run -t --rm=true -e MYUID=$UID -v $PWD/files:/var/lib/globus nlesc/xenon-gridftp-ca
```

In `files/` directory the following files will have been generated:

* `globus-simple-*_all.deb`, contains public key of ca
* `ca-certifcates/`, contains public key of ca, use as X509_CERT_DIR
* `hosts/<hostname>/`, contains key pair of `<hostname>`
* `users/<username>/`, contains key pair of `<username>`, dn of user is `/O=eScienceCenter/OU=local/CN=<username>`, use as X509_USER_DIR
* `simple-ca/`, the ca itself

The public ca should be installed on all gridftp servers and clients.
The host key pairs should be installled on the gridftp servers.
Each user should have a mapping in `/etc/grid-security/grid-mapfile` on each gridftp server.
Gridftp is very picky about hostname resolving so gridftp server and clients should all be able to resolve fully qualified hostnames of each other.

# Create gridftp container

The gridftp image will have certificates for different hostnames.
During boot of the container the certifcate belonging to the current hostname will be activated.
If there is no certificate for the selected hostname the container will boot without gsi support.

## Create container

```
docker build -t nlesc/xenon-gridftp .
```

## Run grid ftp servers

```
docker run -d -h gridftp1.xenontest.nlesc.nl --name=gridftp1 nlesc/xenon-gridftp
docker run -d -h gridftp2.xenontest.nlesc.nl --name=gridftp2 nlesc/xenon-gridftp
```

### Test

```
docker run -t -i --rm=true \
-v $PWD/files:/etc/ca-files:ro \
--link gridftp1:gridftp1.xenontest.nlesc.nl \
--link gridftp2:gridftp2.xenontest.nlesc.nl \
ubuntu:14.04 /bin/bash
apt-get update && apt-get install -y globus-gass-copy-progs
export X509_CERT_DIR=/etc/ca-files/ca-cerficates
# Cant use X509_USER_DIR directly in container because wrong file owner
cp -r /etc/ca-files/users/xenon /root/.globus
touch somefile
/usr/bin/globus-url-copy file:///somefile gsiftp://gridftp1.xenontest.nlesc.nl/home/xenon/somefile
/usr/bin/globus-url-copy gsiftp://gridftp1.xenontest.nlesc.nl/home/xenon/somefile gsiftp://gridftp2.xenontest.nlesc.nl/home/xenon/somefile
/usr/bin/globus-url-copy gsiftp://gridftp2.xenontest.nlesc.nl/home/xenon/somefile file://C
```
There should be a file `/somefile2` present after running commands above.
