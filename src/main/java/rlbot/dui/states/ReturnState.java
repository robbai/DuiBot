package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.DuiPrediction;
import rlbot.dui.State;
import rlbot.obj.Vector2;

public class ReturnState extends State {
	
	//State which has Dui intercept the ball when "in danger"

	public ReturnState() {
		super("Return", Color.magenta);
	}

	@Override
	public double getOutput(DuiData d){
//		final boolean danger = DuiPrediction.isDanger();
		if(!CentraliseState.isBackboardRolling(d.input.ball) && !DuiPrediction.isNice() && (d.ownGoalDistance > 800 && Dui.dif(d.steerBall, d.steerEnemyGoal) >= 102)){
			
        	double angle = Math.toDegrees(d.carDirection.correctionAngle(Dui.ownGoal.minus(d.carPosition)));
			
			//Choose to intercept the ball
        	if(Dui.dif(angle, d.steerBall) <= 65 || d.ballPosition.x < 1100){
        		final double modifier = Math.max(50, d.ballDistance / 2.8D);
        		Vector2 target = new Vector2(d.ballPosition.x > d.carPosition.x ? d.ballPosition.x - modifier : d.ballPosition.x + modifier, d.ballPosition.y);
        		angle = Math.toDegrees(d.carDirection.correctionAngle(target.minus(d.carPosition)));
                d.r.drawLine3d(colour, d.car.position.toFramework(), target.toFramework());
                d.r.drawLine3d(colour, target.toFramework(), d.ballPosition.toFramework());
                
                this.setWeight(2 + (d.ownGoalDistance / 2500) * 5);
            	return angle;
        	}else{
        		this.setWeight(0);
            	return 0;
        	}
        	
        }else{
        	this.setWeight(0);
        	return 0;
        }
	}

}
