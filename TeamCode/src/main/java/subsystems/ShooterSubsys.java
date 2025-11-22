package subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.hardware.motors.CRServo;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class ShooterSubsys extends SubsystemBase{

    private CRServo feeder;
    private Motor shooter;
    private Telemetry telemetry;

    // --- CONFIGURATION ---
    // Change this based on your exact motor! (28 is standard for 1:1 Rev/GoBilda)
    private static final double TICKS_PER_REV = 28.0;

    // The RPM you want to reach
    private static final double TARGET_RPM = 1000;

    // Calculate Ticks Per Second: (RPM / 60) * TicksPerRev
    private static final double TARGET_VELOCITY = (TARGET_RPM / 60.0) * TICKS_PER_REV;

    public ShooterSubsys(CRServo feeder, Motor shooter, Telemetry telemetry) {
        this.feeder = feeder;
        this.shooter = shooter;
        this.telemetry = telemetry;

        // Configure Motor for Velocity Control
        this.shooter.setRunMode(Motor.RunMode.VelocityControl);

        // Zero Power Behavior (Float is usually better for high-speed flywheels)
        this.shooter.setZeroPowerBehavior(Motor.ZeroPowerBehavior.FLOAT);

        // Note: If the motor vibrates or doesn't reach speed, you may need to tune PIDF.
        // this.shooter.setVeloCoefficients(0.05, 0, 0); // Example tuning if needed
    }

    public void spin_shoot() {
        // In VelocityControl mode, .set() takes Ticks-Per-Second, not Power (0-1)
        shooter.set(TARGET_VELOCITY);

    }

    public void feed() {
        feeder.set(1);
    }

    public void stopShooting() {
        shooter.set(0);
        feeder.set(0);
        shooter.stopMotor(); // Ensures velocity is cleared
    }

    @Override
    public void periodic() {
        super.periodic();
        double[] velocityTicksPerSec = shooter.getVeloCoefficients();

        // Convert to RPM: (Velocity / 28) * 60
        double currentRPM = (velocityTicksPerSec[1] / 28.0) * 60;

        // Display on Driver Station
        telemetry.addData("Raw Velocity coeffic", velocityTicksPerSec[1]);
        telemetry.addData("Current RPM ig", currentRPM);
    }
}