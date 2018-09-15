package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.DuiPrediction;
import rlbot.dui.State;
import rlbot.obj.Vector2;

public class ChaseState extends State {
	
	//State which is for ballchasing (when necessary!)
	
	private static final int threshold = 350;

	public ChaseState(){
		super("Chase", Color.DARK_GRAY);
	}

	@Override
	public double getOutput(DuiData d){
		if(!DuiPrediction.isNice() && !DuiPrediction.isDanger()){
			if(Dui.team == 0 ? (d.carPosition.y + threshold > d.ballPosition.y) : (d.carPosition.y - threshold < d.ballPosition.y)){
				Vector2 target = DuiPrediction.ballAfterSeconds(d.ballDistance / (double)Math.min(2300, 300 + d.car.velocity.magnitude())).flatten();
				target = target.plus(Dui.ownGoal.minus(target).normalised().scaled(d.ballDistance / 100));
				d.r.drawLine3d(colour, d.carPosition.toFramework(), target.toFramework());
				this.setWeight(4);
				return Math.toDegrees(d.carDirection.correctionAngle(target.minus(d.carPosition)));
			}
		}
		this.setWeight(0);
		return 0;
	}

}
