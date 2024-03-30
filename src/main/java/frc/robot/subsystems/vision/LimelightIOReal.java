package frc.robot.subsystems.vision;

import frc.lib.LimelightHelpers;

public class LimelightIOReal implements LimelightIO {
    @Override
    public void updateInputs(LimelightIOInputs inputs) {
        LimelightHelpers.PoseEstimate limelightMeasurement = LimelightHelpers.getBotPoseEstimate_wpiBlue("limelight");
        inputs.pose = limelightMeasurement.pose;
        inputs.avgTagDist = limelightMeasurement.avgTagDist;
        inputs.tagCount = limelightMeasurement.tagCount;
        inputs.timestampSeconds = limelightMeasurement.timestampSeconds;
    }
}
