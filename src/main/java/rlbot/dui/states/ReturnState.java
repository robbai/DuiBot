package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.DuiPrediction;
import rlbot.dui.State;
import rlbot.obj.Vector2;

public class ReturnState extends State {
	
	//State which returns Dui back to his own net when the ball is "in danger"

	public ReturnState() {
		super("Return", Color.magenta);
	}

	@Override
	public double getOutput(DuiData d){
		final boolean danger = DuiPrediction.isDanger();
		if(!DuiPrediction.isNice() && (danger || (d.ownGoalDistance > 800 && Dui.dif(d.steerBall, d.steerEnemyGoal) >= 102))){
			
        	double angle = Math.toDegrees(d.carDirection.correctionAngle(Dui.ownGoal.minus(d.carPosition)));
			
        	//Return back to our own goal
			if(!danger && d.ballPosition.distance(Dui.enemyGoal) < 2000){
	            d.r.drawLine3d(colour, d.car.position.toFramework(), Dui.ownGoal.toFramework());
				this.setWeight(0.4);
	        	return angle;
			}
			
			//Choose to intercept the ball or just return back
        	if(Dui.dif(angle, d.steerBall) <= 65 || d.ballPosition.x < 1100 || danger){
        		final double modifier = Math.max(50, d.ballDistance / 2.8D);
        		Vector2 target = new Vector2(d.ballPosition.x > d.carPosition.x ? d.ballPosition.x - modifier : d.ballPosition.x + modifier, d.ballPosition.y);
        		angle = Math.toDegrees(d.carDirection.correctionAngle(target.minus(d.carPosition)));
                d.r.drawLine3d(colour, d.car.position.toFramework(), target.toFramework());
                d.r.drawLine3d(colour, target.toFramework(), d.ballPosition.toFramework());
        	}else{
                d.r.drawLine3d(colour, d.car.position.toFramework(), Dui.ownGoal.toFramework());
        	}
        	
        	this.setWeight(2 + (d.ownGoalDistance / 2500) * 5);
        	return angle;
        }else{
        	this.setWeight(0);
        	return 0;
        }
	}
	
//	@Override
//	public double getOutput(DataPacket input, Vector3 d.ballPosition3, Vector2 d.ballPosition, d.carData d.car, Vector2 d.d.carPosition, Vector2 d.carDirection, Vector2 ownGoal, Vector2 enemyGoal, double d.ballDistance, double d.ownGoalDistance, double d.steerBall, double steerEnemyGoal){		
//    	final Vector2 corner = new Vector2((d.d.carPosition.x > 0 ? 3000 - Math.abs(d.ballPosition.x) / 2 : -3000 + Math.abs(d.ballPosition.x) / 2), (d.car.team == 0 ? -5120 : 5120));
//		if(Dui.dif(d.steerBall, steerEnemyGoal) >= 120 && Math.abs(corner.y - d.d.carPosition.y) + 500 > Math.abs(corner.y - d.ballPosition.y)){
//        	this.setWeight(0.6 + (d.ownGoalDistance / 2500));
//			double y = (d.d.carPosition.y + d.ballPosition.y) / 2;
//        	double x = corner.x + Math.abs(corner.y - d.d.carPosition.y) / Math.abs(corner.y - d.ballPosition.y) * -(corner.x - d.ballPosition.x);
//        	if(x > 4000){
//        		x = 4000;
//        	}else if(x < -4000){
//        		x = -4000;
//        	}
//        	System.out.println(x + ", " + y);
//        	Vector2 target = new Vector2(x, y);
//        	return Math.toDegrees(d.carDirection.correctionAngle(target.minus(d.d.carPosition)));
//        }else{
//        	this.setWeight(0);
//        	return 0;
//        }
//	}

}
