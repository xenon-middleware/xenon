# Adding an adaptor

* Source code in 'adaptors/<adaptor name>/src'.
* Required libraries in 'adaptors/<adaptor name>/lib'.
* Tests in 'test/src'.

## Ant

1. Use 'adaptors/ssh/build.xml' as template for new adaptor's 'adaptors/<adaptor name>/build.xml'.

2. To compile adapter, add following to the 'build' or 'build-all' target in 'adaptors/build.xml':
````xml
<ant dir="<adaptor name>" />
````
(For sonar maintainer: 4. Add classes path to sonar runner configuration.)

