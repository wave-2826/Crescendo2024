package frc.robot.subsystems.drive;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.hardware.CANcoder;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkLowLevel.PeriodicFrame;
import com.revrobotics.CANSparkMax;
import com.revrobotics.REVLibError;
import com.revrobotics.RelativeEncoder;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.RobotController;
import frc.lib.config.CTREConfigs;
import frc.lib.config.SwerveModuleConstants;
import frc.lib.util.ShuffleboardContent;
import frc.robot.Constants;

import java.util.OptionalDouble;
import java.util.Queue;

/**
 * Module IO implementation for SparkMax drive motor controller, SparkMax turn motor controller (NEO,
 * NEO 550, or NEO Vortex), and CANCoder absolute encoder.
 *
 * <p>To calibrate the absolute encoder offsets, point the modules straight (such that forward
 * motion on the drive motor will propel the robot forward) and copy the reported values from the
 * absolute encoders using AdvantageScope. These values are logged under
 * "/Drive/ModuleX/TurnAbsolutePositionRad"
 */
public class SwerveModuleIOSparkMax implements SwerveModuleIO {
  private static final CTREConfigs ctreConfigs = new CTREConfigs();

  private final CANSparkMax driveSparkMax;
  private final CANSparkMax turnSparkMax;
  
  private final CANcoder cancoder;
  private final StatusSignal<Double> turnAbsolutePosition;

  private final RelativeEncoder driveEncoder;
  private final RelativeEncoder turnRelativeEncoder;
  private final Queue<Double> timestampQueue;
  private final Queue<Double> drivePositionQueue;
  private final Queue<Double> turnPositionQueue;

  private final boolean isTurnMotorInverted = true;
  private final Rotation2d absoluteEncoderOffset;

  public SwerveModuleIOSparkMax(SwerveModuleConstants moduleConstants) {
    driveSparkMax = new CANSparkMax(moduleConstants.driveMotorID, MotorType.kBrushless);
    turnSparkMax = new CANSparkMax(moduleConstants.angleMotorID, MotorType.kBrushless);
    cancoder = new CANcoder(moduleConstants.cancoderID);
    cancoder.getConfigurator().apply(ctreConfigs.swerveCanCoderConfig);
    turnAbsolutePosition = cancoder.getAbsolutePosition();
    BaseStatusSignal.setUpdateFrequencyForAll(50.0, turnAbsolutePosition);

    absoluteEncoderOffset = moduleConstants.angleOffset;

    driveSparkMax.restoreFactoryDefaults();
    turnSparkMax.restoreFactoryDefaults();

    driveSparkMax.setCANTimeout(250);
    turnSparkMax.setCANTimeout(250);

    driveEncoder = driveSparkMax.getEncoder();
    turnRelativeEncoder = turnSparkMax.getEncoder();

    turnSparkMax.setInverted(isTurnMotorInverted);
    driveSparkMax.setSmartCurrentLimit(40);
    turnSparkMax.setSmartCurrentLimit(30);
    driveSparkMax.enableVoltageCompensation(12.0);
    turnSparkMax.enableVoltageCompensation(12.0);

    driveEncoder.setPosition(0.0);
    driveEncoder.setMeasurementPeriod(10);
    driveEncoder.setAverageDepth(2);

    turnRelativeEncoder.setPosition(0.0);
    turnRelativeEncoder.setMeasurementPeriod(10);
    turnRelativeEncoder.setAverageDepth(2);

    driveSparkMax.setCANTimeout(0);
    turnSparkMax.setCANTimeout(0);

    driveSparkMax.setPeriodicFramePeriod(PeriodicFrame.kStatus2, (int) (1000.0 / SwerveModule.ODOMETRY_FREQUENCY));
    turnSparkMax.setPeriodicFramePeriod(PeriodicFrame.kStatus2, (int) (1000.0 / SwerveModule.ODOMETRY_FREQUENCY));
    timestampQueue = SparkMaxOdometryThread.getInstance().makeTimestampQueue();

    drivePositionQueue = SparkMaxOdometryThread.getInstance().registerSignal(
      () -> {
        double value = driveEncoder.getPosition();
        if (driveSparkMax.getLastError() == REVLibError.kOk) {
          return OptionalDouble.of(value);
        } else {
          return OptionalDouble.empty();
        }
      }
    );
    turnPositionQueue = SparkMaxOdometryThread.getInstance().registerSignal(
      () -> {
        double value = turnRelativeEncoder.getPosition();
        if (driveSparkMax.getLastError() == REVLibError.kOk) {
          return OptionalDouble.of(value);
        } else {
          return OptionalDouble.empty();
        }
      }
    );

    driveSparkMax.burnFlash();
    turnSparkMax.burnFlash();
  }

