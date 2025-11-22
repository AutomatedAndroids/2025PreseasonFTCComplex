package commands;

import com.arcrobotics.ftclib.command.CommandBase;
import subsystems.IntakeSubsys;

public class IntakeCommand extends CommandBase {

    private final IntakeSubsys intakeSubsys;

    public IntakeCommand(IntakeSubsys intakeSubsys) {
        this.intakeSubsys = intakeSubsys;
        addRequirements(intakeSubsys);
    }

    @Override
    public void initialize() {
        intakeSubsys.turnOnIntake();
    }

    @Override
    public void end(boolean interrupted) {
        intakeSubsys.turnOffIntake();
    }
}