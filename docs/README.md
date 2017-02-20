# Serve site

Serve site on [http://localhost:4000/Xenon/](http://localhost:4000/Xenon/) with the following command
```
docker run --rm --label=jekyll --volume=$(pwd):/srv/jekyll \
-i -t -p 127.0.0.1:4000:4000 jekyll/jekyll:pages jekyll serve
```

# Adding new version

1. Checkout gh-pages branch in some directory and cd to it
2. Create `_posts/YEAR-MONTH-DAY-version.md` Markdown file
    * eg. _/posts/2015-11-25-1.1.0.md
    * Add empty Front Matter block (`---\n----\n`)
    * Content below Front Matter block will be shown on the post page, if there is no content a default set of links will be rendered
3. Create `versions/<version>` directory
4. Add version specific artifacts like javadoc to `versions/<version>/`
5. Commit and push
