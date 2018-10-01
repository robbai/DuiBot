package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.DuiPrediction;
import rlbot.dui.State;

public class TestState2 extends State {
	
	//State which is for testing purposes
	
	private static final int angle = 150;

	public TestState2(){
		super("Test", Color.black);
	}

	@Override
	public double getOutput(DuiData d){
		if(DuiPrediction.isNice() && ((Dui.team == 0 ? d.carPosition.y > d.ballPosition.y : d.carPosition.y < d.ballPosition.y) || Dui.dif(d.steerBall, d.steerEnemyGoal) > angle)){
			d.r.drawString3d("Woops", this.colour, d.car.position.toFramework(), 2, 2);
			this.setWeight(1000);
			return -d.steerBall;
		}
		this.setWeight(0);
		return 0;
	}

}
