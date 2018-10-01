package rlbot.dui;

import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.Vector2;
import rlbot.obj.Vector3;
import rlbot.render.Renderer;

public class DuiData {
	
	public DataPacket input;
	public Renderer r;
	public CarData car;
	public Vector3 ballPosition3;
	public Vector2 ballPosition;
	public Vector2 carPosition;
	public Vector2 carDirection;
	public double ballDistance;
	public double ownGoalDistance;
	public double steerBall;
	public double steerOwnGoal;
	public double steerEnemyGoal;
	
	public DuiData(DataPacket input, Renderer r){
		//General stuff
		this.input = input;
		this.r = r;
		
		//Get the car (very useful!)
        car = input.car;
		
		//Get vectors
		ballPosition3 = input.ball.position;
        ballPosition = ballPosition3.flatten();        
        carPosition = car.position.flatten();
        carDirection = car.orientation.noseVector.flatten();
                
        //Get distances in unreal units
        ballDistance = carPosition.distance(ballPosition);
        ownGoalDistance = carPosition.distance(Dui.ownGoal);
        
        //Get degrees to turn to certain vectors from the car
        steerBall = Math.toDegrees(carDirection.correctionAngle(ballPosition.minus(carPosition)));
        steerEnemyGoal = Math.toDegrees(carDirection.correctionAngle(Dui.ownGoal.minus(carPosition)));
        steerEnemyGoal = Math.toDegrees(carDirection.correctionAngle(Dui.enemyGoal.minus(carPosition)));
	}

}
