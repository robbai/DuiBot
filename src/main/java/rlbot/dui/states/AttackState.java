

package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.Vector2;
import rlbot.obj.Vector3;
import rlbot.render.Renderer;

public class AttackState extends State {
	
	//State which is the most active, makes Dui angle itself to face both the ball and the opponent's goal
	
	/**The clamp value for the curve on the X-axis*/private final int xClamp = 3940; //3850	
	/**How weighted the curve will be to the player*/private final double playerWeight = 1.2D;
	/**How many lines there are when drawing our path*/private final int curves = 20;
	/**How far each drawn line goes*/private final double scale = 0.21D;
	/**Which point of the line to point towards*/private final int point = 6;
	
	public AttackState(Dui dui){
		super(dui, "Attack", Color.red);
	}

	@Override
	public double getOutput(DuiData d){
		if(!KickoffState.isKickoff(d.input.ball) && !CentraliseState.isBackboardRolling(d.input.ball)){
            double enemyGoalDistance = d.carPosition.distance(dui.enemyGoal);
        	this.setWeight(1.2 + (d.car.boost / 200D) + (enemyGoalDistance / 16000));
        	        
        	//Predict where the ball will be when we get there
        	Vector3 ballPredict = dui.duiPrediction.ballAfterSeconds(d.ballDistance / (double)Math.min(2300, 340 + d.car.velocity.magnitude()));
        	
        	//Get an appropriate target for where Dui is shooting, rather than the centre of the goal
        	Vector2 enemyGoal = new Vector2(Math.max(-770, Math.min(770, ballPredict.x)), dui.enemyGoal.y);
        	d.r.drawCenteredRectangle3d(colour, enemyGoal.toFramework(), 30, 30, false);
        	
        	//We add a slight offset to the ball to ensure we hit it at the correct angle
        	//Useful when we don't start with the best angle 
        	d.r.drawCenteredRectangle3d(this.colour, ballPredict.flatten().toFramework(), 26, 26, false);
        	ballPredict = ballPredict.minus(enemyGoal.minus(ballPredict.flatten()).normalised().scaled(Math.min(22, d.ballDistance / 25D)));        	
        	d.r.drawCenteredRectangle3d(this.colour, ballPredict.flatten().toFramework(), 20, 20, false);
        	
        	//Get the target to point towards
        	Vector2 target = target(d.input, d.carPosition, enemyGoal, d.r, curves, ballPredict, d.car);        	
        	return Math.toDegrees(d.carDirection.correctionAngle(target.minus(d.carPosition)));
        }
        this.setWeight(0);
        return 0;
	}

	/**This method is used for creating a curved line of attack towards the ball*/
	private Vector2 target(DataPacket input, Vector2 start, Vector2 enemyGoal, Renderer r, int depth, Vector3 ball, CarData car){
		if(depth == 0){
	    	//The final line shows where Dui will predict the ball to go when hit from this path
//			r.drawLine3d(colour, start.toFramework(), start.plus(ball.flatten().minus(start).scaled(10000)).confineRatio().toFramework());
			return null;
		}
		
		double y = ((ball.y + Math.abs(start.y - ball.y) * (car.team == 0 ? -1 : 1)) * playerWeight + ball.y) / (1 + playerWeight);
    	double x = enemyGoal.x + ((ball.x - enemyGoal.x) * dui.dif(enemyGoal.y, start.y) / dui.dif(enemyGoal.y, ball.y)); 
    	
    	int xClampNew = (int)Math.max(Math.abs(ball.x), xClamp);
    	x = Math.max(-xClampNew, Math.min(xClampNew, x)); //Clamp
    	
    	Vector2 target = new Vector2(x, y);
    	Vector2 halfTarget = start.plus(target.minus(start).scaled(scale));
    	
    	//Here we connect the points of our pathway to create a curved line
    	r.drawLine3d(colour, start.toFramework(), halfTarget.toFramework());
    	
    	//We choose to return based on which point of the curve we want Dui to point towards
    	Vector2 result = target(input, halfTarget, enemyGoal, r, depth - 1, ball, car);
    	if(depth == curves - point) r.drawCenteredRectangle3d(colour, result.toFramework(), 15, 15, false);
    	return (depth > curves - point ? result : halfTarget);
	}

}
