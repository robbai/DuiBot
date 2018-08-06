package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.*;
import rlbot.render.Renderer;

public class KickoffState extends State {

	public KickoffState() {
		super("Kickoff");
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, Vector2 ownGoal, Vector2 enemyGoal, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){
		if(isKickoff(input)){
	        r.drawLine3d(Color.white, car.position.toFramework(), input.ball.position.toFramework());
			this.setWeight(1000);
	    	return steerBall / 5;
		}else{
			this.setWeight(0);
	    	return 0;
		}
	}

	public static boolean isKickoff(final DataPacket input) {
		return input.ball.position.flatten().isZero() && input.ball.velocity.isZero();
	}

}
