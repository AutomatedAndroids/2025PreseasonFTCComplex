package subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import org.firstinspires.ftc.robotcore.external.Telemetry;

// Import the UniversalGyro from your util package
import util.UniversalGyro;

public class MechDriveSubsys extends SubsystemBase {

    private final Motor frontLeft, frontRight, backLeft, backRight;
    // Changed from GyroEx to UniversalGyro to be explicit
    private final UniversalGyro gyro;
    private final Telemetry telemetry;

    private boolean isFieldCentric = false;
    private double driveSpeedMultiplier = 1.0;

    public MechDriveSubsys(Motor fL, Motor fR, Motor bL, Motor bR, UniversalGyro gyro, Telemetry telemetry) {
        this.frontLeft = fL;
        this.frontRight = fR;
        this.backLeft = bL;
        this.backRight = bR;
        this.gyro = gyro;
        this.telemetry = telemetry;

        // --- MOTOR CONFIGURATION ---
        this.frontLeft.setRunMode(Motor.RunMode.RawPower);
        this.frontRight.setRunMode(Motor.RunMode.RawPower);
        this.backLeft.setRunMode(Motor.RunMode.RawPower);
        this.backRight.setRunMode(Motor.RunMode.RawPower);

        // BRAKE MODE (Stops drifting)
        this.frontLeft.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        this.frontRight.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        this.backLeft.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        this.backRight.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);

        // --- CRITICAL FIX: INVERT RIGHT MOTORS ---
        this.frontRight.setInverted(true);
        this.backRight.setInverted(true);

        this.gyro.reset();
    }

    @Override
    public void periodic() {
        telemetry.addData("Drive Mode", isFieldCentric ? "Field Centric" : "Robot Centric");
        telemetry.addData("Heading", getHeading());
    }

    public void drive(double strafeSpeed, double forwardSpeed, double turnSpeed) {

        // Field Centric Math
        if (isFieldCentric) {
            double heading = Math.toRadians(getHeading());
            double rotX = strafeSpeed * Math.cos(-heading) - forwardSpeed * Math.sin(-heading);
            double rotY = strafeSpeed * Math.sin(-heading) + forwardSpeed * Math.cos(-heading);
            strafeSpeed = rotX;
            forwardSpeed = rotY;
        }

        // Mecanum Math
        double denominator = Math.max(Math.abs(forwardSpeed) + Math.abs(strafeSpeed) + Math.abs(turnSpeed), 1);

        double flPower = (forwardSpeed + strafeSpeed + turnSpeed) / denominator;
        double blPower = (forwardSpeed - strafeSpeed + turnSpeed) / denominator;
        double frPower = (forwardSpeed - strafeSpeed - turnSpeed) / denominator;
        double brPower = (forwardSpeed + strafeSpeed - turnSpeed) / denominator;

        frontLeft.set(flPower * driveSpeedMultiplier);
        backLeft.set(blPower * driveSpeedMultiplier);
        frontRight.set(frPower * driveSpeedMultiplier);
        backRight.set(brPower * driveSpeedMultiplier);
    }

    public void toggleFieldCentric() {
        isFieldCentric = !isFieldCentric;
    }

    public void resetGyro() {
        gyro.reset();
    }

    public double getHeading() {
        return gyro.getHeading();
    }

    public void setMaxSpeed(double speed) {
        this.driveSpeedMultiplier = Math.max(0, Math.min(1, speed));
    }
}