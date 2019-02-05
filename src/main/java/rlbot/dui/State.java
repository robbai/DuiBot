package rlbot.dui;

import java.awt.Color;

public abstract class State {
	
	public Dui dui;
	private String name;
	public Color colour;
	private double weight;
	
	public State(Dui dui, String name, Color colour) {
		super();
		this.dui = dui;
		this.name = name;
		this.setWeight(0);
		this.colour = colour;
		dui.states.add(this);
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
		return name + " [" + dui.r(getWeight() / divisor) + "]";
	}

}
