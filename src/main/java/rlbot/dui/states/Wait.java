package rlbot.dui.states;

import java.awt.Color;

import rlbot.dui.State;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.obj.Vector2;
import rlbot.obj.Vector3;
import rlbot.render.Renderer;

public class Wait extends State {
	
	//Waits under the position of the ball when it gets high

	public Wait() {
		super("Wait");
	}

	@Override
	public double getOutput(DataPacket input, Vector3 ballPosition3, Vector2 ballPosition, CarData car, Vector2 carPosition, Vector2 carDirection, Vector2 ownGoal, Vector2 enemyGoal, double ballDistance, double ownGoalDistance, double steerBall, double steerEnemyGoal, Renderer r){
		final int fps = 10;
		Vector3 vel = input.ball.velocity.scaled(1D / (double)fps);
		
		if(!vel.isZero() && (ballPosition3.z > 500 || Math.abs(input.ball.velocity.z) > 800)){
			Vector3 last = ballPosition3.clone();
			for(int i = 0; i < 10 * fps; i++){
				
				vel.z -= Math.min(650D / (double)fps, last.z);
				if(last.z > 64){
					vel.x -= (vel.x * 0.03D * (1D / (double)fps));
					vel.y -= (vel.y * 0.03D * (1D / (double)fps));
				}else{
					break;
//					vel.z *= 0.75;
				}
				
				if(Math.abs(last.x) > 4096){
					vel.x = -vel.x;
					last.y = Math.max(-4095, Math.min(4095, last.y));
				}
				
				if(Math.abs(last.y) > 5120){
					vel.y = -vel.y;
					last.y = Math.max(-5119, Math.min(5119, last.y));
				}
				
				Vector3 latest = last.clone().plus(vel);
				r.drawLine3d(Color.yellow, last.toFramework(), latest.toFramework());
				last = latest;
			}
			
			this.setWeight(4000 / (1 + Math.abs(ballPosition3.x)) * 0.01D);
			return Math.toDegrees(carDirection.correctionAngle(last.flatten().minus(carPosition)));			
		}
		
		this.setWeight(0);
    	return 0;
	}

}
