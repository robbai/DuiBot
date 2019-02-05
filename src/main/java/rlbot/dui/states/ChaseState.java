package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.obj.Vector2;

public class ChaseState extends State {
	
	//State which is for ballchasing (when necessary!)
	
	private final int threshold = 350;

	public ChaseState(Dui dui){
		super(dui, "Chase", Color.DARK_GRAY);
	}

	@Override
	public double getOutput(DuiData d){
		if(chase(d)){
			Vector2 target = dui.duiPrediction.ballAfterSeconds(d.ballDistance / (double)Math.min(2300, 550 + d.car.velocity.magnitude())).flatten();
			
			final double enemyDistance = getClosestEnemyToBall(d);
			final double s = Math.min(d.ballDistance * 0.3D, enemyDistance * 0.1D);
			target = target.plus(dui.ownGoal.minus(target).normalised().scaled(s));
			
			d.r.drawLine3d(colour, d.carPosition.toFramework(), target.toFramework());
//			d.r.drawString3d((int)s + "uu", colour, target.toFramework(), 2, 2);
			
			this.setWeight(4 + (enemyDistance / 250D));
			return Math.toDegrees(d.carDirection.correctionAngle(target.minus(d.carPosition)));
		}
		this.setWeight(0);
		return 0;
	}
	
	public double getClosestEnemyToBall(DuiData d){
		CarData closest = null;
		double shortestDistance = 5120 * 2;
		for(CarData car : d.input.cars){
			if(car == null || car.team == dui.team) continue;
			double distance = car.position.flatten().distance(d.ballPosition);
			if(closest == null || distance < shortestDistance){
				closest = car;
				shortestDistance = distance;
			}
		}
		return shortestDistance;
	}
	
	public boolean chase(DuiData d){
		if(!dui.duiPrediction.isNice() && !dui.duiPrediction.isDanger() && dui.dif(d.steerBall, d.steerEnemyGoal) > 65){
			return (dui.team == 0 ? (d.carPosition.y + threshold > d.ballPosition.y) : (d.carPosition.y - threshold < d.ballPosition.y));
		}else{
			return false;
		}
	}

}
