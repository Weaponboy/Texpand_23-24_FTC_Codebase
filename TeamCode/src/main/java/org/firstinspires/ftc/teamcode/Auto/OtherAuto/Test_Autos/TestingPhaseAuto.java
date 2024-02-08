package org.firstinspires.ftc.teamcode.Auto.OtherAuto.Test_Autos;

import static org.firstinspires.ftc.teamcode.Constants_and_Setpoints.Constants.propPos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.Auto.Comp_Autos.Preload.Auto_Methods;
import org.firstinspires.ftc.teamcode.Odometry.ObjectAvoidance.old.Vector2D;
import org.firstinspires.ftc.teamcode.Odometry.Pathing.Follower.mecanumFollower;
import org.firstinspires.ftc.teamcode.Odometry.Pathing.PathGeneration.pathBuilderSubClasses.blueLeftBuilder;
import org.firstinspires.ftc.teamcode.Odometry.Pathing.PathGeneration.pathBuilderSubClasses.blueRightBuilder;
import org.firstinspires.ftc.teamcode.Odometry.Pathing.PathGeneration.pathBuilderSubClasses.redRightBuilder;
import org.firstinspires.ftc.teamcode.VisionTesting.VisionPortalProcessers.propDetectionByAmount;
import org.firstinspires.ftc.teamcode.hardware._.Collection;
import org.firstinspires.ftc.teamcode.hardware._.Drivetrain;
import org.firstinspires.ftc.teamcode.hardware._.Odometry;
import org.firstinspires.ftc.vision.VisionPortal;

@Autonomous(name = "Testing Phase blue preload left", group = "Preload")
public class TestingPhaseAuto extends LinearOpMode implements Auto_Methods {

    public WebcamName frontCam;

    public VisionPortal portal;

    org.firstinspires.ftc.teamcode.VisionTesting.VisionPortalProcessers.propDetectionByAmount propDetectionByAmount = new propDetectionByAmount(telemetry, org.firstinspires.ftc.teamcode.VisionTesting.VisionPortalProcessers.propDetectionByAmount.Side.left, org.firstinspires.ftc.teamcode.VisionTesting.VisionPortalProcessers.propDetectionByAmount.color.blue);

    /**hardware objects*/
    Odometry odometry = new Odometry(210, 23, 270);

    Drivetrain drive = new Drivetrain();

    /**pathing objects*/
    blueLeftBuilder firstPath = new blueLeftBuilder();

    blueLeftBuilder secondPath = new blueLeftBuilder();

    blueLeftBuilder collect = new blueLeftBuilder();

    blueLeftBuilder deliver = new blueLeftBuilder();

    blueRightBuilder lastToBackboard = new blueRightBuilder();

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

