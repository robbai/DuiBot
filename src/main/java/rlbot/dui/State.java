package rlbot.dui;

import java.awt.Color;

public abstract class State {
	
	private String name;
	public Color colour;
	private double weight;
	
	public State(String name, Color colour) {
		super();
		this.name = name;
		this.setWeight(0);
		this.colour = colour;
		Dui.states.add(this);
	}
	
	public String getName() {
		return name;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public abstract double getOutput(DuiData d); 

	public String toString(final double divisor) {
		return name + " [" + Dui.r(getWeight() / divisor) + "]";
	}

}
