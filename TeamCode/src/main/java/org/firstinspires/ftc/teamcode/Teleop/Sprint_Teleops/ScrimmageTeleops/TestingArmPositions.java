package org.firstinspires.ftc.teamcode.Teleop.Sprint_Teleops.ScrimmageTeleops;

import static org.firstinspires.ftc.teamcode.Constants_and_Setpoints.Non_Hardware_Objects.currentGamepad1;
import static org.firstinspires.ftc.teamcode.Constants_and_Setpoints.Non_Hardware_Objects.currentGamepad2;
import static org.firstinspires.ftc.teamcode.Constants_and_Setpoints.Non_Hardware_Objects.previousGamepad1;
import static org.firstinspires.ftc.teamcode.Constants_and_Setpoints.Non_Hardware_Objects.previousGamepad2;
import static java.lang.Thread.sleep;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.hardware._.Delivery_Slides;
import org.firstinspires.ftc.teamcode.hardware._.Sensors;

@TeleOp
public class TestingArmPositions extends OpMode {
    Delivery_Slides delivery_slides = new Delivery_Slides();
    ServoImplEx secondPivotLeft;
    ServoImplEx secondPivotRight;
    ServoImplEx RightClaw;
    ServoImplEx LeftClaw;

    ServoImplEx RotateClaw;

    ServoImplEx mainPivotLeft;
    ServoImplEx mainPivotRight;

    ServoImplEx secondRotate;

    Servo RotateArm;

    ElapsedTime pivotMoveTimeDelivery = new ElapsedTime();
    ElapsedTime pivotMoveTimeAuto = new ElapsedTime();
    ElapsedTime pivotMoveTimeCollection = new ElapsedTime();

    Sensors sensors = new Sensors();

    double timeToWaitDelivery;
    double timeToWaitCollection;

    double timePerDegree = 7;

    double collectTopPivotPos = 0.23;
    double intermediateTopPivot = 0.3;
    double deliveryTopPivot = 0.7;
    double deliveryTopPivotNew = 1;
    double safeTopPivot = 0.3;

    double deliveryTopPivotAuto = 0.73;
    double deliverySecondPivotAuto = 0.2;
    double distancecalc;
    double avoidIntakeSecondPivot = 0.8;
    double collectSecondPivot = 0.85;
    double deliverySecondPivot = -0.16;
    double lowdeliveryTopPivot = 1;

    double clawOpenDeliver = 0.5;
    double clawOpen = 0.6;
    double clawClosed = 1;

    double rotateCollect = 0.55;
    double rotateDeliver = 0.55;
    double rotateRight = 1;

    double secondRotateMiddle = 0.5;
    double secondRotateMiddleCollect = 0.5;

    double targetRotatePos;
    static final double adjustFactor = 1.1;
    boolean closeToCollection;

    public void setMainPivotOffSet(double mainPivotOffSet) {
        this.mainPivotOffSet = mainPivotOffSet;
    }
    boolean firtloop = true;
    double mainPivotOffSet = 0;
    double targetMainPivot = 0;
    static final double servoPosPerTick = 0.00004100;
    static final double mainToSecondConst = 0.5/0.3;
    static final double mainservopospermm = 0.0126;
    static final double mindistancemm = 8;
    static final double maxdistancemm = 20;
    static final double distanceToPivot = 0.1495;

    boolean DeliveryMovingAuto = false;
    boolean DeliveryMoving = false;
    boolean CollectionMoving = false;
    boolean intermediateMoving = false;

    double deliveryIncrement = 0.02;
    public RobotArm[][] robotArmState = new RobotArm[11][];
    RobotArm robotArmHome;
    int armPosition = 1;
    HardwareMap hmap;

    @Override
    public void init() {
        init(hardwareMap);
    }

