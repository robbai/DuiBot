package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiPrediction;
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
		if((carPosition.distance(Dui.ownGoal) < 2500) || (car.team == 0 ? carPosition.y < -4800 : carPosition.y > 4800)){
			if(DuiPrediction.isDanger()){
				r.drawLine3d(colour, carPosition.toFramework(), ballPosition.toFramework());
				this.setWeight(7 + 2000 / ballDistance);
				return steerBall;
			}else if(Math.abs(carPosition.y) > 5120){
				Vector2 defence = new Vector2(ballPosition.x / 3, car.team == 0 ? -5010 + (ballDistance / 50): 5010 - (ballDistance / 50));
				r.drawLine3d(colour, carPosition.toFramework(), defence.toFramework());
				this.setWeight(5 + 1000 / ballDistance);
	        	return Math.toDegrees(carDirection.correctionAngle(defence.minus(carPosition)));   
			}else{
				this.setWeight(1);
				return steerBall;
			}
		}else{
			this.setWeight(0);
			return 0;
		}
	}

}
