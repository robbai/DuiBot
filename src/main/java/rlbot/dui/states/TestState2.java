package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiData;
import rlbot.dui.State;

public class TestState2 extends State {
	
	//State which is for testing purposes
	
	private final int angle = 150;

	public TestState2(Dui dui){
		super(dui, "Test", Color.black);
	}

	@Override
	public double getOutput(DuiData d){
		if(dui.duiPrediction.isNice() && ((dui.team == 0 ? d.carPosition.y > d.ballPosition.y : d.carPosition.y < d.ballPosition.y) || dui.dif(d.steerBall, d.steerEnemyGoal) > angle)){
			d.r.drawString3d("Woops", this.colour, d.car.position.toFramework(), 2, 2);
			this.setWeight(1000);
			return -d.steerBall;
		}
		this.setWeight(0);
		return 0;
	}

}