    @Override
    public void loop() {
        previousGamepad1.copy(currentGamepad1);
        currentGamepad1.copy(gamepad1);
        if (gamepad1.dpad_up){
            setMainPivot(getMainPivotPosition()+0.001);
        }
        if (gamepad1.dpad_down) {
            setMainPivot(getMainPivotPosition()-0.001);
        }

        if (gamepad1.a){
            setSecondPivot(getSecondPivotPosition()+0.001);
        }
        if (gamepad1.b) {
            setSecondPivot(getSecondPivotPosition()-0.001);
        }

        if (gamepad1.back){
            RotateArm.setPosition(RotateArm.getPosition()+0.001);
        }
        if (gamepad1.start) {
            RotateArm.setPosition(RotateArm.getPosition()-0.001);
        }

        if (gamepad1.left_trigger > 0){
            secondRotate.setPosition(secondRotate.getPosition()+0.001);
        }
        if (gamepad1.right_trigger > 0) {
            secondRotate.setPosition(secondRotate.getPosition()-0.001);
        }

        if (gamepad1.x){
            RotateClaw.setPosition(RotateClaw.getPosition()+0.001);
        }
        if (gamepad1.y) {
            RotateClaw.setPosition(RotateClaw.getPosition()-0.001);
        }
        if (currentGamepad1.right_bumper && !previousGamepad1.right_bumper && RightClaw.getPosition() == clawOpen) {
            RightClaw.setPosition(clawClosed);

        }else if(currentGamepad1.right_bumper && !previousGamepad1.right_bumper && RightClaw.getPosition() == clawClosed){
            RightClaw.setPosition(clawOpen);
        }

        if (currentGamepad1.left_bumper && !previousGamepad1.left_bumper && LeftClaw.getPosition() == clawOpen) {
            LeftClaw.setPosition(clawClosed);

        }else if(currentGamepad1.left_bumper && !previousGamepad1.left_bumper && LeftClaw.getPosition() == clawClosed){
            LeftClaw.setPosition(clawOpen);
        }

        if ((!previousGamepad1.dpad_left && gamepad1.dpad_left) || (!previousGamepad1.dpad_right && gamepad1.dpad_right)) {

            if (!previousGamepad1.dpad_right && gamepad1.dpad_right) {
                armPosition +=1;
            }else if (!previousGamepad1.dpad_left && gamepad1.dpad_left) {
                armPosition -=1;
            }
            if (armPosition < 5) {
                setArmPosition(1,armPosition);
            }

        }

        telemetry.addData("Main Pivot", mainPivotLeft.getPosition());
        telemetry.addData("Arm Rotate", RotateArm.getPosition());
        telemetry.addData("Second Rotate", secondRotate.getPosition());
        telemetry.addData("Second Pivot", secondPivotLeft.getPosition());
        telemetry.addData("Gripper Rotate", RotateClaw.getPosition());
        telemetry.addData("Slides height", delivery_slides.getCurrentposition());
        telemetry.addData("armPosition", armPosition);
        telemetry.update();

        delivery_slides.SlidesBothPower(0.0005);
    }

    /**
     *
     * @param hardwareMap this gets the hardwaremap from whatever opmode we are running
     */
    public void init(HardwareMap hardwareMap){
        hmap = hardwareMap;
        delivery_slides.init(hmap);
        pivotMoveTimeDelivery.reset();
        pivotMoveTimeCollection.reset();

        sensors.init(hardwareMap);
        previousGamepad1 = new Gamepad();
        currentGamepad1 = new Gamepad();
        /**servos init*/

        secondPivotLeft = hardwareMap.get(ServoImplEx.class, "RightPivot");
        secondPivotRight = hardwareMap.get(ServoImplEx.class, "LeftPivot");

        secondPivotRight.setDirection(Servo.Direction.REVERSE);

        secondPivotLeft.setPwmRange(new PwmControl.PwmRange(600, 2500));
        secondPivotRight.setPwmRange(new PwmControl.PwmRange(600, 2500));

        RightClaw = hardwareMap.get(ServoImplEx.class, "RightClaw");
        LeftClaw = hardwareMap.get(ServoImplEx.class, "LeftClaw");

        RightClaw.setDirection(Servo.Direction.FORWARD);
        LeftClaw.setDirection(Servo.Direction.REVERSE);

        RightClaw.setPwmRange(new PwmControl.PwmRange(600, 2500));
        LeftClaw.setPwmRange(new PwmControl.PwmRange(600, 2500));

        mainPivotLeft = hardwareMap.get(ServoImplEx.class, "leftmain");
        mainPivotRight = hardwareMap.get(ServoImplEx.class, "rightmain");

        mainPivotLeft.setDirection(Servo.Direction.REVERSE);

        mainPivotLeft.setPwmRange(new PwmControl.PwmRange(600, 2500));
        mainPivotRight.setPwmRange(new PwmControl.PwmRange(600, 2500));

        RotateClaw = hardwareMap.get(ServoImplEx.class, "ClawRotate");

        RotateClaw.setDirection(Servo.Direction.FORWARD);

        RotateClaw.setPwmRange(new PwmControl.PwmRange(700, 2300));

        secondRotate = hardwareMap.get(ServoImplEx.class, "secondRotate");

        secondRotate.setPwmRange(new PwmControl.PwmRange(600, 2500));

        RotateArm = hardwareMap.get(Servo.class, "RotateArm");

        RotateArm.setPosition(0.5);

        setMainPivot(collectTopPivotPos);

        secondRotate.setPosition(secondRotateMiddleCollect);



        RotateClaw.setPosition(rotateCollect);

        RightClaw.setPosition(clawClosed);
        LeftClaw.setPosition(clawClosed);


        for (int i = 0; i < robotArmState.length; i++) {
            // Alternating between 6 and 7 elements
            int elementsInRow = (i % 2 == 0) ? 6 : 7;
            robotArmState[i] = new RobotArm[elementsInRow];
        }

        robotArmState[1][1] = new RobotArm(0.876, 0.637, 0.294, 0.103, 0.578, 120);
        robotArmState[1][2] = new RobotArm(0.852, 0.500, 0.483, 0.0826, 0.539, 0);
        robotArmState[1][3] = new RobotArm(0.848, 0.389, 0.629, 0.127, 0.509, 0);
        robotArmState[1][4] = new RobotArm(0.919, 0.229, 0.808, 0.183, 0.457, 250);
        robotArmHome = new RobotArm(0.780, 0.48, 0.5, -0.16, 0.55, 150);




    }

