The common command files are

	run.bat	to run remote stepper - use before ct
	run-async.bat	to run remote stepper with asynchronous observervations - use before ct
	atm.bat	to run ATMExample interactively

The various analysis and test files are named according to this convention

	<type>_<test>.<ext>

<type> is one of

	ct	command file to run a testsuite with NModel conformance tester
	split	command file splitting a testsuite into individual test cases
	run	command file to run a testsuite with file stepper

<test> see NModelRS/dotnet/ATM/test/README.txt

<ext> is file extension, like txt or bat.

See also NModelRS/dotnet/ATM/test for analysis and generation of testsuites. 