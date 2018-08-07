package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.*;
import rlbot.render.Renderer;

public class ReturnState extends State {
	
	//State which returns Dui back to his own net when the ball is "in danger"

	public ReturnState() {
		super("Return");
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, Vector2 ownGoal, Vector2 enemyGoal, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){		
		if((ownGoalDistance > 800 && Dui.dif(steerBall, steerEnemyGoal) >= 90) || car.position.z > 1000){
			
        	double angle = Math.toDegrees(carDirection.correctionAngle(ownGoal.minus(carPosition)));
			
			if(ballPosition.distance(enemyGoal) < 2000){
	            r.drawLine3d(Color.magenta, car.position.toFramework(), ownGoal.toFramework());
				this.setWeight(0.4);
	        	return angle;
			}
			
        	if(Dui.dif(angle, steerBall) <= 65 || ballPosition.x < 1100){
        		double modifier = Math.max(50, ballDistance / 3.5);
        		Vector2 target = new Vector2(ballPosition.x > carPosition.x ? ballPosition.x - modifier : ballPosition.x + modifier, ballPosition.y);
        		angle = Math.toDegrees(carDirection.correctionAngle(target.minus(carPosition)));
                r.drawLine3d(Color.magenta, car.position.toFramework(), target.toFramework());
        	}else{
                r.drawLine3d(Color.magenta, car.position.toFramework(), ownGoal.toFramework());
        	}
        	
        	this.setWeight(0.6 + (ownGoalDistance / 2500) * 2);
        	return angle;
        }else{
        	this.setWeight(0);
        	return 0;
        }
	}
	
//	@Override
//	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, Vector2 ownGoal, Vector2 enemyGoal, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal){		
//    	final Vector2 corner = new Vector2((carPosition.x > 0 ? 3000 - Math.abs(ballPosition.x) / 2 : -3000 + Math.abs(ballPosition.x) / 2), (car.team == 0 ? -5120 : 5120));
//		if(Dui.dif(steerBall, steerEnemyGoal) >= 120 && Math.abs(corner.y - carPosition.y) + 500 > Math.abs(corner.y - ballPosition.y)){
//        	this.setWeight(0.6 + (ownGoalDistance / 2500));
//			double y = (carPosition.y + ballPosition.y) / 2;
//        	double x = corner.x + Math.abs(corner.y - carPosition.y) / Math.abs(corner.y - ballPosition.y) * -(corner.x - ballPosition.x);
//        	if(x > 4000){
//        		x = 4000;
//        	}else if(x < -4000){
//        		x = -4000;
//        	}
//        	System.out.println(x + ", " + y);
//        	Vector2 target = new Vector2(x, y);
//        	return Math.toDegrees(carDirection.correctionAngle(target.minus(carPosition)));
//        }else{
//        	this.setWeight(0);
//        	return 0;
//        }
//	}

}
