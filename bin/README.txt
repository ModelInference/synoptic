To patch the McScM source tree, do the following:

1. To patch it with patch_mcscm-1.2.1_validation_fix.txt:

# First, make sure that the mcscm src/ folder is in your current path:
$ cd mcscm-1.2.1/

# Then, run patch:
$ patch -p0 < patch_mcscm-1.2.1_validation_fix.txt


2. To patch it with patch_mcscm-1.2.1_verify_quiet.txt

# First, make sure that the mcscm src/ folder is in your current path:
$ cd mcscm-1.2.1/

# Then, run patch:
$ patch -b src/ui/verify.ml patch_mcscm-1.2.1_verify_quiet.txt
