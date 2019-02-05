package rlbot.dui.states;

import java.awt.Color;

import rlbot.boost.BoostManager;
import rlbot.boost.BoostPad;
import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.State;
import rlbot.obj.Vector2;

public class BoostState extends State {
	
	//State which has Dui go collect boost when the ball is not too relevant to either net

	public BoostState(Dui dui){
		super(dui, "Boost", Color.blue);
	}

	@Override
	public double getOutput(DuiData d){ 		
		BoostPad b = getNearestBoostpad(d.ballPosition, d.ballDistance);
        if(b != null && !dui.duiPrediction.isDanger() && !KickoffState.isKickoff(d.input.ball) && d.car.boost < 30){
        	Vector2 boostLocation = b.getLocation().flatten();
        	double boostDistance = d.carPosition.distance(boostLocation);
        	double angle = Math.toDegrees(d.carDirection.correctionAngle(boostLocation.minus(d.carPosition)));
        	if((Math.abs(angle) < 30 && boostDistance < d.ballDistance) || (d.ballDistance > 4000 && Math.abs(d.carPosition.y) < 5120 && d.ballPosition.distance(dui.ownGoal) > Math.max(1300, d.carPosition.distance(dui.ownGoal)) && Math.abs(d.ballPosition.y) > 100)){
        		d.r.drawLine3d(colour, d.car.position.toFramework(), boostLocation.toFramework());
	            this.setWeight(Math.pow((100D - d.car.boost) / 100D, 2) * Math.max(40, 2D * Math.abs(dui.dif(d.steerBall, angle))));
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
	
	private BoostPad getNearestBoostpad(Vector2 distanceFrom, double bestDistance){
		if(distanceFrom == null) return null;
    	BoostPad best = null;
    	for(BoostPad b : BoostManager.getFullBoosts()){
			Vector2 pos = b.getLocation().flatten();
			double dist = distanceFrom.distance(pos);
    		if(b.isActive() && dist < bestDistance){
    			best = b;
    			bestDistance = dist;
    		}
    	}    	
		return best;
	}

}
