VerifyPin is a simple authentication program that verifies pin. It
allows pin entering for at most 3 times.

Directory overview:
=====================

- src/ contains the source code for VerifyPin in Java.

- verify-pin.dot.png is pre-generated with stateful synoptic for
  trace.txt with args.txt

- predicted paths is a post-processing of the model in
  verify-pin.dot.png to emit paths that are in the model but are not
  in trace.txt -- that is, they are predicted by the StatefulSyn
  approach.
