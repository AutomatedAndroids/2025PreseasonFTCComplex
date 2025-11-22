package util;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.button.GamepadButton;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.arcrobotics.ftclib.hardware.motors.CRServo;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import subsystems.IntakeSubsys;
import subsystems.MechDriveSubsys;
import subsystems.ShooterSubsys;
import commands.DriveCommand;
import commands.FeederCommand;

@TeleOp(name = "Main TeleOp")
public class BasicTeleOp extends CommandOpMode {

    private MechDriveSubsys driveSubsys;
    private IntakeSubsys intakeSubsys;
    private ShooterSubsys shooterSubsys;

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

        UniversalGyro gyro = new UniversalGyro(hardwareMap, "imu");
        gyro.init();

        // 2. INITIALIZE SUBSYSTEMS
        driveSubsys = new MechDriveSubsys(fL, fR, bL, bR, gyro, telemetry);
        intakeSubsys = new IntakeSubsys(intakeMotor, sortLeft, sortRight, telemetry);
        shooterSubsys = new ShooterSubsys(feederServo, shooterMotor, telemetry);

        // 3. INITIALIZE GAMEPADS
        driver = new GamepadEx(gamepad1);
        operator = new GamepadEx(gamepad2);

        // 4. DEFAULT COMMANDS
        // Standard Controls: Left Stick moves/strafes, Right Stick turns
        driveSubsys.setDefaultCommand(new DriveCommand(
                driveSubsys,
                () -> driver.getLeftX(),     // Strafe
                () -> -driver.getLeftY(),    // Forward (Inverted)
                () -> -driver.getRightX()     // Turn
        ));

        // 5. CONFIGURE BUTTON BINDINGS
        configureButtons();

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    private void configureButtons() {
        // --- DRIVER ---
        new GamepadButton(driver, GamepadKeys.Button.A)
                .whenPressed(new InstantCommand(driveSubsys::resetGyro, driveSubsys));

        new GamepadButton(driver, GamepadKeys.Button.B)
                .whenPressed(new InstantCommand(driveSubsys::toggleFieldCentric, driveSubsys));

        new GamepadButton(driver, GamepadKeys.Button.RIGHT_BUMPER)
                .whenPressed(new InstantCommand(() -> driveSubsys.setMaxSpeed(1.0)))
                .whenReleased(new InstantCommand(() -> driveSubsys.setMaxSpeed(0.5)));

        // --- OPERATOR ---
        new GamepadButton(operator, GamepadKeys.Button.LEFT_BUMPER)
                .whenPressed(new InstantCommand(intakeSubsys::turnOnIntake, intakeSubsys));

        new GamepadButton(operator, GamepadKeys.Button.X)
                .whenPressed(new InstantCommand(intakeSubsys::turnOffIntake, intakeSubsys));

        // Shooter uses Velocity Control now
        new GamepadButton(operator, GamepadKeys.Button.RIGHT_BUMPER)
                .whenPressed(new InstantCommand(shooterSubsys::spin_shoot, shooterSubsys));

        new GamepadButton(operator, GamepadKeys.Button.Y)
                .whenPressed(new InstantCommand(shooterSubsys::stopShooting, shooterSubsys));

        new GamepadButton(operator, GamepadKeys.Button.A)
                .whileHeld(new FeederCommand(shooterSubsys));

        new GamepadButton(operator, GamepadKeys.Button.DPAD_LEFT)
                .whenPressed(new InstantCommand(() -> intakeSubsys.sort(false), intakeSubsys));

        new GamepadButton(operator, GamepadKeys.Button.DPAD_RIGHT)
                .whenPressed(new InstantCommand(() -> intakeSubsys.sort(true), intakeSubsys));

        new GamepadButton(operator, GamepadKeys.Button.DPAD_UP)
                .whenPressed(new InstantCommand(intakeSubsys::sort, intakeSubsys));
    }
}