    @Override
    public void runOpMode() throws InterruptedException {

        initialize();

        while(opModeInInit() && !lockIn){

            telemetry.addData("Auto activated", auto);
            telemetry.addData("press y for 2+0", "");
            telemetry.addData("press a for 2+2", "");
            telemetry.addData("press b for 2+4", "");
            telemetry.addData("press x to lock in!!!!", "");

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

        waitForStart();

        collection.setIntakeHeight(Collection.intakeHeightState.letClawThrough);
        collection.updateIntakeHeight();

        portal.close();

        if (propPos == 1){

            firstPath.buildPath(blueLeftBuilder.Position.left, blueLeftBuilder.Section.preload);

            while (!(phase == Phase.finished)){

                switch (phase){
                    case purple:

                        odometry.update();

                        if (!builtPath){
                            builtPath = true;
                            follower.setPath(firstPath.followablePath, firstPath.pathingVelocity);
                            targetHeading = 240;
                        }

                        if (pathing){
                            pathing = follower.followPathAuto(targetHeading, odometry, drive);

                            if (Math.abs(250 - odometry.X) < 15 && Math.abs(46 - odometry.Y) < 15){
                                targetHeading = 180;
                                phase = Phase.yellow;
                            }

                        }

                        break;
                    case yellow:

                        odometry.update();

                        if (pathing){

                            pathing = follower.followPathAuto(targetHeading, odometry, drive);

                        }else if (Math.abs(300 - odometry.X) < 2 && Math.abs(82.5 - odometry.Y) < 2){
                            if (auto == Auto.preload){
                                dropYellowPixelWait();
                                phase = Phase.finished;
                            }else {
                                dropYellowPixel();

                                collect.buildPath(blueLeftBuilder.Section.collect);

                                deliver.buildPath(blueLeftBuilder.Section.deliver);

                                phase = Phase.first2;
                            }
                        }

                        break;
                    case first2:

                        if (!builtPath){
                            builtPath = true;
                            follower.setPath(collect.followablePath, collect.pathingVelocity);
                            targetHeading = 180;
                        }

                        if (pathing){
                            pathing = follower.followPathAuto(targetHeading, odometry, drive);

                            if (Math.abs(250 - odometry.X) < 15 && Math.abs(46 - odometry.Y) < 15){
                                targetHeading = 180;
                                phase = Phase.yellow;
                            }

                        }else if (Math.abs(300 - odometry.X) < 2 && Math.abs(110 - odometry.Y) < 2){
                            dropYellowPixelWait();
                            phase = Phase.finished;
                        }

                        break;
                    default:
                }

            }

        } else if (propPos == 2) {

            firstPath.buildPath(blueLeftBuilder.Position.center, blueLeftBuilder.Section.preload);

            while (!(phase == Phase.finished)){

                switch (phase){
                    case purple:

                        odometry.update();

                        if (!builtPath){
                            builtPath = true;
                            follower.setPath(firstPath.followablePath, firstPath.pathingVelocity);
                            targetHeading = 270;
                        }

                        if (pathing){
                            pathing = follower.followPathAuto(targetHeading, odometry, drive);

                            if (Math.abs(236 - odometry.X) < 15 && Math.abs(60 - odometry.Y) < 15){
                                targetHeading = 180;
                                phase = Phase.yellow;
                            }

                        }

                        break;
                    case yellow:

                        odometry.update();

                        if (pathing){

                            pathing = follower.followPathAuto(targetHeading, odometry, drive);

                        }else if (Math.abs(300 - odometry.X) < 2 && Math.abs(97.5 - odometry.Y) < 2){

                            dropYellowPixelWait();
                            phase = Phase.finished;

                        }

                        break;
                    default:
                }

            }

        } else if (propPos == 3) {

            firstPath.buildPath(blueLeftBuilder.Position.right, blueLeftBuilder.Section.preload, redRightBuilder.pathSplit.first);

            secondPath.buildPath(blueLeftBuilder.Position.right, blueLeftBuilder.Section.preload, redRightBuilder.pathSplit.second);

            while (!(phase == Phase.finished)){

                switch (phase){
                    case purple:

                        odometry.update();

                        if (!builtPath){
                            builtPath = true;
                            follower.setPath(firstPath.followablePath, firstPath.pathingVelocity);
                            targetHeading = 270;
                        }

                        if (pathing){
                            pathing = follower.followPathAuto(targetHeading, odometry, drive);

                            if (Math.abs(210 - odometry.X) < 15 && Math.abs(70 - odometry.Y) < 15){
                                targetHeading = 0;
                            }

                        }else if (Math.abs(210 - odometry.X) < 2 && Math.abs(90 - odometry.Y) < 2){
                            phase = Phase.yellow;
                            builtPath = false;
                        }

                        break;
                    case yellow:

                        odometry.update();

                        if (!builtPath){
                            builtPath = true;
                            follower.setPath(secondPath.followablePath, secondPath.pathingVelocity);
                            targetHeading = 0;
                        }

                        if (pathing){
                            pathing = follower.followPathAuto(targetHeading, odometry, drive);

                            if (Math.abs(250 - odometry.X) < 15 && Math.abs(90 - odometry.Y) < 15){
                                targetHeading = 180;
                            }

                        }else if (Math.abs(300 - odometry.X) < 2 && Math.abs(110 - odometry.Y) < 2){
                            dropYellowPixelWait();
                            phase = Phase.finished;
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

        odometry.update();

        frontCam = hardwareMap.get(WebcamName.class, "frontcam");

        portal = VisionPortal.easyCreateWithDefaults(frontCam, propDetectionByAmount);

    }

}
