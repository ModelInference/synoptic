# Building #

  * The easiest way to build a tool is to use the `ant` command at the top-level of the repository. For example, running `ant synoptic` will re-build the synoptic .class files and package the .class files into `lib/synoptic.jar`. There are similar targets for CSight, InvariMint, and Perfume. Each tool-specific target in the corresponding `build.xml` first removes all .class files for the tool, then builds these .class files, and finally packages the tool into a jar file.

# Running #

  * Each tool has a corresponding .sh script that you can use to invoke the (already compiled) tool from the command line. For example, to run Synoptic use the `synoptic.sh` script, to run Perfume use `perfume.sh`, etc. These scripts pass all command line arguments to the underlying tool. They run the compiled .class file of the tool (not the version packaged into a jar).

  * If you built a jar for the tool (which are placed into `lib/` by default). You can also invoke the tool from a jar file using another corresponding .sh script. For example, to run Synoptic from `lib/synoptic.jar` use `synoptic-jar.sh`, etc.

# Log file parsing #

  * The -c argument allows you to place all of your command line args into a file. This argument is available in all of the tools. A typical (Synoptic) args file looks like this:
```
-o osx-login-example.png
-r (?<TYPE>.+)
-s --
```

Note that when specifying the -r, -s, and -m options in an args file, whitespace **matters**. For example, note that including spaces after `(?<TYPE>.+)` will produce a different regular expression. Also, do not use quotes around your regular expressions, as these will not be stripped away (on the command line your shell strips these off).

# Synoptic notes #

# InvariMint notes #

# CSight notes #

  * CSight is distributed with a [Spin](http://spinroot.com/spin/whatispin.html) model checker binary that only works on 64-bit Linux.

  * CSight is distributed with two [McScM](https://altarica.labri.fr/forge/projects/mcscm/wiki/) model checker binaries that work on 64-bit OSX 10.8+ and 64-bit Linux.

# Perfume notes #