# Serve site

Serve site on [http://localhost:4000/Xenon/](http://localhost:4000/Xenon/) with the following command 
```
docker run --rm --label=jekyll --volume=$(pwd):/srv/jekyll \
-it -p 127.0.0.1:4000:4000 jekyll/jekyll:pages
```

# Adding new version

1. Checkout gh-pages branch in some directory and cd to it
2. Create _posts/YEAR-MONTH-DAY-version.md
    * eg. _/posts/2015-11-25-1.1.0.md 
    * content will be shown on the post page 
3. Create `versions/<version>` directory
4. Add version specific artifacts like javadoc to `versions/<version>/`
5. Commit and push
