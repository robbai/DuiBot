package rlbot.dui;

import java.awt.Color;

import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.Vector2;
import rlbot.obj.Vector3;
import rlbot.render.Renderer;

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
	
	public abstract double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, Vector2 ownGoal, Vector2 enemyGoal, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r); 

	public String toString(final double divisor) {
		return name + " [" + Dui.r(getWeight() / divisor) + "]";
	}

}
