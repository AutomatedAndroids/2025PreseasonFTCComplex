package commands;

import com.arcrobotics.ftclib.command.CommandBase;
import subsystems.ShooterSubsys;

public class ShooterCommand extends CommandBase {

    private final ShooterSubsys shooterSubsys;

    public ShooterCommand(ShooterSubsys shooterSubsys) {
        this.shooterSubsys = shooterSubsys;
        addRequirements(shooterSubsys);
    }

    @Override
    public void initialize() {
        shooterSubsys.spin_shoot();
    }

    @Override
    public void end(boolean interrupted) {
        shooterSubsys.stopShooting();
    }
}