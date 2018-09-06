package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.DuiData;
import rlbot.dui.State;
import rlbot.input.BallData;
import rlbot.obj.Vector2;

public class CentraliseState extends State {
	
	//State which centralises Dui when the ball is rolling across the backboard

	public CentraliseState() {
		super("Centralise", Color.CYAN);
	}

	@Override
	public double getOutput(DuiData d){
		if(isBackboardRolling(d.input.ball)){
			
			//Define a destination based on whether this is our goal or the opponent's
			final boolean attacking = ((d.car.team == 0 ? 1 : -1) * d.ballPosition.y > 0);
			Vector2 dest = new Vector2(d.carPosition.x / 2, (d.car.team == 0 ? 1 : -1) * (attacking ? 3550 : -5075));
			
			this.setWeight(10); //Important!
			d.r.drawLine3d(colour, d.carPosition.toFramework(), dest.toFramework());
			return Math.toDegrees(d.carDirection.correctionAngle(dest.minus(d.carPosition)));
		}else{
			this.setWeight(0);
	    	return 0;
		}
	}

	/**This method tells us whether the ball is rolling toward the net from the backboard*/
	public static boolean isBackboardRolling(BallData ball){
		//Ball must be near the back corners
		if(Math.abs(ball.position.y) < 4800) return false;
		if(Math.abs(ball.position.x) < 950) return false;
		
		//Ball must be moving towards the goal (somewhat fast)
		if(ball.position.x > 0){
			if(ball.velocity.x > -400) return false;
		}else{
			if(ball.velocity.x < 400) return false;
		}
		
		//If it's falling fast, treat it normally
		return ball.velocity.z > -230;
	}

}
