

package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiPrediction;
import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.Vector2;
import rlbot.obj.Vector3;
import rlbot.render.Renderer;

public class AttackState extends State {
	
	//State which is the most active, makes Dui angle itself to face both the ball and the opponent's goal
	
	/**The clamp value for the curve on the X-axis*/
	private static final int xClamp = 3990; //3850
	
	/**How weighted the curve will be to the player*/
	private final double playerWeight = 1.48D;
	
	/**How many lines there are*/
	private static final int curves = 20;
	
	/**How far each drawn line goes*/
	private static final double scale = (1D / curves);

	/**Which point of the line*/ 
	private static final int point = 5;
	
	public AttackState() {
		super("Attack", Color.red);
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){
		if(!KickoffState.isKickoff(input.ball) && !CentraliseState.isBackboardRolling(input.ball)){
            double enemyGoalDistance = carPosition.distance(Dui.enemyGoal);
        	this.setWeight(1.2 + (car.boost / 200D) + (enemyGoalDistance / 16000));
        	        
        	//Predict where the ball will be when we get there
        	Vector3 ballPredict = DuiPrediction.ballAfterSeconds((double)ballDistance / (double)Math.min(2300, 300 + car.velocity.magnitude()));
        	
        	//Get an appropriate target for where Dui is shooting, rather than the centre of the goal
        	Vector2 enemyGoal = new Vector2(Math.max(-780, Math.min(780, ballPredict.x)), Dui.enemyGoal.y);
        	r.drawCenteredRectangle3d(Color.ORANGE, enemyGoal.toFramework(), 30, 30, false);
        	
        	//We add a slight offset to the ball to ensure we hit it at the right face
        	//Useful when we don't start with the best angle 
        	r.drawCenteredRectangle3d(this.colour, ballPredict.flatten().toFramework(), 30, 30, false);
        	ballPredict = ballPredict.minus(enemyGoal.minus(ballPredict.flatten()).normalised().scaled(36));        	
        	r.drawCenteredRectangle3d(this.colour, ballPredict.flatten().toFramework(), 20, 20, false);
        	
        	Vector2 target = target(input, carPosition, enemyGoal, r, curves, ballPredict, car);        	
        	return Math.toDegrees(carDirection.correctionAngle(target.minus(carPosition)));
        }else{
        	this.setWeight(0);
        	return 0;
        }
	}

	/**This method is used for creating a curved line of attack towards the ball*/
	private Vector2 target(DataPacket input, Vector2 start, Vector2 enemyGoal, Renderer r, int depth, Vector3 ball, CarData car){
		if(depth == 0) return null;
		
		double y = ((ball.y + Math.abs(start.y - ball.y) * (car.team == 0 ? -1 : 1)) * playerWeight + ball.y) / (1 + playerWeight);
    	double x = enemyGoal.x + ((ball.x - enemyGoal.x) * Dui.dif(enemyGoal.y, start.y) / Dui.dif(enemyGoal.y, ball.y)); 
    	
    	int xClampNew = (int)(Math.abs(ball.x) > xClamp ? ball.x : xClamp);
    	x = Math.max(-xClampNew, Math.min(xClampNew, x)); //Clamp
    	
    	Vector2 target = new Vector2(x, y);
    	Vector2 halfTarget = start.plus(target.minus(start).scaled(scale));
    	
    	//Here we connect the points of our pathway to create a curved line
    	//The final orange line shows where Dui will predict the ball to go when hit from this path
    	r.drawLine3d((depth == 1 ? Color.ORANGE : colour), start.toFramework(), (depth == 1 ? start.plus(target.minus(start).scaled(10000)).confine().toFramework() : halfTarget.toFramework()));
    	
    	//We choose to return based on which point of the curve we want Dui to point towards
    	Vector2 result = target(input, halfTarget, enemyGoal, r, depth - 1, ball, car);
    	if(depth == curves - point) r.drawCenteredRectangle3d(Color.ORANGE, result.toFramework(), 20, 20, false);
    	return (depth > curves - point ? result : halfTarget);
	}

}
