package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.*;
import rlbot.render.Renderer;

public class DefendState extends State {
	
	//State which has Dui sit close to its own net when the ball is closeby

	public DefendState() {
		super("Defend");
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, Vector2 ownGoal, Vector2 enemyGoal, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r) {
		final boolean danger = 2000 > carPosition.distance(ownGoal);
		if(Math.abs(carPosition.y) > 4500 || danger){
			if(Math.abs(carPosition.y) > 5000){
				Vector2 defence = new Vector2(ballPosition.x / 3, car.team == 0 ? -5050 + (ballDistance / 50): 5050 - (ballDistance / 50));
				r.drawLine3d(Color.green, input.car.position.toFramework(), defence.toFramework());
				this.setWeight(5 + 1000 / ballDistance);
	        	return Math.toDegrees(carDirection.correctionAngle(defence.minus(carPosition)));   
			}else{
				this.setWeight(7 + 2000 / ballDistance);
				return steerBall;
			}
		}else{
			this.setWeight(0);
			return 0;
		}
	}

}
