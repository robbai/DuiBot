package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiPrediction;
import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.Vector2;
import rlbot.obj.Vector3;
import rlbot.render.Renderer;

public class TestState extends State {
	
	//State which is for testing purposes
	
	private static int targetIndex = -1;
	private static long targetSet = 0L;

	public TestState(){
		super("Test", Color.black);
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){
		if(!car.isSupersonic){
			this.setWeight(0);
			return 0;
		}
		
		//Set a new target
		if(targetIndex == -1 || (System.currentTimeMillis() - targetSet > 8000)){
			getTarget(ballPosition);
		}
			
		//Chase
		if(targetIndex != -1){
			CarData target = Dui.cars[targetIndex];
			double ang = Math.toDegrees(carDirection.correctionAngle(target.position.flatten().minus(carPosition)));
			double targetDistance = carPosition.distance(target.position.flatten());
			if(target != null && !DuiPrediction.isDanger() && ((System.currentTimeMillis() - targetSet < 2750) || DuiPrediction.isNice() || (Dui.dif(steerBall, ang) < 20 && ballDistance > targetDistance))){
				if(System.currentTimeMillis() - targetSet > 8000) targetSet = System.currentTimeMillis();
				r.drawLine3d(colour, carPosition.toFramework(), target.position.flatten().toFramework());
				r.drawCenteredRectangle3d(colour, target.position.flatten().toFramework(), 30, 30, false);
				this.setWeight((4000D / Math.max(1, System.currentTimeMillis() - targetSet)) * 10D);
		    	return ang;
			}
		}
	    	
		this.setWeight(0);
		return 0;
	}
	
	private static void getTarget(Vector2 ballPosition){
		targetIndex = -1;
		double targetDistanceFromBall = 6000;
		for(int i = 0; i < Dui.cars.length; i++){
			CarData c = Dui.cars[i];
			if(c == null || c.team == Dui.team) continue; 
			double distance = c.position.flatten().distance(ballPosition);
			if(distance < targetDistanceFromBall){
				targetIndex = i;
				targetDistanceFromBall = distance;
			}
		}
	}

}
