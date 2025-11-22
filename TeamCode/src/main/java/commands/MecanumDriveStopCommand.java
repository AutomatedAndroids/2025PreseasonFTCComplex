package commands;

import com.arcrobotics.ftclib.command.RunCommand;

public class MecanumDriveStopCommand extends RunCommand {
    public MecanumDriveStopCommand(FTCLibMecanumDriveSubsystem driveSubsystem) {
        super(driveSubsystem::stop, driveSubsystem);
    }
}
