# Student Project Ideas #

---


Synoptic is being actively developed and there are many ways to make a significant contribution to the project. This page lists project ideas that are intended for undergraduate students in computer science. The degree of familiarity with Synoptic, project difficulty, and the amount of independent research required all vary from one project to the next. However, we hope that this list is a representative sample of topics that a student joining the Synoptic project might work on. Note that this list is not intended to be exhaustive; we have other project ideas that are not mentioned here and we are open to considering ideas for projects that students may generate on their own.


## Mining Apache Logs ##

---


Synoptic can be used to study Apache access logs to understand user website behavior. This understanding can then be leveraged to, for example, optimize the website's design. This project could be roughly broken up into the following steps:
  1. Define a set of regular expressions to parse the Apache log format.
  1. Run Synoptic on logs from a few different websites.
  1. Consider what can be gleaned from the resulting graphs.
  1. Design and implement a few extension to Synoptic to make the output more relevant to the idea.
  1. Finally, figure out how to leverage Synoptic's output in website design.

## Making Synoptic graphs Interactive ##

---


This project idea is very broad. Here are some neat ideas in this space:
  * It would be nice to allow users to interactively refine and coarsen a single node or a sections of the Synoptic graph, with just a few clicks.
  * It would help users to expose the mapping between nodes in the graph and the input log files where the corresponding event instances are found. So, but studying the graph a user can quickly navigate to the events in the log.
  * Finally, it would be neat to expose the Synoptic refinement and coarsening steps visually. The user could watch as the Synoptic graph evolves, and possibly pause\rewind Synoptic execution as it occurs.

## Performance-based Splitting Heuristics ##

---


This is another broad idea that hasn't been completely worked out. The key idea is to refine the graph not by just relying on differences in event behavior, but by using performance data associated with events. This topic would require significant familiarity with Synoptic internals.