package rlbot.dui.states;

import java.awt.Color;

import rlbot.boost.BoostManager;
import rlbot.boost.BoostPad;
import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.DuiPrediction;
import rlbot.dui.State;
import rlbot.obj.Vector2;

public class BoostState extends State {
	
	//State which has Dui go collect boost when the ball is not too relevant to either net

	public BoostState() {
		super("Boost", Color.blue);
	}

	@Override
	public double getOutput(DuiData d){ 		
		BoostPad b = getNearestBoostpad(d.carPosition);
        if(b != null && !DuiPrediction.isDanger() && !KickoffState.isKickoff(d.input.ball)){
        	Vector2 boostLocation = b.getLocation().flatten();
        	double boostDistance = d.carPosition.distance(boostLocation);
        	double angle = Math.toDegrees(d.carDirection.correctionAngle(boostLocation.minus(d.carPosition)));
        	if((Math.abs(angle) < 30 && boostDistance < d.ballDistance) || (d.ballDistance > 4000 && Math.abs(d.carPosition.y) < 5120 && d.ballPosition.distance(Dui.ownGoal) > Math.max(1300, d.carPosition.distance(Dui.ownGoal)) && Math.abs(d.ballPosition.y) > 100)){
        		d.r.drawLine3d(colour, d.car.position.toFramework(), boostLocation.toFramework());
	            this.setWeight(Math.pow((100D - d.car.boost) / 100D, 2) * Math.max(40, 2D * Math.abs(Dui.dif(d.steerBall, angle))));
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
    	for(BoostPad b : BoostManager.getFullBoosts()){
			Vector2 pos = b.getLocation().flatten();
			double dist = carPosition.distance(pos);
    		if(b.isActive() && (best == null || dist < bestDistance)){
    			best = b;
    			bestDistance = dist;
    		}
    	}    	
		return best;
	}

}
