# Creating a release

Step-by-step guide on creating a Xenon release

## Up the version number and add changelog

To create a release, version numbers should be updated in:

- README.md
- gradle/common.gradle
- CHANGELOG.md

Also, in `CHANGELOG.md` a section should be added with 
notable changes in this version.

## Update the documentation

Then generate the javadoc for this version by typing:

   ./gradlew javadocDevel

Make sure the following directories exist afterwards 

   docs/versions/<version>/javadoc-devel
   docs/versions/<version>/javadoc

as the `javadoc-devel` may not be generated if the `javadoc` already 
exists (this is a bug in the gradle file). 

## Add a version post file

In the directory `docs/_posts` there should be a list of yml files 
with the release numbers, like so:

    2014-06-03-1.0.0.yml  
    2015-12-16-1.1.0.yml  
    2017-02-20-1.2.0.yml 

Add a new yml file with `<date>-<version>.yml` as a filename, and 
containing the new version like so:

    ---
    title: 1.2.2
    ---
   
## Commit the changes

Next, commit all changes you have made to the master branch. If you check with  

    git status

you should get something like this:

    On branch master
    Your branch is up-to-date with 'origin/master'.
    Changes not staged for commit:

	modified:   CHANGELOG.md
	modified:   README.md
	modified:   gradle/common.gradle

    Untracked files:
    (use "git add <file>..." to include in what will be committed)

	docs/_posts/2017-05-08-1.2.2.yml
	docs/versions/1.2.2/

Add and commit these files using `git add` and `git commit` and `git push`.

