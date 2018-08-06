package rlbot.dui;

import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.*;
import rlbot.render.Renderer;

public abstract class State {
	
	private String name;
	private double weight;
	
	public State(String name) {
		super();
		this.name = name;
		this.setWeight(0);
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
