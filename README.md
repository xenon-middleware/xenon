Serve site with 
```
docker run --rm --label=jekyll --volume=$(pwd):/srv/jekyll \
-it -p 127.0.0.1:4000:4000 jekyll/jekyll:pages
```

Visit [http://localhost:4000/Xenon/](http://localhost:4000/Xenon/) 

# Adding new version

In gh-pages branch
1. Create _posts/YEAR-MONTH-DAY-version.md, content will be shown on the post page 
2. Create versions/<version> folder
3. Add javadoc from build/javadoc to versions/<version>/
4. Commit and push
