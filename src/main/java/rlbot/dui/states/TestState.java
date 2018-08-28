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

public class TestState extends State {
	
	//State which is for testing purposes
	
	private static final int threshold = 350;

	public TestState(){
		super("Test", Color.black);
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){
		if(!DuiPrediction.isNice() && !DuiPrediction.isDanger()){
			if(Dui.team == 0 ? (carPosition.y + threshold > ballPosition.y) : (carPosition.y - threshold < ballPosition.y)){
				Vector2 target = DuiPrediction.ballAfterSeconds(ballDistance / (double)Math.min(2300, 300 + car.velocity.magnitude())).flatten();
//				target = target.plus(Dui.ownGoal.minus(target).normalised().scaled(ballDistance / 50));
				r.drawLine3d(colour, carPosition.toFramework(), target.toFramework());
				r.drawCenteredRectangle3d(colour, target.toFramework(), 20, 20, false);
				this.setWeight(2);
				return Math.toDegrees(carDirection.correctionAngle(target.minus(carPosition)));
			}
		}
		this.setWeight(0);
		return 0;
	}

}
