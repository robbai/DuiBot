package rlbot.dui.states;

import java.awt.Color;

import rlbot.boost.BoostManager;
import rlbot.boost.BoostPad;
import rlbot.dui.Dui;
import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.*;
import rlbot.render.Renderer;

public class BoostState extends State {
	
	//State which has Dui go collect boost when the ball is not too relevant to either net

	public BoostState() {
		super("Boost");
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, Vector2 ownGoal, Vector2 enemyGoal, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){ 		
		BoostPad b = getNearestBoostpad(carPosition);
        if(b != null){
        	Vector2 boostLocation = b.getLocation().flatten();
        	double boostDistance = carPosition.distance(boostLocation);
        	double angle = Math.toDegrees(carDirection.correctionAngle(boostLocation.minus(carPosition)));
        	if((Math.abs(angle) < 10 && boostDistance * 1.2 < ballDistance) || (ballDistance > 4000 && Math.abs(carPosition.y) < 4950 && ballPosition.distance(ownGoal) > Math.max(1200, carPosition.distance(ownGoal)) && Math.abs(ballPosition.y) > 100)){
        		r.drawLine3d(Color.blue, car.position.toFramework(), boostLocation.toFramework());
	            this.setWeight(Math.pow((100 - car.boost) / 100D, 2) * (b.isFullBoost() ? Math.max(50, Math.abs(Dui.dif(steerBall, angle))) : 0.8));
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
	
	private BoostPad getNearestBoostpad(Vector2 carPosition){
		if(carPosition == null) return null;
    	BoostPad best = null;
    	double bestDistance = 0;    	
    	for(BoostPad b : BoostManager.getSmallBoosts()){
			Vector2 pos = b.getLocation().flatten();
			double dist = carPosition.distance(pos);
    		if(b.isActive() && (best == null || dist < bestDistance)){
    			best = b;
    			bestDistance = dist;
    		}
    	}    	
    	for(BoostPad b : BoostManager.getFullBoosts()){
			Vector2 pos = b.getLocation().flatten();
			double dist = carPosition.distance(pos) / 6;
    		if(b.isActive() && (best == null || dist < bestDistance)){
    			best = b;
    			bestDistance = dist;
    		}
    	}    	
		return best;
	}

}
