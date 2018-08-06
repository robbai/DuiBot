package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.*;
import rlbot.render.Renderer;

public class AttackState extends State {

	public AttackState() {
		super("Attack");
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, Vector2 ownGoal, Vector2 enemyGoal, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){
		//  && ((car.team == 0 ? 1 : -1) * carPosition.y + 1000) < ((car.team == 0 ? 1 : -1) * ballPosition.y)
		if(Math.abs(carPosition.y) <= 5120){
            double enemyGoalDistance = carPosition.distance(enemyGoal);
        	this.setWeight(1.2 + (car.boost / 200D) + (enemyGoalDistance / 16000));
        	
        	if(ballDistance < 800 && Dui.dif(steerEnemyGoal, steerBall) < 18){
        		return steerBall;
        	}
        	
        	double y = Dui.dif(steerEnemyGoal, steerBall) * 2.2 * (car.team == 0 ? -1 : 1) * Math.min(3, ballDistance / 350) / 2 + ballPosition.y;        	
        	double x = (ballPosition.x) * Dui.dif(enemyGoal.y, carPosition.y) / Dui.dif(enemyGoal.y, ballPosition.y);
        	
        	if(x > 4000){
        		x = 4000;
        	}else if(x < -4000){
        		x = -4000;
        	}
        	
//        	System.out.println((ballPosition.x - x) + ", " + (ballPosition.y - y));
        	
        	Vector2 target = new Vector2(x, y);
        	r.drawLine3d(Color.red, input.car.position.toFramework(), target.toFramework());
//        	r.drawLine3d(Color.red, ballPosition3.flatten().toFramework(), enemyGoal.toFramework());
        	
        	return Math.toDegrees(carDirection.correctionAngle(target.minus(carPosition)));
        }else{
        	this.setWeight(0);
        	return 0;
        }
	}

}
