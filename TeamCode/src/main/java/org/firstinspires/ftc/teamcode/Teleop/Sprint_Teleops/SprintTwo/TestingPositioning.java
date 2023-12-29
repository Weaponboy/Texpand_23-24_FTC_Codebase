package org.firstinspires.ftc.teamcode.Teleop.Sprint_Teleops.SprintTwo;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.hardware.Odometry;

@TeleOp
public class TestingPositioning extends OpMode {

    Odometry odometry = new Odometry(210, 337, 90);

    @Override
    public void init() {
        odometry.init(hardwareMap);
    }

    @Override
    public void loop() {
        odometry.update();
        telemetry.addData("x", odometry.X);
        telemetry.addData("y", odometry.Y);
        telemetry.addData("heading", odometry.heading);
        telemetry.update();
    }

}
