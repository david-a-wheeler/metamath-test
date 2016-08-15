
Note that this test suite has already caused some
variances to be detected and fixed.

Mario Carneiro reported on 14 Aug 2016 17:34:18 -0400:
"I fixed mmj2 so it will pass emptyline.mm (it was specifically looking for
this case and giving an error, I changed it to give an info message
instead), iset.mm (problems with DV checking with local $f) and nf.mm (it
wasn't resetting the stack when the symbol buffer overflows).
I also modified metamath-test to download mmj2 directly from
the github repo, and build from source. It now passes travis checks."

