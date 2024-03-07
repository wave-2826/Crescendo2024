package frc.robot.subsystems.transport;

import com.revrobotics.CANSparkBase.ControlType;
import com.revrobotics.CANSparkMax;
import com.revrobotics.SparkPIDController;

import frc.robot.Constants;

public class TransportIOReal implements TransportIO {
  private CANSparkMax frontIntakeMotor;
  private CANSparkMax lowerTransportMotor;
  private CANSparkMax backIntakeMotor;
  private CANSparkMax upperTransportMotor;

  private SparkPIDController upperTransportPIDController;
  private SparkPIDController frontIntakePIDController;
  private SparkPIDController lowerTransportPIDController;
  private SparkPIDController backIntakePIDController;

  public TransportIOReal() {
    frontIntakeMotor = new CANSparkMax(Constants.Intake.frontIntakeMotorCANID, CANSparkMax.MotorType.kBrushless);
    lowerTransportMotor = new CANSparkMax(Constants.Transport.lowerTransportMotorCANID, CANSparkMax.MotorType.kBrushless);
    backIntakeMotor = new CANSparkMax(Constants.Intake.backIntakeMotorCANID, CANSparkMax.MotorType.kBrushless);
    upperTransportMotor = new CANSparkMax(Constants.Transport.upperTransportMotorCANID, CANSparkMax.MotorType.kBrushless);

    frontIntakePIDController = frontIntakeMotor.getPIDController();
    lowerTransportPIDController = lowerTransportMotor.getPIDController();
    backIntakePIDController = backIntakeMotor.getPIDController();
    upperTransportPIDController = upperTransportMotor.getPIDController();
    
    Constants.Intake.intakeMotorConfig.configure(backIntakeMotor, backIntakePIDController);
    Constants.Intake.intakeMotorConfig.configure(frontIntakeMotor, frontIntakePIDController);

    Constants.Transport.transportMotorConfig.configure(lowerTransportMotor, lowerTransportPIDController);
    Constants.Transport.transportMotorConfig.configure(upperTransportMotor, upperTransportPIDController);
  }

  /**
   * Gets the rpm that an intake motor should run at, based on the target speed of the edge of the wheels and belt.  
   * This is effectively the speed that the notes move at.
   * @param speedMetersPerSecond The speed the the belt/edge of intake wheels will move at, in meters per second.
   * This is effectively the speed that the note moves.
   * @return
   */
  private double getIntakeMotorRPM(double speedMetersPerSecond, double gearRatio) {
    double wheelCircumference = Constants.Intake.wheelRadius * Math.PI * 2;
    double revolutionsPerSecond = speedMetersPerSecond / wheelCircumference * gearRatio;
    return revolutionsPerSecond * 60;
  }

  /**
   * Sets the transport speeds. Speeds are how fast the belt/edge of intake wheels will move at, in meters per second.
   * This is effectively the speed that the note moves.  
   * If the intake is running faster than the transport, the transport will run at the intake speed.
   * @param transportSpeedMetersPerSecond The speed that the upper transport wheels will spin.
   * @param intakeSpeedMetersPerSecond The speed that the intake wheels will spin.
   */
  @Override
  public void setTransportSpeed(double transportSpeedMetersPerSecond, double intakeSpeedMetersPerSecond) {
    double frontRPM = getIntakeMotorRPM(intakeSpeedMetersPerSecond, 25);
    double beltRPM = getIntakeMotorRPM(intakeSpeedMetersPerSecond, 15);

    frontIntakePIDController.setReference(frontRPM, ControlType.kVelocity);
    lowerTransportPIDController.setReference(-beltRPM, ControlType.kVelocity);
    backIntakePIDController.setReference(beltRPM, ControlType.kVelocity);

    double upperTransportSpeed = getIntakeMotorRPM(transportSpeedMetersPerSecond, 25);

    upperTransportPIDController.setReference(-upperTransportSpeed, ControlType.kVelocity);
  }
}