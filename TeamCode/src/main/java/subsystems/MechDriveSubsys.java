package subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.GyroEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import java.util.Arrays;
import java.util.Collections;

public class MechDriveSubsys extends SubsystemBase {

    private final Motor frontLeft, frontRight, backLeft, backRight;
    private final GyroEx gyro;
    private final Telemetry telemetry;

    // Drive settings
    private boolean isFieldCentric = false;
    private double driveSpeedMultiplier = 1.0;

    public MechDriveSubsys(Motor fL, Motor fR, Motor bL, Motor bR, GyroEx gyro, Telemetry telemetry) {
        this.frontLeft = fL;
        this.frontRight = fR;
        this.backLeft = bL;
        this.backRight = bR;
        this.gyro = gyro;
        this.telemetry = telemetry;

        // Configure motors (assumed generic configuration, adjust if you need specific brake modes)
        this.frontLeft.setRunMode(Motor.RunMode.RawPower);
        this.frontRight.setRunMode(Motor.RunMode.RawPower);
        this.backLeft.setRunMode(Motor.RunMode.RawPower);
        this.backRight.setRunMode(Motor.RunMode.RawPower);

        this.gyro.reset();
    }

    @Override
    public void periodic() {
        // Simple telemetry to know the subsystem is alive and what mode it's in
        telemetry.addData("Drive Mode", isFieldCentric ? "Field Centric" : "Robot Centric");
        telemetry.addData("Heading", getHeading());
        telemetry.addData("Speed Mult", driveSpeedMultiplier);
    }

    /**
     * The main drive method.
     * @param strafeSpeed  Left/Right stick X
     * @param forwardSpeed Forward/Back stick Y
     * @param turnSpeed    Turn stick X
     */
    public void drive(double strafeSpeed, double forwardSpeed, double turnSpeed) {

        // 1. Apply Field Centric adjustment if enabled
        if (isFieldCentric) {
            // Calculate rotation based on gyro angle
            double heading = Math.toRadians(getHeading());
            double rotX = strafeSpeed * Math.cos(-heading) - forwardSpeed * Math.sin(-heading);
            double rotY = strafeSpeed * Math.sin(-heading) + forwardSpeed * Math.cos(-heading);

            strafeSpeed = rotX;
            forwardSpeed = rotY;
        }

        // 2. Calculate Mecanum Power for each wheel
        // Math: Denominator is the largest motor power (absolute value) or 1
        // This ensures all powers maintain the same ratio, but only if at least one is > 1
        double denominator = Math.max(Math.abs(forwardSpeed) + Math.abs(strafeSpeed) + Math.abs(turnSpeed), 1);

        double flPower = (forwardSpeed + strafeSpeed + turnSpeed) / denominator;
        double blPower = (forwardSpeed - strafeSpeed + turnSpeed) / denominator;
        double frPower = (forwardSpeed - strafeSpeed - turnSpeed) / denominator;
        double brPower = (forwardSpeed + strafeSpeed - turnSpeed) / denominator;

        // 3. Apply global speed limit and set motors
        frontLeft.set(flPower * driveSpeedMultiplier);
        backLeft.set(blPower * driveSpeedMultiplier);
        frontRight.set(frPower * driveSpeedMultiplier);
        backRight.set(brPower * driveSpeedMultiplier);
    }

    /**
     * Toggles between Field Centric and Robot Centric control.
     */
    public void toggleFieldCentric() {
        isFieldCentric = !isFieldCentric;
    }

    /**
     * Resets the Gyro heading to 0.
     * Call this when the driver aligns the robot forward and presses a reset button.
     */
    public void resetGyro() {
        gyro.reset();
    }

    public double getHeading() {
        // Setup to return -180 to 180 or 0 to 360 depending on gyro config
        // FTCLib usually handles this well.
        return gyro.getHeading();
    }

    public void setMaxSpeed(double speed) {
        this.driveSpeedMultiplier = Math.max(0, Math.min(1, speed));
    }

    public void stop() {
        frontLeft.stopMotor();
        frontRight.stopMotor();
        backLeft.stopMotor();
        backRight.stopMotor();
    }
}