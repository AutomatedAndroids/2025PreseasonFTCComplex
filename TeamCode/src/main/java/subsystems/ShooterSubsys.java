package subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.CRServo;
import com.qualcomm.robotcore.hardware.Servo;

public class ShooterSubsys extends SubsystemBase {

    private CRServo feeder;

    private Motor shooter;
    public ShooterSubsys(CRServo feeder, Motor shooter) {
        this.feeder = feeder;
        this.shooter = shooter;
    }

    public void spin_shoot() {
        shooter.set(1);
    }
    public void feed() {
        feeder.set(1);
    }

    public void stopShooting() {
        shooter.set(0);
        feeder.set(0);

    }


}
