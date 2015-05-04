# Introduction #

---

Synoptic is written in Java and is primarily developed using [Eclipse](http://www.eclipse.org/).

For a description of the GWT-based front-end to Synoptic, see [this page](DocsDevelopmentGWT.md).

For a detail description of our development practices, see [DocsDevelopmentPractices](DocsDevelopmentPractices.md).

# Working with the repository #

---

We use Mercurial (Hg) for our code repository. For details on how to check-out our googlecode repository consult [this](http://code.google.com/p/synoptic/source/checkout) page. To push your changes to the repository you'll need to [authenticate](https://code.google.com/hosting/settings) with an auto-generated googlecode.com password.

For more information about how to use Hg consult the following resources: [1](http://hgbook.red-bean.com/read/), [2](http://mercurial.selenic.com/wiki/BeginnersGuides).

You might also consider using [MercurialEclipse](http://www.javaforge.com/project/HGE) which integrates Hg with Eclipse (for more Eclipse plugin advice see below).

The command line Mercurial command (`hg`) can be extended in numerous ways. You can use the following [`.hgrc` file template](hgrcTemplate.md) as a starting point.


# Building and Running Synoptic #

---

We'll assume here that you have used Hg to clone the project into _synoptic/_.

## Building ##

You can build in one of two ways. Either build from within Eclipse or build from the command line:

  * **Eclipse:**
    1. Import a new project and point it to _synoptic/synoptic/_
    1. Under _Project Properties_ go to _Java Build Path_ -> _Libraries_. Click on _Add Variable_ to add a SYNOPTIC\_LIB variable. Set its value to be the absolute path of _synoptic/lib'_

  * **Command line:**
```
$ cd synoptic/synoptic/ && ant
```


## Running ##

To run Synoptic from Eclipse create a new run configuration and point it to use main.Main for its Main class.

To run Synoptic using the command line use the following command from within the _synoptic/_ dir:
```
$ java -ea -cp ./lib/*:./synoptic/bin/ synoptic.main.SynopticMain
```

You can also run `synoptic.sh` script located in _synoptic/_ which runs the above command line for you (and passes any additional arguments to Main). You can pass in the -h or the -H options to see a usage screen.

## Running Tests ##

To run all the tests pass the `--runAllTests` option to Synoptic.

# Library Dependencies #

---

The synoptic source has a number of dependencies on external libraries. These libraries are included in the repository. Here we document the status of each dependency and what the library is used for.

| **Library** | **Version required** | **Status of dependency** | **What its used for** | Notes |
|:------------|:---------------------|:-------------------------|:----------------------|:------|
| [dk.brics.grammar](http://www.brics.dk/grammar/) | 2.0-4 | Essential | Used extensively by InvariMint |  |
| [Raphaël](http://raphaeljs.com/) |  | Essential | Used in the GWT interface for model/invariants visualizations |  |
| [Dracula](http://www.graphdracula.net/), [on github](https://github.com/strathausen/dracula) |  | Essential | Used in the GWT interface for model visualization | Synoptic uses a modified version of this library. |
| [plume](http://code.google.com/p/plume-lib/) |  | Essential | Used for command line argument processing |  |
| [jung](http://jung.sourceforge.net/) |  | Optional | Used for interactive graph output |  |
| [protobuf-java](http://code.google.com/p/protobuf-java-format/) |  | Optional | Used for parsing files in protobuf message format |  |
| [daikon](http://plse.cs.washington.edu/daikon/download/) |  | Unused | Plans to use for mining structural trace invariants |  |
| [stixar](http://code.google.com/p/stixar-graphlib/) |  | Unused | Potentially useful collection of graph algorithms |  |

# Code Documentation #

---

We prefer to maintain code documentation as close to the code as possible. This means that you should strive to maintain _code documentation_ in the code, not on the wiki.

# Useful Eclipse Plugins and Other Tools #

---

We have found the following plugins to be useful during development, so we recommend that you install and use them.

  * [Google Eclipse plugin](http://code.google.com/eclipse/) : includes [GWT](http://code.google.com/webtoolkit/) support
  * [MercurialEclipse](http://www.javaforge.com/project/HGE) : integrates Hg with Eclipse
  * [EclEmma](http://www.eclemma.org/) : provides code coverage feedback
  * [FindBugs](http://findbugs.sourceforge.net/) : finds common bugs
  * [JDepend](http://andrei.gmxhome.de/jdepend4eclipse/) : tracks class dependencies in a project
  * [CheckStyle](http://eclipse-cs.sourceforge.net/) : checks code style and alerts you of problems
  * [Emacs+](http://www.mulgasoft.com/emacsplus/installation-details) : in-depth integration of emacs with Eclipse

We also use [Crystal](http://code.google.com/p/crystalvc/), a tool for proactive detection of conflict in Hg. Follow the user manual to set up Crystal.