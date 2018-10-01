package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.DuiAerial;
import rlbot.dui.DuiData;
import rlbot.dui.State;
import rlbot.obj.Vector3;

public class TakeoffState extends State {
	
	//State which is for aiming the car while on the ground to prepare for an aerial

	public TakeoffState(){
		super("Takeoff", Color.GRAY);
	}

	@Override
	public double getOutput(DuiData d){
		if(!d.car.hasWheelContact || !DuiAerial.enabled){
			this.setWeight(0);
			return 0;
		}else{
			if(DuiAerial.target == null) DuiAerial.setTarget(d);
			
			if(DuiAerial.shouldStartAerial(d, true)){
				this.setWeight(5000);
				
				for(byte i = 0; i < 1; i++){
					Vector3 target = (i == 1 ? DuiAerial.target : d.ballPosition3);
					d.r.drawLine3d(this.colour, d.car.position.toFramework(), target.toFramework()); //Hypotenuse
					d.r.drawLine3d(this.colour, target.withZ(d.car.position.z).toFramework(), target.toFramework()); //Opposite
					d.r.drawLine3d(this.colour, target.withZ(d.car.position.z).toFramework(), d.car.position.toFramework()); //Adjacent
				}
				
				return DuiAerial.steerTarget;
			}else{
				this.setWeight(0);
				return 0;
			}		
		
		}
	}

}
