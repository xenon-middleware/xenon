

Grid ftp uses certificates for server and users.
To create the certificates a certifcate authority is needed.
The `ca` directory contains a Dockerfile that manage the ca.

The grid servers need to be run with predefined hostnames.

# Create a ca and certificates

Build the ca container.

```
docker build -t nlesc/xenon-gridftp-ca ca
```

## Create a new ca

Start the container

```
docker run -t -i --rm=true -e MYUID=$UID -v $PWD/files/globus:/var/lib/globus -v $PWD/files/grid-security:/etc/grid-security nlesc/xenon-gridftp-ca
```

In container run

```
cd /var/lib/globus
/usr/bin/grid-ca-create -noint -subject "CN=eScience Center Simple CA, O=eScienceCenter" \
-email nobody@example.com -days 3650 -pass mycainsecurepassword -nobuild
echo 1 | grid-default-ca
echo 1 | grid-ca-package -d -r
chown -R $MYUID /var/lib/globus /etc/grid-security
exit
```
## Create a host key pair

```
NEWHOST=gridftp1.test
cd /var/lib/globus
mkdir $NEWHOST && cd $NEWHOST
grid-cert-request -host $NEWHOST -nopassphrase -dir .
grid-ca-sign -passin pass:mycainsecurepassword -days 3560 -in hostcert_request.pem -out hostcert.pem
chown -R $MYUID /var/lib/globus /etc/grid-security
exit
```

## Create a user key pair

```
mkdir testuser
cd testuser
grid-cert-request -nopassphrase -ca -dir .
grid-ca-sign -days 3560  -in usercert_request.pem -out usercert.pem
openssl rsa -in userkey.pem -out userkey.rsa.pem
exit
```
