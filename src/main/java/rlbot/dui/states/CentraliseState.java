package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.State;
import rlbot.input.BallData;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.Vector2;
import rlbot.obj.Vector3;
import rlbot.render.Renderer;

public class CentraliseState extends State {
	
	//State which centralises Dui when the ball is rolling across the backboard

	public CentraliseState() {
		super("Centralise", Color.CYAN);
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){
		if(isBackboardRolling(input.ball)){
			
			//Define a destination based on whether this is our goal or the opponent's
			final boolean attacking = ((car.team == 0 ? 1 : -1) * ballPosition.y > 0);
			Vector2 dest = new Vector2(carPosition.x / 2, (car.team == 0 ? 1 : -1) * (attacking ? 3550 : -5075));
			
			this.setWeight(10); //Important!
			r.drawLine3d(colour, carPosition.toFramework(), dest.toFramework());
			r.drawCenteredRectangle3d(colour, dest.toFramework(), 30, 30, false);
			return Math.toDegrees(carDirection.correctionAngle(dest.minus(carPosition)));
		}else{
			this.setWeight(0);
	    	return 0;
		}
	}

	/**This method tells us whether the ball is rolling toward the net from the backboard*/
	public static boolean isBackboardRolling(BallData ball){
		//Ball must be near the back corners
		if(Math.abs(ball.position.y) < 4800) return false;
		if(Math.abs(ball.position.x) < 1000) return false;
		
		//Ball must be moving towards the goal (somewhat fast)
		if(ball.position.x > 0){
			if(ball.velocity.x > -500) return false;
		}else{
			if(ball.velocity.x < 500) return false;
		}
		
		//If it's falling fast, treat it normally
		return ball.velocity.z > -230;
	}

}
