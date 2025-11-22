package util;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.button.GamepadButton;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.arcrobotics.ftclib.hardware.RevIMU;
import com.arcrobotics.ftclib.hardware.motors.CRServo;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

// Import Subsystems
import subsystems.IntakeSubsys;
import subsystems.MechDriveSubsys;
import subsystems.ShooterSubsys;

// Import Commands (Renamed)
import commands.DriveCommand;
import commands.IntakeCommand;
import commands.ShooterCommand;
import commands.FeederCommand;

@TeleOp(name = "Main TeleOp")
public class BasicTeleOp extends CommandOpMode {

    // -- HARDWARE & SUBSYSTEMS --
    private MechDriveSubsys driveSubsys;
    private IntakeSubsys intakeSubsys;
    private ShooterSubsys shooterSubsys;

    // -- GAMEPADS --
    private GamepadEx driver;
    private GamepadEx operator;

    @Override
    public void initialize() {
        // 1. INITIALIZE HARDWARE
        Motor fL = new Motor(hardwareMap, "fL");
        Motor fR = new Motor(hardwareMap, "fR");
        Motor bL = new Motor(hardwareMap, "bL");
        Motor bR = new Motor(hardwareMap, "bR");

        Motor intakeMotor = new Motor(hardwareMap, "intakeMotor");
        Servo sortLeft = hardwareMap.get(Servo.class, "sortLeft");
        Servo sortRight = hardwareMap.get(Servo.class, "sortRight");

        CRServo feederServo = new CRServo(hardwareMap, "feeder");
        Motor shooterMotor = new Motor(hardwareMap, "shooter");

        RevIMU gyro = new RevIMU(hardwareMap, "imu");
        gyro.init();

        // 2. INITIALIZE SUBSYSTEMS
        driveSubsys = new MechDriveSubsys(fL, fR, bL, bR, gyro, telemetry);
        intakeSubsys = new IntakeSubsys(intakeMotor, sortLeft, sortRight, telemetry);
        shooterSubsys = new ShooterSubsys(feederServo, shooterMotor);

        // 3. INITIALIZE GAMEPADS
        driver = new GamepadEx(gamepad1);
        operator = new GamepadEx(gamepad2);

        // 4. DEFAULT COMMANDS
        // Set the drive subsystem to always run this command when no other drive commands are active
        driveSubsys.setDefaultCommand(new DriveCommand(
                driveSubsys,
                () -> driver.getLeftX(),
                () -> driver.getLeftY(),
                () -> driver.getRightX()
        ));

        // 5. CONFIGURE BUTTON BINDINGS
        configureButtons();

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    private void configureButtons() {
        // --- DRIVER CONTROLS (Gamepad 1) ---

        // Button A: Reset Gyro
        new GamepadButton(driver, GamepadKeys.Button.A)
                .whenPressed(new InstantCommand(driveSubsys::resetGyro, driveSubsys));

        // Button B: Toggle Field/Robot Centric
        new GamepadButton(driver, GamepadKeys.Button.B)
                .whenPressed(new InstantCommand(driveSubsys::toggleFieldCentric, driveSubsys));

        // Right Bumper: Turbo Mode (Hold for Max Speed)
        new GamepadButton(driver, GamepadKeys.Button.RIGHT_BUMPER)
                .whenPressed(new InstantCommand(() -> driveSubsys.setMaxSpeed(1.0)))
                .whenReleased(new InstantCommand(() -> driveSubsys.setMaxSpeed(0.5)));


        // --- OPERATOR CONTROLS (Gamepad 2) ---

        // Left Bumper: Intake (While Held)
        new GamepadButton(operator, GamepadKeys.Button.LEFT_BUMPER)
                .whileHeld(new IntakeCommand(intakeSubsys));

        // D-Pad: Sorter Arms (Instant actions)
        new GamepadButton(operator, GamepadKeys.Button.DPAD_LEFT)
                .whenPressed(new InstantCommand(() -> intakeSubsys.sort(false), intakeSubsys));

        new GamepadButton(operator, GamepadKeys.Button.DPAD_RIGHT)
                .whenPressed(new InstantCommand(() -> intakeSubsys.sort(true), intakeSubsys));

        new GamepadButton(operator, GamepadKeys.Button.DPAD_UP)
                .whenPressed(new InstantCommand(intakeSubsys::sort, intakeSubsys));

        // Right Bumper: Shooter Flywheel (While Held)
        new GamepadButton(operator, GamepadKeys.Button.RIGHT_BUMPER)
                .whileHeld(new ShooterCommand(shooterSubsys));

        // Button A: Feed Ring (While Held)
        new GamepadButton(operator, GamepadKeys.Button.A)
                .whileHeld(new FeederCommand(shooterSubsys));
    }
}