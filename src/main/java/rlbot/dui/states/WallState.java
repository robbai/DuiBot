package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.Vector2;
import rlbot.obj.Vector3;
import rlbot.render.Renderer;

public class WallState extends State {
	
	//State which drives Dui to the floor when on a wall

	public WallState() {
		super("Wall", Color.gray);
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){
		if(isOnWall(car)){
			r.drawString3d("Wall", this.colour, car.position.toFramework(), 2, 2);
			this.setWeight(10000);
			
			double f = -car.orientation.getRoll() * 100;
	    	return Math.min(90, Math.max(-90, f));
		}else{
			this.setWeight(0);
	    	return 0;
		}
	}

	public static boolean isOnWall(final CarData car){
		return car.position.z > 220 && car.hasWheelContact;
	}

}
