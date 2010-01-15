The various analysis and test files are named according to this convention

	<type>_<test>.<ext>

<type> is one of

	ct	NModel online conformance tester - use like ct @ct_card1.txt
	fsm	NModel finite state machine to be combined with a model
	mpv	NModel model program viewer - use like mpv @mpv_card1.txt
	otg	NModel offline test generator - use like otg @otg_card1.txt
	testsuite	generated testsuite to be used with ct or java filestepper

<test> is one of

	cancel	custCancel when waitingForAnotherTrans
	card1	transactions with 
		- card 1 with two accounts: CHECKING $100, SAVINGS $1000
		- correct pin code
		- 10 $20 bills in ATM
		- withdrawal of $100
		- transfer of $1000
		- known (deterministic) checking sequence
	notrans	operation without transactions
	pinerror	pin retries scenarios
		- withdrawal after 3 pin errors
		- card is retained after 4 pin errors
		- withdrawal after 5 pin errors (!)
	withdrawal	deterministic transfer scenario with known bank implementation
	transfer	deterministic transfer scenario with known bank implementation
	transfer-nd	non-deterministic transfer scenario with unknown bank implementation

<ext> is file extension, like txt or bat.

See also NModelRS/java/ATM/test for testing of the java ATM implementation. 