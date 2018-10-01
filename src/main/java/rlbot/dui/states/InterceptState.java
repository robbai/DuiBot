package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.DuiPrediction;
import rlbot.dui.State;
import rlbot.obj.Vector2;
import rlbot.render.Renderer;

public class InterceptState extends State {
	
	//State which has Dui intercept the ball when "in danger"

	public InterceptState() {
		super("Intercept", Color.pink);
	}

	@Override
	public double getOutput(DuiData d){
		if(!CentraliseState.isBackboardRolling(d.input.ball) && !DuiPrediction.isNice() && (d.ownGoalDistance > 800 && Dui.dif(d.steerBall, d.steerEnemyGoal) >= 102)){
			
        	double angle = Math.toDegrees(d.carDirection.correctionAngle(Dui.ownGoal.minus(d.carPosition)));
			
			//Choose to intercept the ball
        	if(Dui.dif(angle, d.steerBall) <= 65 || d.ballPosition.x < 1100){
        		final Vector2 target = getPoint(d.carPosition, d.ballPosition, d.r, 16);
        		d.r.drawCenteredRectangle3d(colour, target.toFramework(), 15, 15, false);
        		angle = Math.toDegrees(d.carDirection.correctionAngle(target.minus(d.carPosition)));
                
                this.setWeight(7.5D + (d.ownGoalDistance / 1000) * 6);
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
	
	private Vector2 getPoint(Vector2 start, Vector2 ball, Renderer r, int ply){
		if(ply == 0) return ball;
		
		final double ballDistance = start.distance(ball);
		final double modifier = Math.max(50, ballDistance / 2.2D);
		Vector2 target = new Vector2(ball.x > start.x ? ball.x - modifier : ball.x + modifier, ball.y);
		
		Vector2 halfTarget = start.plus(target.minus(start).scaled(0.175D));
		r.drawLine3d(colour, start.toFramework(), halfTarget.toFramework());
		getPoint(halfTarget, ball, r, ply - 1);
		
		return halfTarget;
	}

}
