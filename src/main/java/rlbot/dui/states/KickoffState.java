package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.State;
import rlbot.input.BallData;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.*;
import rlbot.render.Renderer;

public class KickoffState extends State {
	
	//State which simply curves Dui towards the ball at a kickoff
	
	/**Determines how curved the turn is*/
	private final int smoothness = 5;

	public KickoffState() {
		super("Kickoff", Color.white);
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, Vector2 ownGoal, Vector2 enemyGoal, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){
		if(isKickoff(input.ball)){
	        r.drawLine3d(colour, car.position.toFramework(), input.ball.position.toFramework());
			this.setWeight(1000);
	    	return steerBall / smoothness;
		}else{
			this.setWeight(0);
	    	return 0;
		}
	}

	/**Determines whether the ball is stationary in the middle of the field*/
	public static boolean isKickoff(final BallData ball) {
		return ball.position.flatten().isZero() && ball.velocity.isZero();
	}

}
