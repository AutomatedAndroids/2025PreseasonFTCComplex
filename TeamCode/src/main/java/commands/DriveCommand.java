package commands;

import com.arcrobotics.ftclib.command.CommandBase;
import java.util.function.DoubleSupplier;
import subsystems.MechDriveSubsys;

public class DriveCommand extends CommandBase {

    private final MechDriveSubsys driveSubsys;
    private final DoubleSupplier strafeSpeed, forwardSpeed, turnSpeed;

    public DriveCommand(MechDriveSubsys driveSubsys,
                        DoubleSupplier strafeSpeed,
                        DoubleSupplier forwardSpeed,
                        DoubleSupplier turnSpeed) {
        this.driveSubsys = driveSubsys;
        this.strafeSpeed = strafeSpeed;
        this.forwardSpeed = forwardSpeed;
        this.turnSpeed = turnSpeed;

        addRequirements(driveSubsys);
    }

    @Override
    public void execute() {
        driveSubsys.drive(
                strafeSpeed.getAsDouble(),
                forwardSpeed.getAsDouble(),
                turnSpeed.getAsDouble()
        );
    }
}