package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.*;
import rlbot.render.Renderer;

public class DefendState extends State {
	
	//State which has Dui sit close to its own net when the ball is close-by

	public DefendState() {
		super("Defend", Color.green);
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r) {
		if((carPosition.distance(Dui.ownGoal) < 2000) || (car.team == 0 ? carPosition.y < -4700 : carPosition.y > 4700)){
			if(Math.abs(carPosition.y) > 4900){
				Vector2 defence = new Vector2(ballPosition.x / 3, car.team == 0 ? -5050 + (ballDistance / 50): 5050 - (ballDistance / 50));
				r.drawLine3d(colour, carPosition.toFramework(), defence.toFramework());
				this.setWeight(5 + 1000 / ballDistance);
	        	return Math.toDegrees(carDirection.correctionAngle(defence.minus(carPosition)));   
			}else{
				r.drawLine3d(colour, carPosition.toFramework(), ballPosition.toFramework());
				this.setWeight(7 + 2000 / ballDistance);
				return steerBall;
			}
		}else{
			this.setWeight(0);
			return 0;
		}
	}

}
