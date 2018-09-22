package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.DuiData;
import rlbot.dui.State;
import rlbot.input.BallData;
import rlbot.obj.Vector2;

public class KickoffState extends State {
	
	//State which simply curves Dui towards the ball at a kickoff

	public KickoffState() {
		super("Kickoff", Color.white);
	}

	@Override
	public double getOutput(DuiData d){
		if(isKickoff(d.input.ball)){
			d.r.drawString3d((int)Math.abs(d.carPosition.y) + "", this.colour, d.car.position.toFramework(), 2, 2);
			double carWeight = (d.ballDistance > 3000 ? 0.9D * (1800D / d.ballDistance) : 0.1D);
			Vector2 target = new Vector2(d.ballPosition.x, (d.carPosition.y * carWeight + d.ballPosition.y) / (carWeight + 1D));
	        d.r.drawLine3d(colour, d.carPosition.toFramework(), target.toFramework());
			this.setWeight(1000);
	    	return Math.toDegrees(d.carDirection.correctionAngle(target.minus(d.carPosition)));
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
