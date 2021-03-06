package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.State;
import rlbot.obj.Vector2;
import rlbot.obj.Vector3;

public class TestState extends State {
	
	//State which is for testing purposes
	
	private final int goalWidth = 820;
	private final int maxGoalAngle = 150;

	public TestState(Dui dui){
		super(dui, "Test", Color.black);
	}

	@Override
	public double getOutput(DuiData d){
		if(!KickoffState.isKickoff(d.input.ball) && (Math.abs(d.ballPosition.y) < 5050 || Math.abs(d.ballPosition.x) < goalWidth / 2) && d.ballDistance < 5000 && Math.abs(d.carPosition.y) < 5120){
			final Vector2 leftPost = dui.enemyGoal.withX(dui.team == 0 ? -goalWidth : goalWidth);
			final Vector2 rightPost = dui.enemyGoal.withX(dui.team == 0 ? goalWidth : -goalWidth);
			
			//Predict where the ball will be when we get there
	    	Vector3 ballPredict = dui.duiPrediction.ballAfterSeconds(d.ballDistance / (double)Math.min(2300, 340 + d.car.velocity.magnitude()));
	    	double steerBall = Math.toDegrees(d.carDirection.correctionAngle(ballPredict.flatten().minus(d.carPosition)));
			
			double steerLeft = Math.toDegrees(d.carDirection.correctionAngle(leftPost.minus(d.carPosition)));
			double steerRight = Math.toDegrees(d.carDirection.correctionAngle(rightPost.minus(d.carPosition)));
			
			if(Math.abs(steerLeft) < maxGoalAngle && Math.abs(steerRight) < maxGoalAngle){
				d.r.drawLine3d(colour, d.carPosition.toFramework(), leftPost.toFramework());
				d.r.drawLine3d(colour, d.carPosition.toFramework(), rightPost.toFramework());
				final boolean correct = (steerBall > steerLeft && steerBall < steerRight);
				d.r.drawString3d((int)steerLeft + " < " + (int)steerBall + " < " + (int)steerRight, correct ? Color.white : Color.gray, d.car.position.toFramework(), 2, 2);
				if(correct){
					d.r.drawLine3d(Color.white, d.carPosition.toFramework(), d.carPosition.plus(ballPredict.flatten().minus(d.carPosition).scaled(10000)).confineRatio().toFramework());
					this.setWeight(10000);
					return steerBall;
				}
			}
		}
		this.setWeight(0);
		return 0;
	}

}
