
# Legal

There are 2 sets of licenses to consider. These are:

1. runtime libraries
1. development libraries, for example for testing and checking code style

This document uses the short names from https://spdx.org/licenses/ to identify 
licenses.

## runtime libraries

For now, I limited the runtime libraries to the list of jars from 
./build/install/xenon/lib/ that you get after issuing this command:

```bash
./gradlew installDist
```

[*Apache-2.0*](https://spdx.org/licenses/Apache-2.0.html#licenseText)

1. sshd-core-1.4.0.jar
   - notice: ...
1. sardine-5.7.jar
   - notice: ...
1. commons-net-3.3.jar
   - notice: ...
1. s3-2.0.2.jar
   - notice: ...
1. gson-2.5.jar
   - notice: ...
1. jclouds-blobstore-2.0.2.jar
   - notice: ...
1. jclouds-core-2.0.2.jar
   - notice: ...
1. commons-logging-1.2.jar
   - notice: ...
1. aws-s3-2.0.2.jar
   - notice: ...
1. javax.inject-1.jar
   - notice: ...
1. guice-assistedinject-3.0.jar
   - notice: ...
1. guava-16.0.1.jar
   - notice: ...
1. cglib-2.2.1-v20090111.jar
   - notice: ...
1. commons-codec-1.9.jar
   - notice: ...
1. guice-3.0.jar
   - notice: ...
1. sts-2.0.2.jar
   - notice: ...
1. httpcore-4.4.3.jar
   - notice: ...
1. httpclient-4.5.1.jar
   - notice: ...
1. joda-time-2.8.1.jar
   - notice: ...
1. java-xmlbuilder-1.1.jar
   - notice: ...

[*MIT*](https://spdx.org/licenses/MIT.html#licenseText)

1. slf4j-api-1.7.22.jar
   - notice: ...

[*LGPL-2.1*](https://spdx.org/licenses/LGPL-2.1.html#licenseText) and [*EPL-1.0*](https://spdx.org/licenses/EPL-1.0.html#licenseText) dual-license

1. logback-classic-1.0.11.jar
   - notice: ...
1. logback-core-1.0.11.jar
   - notice: ...

[*CDDL-1.0*](https://spdx.org/licenses/CDDL-1.0.html#licenseText)

1. jsr250-api-1.0.jar
   - notice: ...
1. jsr311-api-1.1.1.jar
   - notice: ...

*public domain*

1. aopalliance-1.0.jar
   - notice: ...
1. base64-2.3.8.jar
   - notice: ...

*no license*

1. asm-3.1.jar
   - notice: ...


## development libraries

TODO

