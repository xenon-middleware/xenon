# Contributing guidelines

We welcome any kind of contribution to our software, from simple comment or question to a full fledged [pull request](https://help.github.com/articles/about-pull-requests/).

Ideally, the process of making a contribution can be threefold, as follows:

1. you have a question;
1. you think you may have found a bug (including unexpected behavior);
1. you want to make some kind of change to the code base (e.g. to fix a bug, to add a new feature, to update documentation).

In the first case:

- use the search functionality [here](https://github.com/NLeSC/Xenon/issues) to see if someone already filed the same issue;
- if your issue search did not yield any relevant results, make a new issue;
- apply the "Question" label; apply other labels when relevant.

In the second case:

- use the search functionality [here](https://github.com/NLeSC/Xenon/issues) to see if someone already filed the same issue;
- if your issue search did not yield any relevant results, make a new issue, making sure to provide enough information to the rest of the community to understand the cause and context of the problem. Depending on the issue, you may want to include:
    - the [SHA hashcode](https://help.github.com/articles/autolinked-references-and-urls/#commit-shas) of the commit that is causing your problem;
    - some identiying information (name and version number) for dependencies you're using;
    - information about the operating system;
- apply relevant labels.

In the third case:

- (**important**) announce your plan to the rest of the community _before you start working_. This announcement should be in the form of a (new) issue.
- (**important**) wait until some kind of concensus is reached about your idea being a good idea.
- if needed, fork the repository to your own Github profile and create your own feature branch off of the latest master commit. While working on your feature branch, make sure to stay up to date with the master branch by pulling in changes, possibly from the 'upstream' repository (follow the instructions [here](https://help.github.com/articles/configuring-a-remote-for-a-fork/) and [here](https://help.github.com/articles/syncing-a-fork/)).
- make sure the existing unit tests still work by running ``./gradlew test``
- make sure that the existing integration tests still work by running ``./gradlew integrationTest`` and ``./gradlew dockerIntegrationTest``
- add your own unit tests and integration tests (if necessary)
- update or expand the documentation


In case you feel like you've made a valuable contribution, but you don't know how to write or run tests for it, or how to generate the documentation: don't let this discourage you from making the pull request; we can help you! Just go ahead and submit the pull request, but keep in mind that you might be asked to append additional commits to your pull request (have a look at some of our old pull requests to see how this works, for example [#294](https://github.com/NLeSC/Xenon/pull/294)).


