package org.firstinspires.ftc.teamcode.Auto.Comp_Autos;

import static org.firstinspires.ftc.teamcode.Constants_and_Setpoints.Constants.propPos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.Auto.Comp_Autos.Methods.CycleMethods;
import org.firstinspires.ftc.teamcode.Odometry.ObjectAvoidance.old.Vector2D;
import org.firstinspires.ftc.teamcode.Odometry.Pathing.Follower.mecanumFollower;
import org.firstinspires.ftc.teamcode.Odometry.Pathing.PathGeneration.pathBuilderSubClasses.blueRightBuilder;
import org.firstinspires.ftc.teamcode.Odometry.Pathing.PathGeneration.pathBuilderSubClasses.redLeftBuilder;
import org.firstinspires.ftc.teamcode.Odometry.Pathing.PathGeneration.pathBuilderSubClasses.redRightBuilder;
import org.firstinspires.ftc.teamcode.VisionTesting.VisionPortalProcessers.propDetectionByAmount;
import org.firstinspires.ftc.teamcode.hardware._.Collection;
import org.firstinspires.ftc.teamcode.hardware._.Delivery;
import org.firstinspires.ftc.teamcode.hardware._.Drivetrain;
import org.firstinspires.ftc.teamcode.hardware._.Odometry;
import org.firstinspires.ftc.vision.VisionPortal;

@Autonomous(name = "Red Left", group = "State")
public class Red_Left extends LinearOpMode implements CycleMethods {

    public WebcamName frontCam;

    public VisionPortal portal;

    org.firstinspires.ftc.teamcode.VisionTesting.VisionPortalProcessers.propDetectionByAmount propDetectionByAmount = new propDetectionByAmount(telemetry, org.firstinspires.ftc.teamcode.VisionTesting.VisionPortalProcessers.propDetectionByAmount.Side.left, org.firstinspires.ftc.teamcode.VisionTesting.VisionPortalProcessers.propDetectionByAmount.color.red);

    /**hardware objects*/
    Odometry odometry = new Odometry(90, 337, 90);

    Drivetrain drive = new Drivetrain();

    /**pathing objects*/
    redLeftBuilder firstPath = new redLeftBuilder();

    redLeftBuilder secondPath = new redLeftBuilder();

    redLeftBuilder collect = new redLeftBuilder();

    redLeftBuilder deliver = new redLeftBuilder();

    mecanumFollower follower = new mecanumFollower();

    boolean builtPath = false;

    enum Phase{
        purple,
        yellow,
        first2,
        second2,
        finished
    }

    enum Auto{
        preload,
        two,
        four
    }

    boolean pathing = false;

    double targetHeading;

    Phase phase = Phase.purple;

    Auto auto = Auto.preload;

    boolean lockIn = false;

    boolean onlyOnce = true;

    boolean delivering = false;

    boolean gotTwo = false;

    ElapsedTime gripperControl = new ElapsedTime();

