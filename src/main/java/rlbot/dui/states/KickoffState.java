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

	public KickoffState() {
		super("Kickoff", Color.white);
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){
		if(isKickoff(input.ball)){
			double smoothness = Math.abs(carPosition.y) / 11000D;
			Vector2 target = new Vector2(ballPosition.x, (carPosition.y * smoothness + ballPosition.y) / (smoothness + 1D));
	        r.drawLine3d(colour, carPosition.toFramework(), target.toFramework());
			this.setWeight(1000);
	    	return Math.toDegrees(carDirection.correctionAngle(target.minus(carPosition)));
		}
		this.setWeight(0);
	    return 0;
	}

	/**Determines whether the ball is stationary in the middle of the field*/
	public static boolean isKickoff(final BallData ball) {
		return ball.position.flatten().isZero() && ball.velocity.isZero();
	}

}
