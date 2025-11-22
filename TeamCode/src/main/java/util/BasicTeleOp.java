package teleop;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.RunCommand;
import com.arcrobotics.ftclib.command.button.GamepadButton;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.arcrobotics.ftclib.hardware.RevIMU;
import com.arcrobotics.ftclib.hardware.motors.CRServo;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

// Import your subsystems
import subsystems.IntakeSubsys;
import subsystems.MechDriveSubsys;
import subsystems.ShooterSubsys;

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
        // Note: We look for names in the HardwareMap.
        // Ensure your Driver Station config matches these strings.

        // Drive Motors
        Motor fL = new Motor(hardwareMap, "fL");
        Motor fR = new Motor(hardwareMap, "fR");
        Motor bL = new Motor(hardwareMap, "bL");
        Motor bR = new Motor(hardwareMap, "bR");

        // Intake Hardware
        Motor intakeMotor = new Motor(hardwareMap, "intakeMotor");
        Servo sortLeft = hardwareMap.get(Servo.class, "sortLeft");
        Servo sortRight = hardwareMap.get(Servo.class, "sortRight");

        // Shooter Hardware
        // Note: Subsystem asks for FTCLib CRServo, not Qualcomm CRServo
        CRServo feederServo = new CRServo(hardwareMap, "feeder");
        Motor shooterMotor = new Motor(hardwareMap, "shooter");

        // Gyro (Assuming Rev Hub IMU)
        RevIMU gyro = new RevIMU(hardwareMap, "imu");
        gyro.init();

        // 2. INITIALIZE SUBSYSTEMS
        driveSubsys = new MechDriveSubsys(fL, fR, bL, bR, gyro, telemetry);
        intakeSubsys = new IntakeSubsys(intakeMotor, sortLeft, sortRight, telemetry);
        shooterSubsys = new ShooterSubsys(feederServo, shooterMotor);

        // 3. INITIALIZE GAMEPADS
        driver = new GamepadEx(gamepad1);
        operator = new GamepadEx(gamepad2);

        // 4. ASSIGN DEFAULT COMMANDS
        // This runs the drive loop constantly when no other drive commands are active.
        driveSubsys.setDefaultCommand(new RunCommand(() -> {
            driveSubsys.drive(
                    driver.getLeftX(),   // Strafe
                    driver.getLeftY(),   // Forward
                    driver.getRightX()   // Turn
            );
        }, driveSubsys));

        // 5. CONFIGURE BUTTON BINDINGS
        configureButtons();

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    private void configureButtons() {
        // --- DRIVER CONTROLS (Gamepad 1) ---

        // Button A: Reset Gyro (Zero Heading)
        new GamepadButton(driver, GamepadKeys.Button.A)
                .whenPressed(new InstantCommand(driveSubsys::resetGyro, driveSubsys));

        // Button B: Toggle Field/Robot Centric
        new GamepadButton(driver, GamepadKeys.Button.B)
                .whenPressed(new InstantCommand(driveSubsys::toggleFieldCentric, driveSubsys));

        // Right Bumper: Slow Mode / Turbo Mode Logic
        // Default speed is 1.0 in subsystem, but let's act like holding bumper is "Turbo"
        // or releasing it is "Precision".
        // Current logic: Hold RB for Max Speed, Release for controlled speed (0.5)
        new GamepadButton(driver, GamepadKeys.Button.RIGHT_BUMPER)
                .whenPressed(new InstantCommand(() -> driveSubsys.setMaxSpeed(1.0)))
                .whenReleased(new InstantCommand(() -> driveSubsys.setMaxSpeed(0.5)));


        // --- OPERATOR CONTROLS (Gamepad 2) ---

        // Left Bumper: Intake (While Held)
        new GamepadButton(operator, GamepadKeys.Button.LEFT_BUMPER)
                .whenPressed(new InstantCommand(intakeSubsys::turnOnIntake, intakeSubsys))
                .whenReleased(new InstantCommand(intakeSubsys::turnOffIntake, intakeSubsys));

        // D-Pad: Sorter Arms
        new GamepadButton(operator, GamepadKeys.Button.DPAD_LEFT)
                .whenPressed(new InstantCommand(() -> intakeSubsys.sort(false), intakeSubsys)); // Left

        new GamepadButton(operator, GamepadKeys.Button.DPAD_RIGHT)
                .whenPressed(new InstantCommand(() -> intakeSubsys.sort(true), intakeSubsys));  // Right

        new GamepadButton(operator, GamepadKeys.Button.DPAD_UP)
                .whenPressed(new InstantCommand(intakeSubsys::sort, intakeSubsys));             // Reset/Both

        // Right Bumper: Spin Shooter Flywheel (While Held)
        new GamepadButton(operator, GamepadKeys.Button.RIGHT_BUMPER)
                .whenPressed(new InstantCommand(shooterSubsys::spin_shoot, shooterSubsys))
                .whenReleased(new InstantCommand(shooterSubsys::stopShooting, shooterSubsys));

        // Button A: Feed Ring (While Held)
        // Note: Based on your subsystem, stopShooting() stops BOTH feeder and shooter.
        // If you release A while holding RB, the shooter might stop momentarily depending on timing.
        new GamepadButton(operator, GamepadKeys.Button.A)
                .whenPressed(new InstantCommand(shooterSubsys::feed, shooterSubsys))
                .whenReleased(new InstantCommand(shooterSubsys::stopShooting, shooterSubsys));
    }
}