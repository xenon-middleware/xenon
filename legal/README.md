
# Legal

The Xenon library is copyrighted by the Netherlands eScience Center and released
under the Apache License, Version 2.0. A copy of the license may be obtained from [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0).

Xenon uses several third-party libraries that have their own (permissive, open 
source) licenses. This document uses the short names from https://spdx.org/licenses/ 
to unambiguously identify licenses. There are 2 sets of licenses to consider.
These are:

1. runtime libraries
1. development libraries, for example for testing and checking code style



## runtime libraries

For now, I limited the runtime libraries to the list of jars from 
./build/install/xenon/lib/ that you get after issuing this command:

```bash
./gradlew installDist
```

[*Apache-2.0*](https://spdx.org/licenses/Apache-2.0.html#licenseText)

1. aws-s3-2.0.2.jar
   - notice: ...
1. cglib-2.2.1-v20090111.jar
   - notice: ...
1. commons-codec-1.9.jar
   - notice: ...
1. commons-logging-1.2.jar
   - notice: ...
1. commons-net-3.3.jar
   - notice: ...
1. gson-2.5.jar
   - notice: ...
1. guava-16.0.1.jar
   - notice: ...
1. guice-3.0.jar
   - notice: ...
1. guice-assistedinject-3.0.jar
   - notice: ...
1. httpclient-4.5.1.jar
   - notice: ...
1. httpcore-4.4.3.jar
   - notice: ...
1. javax.inject-1.jar
   - notice: ...
1. java-xmlbuilder-1.1.jar
   - notice: ...
1. jclouds-blobstore-2.0.2.jar
   - notice: ...
1. jclouds-core-2.0.2.jar
   - notice: ...
1. joda-time-2.8.1.jar
   - notice: ...
1. s3-2.0.2.jar
   - notice: ...
1. sardine-5.7.jar
   - notice: ...
1. sshd-core-1.4.0.jar
   - notice: ...
1. sts-2.0.2.jar
   - notice: ...


[*MIT*](https://spdx.org/licenses/MIT.html#licenseText)

1. slf4j-api-1.7.22.jar

[*LGPL-2.1*](https://spdx.org/licenses/LGPL-2.1.html#licenseText) and [*EPL-1.0*](https://spdx.org/licenses/EPL-1.0.html#licenseText) dual-license

1. logback-classic-1.0.11.jar
1. logback-core-1.0.11.jar

[*CDDL-1.0*](https://spdx.org/licenses/CDDL-1.0.html#licenseText)

1. jsr250-api-1.0.jar
1. jsr311-api-1.1.1.jar

[*BSD-3-Clause*](https://spdx.org/licenses/BSD-3-Clause.html#licenseText)

1. asm-3.1.jar

*public domain*

1. aopalliance-1.0.jar
1. base64-2.3.8.jar

## development libraries

TODO
(bold does not occur in list above)

- ./gradle/wrapper
    1. **gradle-wrapper.jar**
- ./lib
    1. aopalliance-1.0.jar
    1. asm-3.1.jar
    1. aws-s3-2.0.2.jar
    1. base64-2.3.8.jar
    1. cglib-2.2.1-v20090111.jar
    1. commons-codec-1.9.jar
    1. commons-logging-1.2.jar
    1. commons-net-3.3.jar
    1. gson-2.5.jar
    1. guava-16.0.1.jar
    1. guice-3.0.jar
    1. guice-assistedinject-3.0.jar
    1. httpclient-4.5.1.jar
    1. httpcore-4.4.3.jar
    1. javax.inject-1.jar
    1. java-xmlbuilder-1.1.jar
    1. jclouds-blobstore-2.0.2.jar
    1. jclouds-core-2.0.2.jar
    1. joda-time-2.8.1.jar
    1. jsr250-api-1.0.jar
    1. jsr311-api-1.1.1.jar
    1. s3-2.0.2.jar
    1. sardine-5.7.jar
    1. slf4j-api-1.7.22.jar
    1. sshd-core-1.4.0.jar
    1. sts-2.0.2.jar
- ./lib/runtime
    1. logback-classic-1.0.11.jar
    1. logback-core-1.0.11.jar
- ./lib/test
    1. **hamcrest-core-1.3.jar**
    1. **hamcrest-library-1.3.jar**
    1. **junit-4.12.jar**
    1. **mockito-all-1.9.5.jar**
    1. **system-rules-1.16.0.jar**
