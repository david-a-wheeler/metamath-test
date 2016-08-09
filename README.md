[![Build Status](https://travis-ci.org/david-a-wheeler/metamath-test.svg?branch=master)](https://travis-ci.org/david-a-wheeler/metamath-test)

This is metamath-test, a test suite for verifiers implementing
the metamath specification (see us.metamath.org).

This includes a set of test files that *should* and *should not* pass.
The C metamath implementation includes some sample .mm files,
but no tests that are intended to *not* pass.  That's not good, because
if you replaced a verifier with the program "true" it would produce
the same results :-).  This package includes a
collection of "negative tests" that should *not* pass, and thus
more rigorously tests the verifiers.
This project includes .mm files, but they are
*not* necessarily the latest version of various theorems.
The purpose of this project is to test the verification tools themselves.

It also includes a set of drivers for running various metamath verifiers,
and a .travis.yml file that automatically downloads and compiles
some metamath verifiers, and then tries them on the test files.
That's helpful, because with a single shared test suite, any error
found in any verifier can be added to this shared test suite, and that
helps counter the same error from resurfacing anywhere.

The output conforms to the Test Anything Protocol (TAP); see
<https://en.wikipedia.org/wiki/Test_Anything_Protocol> and
<http://testanything.org/>.

Common changes:
* To add a test, add a new .mm file with the test, and modify
  run-testsuite to invoke it (using "pass" if it should pass or "fail" if
  it should fail).
* To add a new verifier, create a test-... driver to run it, add the name
  to the file DRIVERS, and modify .travis.yml to auto-download & compile it.

Key files:
* run-testsuite-all-drivers : Run testsuite against all drivers in DRIVERS.
* DRIVERS : Text file, list of drivers. "#" at beginning of line is a comment
* run-testsuite DRIVERNAME: Run entire testsuite using given driver
* NAME.mm: Test file, used by run-testsuite

In normal use, run all tests by invoking "run-testsuite-all-drivers".
If you want to run all tests using only the C metamath implementation, type:

    ./run-testsuite ./test-metamath

This testsuite focuses on detecting actual errors in verifying proofs,
not style or formatting issues.

Currently-supported verifiers:
* test-metamath: C metamath implementation by Norm Megill
* test-checkmm: C++ implementation by Eric Schmidt
* test-smetamath: Rust implementation
* test-mmj2: Java implementation (not invoked, some test failures)
* test-mmverifypy: Python implementation

This is MIT licensed, but many of the individual tests are licensed
under the CC0.

Many of the original test files are from the metamath C implementation.
My sincere thanks to Norm Megill for his work on metamath.

