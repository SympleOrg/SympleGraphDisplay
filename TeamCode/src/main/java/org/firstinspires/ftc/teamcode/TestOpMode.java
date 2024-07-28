package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.util.Random;

import top.symple.symplegraphdisplay.SympleGraphDisplay;
import top.symple.symplegraphdisplay.managers.data.Color;
import top.symple.symplegraphdisplay.managers.data.DataListener;
import top.symple.symplegraphdisplay.managers.data.DataListenerGroup;

public class TestOpMode extends LinearOpMode implements DataListenerGroup {

    @DataListener(color = @Color(red = 0, green = 0, blue = 200), fillColor = @Color(red = 0, green = 0, blue = 200, alpha = 0.1f))
    private double testValue = 0;
    @DataListener
    private double speed = 0;

    private void initialize() {
        SympleGraphDisplay.getInstance().init();
        SympleGraphDisplay.getInstance().setUpdateTime(0.05);
        SympleGraphDisplay.getInstance().registerDataListenerGroup(this);
    }

    private void run() {
        SympleGraphDisplay.getInstance().run();

        speed = new Random().nextDouble() * 30;
        testValue = speed / (new Random().nextDouble() * 30);
    }

    private void reset() {
        SympleGraphDisplay.getInstance().reset();
    }

    @Override
    public void runOpMode() throws InterruptedException {
        initialize();

        waitForStart();

        while (!isStopRequested() && opModeIsActive()) {
            run();
        }

        reset();
    }
}
