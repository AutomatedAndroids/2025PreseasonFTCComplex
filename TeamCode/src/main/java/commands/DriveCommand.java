package commands;

import com.arcrobotics.ftclib.command.RunCommand;

public class DriveCommand extends RunCommand {
    public DriveCommand(TankDriveSubsystem driveSubsystem) {
        super(driveSubsystem::drive, driveSubsystem);
    }
}
