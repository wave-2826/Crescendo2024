// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

//other example repos: 4607, 3457
//this code is based on team frc3512 SwerveBot-2022

package frc.robot;

import java.nio.channels.SelectableChannel;

import com.pathplanner.lib.auto.AutoBuilder;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.subsystems.*;
import frc.robot.commands.NoteManagement;
import frc.robot.commands.TeleopIntake;
import frc.robot.commands.TeleopSwerve;
import frc.robot.commands.Auto.LaunchAndDrive;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {

  private final SendableChooser<Command> autoChooser;

  /* Controllers */
  private final Joystick driver = new Joystick(0);
  private final Joystick operator = new Joystick(1);

  /* Drive Controls */
  private static final int translationAxis = XboxController.Axis.kLeftY.value;
  private static final int strafeAxis = XboxController.Axis.kLeftX.value;
  private static final int rotationAxis = XboxController.Axis.kRightX.value;

  /* Driver Buttons */
  private final JoystickButton zeroGyro =
      new JoystickButton(driver, XboxController.Button.kY.value);
  private final JoystickButton robotCentric =
      new JoystickButton(driver, XboxController.Button.kLeftBumper.value);
  private final JoystickButton updateOdometryPose = 
      new JoystickButton(driver, XboxController.Button.kB.value);

  /* Operator Buttons */
  private final JoystickButton launchNote =
      new JoystickButton(operator, XboxController.Button.kA.value);

  /* Subsystems */
  private final Swerve swerveSubsystem = new Swerve();
  private final Launcher launcherSubsystem = new Launcher(operator);
  private final Transport transportSubsystem = new Transport();
  private final Elevator elevatorSubsystem = new Elevator();

  /** The container for the robot. Contains subsystems, IO devices, and commands. */
  public RobotContainer() {
    swerveSubsystem.setDefaultCommand(
      new TeleopSwerve(
        swerveSubsystem,
        () -> -driver.getRawAxis(translationAxis),
        () -> -driver.getRawAxis(strafeAxis),
        () -> -driver.getRawAxis(rotationAxis),
        () -> !robotCentric.getAsBoolean()
      ));

    transportSubsystem.setDefaultCommand(
      new TeleopIntake(
        transportSubsystem,
        driver
      ));

    elevatorSubsystem.setDefaultCommand(
      new NoteManagement(
        elevatorSubsystem,
        transportSubsystem,
        launchNote
      )
    );
    
    // Configure the button bindings
    configureButtonBindings();

    autoChooser = AutoBuilder.buildAutoChooser(); // Default auto will be `Commands.none()`
    autoChooser.addOption("Lunch and Drive", new LaunchAndDrive(swerveSubsystem, launcherSubsystem, 5.0));
    SmartDashboard.putData("Auto Mode", autoChooser);
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    /* Driver Buttons */
    zeroGyro.onTrue(new InstantCommand(swerveSubsystem::zeroGyro));

    updateOdometryPose.onTrue(new InstantCommand(swerveSubsystem::updateOdometryPose));

    /* Operator Buttons */

    /*
     * Operator Button A to Launch the Note
     */
    final JoystickButton buttonA = new JoystickButton(operator, XboxController.Button.kA.value);
    buttonA.onTrue(new InstantCommand(() -> {
      System.out.println("Launching Rollers fast");
      launcherSubsystem.launchRollersFast();
    }));

    /*
     * Operator Y button would set the Launcer to intake the Note.
     * This feature may be temporary to reverse the Launcer rollers.
     */
    final JoystickButton buttonY = new JoystickButton(operator, XboxController.Button.kY.value);
    buttonY.onTrue(new InstantCommand(() -> {
      launcherSubsystem.setLaunchNoteIn();
      launcherSubsystem.launchRollersFast();
      System.out.println("Intaking Note Rollers fast");
    }));

    final JoystickButton rightBumper = new JoystickButton(operator, XboxController.Button.kRightBumper.value);
    rightBumper.onTrue(new InstantCommand(launcherSubsystem::launchRollersSlow));  
  }

  public Joystick getOperator() {
    return operator;
  }

  public Launcher getLauncher() {
    return launcherSubsystem;
  }
  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return autoChooser.getSelected();
  }
}
