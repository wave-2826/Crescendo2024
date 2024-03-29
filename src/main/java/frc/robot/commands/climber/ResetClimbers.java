package frc.robot.commands.climber;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.climber.Climber;

/**
 * Moves the climber sides down until the motors stall. Resets the climber encoders once at the bottom.
 */
public class ResetClimbers extends SequentialCommandGroup {
    public ResetClimbers() {
        Climber climber = Climber.getInstance();
        addRequirements(climber);
        addCommands(
            new ParallelCommandGroup(
                new ClimberSideFullyDown(climber::setLeftPosition, climber::setLeftSpeed, climber::getLeftPosition, climber::getLeftMotorStallingBottom),
                new ClimberSideFullyDown(climber::setRightPosition, climber::setRightSpeed, climber::getRightPosition, climber::getRightMotorStallingBottom)
            ),
            new InstantCommand(climber::resetLeftEncoder),
            new InstantCommand(climber::resetRightEncoder)
        );
    }
}
