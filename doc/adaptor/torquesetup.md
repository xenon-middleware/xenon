Torque setup with docker (OS X)
===============================

Dependencies:

```
brew install boot2docker docker
```

Setup:

```
boot2docker up
docker pull agaveapi/torque
docker run -d -h test.torque.xenon.esciencecenter.nl -p 10022:22 \
    --privileged --name test.torque.xenon agaveapi/torque
VBoxManage controlvm \
    "boot2docker-vm" natpf1 "tcp-port10022,tcp,,10022,,10022";
echo -n "\nHost torque\nUser testuser\nHostName localhost\nPort 10022\n" >> \
    ~/.ssh/config
```

Log in:

```
ssh torque # password testuser
```