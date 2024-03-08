package frc.robot.controls;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.Trigger;

public class VibrationFeedback {
    private static VibrationFeedback instance = null;
    public static VibrationFeedback getInstance() {
        if (instance == null) {
            instance = new VibrationFeedback();
        }
        return instance;
    }

    private final Timer matchTimer = new Timer();

    public void teleopInit() {
        matchTimer.reset();
        matchTimer.start();
    }
    public void teleopExit() {
        matchTimer.stop();
    }
    
    private VibrationFeedback() {
        new Trigger(() -> matchTimer.hasElapsed(45)).onTrue(
            new InstantCommand(() -> runPattern(VibrationPatternType.NinetySecondWarning)));
        new Trigger(() -> matchTimer.hasElapsed(75)).onTrue(
            new InstantCommand(() -> runPattern(VibrationPatternType.SixtySecondWarning)));
        new Trigger(() -> matchTimer.hasElapsed(105)).onTrue(
            new InstantCommand(() -> runPattern(VibrationPatternType.ThirtySecondWarning)));
    }

    private enum Controller {
        Driver,
        Operator,
        Both
    }

    private class SetVibrationCommand extends InstantCommand {
        private static void setVibration(Controller controller, double left, double right) {
            switch (controller) {
                case Driver:
                    Controls.getInstance().setDriverRumble(left, right);
                    break;
                case Operator:
                    Controls.getInstance().setOperatorRumble(left, right);
                    break;
                case Both:
                    Controls.getInstance().setDriverRumble(left, right);
                    Controls.getInstance().setOperatorRumble(left, right);
                    break;
            }
        }

        public SetVibrationCommand(Controller controller, double left, double right) {
            super(() -> setVibration(controller, left, right));
        }
    }

    private class VibrationPulse extends SequentialCommandGroup {
        public VibrationPulse(Controller controller, double left, double right, double duration, double wait) {
            addCommands(
                new SetVibrationCommand(controller, left, right),
                new SetVibrationCommand(controller, 0, 0).withTimeout(duration),
                new WaitCommand(wait)
            );
        }
        public VibrationPulse(Controller controller, double left, double right, double duration) {
            this(controller, left, right, duration, 0.05);
        }
        public VibrationPulse(Controller controller, double left, double right) {
            this(controller, left, right, 0.1);
        }
    }

    public enum VibrationPatternType {
        IntakingNote,

        NinetySecondWarning,
        SixtySecondWarning,
        ThirtySecondWarning
    }

    public void runPattern(VibrationPatternType pattern) {
        switch (pattern) {
            case IntakingNote:
                new SequentialCommandGroup(
                    new VibrationPulse(Controller.Operator, 0.75, 0.5),
                    new VibrationPulse(Controller.Operator, 0.5, 0.75),
                    new VibrationPulse(Controller.Operator, 0.5, 0.5)
                ).schedule();
                break;
            
            case NinetySecondWarning:
                new SequentialCommandGroup(
                    new VibrationPulse(Controller.Both, 0.5, 0.5, 0.3, 0.1),
                    new VibrationPulse(Controller.Both, 0.35, 0.35, 0.3, 0.1),
                    new VibrationPulse(Controller.Both, 0.2, 0.2, 0.3, 0.1)
                ).schedule();
                break;
            case SixtySecondWarning:
                new SequentialCommandGroup(
                    new VibrationPulse(Controller.Both, 0.5, 0.5, 0.3, 0.1),
                    new VibrationPulse(Controller.Both, 0.35, 0.35, 0.3, 0.1)
                ).schedule();
                break;
            case ThirtySecondWarning:
                new VibrationPulse(Controller.Both, 0.5, 0.5, 0.3).schedule();
                break;
        }
    }
}
