package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.DuiData;
import rlbot.dui.State;

public class TestState extends State {
	
	//State which is for testing purposes

	public TestState(){
		super("Test", Color.black);
	}

	@Override
	public double getOutput(DuiData d){
		this.setWeight(0);
		return 0;
	}

}
