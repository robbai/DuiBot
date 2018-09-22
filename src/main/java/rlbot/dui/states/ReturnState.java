package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.State;

public class ReturnState extends State {
	
	//State which returns Dui return to his own net when the ball is "in danger"

	public ReturnState() {
		super("Return", Color.magenta);
	}

	@Override
	public double getOutput(DuiData d){		
		if(d.ownGoalDistance > 800 && Dui.dif(d.steerBall, d.steerEnemyGoal) >= 90){
        	double angle = Math.toDegrees(d.carDirection.correctionAngle(Dui.ownGoal.minus(d.carPosition)));
			d.r.drawLine3d(colour, d.carPosition.toFramework(), Dui.ownGoal.toFramework());
			
			if(d.ballPosition.distance(Dui.enemyGoal) < 2000){
				this.setWeight(0.4);
	        	return angle;
			}
        	
        	this.setWeight(0.8 + (d.ownGoalDistance / 2500) * 2);
        	return angle;
        }else{
        	this.setWeight(0);
        	return 0;
        }
	}

}