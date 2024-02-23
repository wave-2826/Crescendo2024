package frc.robot.commands;

import java.util.function.DoubleSupplier;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants;
import frc.robot.subsystems.Transport;

public class TeleopIntake extends Command {
    private Transport transportSubsystem;

    // Driver controller
    private Joystick operator;

    public TeleopIntake(
            Transport transportSubsystem,
            Joystick operator) {
        this.transportSubsystem = transportSubsystem;
        addRequirements(transportSubsystem);

        this.operator = operator;
    }

    @Override
    public void initialize() {
        // /**
        //  * A trigger for the intake toggle button, from 0 to 1. Gets the value from
        //  * the intake speed axis.
        //  */
        // Trigger intakeTrigger  = new Trigger (() -> {
        //     return driver.getRawAxis(XboxController.Axis.kRightTrigger.value) > Constants.Intake.intakeDeadband;
        // });

        // intakeTrigger.onTrue(new InstantCommand(() -> 
        //     transportSubsystem.setActive(!transportSubsystem.isActive())
        // ));
    }

    @Override
    public void execute() {
        if(DriverStation.isAutonomous()) return;

        // A button to enable the bottom rollers, which includes the intake and bottom transport roller.
        double bottomRollersRunning = operator.getRawAxis(XboxController.Axis.kRightTrigger.value);
        // The button to enable the top transport rollers, which will launch a note.
        double topRollersRunning = operator.getRawAxis(XboxController.Axis.kLeftTrigger.value);
        boolean reverseTransport = operator.getRawButton(XboxController.Button.kB.value);

        transportSubsystem.setIntakeSpeed(bottomRollersRunning > 0.3 ? (reverseTransport ? -10.0 : 10.0) : 0);
        transportSubsystem.setUpperTransportSpeed(topRollersRunning > 0.3 ? (reverseTransport ? -10.0 : 10.0) : 0);
    }
}
