Serve site with 
```
docker run --rm --label=jekyll --volume=$(pwd):/srv/jekyll \
-it -p 127.0.0.1:4000:4000 jekyll/jekyll:pages
```

Visit [http://localhost:4000/Xenon/](http://localhost:4000/Xenon/) 
