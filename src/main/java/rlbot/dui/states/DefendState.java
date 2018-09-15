package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.DuiPrediction;
import rlbot.dui.State;
import rlbot.obj.Vector2;

public class DefendState extends State {
	
	//State which has Dui sit close to its own net when the ball is close-by

	public DefendState() {
		super("Defend", Color.green);
	}

	@Override
	public double getOutput(DuiData d){
		if(!CentraliseState.isBackboardRolling(d.input.ball) && (!DuiPrediction.isDanger() || d.carPosition.y > d.ballPosition.y - 100) && (d.carPosition.distance(Dui.ownGoal) < 2500) || (d.car.team == 0 ? d.carPosition.y < -4800 : d.carPosition.y > 4800)){
			if(DuiPrediction.isDanger()){
				d.r.drawLine3d(colour, d.carPosition.toFramework(), d.ballPosition.toFramework());
				this.setWeight(7 + 2000 / d.ballDistance);
				return d.steerBall;
			}else if(Math.abs(d.carPosition.y) > 5120){
				Vector2 defence = new Vector2(d.ballPosition.x / 3, d.car.team == 0 ? -5010 + (d.ballDistance / 50): 5010 - (d.ballDistance / 50));
				d.r.drawLine3d(colour, d.carPosition.toFramework(), defence.toFramework());
				this.setWeight(5 + 1000 / d.ballDistance);
	        	return Math.toDegrees(d.carDirection.correctionAngle(defence.minus(d.carPosition)));   
			}else{
				this.setWeight(1);
				return d.steerBall;
			}
		}else{
			this.setWeight(0);
			return 0;
		}
	}

}
