# Serve site

Serve site on [http://localhost:4000/Xenon/](http://localhost:4000/Xenon/) with the following command
```
docker run --rm --label=jekyll --volume=$(pwd):/srv/jekyll \
-i -t -p 127.0.0.1:4000:4000 jekyll/jekyll:pages jekyll serve
```

# Updating current version

1. In `_data/version.yml` update the value of the `current` key to new current version, do manually or by using `./gradlew versionSite`
2. Add version specific artifacts like javadoc to `versions/<version>/`, do manually or by using `./gradlew copyJavadoc`
3. Commit and push