  /**
   * Initializes the Shuffleboard data view for this module.
   */
  public void initShuffleboard(SwerveModule module) {
    ShuffleboardContent.initSwerveModuleShuffleboard(module, this);
  }

  @Override
  public void updateInputs(SwerveModuleIOInputs inputs) {
    inputs.drivePositionRad = Units.rotationsToRadians(driveEncoder.getPosition()) / Constants.Swerve.driveGearRatio;
    inputs.driveVelocityRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(driveEncoder.getVelocity()) / Constants.Swerve.driveGearRatio;
    inputs.driveAppliedVolts = driveSparkMax.getAppliedOutput() * driveSparkMax.getBusVoltage();
    inputs.driveCurrentAmps = new double[] {driveSparkMax.getOutputCurrent()};

    inputs.turnAbsolutePosition = Rotation2d.fromRotations(turnAbsolutePosition.getValueAsDouble()).minus(absoluteEncoderOffset);
    inputs.turnPosition = Rotation2d.fromRotations(turnRelativeEncoder.getPosition() / Constants.Swerve.angleGearRatio);
    inputs.turnVelocityRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(turnRelativeEncoder.getVelocity()) / Constants.Swerve.angleGearRatio;
    inputs.turnAppliedVolts = turnSparkMax.getAppliedOutput() * turnSparkMax.getBusVoltage();
    inputs.turnCurrentAmps = new double[] {turnSparkMax.getOutputCurrent()};

    inputs.odometryTimestamps = timestampQueue.stream().mapToDouble((Double value) -> value).toArray();
    inputs.odometryDrivePositionsRad = drivePositionQueue.stream()
      .mapToDouble((Double value) -> Units.rotationsToRadians(value) / Constants.Swerve.driveGearRatio)
      .toArray();
    inputs.odometryTurnPositions = turnPositionQueue.stream()
      .map((Double value) -> Rotation2d.fromRotations(value / Constants.Swerve.angleGearRatio))
      .toArray(Rotation2d[]::new);
    
    timestampQueue.clear();
    drivePositionQueue.clear();
    turnPositionQueue.clear();
  }

  @Override
  public void setDriveVoltage(double volts) {
    driveSparkMax.setVoltage(volts);
  }

  @Override
  public void setTurnVoltage(double volts) {
    turnSparkMax.setVoltage(volts);
  }

  @Override
  public void setDriveBrakeMode(boolean enable) {
    driveSparkMax.setIdleMode(enable ? IdleMode.kBrake : IdleMode.kCoast);
  }

  @Override
  public void setTurnBrakeMode(boolean enable) {
    turnSparkMax.setIdleMode(enable ? IdleMode.kBrake : IdleMode.kCoast);
  }

  public double getCANcoderPositionDegrees() {
    // Position is 0 to 1, so multiply by 360 to get degrees
    return cancoder.getPosition().getValueAsDouble() * 360.0;
  }

  public double getDriveMotorCurrent() {
    return driveSparkMax.getOutputCurrent();
  }

  public double getTurnMotorCurrent() {
    return turnSparkMax.getOutputCurrent();
  }
}