    @Override
    public void runOpMode() throws InterruptedException {

        initialize();

        auto = Auto.four;

        boolean runOnce = false;

        while(!lockIn){

            telemetry.addData("Auto activated", auto);
            telemetry.addData("press y for 2+0", "");
            telemetry.addData("press a for 2+2", "");
            telemetry.addData("press b for 2+4", "");
            telemetry.addData("press x to lock in!!!!", "");
            telemetry.update();

            if (gamepad1.a){
                auto = Auto.two;
            } else if (gamepad1.b) {
                auto = Auto.four;
            } else if (gamepad1.y) {
                auto = Auto.preload;
            }else if (gamepad1.x) {
                lockIn = true;
            }

        }

        telemetry.update();

        waitForStart();

        collection.setIntakeHeight(Collection.intakeHeightState.letClawThrough);
        collection.updateIntakeHeight();

        portal.close();

        if (propPos == 1){

            firstPath.buildPath(redLeftBuilder.Position.left, redLeftBuilder.Section.preload, redLeftBuilder.pixelColor.purple);

            secondPath.buildPath(redLeftBuilder.Position.left, redLeftBuilder.Section.preload, redLeftBuilder.pixelColor.yellow);

            while (!(phase == Phase.finished)  && opModeIsActive()){

                switch (phase){
                    case purple:

                        odometry.update();

                        if (!builtPath){

                            builtPath = true;

                            follower.setPath(firstPath.followablePath, firstPath.pathingVelocity);

                            targetHeading = 60;

                            pathing = true;
                        }

                        if (pathing){

                            pathing = follower.followPathAuto(targetHeading, odometry, drive, 7);

                        }else if (Math.abs(74 - odometry.X) < 8 && Math.abs(286 - odometry.Y) < 8 && !pathing){

                            drive.RF.setPower(0);
                            drive.RB.setPower(0);
                            drive.LF.setPower(0);
                            drive.LB.setPower(0);

                            phase = Red_Left.Phase.yellow;

                            builtPath = false;
                        }

                        break;
                    case yellow:

                        odometry.update();

                        if (!builtPath){

                            builtPath = true;

                            follower.setPath(secondPath.followablePath, secondPath.pathingVelocity);

                            targetHeading = 90;

                            pathing = true;
                        }

                        if (pathing){

                            pathing = follower.followPathAuto(targetHeading, odometry, drive);

                            if (Math.abs(90 - odometry.X) < 5 && Math.abs(240 - odometry.Y) < 5){
                                targetHeading = 180;
                            }

                            if (Math.abs(230 - odometry.X) < 15 && Math.abs(200 - odometry.Y) < 15 && deliverySlides.getCurrentposition() < 50){

                                targetHeading = 180;

                                deliverySlides.DeliverySlides(250, 1);

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                delivery.setArmTargetState(Delivery.armState.deliverAuto);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                            }

                        }else if (Math.abs(300 - odometry.X) < 3 && Math.abs(250 - odometry.Y) < 3 && !pathing){

                            if (auto == Red_Left.Auto.preload){

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                dropYellowPixelWait();
                                phase = Red_Left.Phase.finished;

                            }else {

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                dropYellowPixel();

                                collect.buildPath(redLeftBuilder.Section.collect);

                                deliver.buildPath(redLeftBuilder.Section.deliver);

                                phase = Red_Left.Phase.first2;

                                builtPath = false;

                            }
                        }

                        break;
                    case first2:

                        delivery.updateArm(deliverySlides.getCurrentposition());

                        if (!builtPath){

                            builtPath = true;

                            pathing = true;

                            gotTwo = false;

                            follower.setPath(collect.followablePath, collect.pathingVelocity);

                            delivery.setGripperState(Delivery.GripperState.open);
                            delivery.updateGrippers();

                            collection.setIntakeHeight(Collection.intakeHeightState.thirdPixel);
                            collection.updateIntakeHeight();

                            targetHeading = 180;
                        }

                        odometry.update();

                        if (!delivering){

                            if (Math.abs(125 - odometry.X) < 30 && Math.abs(180 - odometry.Y) < 30){
                                collection.setState(Collection.intakePowerState.on);
                                collection.updateIntakeState();
                            }

                        }

                        if (!gotTwo && !sensors.RightClawSensor.isPressed()){
                            delivery.setRightGripperState(Delivery.rightGripperState.closed);
                            delivery.updateGrippers();
                        }

                        if (!gotTwo && !sensors.LeftClawSensor.isPressed()){
                            delivery.setLeftGripperState(Delivery.leftGripperState.closed);
                            delivery.updateGrippers();
                        }

                        if (!gotTwo && !sensors.RightClawSensor.isPressed() && !sensors.LeftClawSensor.isPressed() && odometry.X < 180){

                            gripperControl.reset();

                            delivery.setGripperState(Delivery.GripperState.closed);
                            delivery.updateGrippers();

                            drive.RF.setPower(0);
                            drive.RB.setPower(0);
                            drive.LF.setPower(0);
                            drive.LB.setPower(0);

                            follower.setPath(deliver.followablePath, deliver.pathingVelocity);

                            follower.resetClosestPoint(new Vector2D(odometry.X, odometry.Y));

                            delivering = true;

                            onlyOnce = false;

                            gotTwo = true;

                        }
                        if (gripperControl.milliseconds() > 250 && gripperControl.milliseconds() < 700 && gotTwo){

                            collection.setState(Collection.intakePowerState.reversed);
                            collection.updateIntakeState();

                        }

                        if (gripperControl.milliseconds() > 700 && gripperControl.milliseconds() < 1600 && gotTwo){

                            collection.setState(Collection.intakePowerState.on);
                            collection.updateIntakeState();

                            delivery.setGripperState(Delivery.GripperState.open);
                            delivery.updateGrippers();

                        }

                        if (gripperControl.milliseconds() > 1600 && gripperControl.milliseconds() < 1700 && gotTwo){

                            collection.setState(Collection.intakePowerState.off);
                            collection.updateIntakeState();

                            delivery.setGripperState(Delivery.GripperState.closed);
                            delivery.updateGrippers();

                        }

                        if (delivering) {

                            if (odometry.X > 180 && odometry.getVerticalVelocity() > -5 && !onlyOnce) {

                                onlyOnce = true;

                                deliverySlides.DeliverySlides(500, 1);

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                            }

                            if (odometry.X > 200 && odometry.getVerticalVelocity() > -5) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                delivery.setArmTargetState(Delivery.armState.deliverAuto);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                            }

                            if (Math.abs(121 - odometry.X) < 15 && Math.abs(180 - odometry.Y) < 15 && (sensors.RightClawSensor.isPressed() || sensors.LeftClawSensor.isPressed())) {

                                collection.setState(Collection.intakePowerState.off);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(70 - odometry.X) < 15 && Math.abs(180 - odometry.Y) < 15 && (sensors.RightClawSensor.isPressed() || sensors.LeftClawSensor.isPressed())) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                collection.setState(Collection.intakePowerState.reversed);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(303 - odometry.X) < 5 && Math.abs(255 - odometry.Y) < 16) {

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                delivery.setArmTargetState(Delivery.armState.delivery);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                                boolean reachedTarget = false;

                                while (!reachedTarget){
                                    reachedTarget = delivery.getArmState() == Delivery.armState.delivery;
                                    delivery.updateArm(deliverySlides.getCurrentposition());
                                }

                                delivery.setGripperState(Delivery.GripperState.open);
                                delivery.updateGrippers();

                                gotTwo = false;

                                sleep(100);

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                if (auto == Red_Left.Auto.two) {

                                    drive.RF.setPower(0);
                                    drive.RB.setPower(0);
                                    drive.LF.setPower(0);
                                    drive.LB.setPower(0);

                                    retractWait();
                                    phase = Red_Left.Phase.finished;

                                } else {

                                    drive.RF.setPower(0);
                                    drive.RB.setPower(0);
                                    drive.LF.setPower(0);
                                    drive.LB.setPower(0);

                                    builtPath = false;
                                    delivering = false;

                                    retract();
                                    phase = Red_Left.Phase.second2;

                                }
                            }
                        }

                        if (pathing){

                            pathing = follower.followPathAuto(targetHeading, odometry, drive, 5);

                        }else if (Math.abs(38 - odometry.X) < 5 && Math.abs(225 - odometry.Y) < 5){

                            follower.setPath(deliver.followablePath, deliver.pathingVelocity);

                            pathing = true;

                            delivering = true;

                            onlyOnce = false;

                        }

                        break;
                    case second2:

                        delivery.updateArm(deliverySlides.getCurrentposition());

                        if (!builtPath){

                            builtPath = true;
                            pathing = true;
                            gotTwo = false;

                            follower.setPath(collect.followablePath, collect.pathingVelocity);

                            delivery.setGripperState(Delivery.GripperState.open);
                            delivery.updateGrippers();

                            collection.setIntakeHeight(Collection.intakeHeightState.firstPixel);
                            collection.updateIntakeHeight();

                            targetHeading = 180;
                        }

                        odometry.update();

                        if (!delivering){

                            if (Math.abs(125 - odometry.X) < 30 && Math.abs(180 - odometry.Y) < 30){
                                collection.setState(Collection.intakePowerState.on);
                                collection.updateIntakeState();
                            }

                        }

                        if (!gotTwo && !sensors.RightClawSensor.isPressed()){
                            delivery.setRightGripperState(Delivery.rightGripperState.closed);
                            delivery.updateGrippers();
                        }

                        if (!gotTwo && !sensors.LeftClawSensor.isPressed()){
                            delivery.setLeftGripperState(Delivery.leftGripperState.closed);
                            delivery.updateGrippers();
                        }

                        if (!gotTwo && !sensors.RightClawSensor.isPressed() && !sensors.LeftClawSensor.isPressed() && odometry.X < 180){

                            gripperControl.reset();

                            delivery.setGripperState(Delivery.GripperState.closed);
                            delivery.updateGrippers();

                            drive.RF.setPower(0);
                            drive.RB.setPower(0);
                            drive.LF.setPower(0);
                            drive.LB.setPower(0);

                            follower.setPath(deliver.followablePath, deliver.pathingVelocity);

                            follower.resetClosestPoint(new Vector2D(odometry.X, odometry.Y));

                            delivering = true;

                            onlyOnce = false;

                            gotTwo = true;

                        }
                        if (gripperControl.milliseconds() > 250 && gripperControl.milliseconds() < 700 && gotTwo){

                            collection.setState(Collection.intakePowerState.reversed);
                            collection.updateIntakeState();

                        }

                        if (gripperControl.milliseconds() > 700 && gripperControl.milliseconds() < 1600 && gotTwo){

                            collection.setState(Collection.intakePowerState.on);
                            collection.updateIntakeState();

                            delivery.setGripperState(Delivery.GripperState.open);
                            delivery.updateGrippers();

                        }

                        if (gripperControl.milliseconds() > 1600 && gripperControl.milliseconds() < 1700 && gotTwo){

                            collection.setState(Collection.intakePowerState.off);
                            collection.updateIntakeState();

                            delivery.setGripperState(Delivery.GripperState.closed);
                            delivery.updateGrippers();

                        }

                        if (delivering) {

                            if (odometry.X > 180 && odometry.getVerticalVelocity() > -5 && !onlyOnce) {

                                onlyOnce = true;

                                deliverySlides.DeliverySlides(700, 1);

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                            }

                            if (Math.abs(121 - odometry.X) < 15 && Math.abs(180 - odometry.Y) < 15 && (sensors.RightClawSensor.isPressed() || sensors.LeftClawSensor.isPressed())) {

                                collection.setState(Collection.intakePowerState.off);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(70 - odometry.X) < 15 && Math.abs(180 - odometry.Y) < 15 && (sensors.RightClawSensor.isPressed() || sensors.LeftClawSensor.isPressed())) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                collection.setState(Collection.intakePowerState.reversed);
                                collection.updateIntakeState();

                            }

                            if (odometry.X > 200 && odometry.getVerticalVelocity() > -5) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                delivery.setArmTargetState(Delivery.armState.deliverAuto);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                            }

                            if (Math.abs(303 - odometry.X) < 5 && Math.abs(255 - odometry.Y) < 16) {

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                delivery.setArmTargetState(Delivery.armState.delivery);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                                boolean reachedTarget = false;

                                while (!reachedTarget){
                                    reachedTarget = delivery.getArmState() == Delivery.armState.delivery;
                                    delivery.updateArm(deliverySlides.getCurrentposition());
                                }

                                delivery.setGripperState(Delivery.GripperState.open);
                                delivery.updateGrippers();

                                gotTwo = false;

                                sleep(100);

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                retractWait();
                                phase = Red_Left.Phase.finished;

                            }
                        }

