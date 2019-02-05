package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.State;

public class ReturnState extends State {
	
	//State which returns Dui return to his own net when the ball is "in danger"

	public ReturnState(Dui dui){
		super(dui, "Return", Color.magenta);
	}

	@Override
	public double getOutput(DuiData d){		
		if(d.ownGoalDistance > 900 && dui.dif(d.steerBall, d.steerEnemyGoal) > 90 && dui.dif(d.steerBall, d.steerOwnGoal) > 35){
        	double angle = Math.toDegrees(d.carDirection.correctionAngle(dui.ownGoal.minus(d.carPosition)));
			d.r.drawLine3d(colour, d.carPosition.toFramework(), dui.ownGoal.toFramework());
			
			if(d.ballPosition.distance(dui.enemyGoal) < 2000){
				this.setWeight(0.8);
			}else{
	        	this.setWeight(1.2 + (d.ownGoalDistance / 2500) * 2);
			}
        	
        	return angle;
        }else{
        	this.setWeight(0);
        	return 0;
        }
	}

}