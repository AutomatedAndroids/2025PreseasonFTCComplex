package commands;

import com.arcrobotics.ftclib.command.CommandBase;
import subsystems.ShooterSubsys;

public class FeederCommand extends CommandBase {

    private final ShooterSubsys shooterSubsys;

    public FeederCommand(ShooterSubsys shooterSubsys) {
        this.shooterSubsys = shooterSubsys;

        // IMPORTANT: We intentionally DO NOT require the subsystem here.
        // This allows the FeederCommand to run in parallel with the ShooterCommand.
        // addRequirements(shooterSubsys);
    }

    @Override
    public void initialize() {
        shooterSubsys.feed();
    }

    @Override
    public void end(boolean interrupted) {
        shooterSubsys.stopFeeding();
        shooterSubsys.stopFlywheels();
    }
}