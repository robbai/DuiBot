package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.Dui;
import rlbot.dui.DuiPrediction;
import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.Vector2;
import rlbot.obj.Vector3;
import rlbot.render.Renderer;

public class ChaseState extends State {
	
	//State which is for ballchasing (when necessary!)
	
	private static final int threshold = 350;

	public ChaseState(){
		super("Chase", Color.DARK_GRAY);
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){
		if(!DuiPrediction.isNice() && !DuiPrediction.isDanger()){
			if(Dui.team == 0 ? (carPosition.y + threshold > ballPosition.y) : (carPosition.y - threshold < ballPosition.y)){
				Vector2 target = DuiPrediction.ballAfterSeconds(ballDistance / (double)Math.min(2300, 300 + car.velocity.magnitude())).flatten();
				target = target.plus(Dui.ownGoal.minus(target).normalised().scaled(ballDistance / 100));
				r.drawLine3d(colour, carPosition.toFramework(), target.toFramework());
				this.setWeight(2);
				return Math.toDegrees(carDirection.correctionAngle(target.minus(carPosition)));
			}
		}
		this.setWeight(0);
		return 0;
	}

}
