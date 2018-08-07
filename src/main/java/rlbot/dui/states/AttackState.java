
package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.*;
import rlbot.render.Renderer;

public class AttackState extends State {
	
	//State which is the most active, makes Dui angle itself to face both the ball and the opponent's goal

	public AttackState() {
		super("Attack");
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, Vector2 ownGoal, Vector2 enemyGoal, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){
		//  && ((car.team == 0 ? 1 : -1) * carPosition.y + 1000) < ((car.team == 0 ? 1 : -1) * ballPosition.y)
		if(Math.abs(carPosition.y) <= 5120 && !KickoffState.isKickoff(input.ball)){
            double enemyGoalDistance = carPosition.distance(enemyGoal);
        	this.setWeight(1.2 + (car.boost / 200D) + (enemyGoalDistance / 16000));
        	
        	//If the ball and opponents goal are closely relevant, just drive to the ball
        	if(ballDistance < 800 && Dui.dif(steerEnemyGoal, steerBall) < 8){
        		return steerBall;
        	}
        	
//        	double y = Dui.dif(steerEnemyGoal, steerBall) * 1.9 * (car.team == 0 ? -1 : 1) * Math.min(3, ballDistance / 350) / 2 + ballPosition.y;
        	
        	Vector2 target = target(input, carPosition, enemyGoal, r, 10);        	
        	return Math.toDegrees(carDirection.correctionAngle(target.minus(carPosition)));
        }else{
        	this.setWeight(0);
        	return 0;
        }
	}

	private Vector2 target(DataPacket input, Vector2 start, Vector2 enemyGoal, Renderer r, int depth){
		if(depth <= 0){
			r.drawLine3d(Color.red, start.toFramework(), enemyGoal.toFramework());
			return null;
		}
		
		double y = (start.y * 2 + input.ball.position.y) / 3;
		
    	double x = (input.ball.position.x) * Dui.dif(enemyGoal.y, start.y) / Dui.dif(enemyGoal.y, input.ball.position.y);    	
    	x = Math.max(-4120, Math.min(4120, x)); //Clamp
    	
    	Vector2 target = new Vector2(x, y);
    	Vector2 halfTarget = start.plus(target.minus(start).scaled(0.25));
    	r.drawLine3d(Color.red, start.toFramework(), halfTarget.toFramework());
    	
    	target(input, halfTarget, enemyGoal, r, depth - 1);
    	return halfTarget;
	}

}
