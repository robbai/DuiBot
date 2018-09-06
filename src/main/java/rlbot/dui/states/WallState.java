package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.DuiData;
import rlbot.dui.State;
import rlbot.input.CarData;

public class WallState extends State {
	
	//State which drives Dui to the floor when on a wall

	public WallState() {
		super("Wall", Color.gray);
	}

	@Override
	public double getOutput(DuiData d){
		int maxZ = (d.ballDistance < 1200 ? (int)(d.ballPosition3.z - 100) : 220);
		if(isOnWall(d.car, maxZ)){
			d.r.drawString3d("Wall", this.colour, d.car.position.toFramework(), 2, 2);
			this.setWeight(10000);
			
			double f = -d.car.orientation.getRoll() * 100;
	    	return Math.min(90, Math.max(-90, f));
		}else{
			this.setWeight(0);
	    	return 0;
		}
	}
	
	public static boolean isOnWall(final CarData car){
		return isOnWall(car, 220);
	}

	private static boolean isOnWall(final CarData car, final int maxZ){
		return car.position.z > maxZ && car.hasWheelContact;
	}

}
