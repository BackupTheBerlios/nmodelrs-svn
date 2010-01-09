/**
 * Provides an <a href="http://nmodel.codeplex.com/">NModel</a> stepper for java implementations.
 * <br>
 * Use {@link dk.hippogrif.nmodel.Stepper} methods in the test harness to read actions and write replies.
 * <br>
 * For <b>offline testing</b> use {@link dk.hippogrif.nmodel.FileStepper}
 * to read a testsuite generated with NModel's Offline test generator otg.
 * The testsuite contains actions and expected replies checked
 * by FileStepper monitoring progress.
 * <br>
 * For <b>online testing</b> use {@link dk.hippogrif.nmodel.RemoteStepper}
 * to receive actions and send replies over TCP to NModel's Conformance tester ct
 * configured with Stepper of RemoteStepper.dll.
 * <br>
 * For <b>asynchronous online testing</b> supplement with {@link dk.hippogrif.nmodel.Observer}
 * to send asynchronous actions as datagrams to ct configured with AsyncStepper of RemoteStepper.dll.
 */

package dk.hippogrif.nmodel;

