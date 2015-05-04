# Introduction #

---


The `.hgrc` file is typically located in your home folder (i.e, in `~/`). It customizes the behavior of the `hg` command. You can find out more about this file [here](http://www.selenic.com/mercurial/hgrc.5.html).

# Template #

---


Note: replace the tokens `FIRSTNAME`, `LASTNAME`, and `EMAIL` below with appropriate values.

```
[extensions]
shelve =
hgext.record=
color =
hgext.convert =
hgext.graphlog = 
fetch =
pager =
progress =
hgext.purge =

[pager]
pager = LESS='FSRX' less
ignore = version, help, update, serve, record

[ui]
# Use compression:
ssh = ssh -C
username=FIRSTNAME LASTNAME <EMAIL>

[color]
status.modified = yellow
status.added = green
status.removed = red
status.deleted = magenta
status.unknown = cyan
status.ignored = cyan
diff.diffline = bold
diff.extended = cyan bold
diff.file_a = red bold
diff.file_b = green bold
diff.hunk = yellow
diff.deleted = red
diff.inserted = green
diff.changed = white
diff.trailingwhitespace = bold red_background
```