package frc.robot.subsystems.climber;

import org.littletonrobotics.junction.AutoLog;

import edu.wpi.first.math.geometry.Rotation2d;

public interface ClimberIO {
  @AutoLog
  public static class ClimberIOInputs {
    public double leftClimberCurrentDrawAmps = 0;
    public double rightClimberCurrentDrawAmps = 0;

    public double leftPosition = 0;
    public double rightPosition = 0;
  }

  /** Updates the set of loggable inputs. */
  public default void updateInputs(ClimberIOInputs inputs) {}
  
  /** Resets the encoder value for the left motor to 0. */
  public default void resetLeftEncoder() {}
  
  /** Resets the encoder value for the right motor to 0. */
  public default void resetRightEncoder() {}

  /**
   * Sets the left motor speed in RPM. Positive values move the elevator downward.
   * @param speed The target speed, in RPM.
   */
  public default void setLeftSpeed(double speed) {}
  /**
   * Sets the right motor speed in RPM. Positive values move the elevator downward.
   * @param speed
   */
  public default void setRightSpeed(double speed) {}
  /**
   * Sets the right motor position in rotations, where 0 is the resting position and positive numbers are upward.
   * @param position
   */
  public default void setRightPosition(double position) {}
  /**
   * Sets the left motor position in rotations, where 0 is the resting position and positive numbers are upward.
   * @param position
   */
  public default void setLeftPosition(double position) {}
}