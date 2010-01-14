/**
 * Provides an <a href="http://nmodel.codeplex.com/">NModel</a> stepper for java implementations.
 * <br>
 * Use {@link dk.hippogrif.nmodel.stepper.Stepper} methods in the test harness to read actions and write replies.
 * <br>
 * For <b>offline testing</b> use {@link dk.hippogrif.nmodel.stepper.FileStepper}
 * to read a testsuite generated with NModel's Offline Test Generator otg.
 * The testsuite contains actions and expected replies checked
 * by FileStepper monitoring progress.
 * <br>
 * For <b>online testing</b> use {@link dk.hippogrif.nmodel.stepper.RemoteStepper}
 * to receive actions and send replies over TCP to NModel's Conformance Tester ct
 * configured with Stepper of RemoteStepperProxy.dll.
 * <br>
 * For <b>asynchronous online testing</b> supplement with {@link dk.hippogrif.nmodel.stepper.Observer}
 * to send observed actions as datagrams to ct configured with AsyncStepper of RemoteStepperProxy.dll.
 */

package dk.hippogrif.nmodel.stepper;

