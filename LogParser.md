# Introduction #

Synoptic reads events from a log file, then summarizes them graphically.
You have to tell Synoptic the format of the trace file so that it can parse
the trace file.  One way to do this is to write a custom log file parser in
Java (TODO: add cross-reference to where writing a custom parser is
described).  An easier way is to write regular expressions that describe
your trace file format.  This wiki page describes how to do that.

The user writes regexes to indicate:
  * each structurally different log line
  * lines that should be ignored, such as comments
  * the granularity of log file parsing, when multiple traces appear within a single file (TODO: give example to clarify that).


(TODO:  Now, give an example or two of how to use it.  Don't defer that
information until the very end.)

(TODO: Are the regexes implicitly anchored?)



# Named Fields #

A log line may contain multiple pieces of information, such as the
event type, the timestamp, and additional information that is specific to
the particular type of event.  (At some point we may mine structural
invariants from such fields.)  The regex indicates these fields via the
Perl named capture group syntax **(?`<`fieldName`>`regex)**,
which is also supported by other regex implementations such as Java 7 and .NET.

A user is permitted to write a named capture group with no regex, as in
**(?`<`myFieldName`>`)**.  This form expands to **\s`*`(?`<`fieldName`>`\S`*`)\s`*`**, indicating that the field consists of non-whitespace, potentially surrounded by whitespace.  Some special fields, described later, have different defaults (TODO: make extensible in configuration file?).

# Special Fields #

Special fields, which have particular semantic value to synoptic are
capitalized.  Here is the current list of special fields:

  * TYPE The field that determines the type of event.  Events with the same type are eligible for partitioning into a single, summarized node.

  * TIME The field that determines the time when the event occurred.

  * HIDE If this field is provided and matches a non-zero length string, then that trace line is not provided as input to synoptic.

  * FILE This field is provided by synoptic, and is the absolute path to the file.

# Setting a Field to a Constant Value #

A field is typically set from the text of a log file line.  However, it is
also possible to set a field to a constant value, if the regular expression
in which it appears matches.

The syntax for these is **(?`<`fieldName=value`>`)**.  A common use is for
the special fields listed above.  For instance, adding **(?`<`HIDE=`>`true)**
to a regex makes the parser ignore any line that matches the regex.


You can also keep a running total of the number of times something has happened.  This can be achieved by specifying **(?<fieldName++>)** or **(?<++fieldName>)**, creating incrementing context values which are initially 0.  The preincrement will increase the field's value before recording it, while the postincrement waits until after.  (TODO: I don't understand this sentence:) Fields of this sort will be included with every event parsed.  They may be reset to a particular value by using the constant field syntax, with a numeric value.

TODO: consider (?`<`fieldName--`>`) ? probably not worth it
TODO: consider (?`<`foo+=value`>`)
TODO: consider allowing regular field matches set incremental-context fields

# Usage #

(TODO: give the command-line arguments as an explicit list, not buried in paragraph form.)

The user may provide multiple regular expressions on the command line:

```
java Main -s (\w*) -p #.*(?<HIDE=>true) -p ((?<TIME>\S*)?\s(?<TYPE>.*))
```

The **-p** flag specifies a regular line parser, while the **-s** flag specifies a special separator parser (TODO: not clear).  Here, the first regular parser indicates that comments (lines starting with #) should be ignored.  The second indicates that the particular line may consist an optional time, consuming the rest as the TYPE.

Another important parameter to pass in is the partition group specifier, **-g**.  This specifies the granularity of log analysis.  The power of synoptic comes from its ability to unify multiple traces, so it becomes important to extract multiple traces from a single log stream.

Each instance of the **-g** flag specifies a field which should be identical in order for the given event to appear in the same trace.
(TODO:  What is the argument to **-g**?  A field name?)

IDEA:  What if there could be an optional mode in which NULL fields take on the values found in nearby events?

The **-s** flag functions by (TODO: in addition to saying how it functions, also say what it does and what it is for) appending **(?`<`SEPCOUNT++`>`)** to the passed
regular expression (TODO: then doing what with it?), and ensuring that SEPCOUNT is among the fields used for partitioning into multiple traces.

Consider logs of this format:

```
# log created at Tue Nov  9 06:43:45 PST 2010
0 Server Connected
104 User 0x01 joined
213 User 0x02 joined
1209 User 0x01 transaction 0x9a initiated
2003 User 0x01 transaction 0x9a completed
2004 User 0x02 transaction 0x9a completed
# garbage collecting...
# garbage collecting... done.
3050 User 0x02 transaction 0x3c initiated
3500 Server Disconnected
4022 User 0x02 left
4022 User 0x02 transaction 0x3c failed
4030 User 0x02 left
4030 User 0x01 transaction 0x3c failed.
# log completed at Tue Nov  9 06:47:12 PST 2010
```

This can be parsed by the following set of parser specs:

```
#.*(?<HIDE=>true)
(?<TIME>) Server (?<TYPE>)
(?<TIME>) (?TYPE=init) User (?<uid>) transaction (?<tuid>) (?<TYPE>)
(?<TIME>) User (?<uid>) (?<TYPE>)
```

(TODO: Are spaces implicitly translated to `\w*`?  If not, then it looks like the above won't work since the init line will require two spaces before "User".)

(TODO: Check in this example, and tell the user how to run it.)

(TODO: What is the `(?TYPE=init)` syntax?  Should that be `(?<TYPE=init>)`?  Also, I don't see how that interacts with the `(?<TYPE>)` at the end of the line.)