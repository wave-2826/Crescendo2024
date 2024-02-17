package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.SparkPIDController;
import com.revrobotics.CANSparkBase.ControlType;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.lib.util.CANSparkMaxUtil;
import frc.lib.util.CANSparkMaxUtil.Usage;
import frc.robot.Constants;

// List class features here, including any motors, sensors, and functionality:
// Two Motors for the intake
// Primary motor has first contact with the Note.  Secondary controls the rollers behind the primary motor.
// This is the intake subsystem where the intake is fully internal in the robot
public class Transport extends SubsystemBase {
  // Declare member variables here
  private CANSparkMax frontIntakeMotor;
  private CANSparkMax lowerTransportMotor;
  private CANSparkMax beltIntakeMotor;
  private CANSparkMax upperTransportMotor;

  private SparkPIDController upperTransportPIDController;
  private SparkPIDController frontIntakePIDController;
  private SparkPIDController lowerTransportPIDController;
  private SparkPIDController beltIntakePIDController;

  public Transport() {
    // Instantiate member variables and necessary code
    frontIntakeMotor = new CANSparkMax(Constants.Intake.frontIntakeMotorCANID, CANSparkMax.MotorType.kBrushless);
    lowerTransportMotor = new CANSparkMax(Constants.Transport.lowerTransportMotorCANID, CANSparkMax.MotorType.kBrushless);
    beltIntakeMotor = new CANSparkMax(Constants.Intake.beltIntakeMotorCANID, CANSparkMax.MotorType.kBrushless);
    upperTransportMotor = new CANSparkMax(Constants.Transport.upperTransportMotorCANID, CANSparkMax.MotorType.kBrushless);

    frontIntakePIDController = frontIntakeMotor.getPIDController();
    lowerTransportPIDController = lowerTransportMotor.getPIDController();
    beltIntakePIDController = beltIntakeMotor.getPIDController();
    upperTransportPIDController = upperTransportMotor.getPIDController();
    
    configMotorControllers();
  }

  
  public void configMotorControllers() {
    frontIntakeMotor.restoreFactoryDefaults();
    CANSparkMaxUtil.setCANSparkMaxBusUsage(frontIntakeMotor, Usage.kPositionOnly);
    frontIntakeMotor.setSmartCurrentLimit(Constants.Intake.intakeCurrentLimit);
    frontIntakeMotor.setIdleMode(Constants.Intake.intakeIdleMode);
    frontIntakePIDController.setP(Constants.Intake.intakeKP);
    frontIntakePIDController.setI(Constants.Intake.intakeKI);
    frontIntakePIDController.setD(Constants.Intake.intakeKD);
    frontIntakePIDController.setFF(Constants.Intake.intakeKFF);
    frontIntakeMotor.enableVoltageCompensation(Constants.Intake.voltageComp);
    frontIntakeMotor.burnFlash();
    
    lowerTransportMotor.restoreFactoryDefaults();
    CANSparkMaxUtil.setCANSparkMaxBusUsage(lowerTransportMotor, Usage.kPositionOnly);
    lowerTransportMotor.setSmartCurrentLimit(Constants.Transport.transportCurrentLimit);
    lowerTransportMotor.setIdleMode(Constants.Transport.transportIdleMode);
    lowerTransportPIDController.setP(Constants.Transport.transportKP);
    lowerTransportPIDController.setI(Constants.Transport.transportKI);
    lowerTransportPIDController.setD(Constants.Transport.transportKD);
    lowerTransportPIDController.setFF(Constants.Transport.transportKFF);
    lowerTransportMotor.enableVoltageCompensation(Constants.Transport.voltageComp);
    lowerTransportMotor.burnFlash();
    
    beltIntakeMotor.restoreFactoryDefaults();
    CANSparkMaxUtil.setCANSparkMaxBusUsage(beltIntakeMotor, Usage.kPositionOnly);
    beltIntakeMotor.setSmartCurrentLimit(Constants.Intake.intakeCurrentLimit);
    beltIntakeMotor.setIdleMode(Constants.Intake.intakeIdleMode);
    beltIntakePIDController.setP(Constants.Intake.intakeKP);
    beltIntakePIDController.setI(Constants.Intake.intakeKI);
    beltIntakePIDController.setD(Constants.Intake.intakeKD);
    beltIntakePIDController.setFF(Constants.Intake.intakeKFF);
    beltIntakeMotor.enableVoltageCompensation(Constants.Intake.voltageComp);
    beltIntakeMotor.burnFlash();

    upperTransportMotor.restoreFactoryDefaults();
    upperTransportMotor.setSmartCurrentLimit(Constants.Transport.transportCurrentLimit);
    upperTransportMotor.enableVoltageCompensation(Constants.Transport.voltageComp);
    upperTransportMotor.setIdleMode(Constants.Transport.transportIdleMode);
    upperTransportPIDController.setP(Constants.Transport.transportKP);
    upperTransportPIDController.setI(Constants.Transport.transportKI);
    upperTransportPIDController.setD(Constants.Transport.transportKD);
    upperTransportPIDController.setFF(Constants.Transport.transportKFF);
    upperTransportMotor.burnFlash();
  }

