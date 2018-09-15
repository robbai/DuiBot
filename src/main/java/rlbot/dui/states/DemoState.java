package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.DuiPrediction;
import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.obj.Vector2;

public class DemoState extends State {
	
	//State which is for demolishing opponents!
	
	private static int targetIndex = -1;
	private static long targetSet = 0L;

	public DemoState(){
		super("Demo", Color.black);
	}

	@Override
	public double getOutput(DuiData d){
		if(!d.car.isSupersonic){
			this.setWeight(0);
			return 0;
		}
		
		//Set a new target
		if(targetIndex == -1 || (System.currentTimeMillis() - targetSet > 8000)){
			getTarget(d.ballPosition);
		}
			
		//Chase
		if(targetIndex != -1){
			CarData target = Dui.cars[targetIndex];
			double ang = Math.toDegrees(d.carDirection.correctionAngle(target.position.flatten().minus(d.carPosition)));
			double targetDistance = d.carPosition.distance(target.position.flatten());
			if(target != null && !DuiPrediction.isDanger() && !target.isSupersonic && ((System.currentTimeMillis() - targetSet < 2750) || DuiPrediction.isNice() || (Dui.dif(d.steerBall, ang) < 20 && d.ballDistance > targetDistance))){
				if(System.currentTimeMillis() - targetSet > 8000) targetSet = System.currentTimeMillis();
				d.r.drawLine3d(colour, d.carPosition.toFramework(), target.position.flatten().toFramework());
				d.r.drawCenteredRectangle3d(colour, target.position.flatten().toFramework(), 30, 30, false);
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
