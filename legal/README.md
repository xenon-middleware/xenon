
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

- items labeled _N_ have a copy of notice file in legal/
- items labeled _L_ have a copy of license file in legal/

[*Apache-2.0*](https://spdx.org/licenses/Apache-2.0.html#licenseText)

1. aws-s3-2.0.2.jar _N, L_
1. cglib-2.2.1-v20090111.jar _N, L_
1. commons-codec-1.9.jar _N, L_
1. commons-logging-1.2.jar _N, L_
1. commons-net-3.3.jar _N, L_
1. gson-2.5.jar _L_
1. guava-16.0.1.jar _L_
1. guice-3.0.jar _N, L_
1. guice-assistedinject-3.0.jar _N, L_
1. httpclient-4.5.1.jar _N, L_
1. httpcore-4.4.3.jar _N, L_
1. javax.inject-1.jar _L_
1. java-xmlbuilder-1.1.jar _L_
1. jclouds-blobstore-2.0.2.jar _N, L_
1. jclouds-core-2.0.2.jar _N, L_
1. joda-time-2.8.1.jar _N, L_
1. s3-2.0.2.jar _N, L_
1. sardine-5.7.jar _L_
1. sshd-core-1.4.0.jar _N, L_
1. sts-2.0.2.jar _N, L_

[*BSD-3-Clause*](https://spdx.org/licenses/BSD-3-Clause.html#licenseText)

1. asm-3.1.jar _L_

[*CDDL-1.0*](https://spdx.org/licenses/CDDL-1.0.html#licenseText)

1. jsr250-api-1.0.jar _L_
1. jsr311-api-1.1.1.jar _L_

[*LGPL-2.1*](https://spdx.org/licenses/LGPL-2.1.html#licenseText) or [*EPL-1.0*](https://spdx.org/licenses/EPL-1.0.html#licenseText) dual-license

1. logback-classic-1.0.11.jar _L_
1. logback-core-1.0.11.jar _L_

[*MIT*](https://spdx.org/licenses/MIT.html#licenseText)

1. slf4j-api-1.7.22.jar _L_

*public domain*

1. aopalliance-1.0.jar
1. base64-2.3.8.jar

## development libraries

[*Apache-2.0*](https://spdx.org/licenses/Apache-2.0.html#licenseText)

1. gradle-wrapper.jar _L_

[*BSD-3-Clause*](https://spdx.org/licenses/BSD-3-Clause.html#licenseText)

1. hamcrest-core-1.3.jar _L_
1. hamcrest-library-1.3.jar _L_

[*CPL-1.0*](https://spdx.org/licenses/CPL-1.0.html#licenseText)

1. system-rules-1.16.0.jar _L_

[*EPL-1.0*](https://spdx.org/licenses/EPL-1.0.html#licenseText)

1. junit-4.12.jar _L_

[*MIT*](https://spdx.org/licenses/MIT.html#licenseText)

1. mockito-all-1.9.5.jar _L_


