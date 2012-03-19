Via Jenny:

* I have sample input consisting of 2 traces that could theoretically
  be refined by Synoptic in 2 ways to satisfy the mined
  invariants. We'll call these models a and b. These models accept a
  different set of synthetic traces.

* In practice, Synoptic consistently generates model a.

* Given the same input, InvariMint creates a model that is the union
  of a and b, accepting both sets of synthetic traces.

* If I remove spurious edges from the InvariMint model, I get model b.

* b =/= a

It seems that InvariMint generates a model that is the union of all
possible Synoptic models, and will potentially also include additional
synthetic traces that stem from the spurious edges we initially
found. Our working definition of spurious edges is insufficient for
distinguishing these two.
