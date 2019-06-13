# Creating a release

Step-by-step guide on creating a Xenon release

## 1. Up the version number and add changelog

To create a release, version numbers should be updated in:

- README.md
- gradle/common.gradle
- CHANGELOG.md

Also, in `CHANGELOG.md` a section should be added with 
notable changes in this version.

## 2. Update the site

To update Xenon version in `docs/_data/version.yml` and update Javadocs inside `docs/` directory run:

```bash
./gradlew publishSite
```

The site is a [Jekyll](https://jekyllrb.com/) powered site and hosted by GitHub pages at http://nlesc.github.io/Xenon/

## 3. Commit the changes

Next, commit all changes you have made to the master branch. If you check with  

    git status

you should get something like this:

    On branch master
    Your branch is up-to-date with 'origin/master'.
    Changes not staged for commit:

	modified:   CHANGELOG.md
	modified:   README.md
	modified:   gradle/common.gradle
    modified:   docs/_config.yml

    Untracked files:
    (use "git add <file>..." to include in what will be committed)

	docs/versions/3.0.0/

Add and commit these files using `git add` and `git commit` and `git push`.

## 4. Create a GitHub release

On GitHub, go to the releases tab and draft a new release.

The tag and title should be set to the version.

The release description should contain a small text to explain what Xenon is and the part of the CHANGELOG.md which pertains to this version.

## 5. Check if DOI is created in Zenodo

Zenodo is linked to the Xenon github, so when a release is created, a DOI 
will be created automatically. Click the DOI badge on the github page to check 
this.

Check that the authors and license of the Zenodo entry are correct.
If not, then correct, save and publish the Zenodo entry.

### 6. Add jars to bintray

To add the release to bintray, do the following: 

```bash
export BINTRAY_USER=<your bintray username>
export BINTRAY_KEY=<your bintray API key>
./gradlew bintrayUpload
```

This should create the new release on bintray and upload all necessary data, jars, etc.

Next, go to the bintray page: 

https://bintray.com/nlesc/xenon/xenon#

and click on 'publish' to publish the release. The latest verion tag usually takes a few minutes to update. 


### Alternative manual bintray step

Note: this step should not be needed if the automated bintray publishing works! It is only here for reference.

To add the necessary jar and pom files to bintray manually, it is easiest to ensure you 
have them locally first. By calling: 

    ./gradlew publishToMavenLocal

gradle should put the required files in:

    ~/.m2/repository/nl/esciencecenter/xenon/xenon/<version>/

Next goto: 

    https://bintray.com/nlesc/xenon/xenon

Click on 'new version' and insert <version> as name. Next, in the overview page 
click on the version number and on `UI` (in the version files box). Bintray will 
ask for the 'Target Repository Path', which should be: 

    /nl/esciencecenter/xenon/xenon/<version>

Click on the `click to add files` button and add the files that where generated in 
the .m2 repository. Click on `save` and then on `publish` to publish the version.
 
### 7. Update applications using Xenon.

Update related repros such as Xenon-examples, pyXenon, xenon-cli, etc

And finally celebrate.
