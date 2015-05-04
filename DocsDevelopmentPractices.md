# Introduction #

---


This page documents how development of the Synoptic code base is organized. The focus here is on development practices. That is, how Synoptic developers coordinate with each other and the specific strategies that we use to produce maintainable and bug-free code. If you find an omission or have a way to improve our day to day activities, add a comment to this page or send an email to the mailing list.


# Development cycle #

---


Our development cycle generally revolves around the following steps:

  1. Create new issues.
  1. Take ownership of an issue.
  1. If the issue is loosely defined, comment on the issue with a solution proposal. If the issue is too large/complex, break it up into a few smaller and more manageable issues.
  1. Commit code to fix the issue in an issue-specific hg branch, periodically merging the `default` branch into the issue branch to simplify merging later on.
  1. Once the solution is ready for review, add a comment to the issue to indicate the revision id containing the proposed fix, and requesting a code review.
  1. After the review, handle all reviewer comments with further commits in the same issue-specific branch and re-request a code review (the code review process repeats until all of the issues raised in the review are resolved).
  1. Leave a final comment on the issue to indicate which revision id contains the final solution.
  1. Ivan closes the issue-specific branch and merges this branch into the default branch.

## Communication is key ##

Throughout this process, it is critical that you communicate any difficulties as soon as possible.
  * If the problem is issue-specific then leave a comment on the issue page.
  * General comments/questions/concerns should be sent to the synoptic mailing list: synoptic@googlegroups.com
  * You can often find and chat with developers on google chat:
    * [Developer list and their gchat ids](http://code.google.com/p/synoptic/people/list)
    * Contact Ivan via `bestchai@gmail`
  * More private concerns should be sent directly to Ivan/Yuriy/Mike.


# Project issues #

---


## Creating an issue ##

If you notice a problem, or come up with a suggestion for improvement, either of the code, or with any of the project artifacts (e.g., the GWT interface, the command line version of Synoptic, documentation on the wiki, etc) then you should create a project issue.

Do not hesitate to create issues for small defects! Issues may range in difficulty -- some may take weeks to complete, while others may be handled with an hour of work. In general, anything that prompts you to leave a `TODO` comment in the code deserves to be turned into a project issue (make sure to mention the issue id in the TODO comment).

The following set of guidelines should be followed to create useful issues:

  * Check for duplication -- search through existing issues to see whether the issue already exists. If yes:
    * Check if the issue has been resolved, perhaps it needs to be reopened?
    * If someone is working on it, can you provide feedback that might help the issue owner?
    * If the issue is under-specified, can you provide a better description?

  * Categorize the issue either as a `Defect` or an `Enhancement`.

  * Describe the issue in detail.
    * An issue you create might be worked on by a different person. Make sure any developer can understand it without much effort.
    * If it is a defect, try to include a sequence of steps to reproduce the problem.
    * Issue description and scope can always be improved by adding comments. So don't worry if the initial version is a bit rough.

  * Do not fill out the `Owner` field of the issue, unless you have discussed it with that person.

  * Use the CC field sparingly.
    * Remember, all project issue activity is already reported as emails to the mailing list.
    * By CCing someone, you are sending them additional copies of the same emails.

  * Fill out the labels section as best you can. Look at similar issues to find the best set of labels.


## Working on an issue ##

  * To begin work on a new issue, first list yourself as the issue `Owner` and change the `Status` of the issue to `Started`.

  * In general, each active issue should have its own named branch in the hg repository. For example, work on [issue 77](https://code.google.com/p/synoptic/issues/detail?id=77) would occur in the `Issue77` branch.

  * Always branch off issue-specific branches from the latest revision in the `default` branch:
```
$ hg pull                                 # get the latest changes
$ hg update default                       # update to the latest changeset in the default branch 
$ hg branch Issue77                       # create a new Issue77 branch
$ hg commit -m 'New branch for Issue 77.' # commit the branching
$ hg push --new-branch                    # push the new branch
```

  * Refrain from committing unrelated code into an issue-specific branch. For example, don't commit a code refactoring in the Synoptic project to a branch intended for work on a SynopticGWT project issue.

  * Before committing, always run the status and diff commands to check your modifications:
```
$ hg status   # list all modified, created, removed files
$ hg diff     # show diff output for all modified files
```

  * Periodically merge the default branch into the issue branch to simplify the merge process later on. This does _not_ modify the default branch, and is only intended to keep _your_ branch up to date with default. Another benefit is that you can test how well your changes mesh with other changes that occurred in parallel. For example, if a class your code depends on has been deleted, then you'll be able to catch this problem with:
```
$ hg up Issue77                                           # switch to the right issue branch
$ hg merge default                                        # merge new changes from default into Issue77 branch
$ hg commit -m 'Updated Issue77 with changes in default.' # commit the merge
```

  * Once you feel that your changes resolve the issue, add a comment on the issue page to indicate the revision id of the solution -- "Solution in _revision 4967df21c60d_, please review". Insert "revision" before the revision number so that it will be automatically hyperlinked. By adding this comment you will trigger a code review process (see below).

  * To expedite the code review, a solution to an issue should:
    * Follow the style guidelines (see below)
    * Contain sufficient documentation in comments
    * Include tests that have reasonable behavior coverage

  * Once you have resolved all comments related to the code review, add an issue comment with the revision id that contains the final solution, e.g., "Fixed in revision 4967df21c60d"

  * Once the final solution is verified, Ivan will change the issue's status to `Fixed`, close the issue-specific branch, and merge the branch into the `default` branch. **Do not do this yourself, unless directed by Ivan.**
```
$ hg up Issue77                                      # switch to the right issue branch
$ hg commit --close-branch -m 'Closing branch.'      # mark the branch as closed
$ hg up default                                      # switch to the default branch
$ hg merge Issue77                                   # merge changes in the issue branch into default
$ hg commit -m 'Merged Issue77 branch into default.' # commit the merge
$ hg push                                            # push out the merged changes
```


# Committing code #

---


The single most important indicator of your activity is the code that you commit and push. Because most issues will have their own named branch, we can make progress in parallel, without being afraid of breaking each other's code. What this means is that you should **commit and push your code early** and you should **make commits of small granularity**.

  * **Commit and push early.** Do not delay pushing out your changes. The sooner that we see your code, the sooner we can spot problems, offer suggestions, and help you. Your workflow should look like:
```
$ hg pull                               # grab any new changes
$ hg up Issue77                         # switch to the appropriate branch
  ## make some changes ##
$ hg commit -m 'Describe changes1..'    # commit changes
  ## make some more changes ##
$ hg commit -m 'Describe changes2..'    # commit changes
   ...
$ hg push                               # share local commits with everyone else
```

  * **Make small, logically coherent commits.** Each commit should be one coherent (though, perhaps incomplete) piece of work. For example, a single commit might add a new class with a rough outline of its public methods, perhaps with a bunch of inlined TODO comments. You do not want to mix many kinds of changes into one commit.

  * **Strive to commit code that compiles.** Code that does not compile is difficult to review. However, it is okay if sometimes your code does not compile (it is your issue branch, after all!).

  * **Include meaningful commit messages.** Always include a commit message that explains as many of the changes you have introduced as possible. Someone will review your code, and it is much easier to interpret a commit if the commit inclues a high level rationale for the changes. An example message is short and to the point, e.g., "Fixed a crashing bug in TraceParser when partition reg exp is Null."

  * **Do not mix formatting changes with code changes.** Google code uses a diff tool that is sensitive to whitespace, which makes it difficult to compare files that are formatted differently. For example, every line might be highlighted as changed/new, even though the code is identical. Make two separate commits, one to change the code formatting (i.e., how the code looks), and another to make changes to the code. If this does happen, you can compare two revisions `r1` and `r2` and ignore whitespace with:
```
$ hg diff -w -r r1:r2
```

  * **Collapse multiple commits that belong together.** If you find yourself making many small commits that feel like they belong in a single commit, then you can 'collapse' these multiple commits into a single commit. Only do this in an issue branch, and _only_ if you haven't yet pushed to google code. You can use the [hg collapse](https://bitbucket.org/peerst/hgcollapse/wiki/Home) extension for this.


# Code reviews #

---


Everyone is encouraged to look over any of the code in the repository and offer suggestions for improvement. Don't hesitate to add `TODO`comments directly to the code, to open new issues, to initiate code reviews on older code, or to [request](http://code.google.com/p/synoptic/issues/entry?show=review) a code review for your own code. Remember that code in the `default` branch is periodically released to our users. So, we want to make sure that it is of as high quality as possible!

The incremental code review process, described below, is intended to improve the quality of **new** code, prior to including it into the `default` branch.

  * Each comment in a code review should:
    * Raise an issue that is localized to the code under review
    * Provide a concrete suggestion for a solution to the raised problem

  * Code review discussions should remain on the page for the revision, e.g., [here](http://code.google.com/p/synoptic/source/detail?r=4967df21c6). However, discussion that might impact the corresponding project issue can be escalated to the issue page.

  * Once your code has been reviewed, read through the code review comments carefully. If something is unclear, or if you disagree with the proposed changes, respond to the comments by going to the page for the revision and clicking on `Start a code review ›` in the top right corner of the body of the page.

  * Do not delay your response to a code review and the implementation of the proposed changes. For issue-specific branches, you should do this _before_ committing any new code. Code understanding fades fast, so please respond while the code review remains fresh for everyone involved.

  * If possible, implement the changes proposed by the code review in a single commit.

  * The commit comment should reference the reviewed revision(s) that are handled by the commit:
```
$ hg commit -m "Handled code review of revision 4967df21c60d"
```



# Eclipse settings #

---


If you use Eclipse, you should also work through the following steps:

  1. Download and import the following `Code Style` and `Compiler` [Eclipse settings file](http://wiki.synoptic.googlecode.com/hg/eclipse-imports/java-code_syle-compiler-prefs.epf) (by going to `File` -> `Import...`). Don't forget to apply the new style after you import it.
  1. Set save actions by going to Eclipse's preferences/options, and under`Java Editor` and `Save Actions` check "Perform the selection actions on save", "Format source code" and "Format all lines", and "Organize imports".
  1. Use the Unix text file line delimiter (especially important if you are using Windows) by going to Eclipse's preferences/options, and under `General` and `Workspace` set "New text file line delimiter" to be "Unix". The answer to [this question](http://stackoverflow.com/questions/8562564/does-eclipse-on-windows-add-control-m-characters) has a screenshot.
  1. By default Eclipse does not enable assertions. Our projects assume that you have assertions enabled at all times. To globally enable assertions as a default for all projects, go to Window -> Preferences -> Java / Installed JREs. Select the JRE and click "Edit...". In the "Default VM arguments" field, add "-ea".

# Java style guidelines #

---


**TODO: fill this out**


# Javascript style guidelines #

---


**TODO: fill this out**