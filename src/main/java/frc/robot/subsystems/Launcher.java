package frc.robot.subsystems;

import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RepeatCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.lib.util.CANSparkMaxUtil;
import frc.lib.util.CANSparkMaxUtil.Usage;
import frc.robot.Constants;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkPIDController;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkBase.SoftLimitDirection;

// List class features here, including any motors, sensors, and functionality:
// 2 Launcher motors to score notes
// 1 motor to aim (~30-60 degree window)
// 1 CAN Coder for getting the absolute angle
public class Launcher extends SubsystemBase {
  // Declare member variables here
  private CANSparkMax topRollerMotor;
  private CANSparkMax bottomRollerMotor;
  private CANSparkMax angleLauncherMotor;

  private RelativeEncoder angleLauncherEncoder;
  private DutyCycleEncoder absoluteAngleLauncherEncoder;

  private final SparkPIDController topLaunchRollerPIDController;
  private final SparkPIDController bottomLaunchRollerPIDController;
  private final SparkPIDController anglePIDController;
  
  boolean intakingNote = false;
  
  public Launcher() {
    // Instantiate member variables and necessary code
    topRollerMotor = new CANSparkMax(Constants.Launcher.topRollerCANID, CANSparkMax.MotorType.kBrushless);
    bottomRollerMotor = new CANSparkMax(Constants.Launcher.bottomRollerCANID, CANSparkMax.MotorType.kBrushless);
    angleLauncherMotor = new CANSparkMax(Constants.Launcher.angleMotorCANID, CANSparkMax.MotorType.kBrushless);

    angleLauncherEncoder = angleLauncherMotor.getEncoder();
    absoluteAngleLauncherEncoder = new DutyCycleEncoder(0);

    topLaunchRollerPIDController = topRollerMotor.getPIDController();
    bottomLaunchRollerPIDController = bottomRollerMotor.getPIDController();

    anglePIDController = angleLauncherMotor.getPIDController();

    Constants.Launcher.rollerConfig.configure(topRollerMotor, topLaunchRollerPIDController);
    Constants.Launcher.rollerConfig.configure(bottomRollerMotor, bottomLaunchRollerPIDController);

    // Constants.Launcher.angleConfig.configure(angleLauncherMotor, anglePIDController);
    // Doesn't work. TODO: use the configure method instead of manually doing it here?
    angleLauncherMotor.restoreFactoryDefaults();
    CANSparkMaxUtil.setCANSparkMaxBusUsage(angleLauncherMotor, Usage.kPositionOnly);
    angleLauncherMotor.setSmartCurrentLimit(15);
    angleLauncherMotor.setIdleMode(IdleMode.kBrake);
    anglePIDController.setP(0.1);
    anglePIDController.setI(0.0);
    anglePIDController.setD(0.0);
    anglePIDController.setFF(0.000175);
    angleLauncherMotor.enableVoltageCompensation(12.0);
    angleLauncherMotor.setInverted(Constants.Launcher.invertAngle);
    angleLauncherMotor.setSoftLimit(SoftLimitDirection.kReverse, (float)Constants.Launcher.softStopMarginLow.getRotations());
    angleLauncherMotor.setSoftLimit(SoftLimitDirection.kForward, (float)(1 - Constants.Launcher.softStopMarginHigh.getRotations()));
    angleLauncherMotor.burnFlash();
    resetToAbsolute();

    launchRollersSlow();
  }

  public double getAbsoluteLauncherAngleDegrees() {
    return absoluteAngleLauncherEncoder.getAbsolutePosition() * 360;
  }

  public double getLauncherConchAngleDegrees() {
    return (angleLauncherEncoder.getPosition() / Constants.Launcher.angleMotorGearboxReduction * 360.) % 360.;
  }

  public void resetToAbsolute() {
    double absolutePositionDegrees = (getAbsoluteLauncherAngleDegrees() - Constants.Launcher.angleOffset) % 360.;
    angleLauncherEncoder.setPosition(absolutePositionDegrees / 360. * Constants.Launcher.angleMotorGearboxReduction);
  }

  /**
   * The current target launcher angle.
   */
  public double launcherAngle = 45;
  public double launcherSpeed = 1200;
  /**
   * Calculates the required conch angle from the wanted launcher angle.
   * @param angle
   */
  public void setLauncherAngle(Rotation2d angle) {
    launcherAngle = angle.getDegrees();
    SmartDashboard.putNumber("LauncherAngle", launcherAngle);
    
    // All length units here are in inches
    double conchToPivotDistance = 5.153673;
    double pivotToConchReactionBarDistance = 4.507;
    double angleOffsetRadians = Units.degreesToRadians(23.53);
    // Law of cosines
    double requiredRadius = Math.sqrt(
      conchToPivotDistance * conchToPivotDistance + pivotToConchReactionBarDistance * pivotToConchReactionBarDistance
      - 2 * conchToPivotDistance * pivotToConchReactionBarDistance * Math.cos(Math.max(angle.getRadians() - angleOffsetRadians, 0.0))
    );
    
    double lowRadius = 0.1875; // in, from CAD
    double radiusIncrease = 3.4375; // in, from CAD: (4 -0.375/2-0.5/2)-(1/8)

    double conchAngleRadians = (requiredRadius - lowRadius) / radiusIncrease * (Math.PI * 2);
    if(conchAngleRadians < Constants.Launcher.softStopMarginLow.getRadians()) {
      // The angle is lower than we can achieve
      conchAngleRadians = Constants.Launcher.softStopMarginLow.getRadians();
    }
    if(conchAngleRadians > Math.PI * 2 - Constants.Launcher.softStopMarginHigh.getRadians()) {
      // The angle is higher than we can achieve
      conchAngleRadians = Math.PI * 2 - Constants.Launcher.softStopMarginHigh.getRadians();
    }

    anglePIDController.setReference(Rotation2d.fromRadians(conchAngleRadians).getRotations() * Constants.Launcher.angleMotorGearboxReduction, CANSparkMax.ControlType.kPosition);
  }
  /**
   * Gets the current target launcher angle.
   * @return
   */
  public double getLauncherAngle() {
    return launcherAngle;
  }

  /**
   * Enables the launch rollers.
   */
  public void launchRollersFast() {
    launcherSpeed = Constants.Launcher.maxRollerVelocity;
  }

  /**
   * Disables the launch rollers.
   */
  public void launchRollersSlow() {
    launcherSpeed = Constants.Launcher.launchRollerVelocity;
  }

  /**
   * Changes the current launcher speed by amount (launcher speed = launcher speed + amount).
   * @param amount
   */
  public void changeLauncherSpeed(double amount) {
    launcherSpeed += amount;
  }

  /**
   * 
   */
  public void setLauncherInverted(boolean inverted) {
    intakingNote = inverted;
  }

  public void runLauncher() {
    if (intakingNote) {
      topLaunchRollerPIDController.setReference(-1500, CANSparkMax.ControlType.kVelocity);
      bottomLaunchRollerPIDController.setReference(-1500, CANSparkMax.ControlType.kVelocity);    
    } else {
      topLaunchRollerPIDController.setReference(launcherSpeed, CANSparkMax.ControlType.kVelocity);
      bottomLaunchRollerPIDController.setReference(launcherSpeed, CANSparkMax.ControlType.kVelocity);    
    }
  }

  @Override
  public void periodic() {
    runLauncher();
    
    SmartDashboard.putNumber("Launcher absolute encoder", getAbsoluteLauncherAngleDegrees());
    SmartDashboard.putNumber("Launcher relative encoder", getLauncherConchAngleDegrees());
  }
}