  double desiredSpeedMetersPerSecond = 0;
  /**
   * If we're currently ejecting a note.
   * This happens when we accidentally intake two notes.
   * When this is true, the front and back intake rollers
   * run quickly in reverse to eject the note.
   */
  boolean isEjectingNote = false;

  /**
   * The speed of the top transport motor when the intake is not running.
   */
  double upperTransportSpeedTarget = 0; 

  /**
   * Sets the speed of the top transport motor. If the intake is running, this is overriden to avoid ripping
   * notes and/or the robot apart.
   * @param speed The speed the the edge of the wheels will move at, in meters per second.
   * This is effectively the speed that the note moves.
   */
  public void setUpperTransportSpeed(double speed) {
    upperTransportSpeedTarget = speed;
  }
    
  /**
   * Returns if the intake is currently active, meaning the speed is nonzero.
   * @return
   */
  public boolean isActive() {
    return desiredSpeedMetersPerSecond != 0;
  }

  /**
   * Sets the intake active. False sets the speed to 0 and true sets the speed to `Constants.Intake.intakeSpeed`.
   * @param active
   */
  public void setActive(boolean active) {
    setIntakeSpeed(active ? Constants.Intake.intakeSpeed : 0);
  }

  /**
   * Sets the intake motors to the given speed.
   * @param speed The speed the the belt/edge of intake wheels will move at, in meters per second.
   * This is effectively the speed that the note moves.
   */
  private void setIntakeSpeed(double speedInMetersPerSecond) {
    desiredSpeedMetersPerSecond = speedInMetersPerSecond;
  }

  /**
   * Begin ejecting the note currently in the intake.  
   * This happens when we accidentally intake two notes.
   */
  public void ejectNote() {
    isEjectingNote = true;
  }

  /**
   * Begin ejecting the note currently in the intake.  
   * This happens when we accidentally intake two notes.
   */
  public void stopEjectingNote() {
    isEjectingNote = false;
  }

  /**
   * Gets the rpm that an intake motor should run at, based on the target speed of the edge of the wheels and belt.  
   * This is effectively the speed that the notes move at.
   * @param speedMetersPerSecond The speed the the belt/edge of intake wheels will move at, in meters per second.
   * This is effectively the speed that the note moves.
   * @return
   */
  private double getIntakeMotorRPM(double speedMetersPerSecond) {
    double beltPulleyCircumference = Constants.Intake.beltPulleyRadius * Math.PI * 2;
    double revolutionsPerSecond = speedMetersPerSecond / beltPulleyCircumference;
    return revolutionsPerSecond * 60;
  }

  @Override
  public void periodic() {
    double speedMetersPerSecond = isEjectingNote ? -Constants.Intake.ejectSpeedMetersPerSecond : desiredSpeedMetersPerSecond;
    
    double rpm = getIntakeMotorRPM(speedMetersPerSecond);

    frontIntakePIDController.setReference(rpm, ControlType.kVelocity);
    lowerTransportPIDController.setReference(-rpm, ControlType.kVelocity);
    beltIntakePIDController.setReference(-rpm, ControlType.kVelocity);

    double upperTransportSpeed = 0;
    if (rpm > 0) {
      upperTransportSpeed = rpm;
    } else {
      upperTransportSpeed = getIntakeMotorRPM(upperTransportSpeedTarget);
    }

    upperTransportPIDController.setReference(upperTransportSpeed, ControlType.kVelocity);
  }
}