                        if (pathing){

                            pathing = follower.followPathAuto(targetHeading, odometry, drive, 5);

                        }else if (Math.abs(38 - odometry.X) < 5 && Math.abs(225 - odometry.Y) < 5) {

                            follower.setPath(deliver.followablePath, deliver.pathingVelocity);

                            pathing = true;

                            delivering = true;

                            onlyOnce = false;

                        }

                        break;
                    default:
                }

            }

        } else if (propPos == 2) {

            firstPath.buildPath(redLeftBuilder.Position.center, redLeftBuilder.Section.preload);

            boolean gotWhite = false;

            while (!(phase == Phase.finished) && opModeIsActive()){

                switch (phase){
                    case purple:

                        odometry.update();

                        if (!builtPath){

                            builtPath = true;

                            follower.setPath(firstPath.followablePath, firstPath.pathingVelocity);

                            targetHeading = 90;

                            pathing = true;
                        }

                        if (pathing){

                            pathing = follower.followPathAuto(targetHeading, odometry, drive, 2);

                            if (Math.abs(65 - odometry.X) < 15 && Math.abs(289 - odometry.Y) < 15){

                                targetHeading = 180;

                                delivery.setLeftGripperState(Delivery.leftGripperState.open);
                                delivery.updateGrippers();

                                collection.setIntakeHeight(Collection.intakeHeightState.thirdPixel);
                                collection.updateIntakeHeight();

                                collection.setState(Collection.intakePowerState.on);
                                collection.updateIntakeState();

                            }

                            if (!sensors.LeftClawSensor.isPressed() && !gotWhite){

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                gripperControl.reset();

                                gotWhite = true;

                            }

                            if (gripperControl.milliseconds() > 200 && gripperControl.milliseconds() < 1000  && gotWhite) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                collection.setState(Collection.intakePowerState.reversed);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(131 - odometry.X) < 15 && Math.abs(191 - odometry.Y) < 15){

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                collection.setState(Collection.intakePowerState.off);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(101 - odometry.X) < 15 && Math.abs(194 - odometry.Y) < 15){

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                collection.setState(Collection.intakePowerState.reversed);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(230 - odometry.X) < 15 && Math.abs(210 - odometry.Y) < 15 && deliverySlides.getCurrentposition() < 50){

                                targetHeading = 180;

                                deliverySlides.DeliverySlides(250, 1);

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                delivery.setArmTargetState(Delivery.armState.deliverAuto);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                            }

                        }else if (Math.abs(300 - odometry.X) < 3 && Math.abs(273 - odometry.Y) < 3 && !pathing) {

                            if (auto == Auto.preload){

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                dropYellowPixelWait();

                                phase = Phase.finished;

                            }else {

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                dropYellowPixel(true);

                                collect.buildPath(redLeftBuilder.Section.collect);

                                deliver.buildPath(redLeftBuilder.Section.deliver);

                                builtPath = false;

                                phase = Phase.first2;
                            }
                        }

                        break;
                    case first2:

                        delivery.updateArm(deliverySlides.getCurrentposition());

                        if (!builtPath){

                            builtPath = true;

                            pathing = true;

                            gotTwo = false;

                            follower.setPath(collect.followablePath, collect.pathingVelocity);

                            delivery.setGripperState(Delivery.GripperState.open);
                            delivery.updateGrippers();

                            collection.setIntakeHeight(Collection.intakeHeightState.thirdPixel);
                            collection.updateIntakeHeight();

                            targetHeading = 180;
                        }

                        odometry.update();

                        if (!delivering){

                            if (Math.abs(125 - odometry.X) < 30 && Math.abs(180 - odometry.Y) < 30){
                                collection.setState(Collection.intakePowerState.on);
                                collection.updateIntakeState();
                            }

                        }

                        if (!gotTwo && !sensors.RightClawSensor.isPressed()){
                            delivery.setRightGripperState(Delivery.rightGripperState.closed);
                            delivery.updateGrippers();
                        }

                        if (!gotTwo && !sensors.LeftClawSensor.isPressed()){
                            delivery.setLeftGripperState(Delivery.leftGripperState.closed);
                            delivery.updateGrippers();
                        }

                        if (!gotTwo && !sensors.RightClawSensor.isPressed() && !sensors.LeftClawSensor.isPressed() && odometry.X < 180){

                            gripperControl.reset();

                            delivery.setGripperState(Delivery.GripperState.closed);
                            delivery.updateGrippers();

                            drive.RF.setPower(0);
                            drive.RB.setPower(0);
                            drive.LF.setPower(0);
                            drive.LB.setPower(0);

                            follower.setPath(deliver.followablePath, deliver.pathingVelocity);

                            follower.resetClosestPoint(new Vector2D(odometry.X, odometry.Y));

                            delivering = true;

                            onlyOnce = false;

                            gotTwo = true;

                        }
                        if (gripperControl.milliseconds() > 250 && gripperControl.milliseconds() < 700 && gotTwo){

                            collection.setState(Collection.intakePowerState.reversed);
                            collection.updateIntakeState();

                        }

                        if (gripperControl.milliseconds() > 700 && gripperControl.milliseconds() < 1600 && gotTwo){

                            collection.setState(Collection.intakePowerState.on);
                            collection.updateIntakeState();

                            delivery.setGripperState(Delivery.GripperState.open);
                            delivery.updateGrippers();

                        }

                        if (gripperControl.milliseconds() > 1600 && gripperControl.milliseconds() < 1700 && gotTwo){

                            collection.setState(Collection.intakePowerState.off);
                            collection.updateIntakeState();

                            delivery.setGripperState(Delivery.GripperState.closed);
                            delivery.updateGrippers();

                        }

                        if (delivering) {

                            if (odometry.X > 180 && odometry.getVerticalVelocity() > -5 && !onlyOnce) {

                                onlyOnce = true;

                                deliverySlides.DeliverySlides(500, 1);

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                            }

                            if (odometry.X > 200 && odometry.getVerticalVelocity() > -5) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                delivery.setArmTargetState(Delivery.armState.deliverAuto);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                            }

                            if (Math.abs(121 - odometry.X) < 15 && Math.abs(180 - odometry.Y) < 15 && (sensors.RightClawSensor.isPressed() || sensors.LeftClawSensor.isPressed())) {

                                collection.setState(Collection.intakePowerState.off);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(70 - odometry.X) < 15 && Math.abs(180 - odometry.Y) < 15 && (sensors.RightClawSensor.isPressed() || sensors.LeftClawSensor.isPressed())) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                collection.setState(Collection.intakePowerState.reversed);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(303 - odometry.X) < 5 && Math.abs(250 - odometry.Y) < 16) {

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                delivery.setArmTargetState(Delivery.armState.delivery);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                                boolean reachedTarget = false;

                                while (!reachedTarget){
                                    reachedTarget = delivery.getArmState() == Delivery.armState.delivery;
                                    delivery.updateArm(deliverySlides.getCurrentposition());
                                }

                                delivery.setGripperState(Delivery.GripperState.open);
                                delivery.updateGrippers();

                                gotTwo = false;

                                sleep(100);

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                if (auto == Red_Left.Auto.two) {

                                    drive.RF.setPower(0);
                                    drive.RB.setPower(0);
                                    drive.LF.setPower(0);
                                    drive.LB.setPower(0);

                                    retractWait();
                                    phase = Red_Left.Phase.finished;

                                } else {

                                    drive.RF.setPower(0);
                                    drive.RB.setPower(0);
                                    drive.LF.setPower(0);
                                    drive.LB.setPower(0);

                                    builtPath = false;
                                    delivering = false;

                                    retract();
                                    phase = Red_Left.Phase.second2;

                                }
                            }
                        }

                        if (pathing){

                            pathing = follower.followPathAuto(targetHeading, odometry, drive, 5);

                        }else if (Math.abs(41 - odometry.X) < 5 && Math.abs(225 - odometry.Y) < 5){

                            follower.setPath(deliver.followablePath, deliver.pathingVelocity);

                            pathing = true;

                            delivering = true;

                            onlyOnce = false;

                        }

                        break;
                    case second2:

                        delivery.updateArm(deliverySlides.getCurrentposition());

                        if (!builtPath){

                            builtPath = true;
                            pathing = true;
                            gotTwo = false;

                            follower.setPath(collect.followablePath, collect.pathingVelocity);

                            delivery.setGripperState(Delivery.GripperState.open);
                            delivery.updateGrippers();

                            collection.setIntakeHeight(Collection.intakeHeightState.firstPixel);
                            collection.updateIntakeHeight();

                            targetHeading = 180;
                        }

                        odometry.update();

                        if (!delivering){

                            if (Math.abs(125 - odometry.X) < 30 && Math.abs(180 - odometry.Y) < 30){
                                collection.setState(Collection.intakePowerState.on);
                                collection.updateIntakeState();
                            }

                        }

                        if (!gotTwo && !sensors.RightClawSensor.isPressed()){
                            delivery.setRightGripperState(Delivery.rightGripperState.closed);
                            delivery.updateGrippers();
                        }

                        if (!gotTwo && !sensors.LeftClawSensor.isPressed()){
                            delivery.setLeftGripperState(Delivery.leftGripperState.closed);
                            delivery.updateGrippers();
                        }

                        if (!gotTwo && !sensors.RightClawSensor.isPressed() && !sensors.LeftClawSensor.isPressed() && odometry.X < 180){

                            gripperControl.reset();

                            delivery.setGripperState(Delivery.GripperState.closed);
                            delivery.updateGrippers();

                            drive.RF.setPower(0);
                            drive.RB.setPower(0);
                            drive.LF.setPower(0);
                            drive.LB.setPower(0);

                            follower.setPath(deliver.followablePath, deliver.pathingVelocity);

                            follower.resetClosestPoint(new Vector2D(odometry.X, odometry.Y));

                            delivering = true;

                            onlyOnce = false;

                            gotTwo = true;

                        }
                        if (gripperControl.milliseconds() > 250 && gripperControl.milliseconds() < 700 && gotTwo){

                            collection.setState(Collection.intakePowerState.reversed);
                            collection.updateIntakeState();

                        }

                        if (gripperControl.milliseconds() > 700 && gripperControl.milliseconds() < 1600 && gotTwo){

                            collection.setState(Collection.intakePowerState.on);
                            collection.updateIntakeState();

                            delivery.setGripperState(Delivery.GripperState.open);
                            delivery.updateGrippers();

                        }

                        if (gripperControl.milliseconds() > 1600 && gripperControl.milliseconds() < 1700 && gotTwo){

                            collection.setState(Collection.intakePowerState.off);
                            collection.updateIntakeState();

                            delivery.setGripperState(Delivery.GripperState.closed);
                            delivery.updateGrippers();

                        }

                        if (delivering) {

                            if (odometry.X > 180 && odometry.getVerticalVelocity() > -5 && !onlyOnce) {

                                onlyOnce = true;

                                deliverySlides.DeliverySlides(700, 1);

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                            }

                            if (Math.abs(121 - odometry.X) < 15 && Math.abs(180 - odometry.Y) < 15 && (sensors.RightClawSensor.isPressed() || sensors.LeftClawSensor.isPressed())) {

                                collection.setState(Collection.intakePowerState.off);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(70 - odometry.X) < 15 && Math.abs(180 - odometry.Y) < 15 && (sensors.RightClawSensor.isPressed() || sensors.LeftClawSensor.isPressed())) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                collection.setState(Collection.intakePowerState.reversed);
                                collection.updateIntakeState();

                            }

                            if (odometry.X > 200 && odometry.getVerticalVelocity() > -5) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                delivery.setArmTargetState(Delivery.armState.deliverAuto);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                            }

                            if (Math.abs(303 - odometry.X) < 5 && Math.abs(250 - odometry.Y) < 16) {

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                delivery.setArmTargetState(Delivery.armState.delivery);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                                boolean reachedTarget = false;

                                while (!reachedTarget){
                                    reachedTarget = delivery.getArmState() == Delivery.armState.delivery;
                                    delivery.updateArm(deliverySlides.getCurrentposition());
                                }

                                delivery.setGripperState(Delivery.GripperState.open);
                                delivery.updateGrippers();

                                gotTwo = false;

                                sleep(100);

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                retractWait();
                                phase = Red_Left.Phase.finished;

                            }
                        }

                        if (pathing){

                            pathing = follower.followPathAuto(targetHeading, odometry, drive, 5);

                        }else if (Math.abs(41 - odometry.X) < 5 && Math.abs(225 - odometry.Y) < 5) {

                            follower.setPath(deliver.followablePath, deliver.pathingVelocity);

                            pathing = true;

                            delivering = true;

                            onlyOnce = false;

                        }

                        break;
                    default:
                }

            }

        } else if (propPos == 3) {

            firstPath.buildPath(redLeftBuilder.Position.right, redLeftBuilder.Section.preload);

            boolean gotWhite = false;

            while (!(phase == Phase.finished) && opModeIsActive()){

                switch (phase){
                    case purple:

                        odometry.update();

                        if (!builtPath){

                            builtPath = true;

                            follower.setPath(firstPath.followablePath, firstPath.pathingVelocity);

                            targetHeading = 90;

                            pathing = true;

                            runOnce = false;
                        }

                        if (pathing){

                            pathing = follower.followPathAuto(targetHeading, odometry, drive, 3);

                            if (Math.abs(90 - odometry.X) < 15 && Math.abs(305 - odometry.Y) < 15){

                                targetHeading = 180;

                                delivery.setLeftGripperState(Delivery.leftGripperState.open);
                                delivery.updateGrippers();

                                collection.setIntakeHeight(Collection.intakeHeightState.thirdPixel);
                                collection.updateIntakeHeight();

                                collection.setState(Collection.intakePowerState.on);
                                collection.updateIntakeState();

                            }

                            if (!sensors.LeftClawSensor.isPressed() && !gotWhite){

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                gripperControl.reset();

                                gotWhite = true;

                            }

                            if (gripperControl.milliseconds() > 200 && gripperControl.milliseconds() < 1000 && gotWhite) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                collection.setState(Collection.intakePowerState.reversed);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(131 - odometry.X) < 15 && Math.abs(191 - odometry.Y) < 15){

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                collection.setState(Collection.intakePowerState.off);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(230 - odometry.X) < 15 && Math.abs(200 - odometry.Y) < 15 && deliverySlides.getCurrentposition() < 50){

                                targetHeading = 180;

                                deliverySlides.DeliverySlides(250, 1);

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                delivery.setArmTargetState(Delivery.armState.deliverAuto);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                            }

                        }else if  (Math.abs(300 - odometry.X) < 5 && Math.abs(285 - odometry.Y) < 5 && !pathing){

                            if (auto == Red_Left.Auto.preload){

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                dropYellowPixelWait();
                                phase = Red_Left.Phase.finished;

                            }else {

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                dropYellowPixel(true);

                                collect.buildPath(redLeftBuilder.Section.collect);

                                deliver.buildPath(redLeftBuilder.Section.deliver);

                                phase = Red_Left.Phase.first2;

                                builtPath = false;
                            }
                        }

                        break;
                    case first2:

                        delivery.updateArm(deliverySlides.getCurrentposition());

                        if (!builtPath){

                            builtPath = true;

                            pathing = true;

                            gotTwo = false;

                            follower.setPath(collect.followablePath, collect.pathingVelocity);

                            delivery.setGripperState(Delivery.GripperState.open);
                            delivery.updateGrippers();

                            collection.setIntakeHeight(Collection.intakeHeightState.thirdPixel);
                            collection.updateIntakeHeight();

                            targetHeading = 180;
                        }

                        odometry.update();

                        if (!delivering){

                            if (Math.abs(125 - odometry.X) < 30 && Math.abs(208 - odometry.Y) < 30){
                                collection.setState(Collection.intakePowerState.on);
                                collection.updateIntakeState();
                            }

                        }

                        if (!gotTwo && !sensors.RightClawSensor.isPressed()){
                            delivery.setRightGripperState(Delivery.rightGripperState.closed);
                            delivery.updateGrippers();
                        }

                        if (!gotTwo && !sensors.LeftClawSensor.isPressed()){
                            delivery.setLeftGripperState(Delivery.leftGripperState.closed);
                            delivery.updateGrippers();
                        }

                        if (!gotTwo && !sensors.RightClawSensor.isPressed() && !sensors.LeftClawSensor.isPressed() && odometry.X < 180){

                            gripperControl.reset();

                            delivery.setGripperState(Delivery.GripperState.closed);
                            delivery.updateGrippers();

                            drive.RF.setPower(0);
                            drive.RB.setPower(0);
                            drive.LF.setPower(0);
                            drive.LB.setPower(0);

                            follower.setPath(deliver.followablePath, deliver.pathingVelocity);

                            follower.resetClosestPoint(new Vector2D(odometry.X, odometry.Y));

                            delivering = true;

                            onlyOnce = false;

                            gotTwo = true;

                        }

                        if (gripperControl.milliseconds() > 250 && gripperControl.milliseconds() < 1700 && gotTwo){

                            collection.setState(Collection.intakePowerState.reversed);
                            collection.updateIntakeState();

                        }

                        if (gripperControl.milliseconds() > 1700 && gripperControl.milliseconds() < 2600 && gotTwo){

                            collection.setState(Collection.intakePowerState.on);
                            collection.updateIntakeState();

                            delivery.setGripperState(Delivery.GripperState.open);
                            delivery.updateGrippers();

                        }

                        if (gripperControl.milliseconds() > 2600 && gripperControl.milliseconds() < 2700 && gotTwo){

                            collection.setState(Collection.intakePowerState.off);
                            collection.updateIntakeState();

                            delivery.setGripperState(Delivery.GripperState.closed);
                            delivery.updateGrippers();

                        }

                        if (delivering) {

                            if (odometry.X > 180 && odometry.getVerticalVelocity() > -5 && !onlyOnce) {

                                onlyOnce = true;

                                deliverySlides.DeliverySlides(500, 1);

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                            }

                            if (odometry.X > 200 && odometry.getVerticalVelocity() > -5) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                delivery.setArmTargetState(Delivery.armState.deliverAuto);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                            }

                            if (Math.abs(121 - odometry.X) < 15 && Math.abs(208 - odometry.Y) < 15 && (sensors.RightClawSensor.isPressed() || sensors.LeftClawSensor.isPressed())) {

                                collection.setState(Collection.intakePowerState.off);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(70 - odometry.X) < 15 && Math.abs(214 - odometry.Y) < 15 && (sensors.RightClawSensor.isPressed() || sensors.LeftClawSensor.isPressed())) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                collection.setState(Collection.intakePowerState.reversed);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(300 - odometry.X) < 5 && Math.abs(270 - odometry.Y) < 16) {

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                delivery.setArmTargetState(Delivery.armState.delivery);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                                boolean reachedTarget = false;

                                while (!reachedTarget){
                                    reachedTarget = delivery.getArmState() == Delivery.armState.delivery;
                                    delivery.updateArm(deliverySlides.getCurrentposition());
                                }

                                delivery.setGripperState(Delivery.GripperState.open);
                                delivery.updateGrippers();

                                gotTwo = false;

                                sleep(100);

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                if (auto == Red_Left.Auto.two) {

                                    drive.RF.setPower(0);
                                    drive.RB.setPower(0);
                                    drive.LF.setPower(0);
                                    drive.LB.setPower(0);

                                    retractWait();
                                    phase = Red_Left.Phase.finished;

                                } else {

                                    drive.RF.setPower(0);
                                    drive.RB.setPower(0);
                                    drive.LF.setPower(0);
                                    drive.LB.setPower(0);

                                    builtPath = false;
                                    delivering = false;

                                    retract();
                                    phase = Red_Left.Phase.second2;

                                }
                            }
                        }

                        if (pathing){

                            pathing = follower.followPathAuto(targetHeading, odometry, drive, 5);

                        }else if (Math.abs(41 - odometry.X) < 5 && Math.abs(264 - odometry.Y) < 5){

                            follower.setPath(deliver.followablePath, deliver.pathingVelocity);

                            pathing = true;

                            delivering = true;

                            onlyOnce = false;

                        }

                        break;
                    case second2:

                        delivery.updateArm(deliverySlides.getCurrentposition());

                        if (!builtPath){

                            builtPath = true;
                            pathing = true;
                            gotTwo = false;

                            follower.setPath(collect.followablePath, collect.pathingVelocity);

                            delivery.setGripperState(Delivery.GripperState.open);
                            delivery.updateGrippers();

                            collection.setIntakeHeight(Collection.intakeHeightState.firstPixel);
                            collection.updateIntakeHeight();

                            targetHeading = 180;
                        }

                        odometry.update();

                        if (!delivering){

                            if (Math.abs(125 - odometry.X) < 30 && Math.abs(208 - odometry.Y) < 30){
                                collection.setState(Collection.intakePowerState.on);
                                collection.updateIntakeState();
                            }

                        }

                        if (!gotTwo && !sensors.RightClawSensor.isPressed()){
                            delivery.setRightGripperState(Delivery.rightGripperState.closed);
                            delivery.updateGrippers();
                        }

                        if (!gotTwo && !sensors.LeftClawSensor.isPressed()){
                            delivery.setLeftGripperState(Delivery.leftGripperState.closed);
                            delivery.updateGrippers();
                        }

                        if (!gotTwo && !sensors.RightClawSensor.isPressed() && !sensors.LeftClawSensor.isPressed() && odometry.X < 180){

                            gripperControl.reset();

                            delivery.setGripperState(Delivery.GripperState.closed);
                            delivery.updateGrippers();

                            drive.RF.setPower(0);
                            drive.RB.setPower(0);
                            drive.LF.setPower(0);
                            drive.LB.setPower(0);

                            follower.setPath(deliver.followablePath, deliver.pathingVelocity);

                            follower.resetClosestPoint(new Vector2D(odometry.X, odometry.Y));

                            delivering = true;

                            onlyOnce = false;

                            gotTwo = true;

                        }

                        if (gripperControl.milliseconds() > 250 && gripperControl.milliseconds() < 1700 && gotTwo){

                            collection.setState(Collection.intakePowerState.reversed);
                            collection.updateIntakeState();
                        }

                        if (gripperControl.milliseconds() > 1700 && gripperControl.milliseconds() < 2600 && gotTwo){

                            collection.setState(Collection.intakePowerState.on);
                            collection.updateIntakeState();

                            delivery.setGripperState(Delivery.GripperState.open);
                            delivery.updateGrippers();

                        }

                        if (gripperControl.milliseconds() > 2600 && gripperControl.milliseconds() < 2700 && gotTwo){

                            collection.setState(Collection.intakePowerState.off);
                            collection.updateIntakeState();

                            delivery.setGripperState(Delivery.GripperState.closed);
                            delivery.updateGrippers();

                        }

                        if (delivering) {

                            if (odometry.X > 180 && odometry.getVerticalVelocity() > -5 && !onlyOnce) {

                                onlyOnce = true;

                                deliverySlides.DeliverySlides(700, 1);

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                            }

                            if (Math.abs(121 - odometry.X) < 15 && Math.abs(208 - odometry.Y) < 15 && (sensors.RightClawSensor.isPressed() || sensors.LeftClawSensor.isPressed())) {

                                collection.setState(Collection.intakePowerState.off);
                                collection.updateIntakeState();

                            }

                            if (Math.abs(70 - odometry.X) < 15 && Math.abs(214 - odometry.Y) < 15 && (sensors.RightClawSensor.isPressed() || sensors.LeftClawSensor.isPressed())) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                collection.setState(Collection.intakePowerState.reversed);
                                collection.updateIntakeState();

                            }

                            if (odometry.X > 200 && odometry.getVerticalVelocity() > -5) {

                                delivery.setGripperState(Delivery.GripperState.closed);
                                delivery.updateGrippers();

                                delivery.setArmTargetState(Delivery.armState.deliverAuto);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                            }

                            if (Math.abs(300 - odometry.X) < 5 && Math.abs(270 - odometry.Y) < 10) {

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                delivery.setArmTargetState(Delivery.armState.delivery);
                                delivery.updateArm(deliverySlides.getCurrentposition());

                                boolean reachedTarget = false;

                                while (!reachedTarget){
                                    reachedTarget = delivery.getArmState() == Delivery.armState.delivery;
                                    delivery.updateArm(deliverySlides.getCurrentposition());
                                }

                                delivery.setGripperState(Delivery.GripperState.open);
                                delivery.updateGrippers();

                                gotTwo = false;

                                sleep(100);

                                drive.RF.setPower(0);
                                drive.RB.setPower(0);
                                drive.LF.setPower(0);
                                drive.LB.setPower(0);

                                retractWait();
                                phase = Red_Left.Phase.finished;

                            }
                        }

                        if (pathing){

                            pathing = follower.followPathAuto(targetHeading, odometry, drive, 5);

                        }else if (Math.abs(39 - odometry.X) < 5 && Math.abs(264 - odometry.Y) < 5) {

                            follower.setPath(deliver.followablePath, deliver.pathingVelocity);

                            pathing = true;

                            delivering = true;

                            onlyOnce = false;

                        }

                        break;
                    default:
                }
            }
        }

    }

    private void initialize(){

        //init hardware
        odometry.init(hardwareMap);

        drive.init(hardwareMap);

        init(hardwareMap);

        sensors.init(hardwareMap);

        odometry.update();

        frontCam = hardwareMap.get(WebcamName.class, "frontcam");

        portal = VisionPortal.easyCreateWithDefaults(frontCam, propDetectionByAmount);

    }

}