    public void setRotateClaw(double position){
        RotateClaw.setPosition(position);
    }

    public double getSecondPivotPosition(){
        return (secondPivotLeft.getPosition() + secondPivotRight.getPosition())/2;
    }

    public double getMainPivotPosition(){
        return (mainPivotLeft.getPosition() + mainPivotRight.getPosition()) / 2;
    }

    private void setMainPivot(double position){
        mainPivotLeft.setPosition(position);
        mainPivotRight.setPosition(position);
    }

    public double getTopPivotPosition(){
        return secondPivotLeft.getPosition();
    }

    private void setClaws(double position){
        LeftClaw.setPosition(position);
        RightClaw.setPosition(position);
    }

    private void setSecondPivot(double position){
        secondPivotLeft.setPosition(position);
        secondPivotRight.setPosition(position);
    }
    public void setArmPositionInstant(RobotArm robotArm){
        int sleep;
        if (getMainPivotPosition() < 0.7){
            sleep = 600;
            delivery_slides.DeliverySlides(robotArm.slides,0.5);
        }else{
            sleep = 50;
        }

        try{
            sleep(50);
        }catch(Exception e){

        }
        setMainPivot(robotArm.mainPivot);
        setSecondPivot(robotArm.secondPivot);
        secondRotate.setPosition(robotArm.secondRotate);
        setRotateClaw(robotArm.ClawRotate);
        RotateArm.setPosition(robotArm.armRotate);
        try{
            sleep(sleep);
        }catch(Exception e){

        }
    }
    public void setArmPosition(int Row, int Column){
        setArmPositionInstant(robotArmHome);

        delivery_slides.DeliverySlides(robotArmState[Row][Column].slides,0.5);
        setSecondPivot(robotArmState[Row][Column].secondPivot);
        secondRotate.setPosition(robotArmState[Row][Column].secondRotate);
        setRotateClaw(robotArmState[Row][Column].ClawRotate);

        try{
            sleep(100);
        }catch(Exception e){

        }
        RotateArm.setPosition(robotArmState[Row][Column].armRotate);
        setMainPivot(0.8);
        try{
            sleep(150);
        }catch(Exception e){

        }
        setMainPivot(robotArmState[Row][Column].mainPivot);
        delivery_slides.SlidesBothPower(0.0005);
    }
    public class RobotArm {
        private double mainPivot;
        private double armRotate;
        private double secondRotate;
        private double secondPivot;
        private double ClawRotate;
        private int slides;

        public RobotArm(double mainPivot, double armRotate, double secondRotate, double secondPivot, double ClawRotate, int slides) {
            this.mainPivot = mainPivot;
            this.armRotate = armRotate;
            this.secondRotate = secondRotate;
            this.secondPivot = secondPivot;
            this.ClawRotate = ClawRotate;
            this.slides = slides;
        }


    }


}

