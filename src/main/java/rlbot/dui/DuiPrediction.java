package rlbot.dui;

import java.awt.Color;

import rlbot.input.BallData;
import rlbot.obj.Vector3;
import rlbot.render.Renderer;

public class DuiPrediction {
	
	private final static int fps = 15;
	private final static int maxSeconds = 6;
	private static int furthestCalculated = 0;
	private static Vector3[] positions;
	
	public static void update(BallData ball, Renderer r){
		positions = new Vector3[fps * maxSeconds];
		positions[0] = ball.position.clone();
		furthestCalculated = 0;
		if(ball.velocity.isZero()) return;
		Vector3 vel = ball.velocity.scaled(1D / (double)fps);
		boolean render = true;
		for(int i = 1; i < maxSeconds * fps; i++){
			furthestCalculated ++;
			if(positions[i - 1].z > 64){
				vel.z -= Math.min(650D / (double)fps, positions[i - 1].z);
				vel.x -= (vel.x * 0.03D * (1D / (double)fps));
				vel.y -= (vel.y * 0.03D * (1D / (double)fps));
			}else{
				vel.z = -(vel.z * 0.75);
			}
			if(Math.abs(positions[i - 1].x) > 4096){
				vel.x = -vel.x;
				positions[i - 1].y = Math.max(-4095, Math.min(4095, positions[i - 1].y));
				render = false;
			}
			if(Math.abs(positions[i - 1].y) > 5120){
				vel.y = -vel.y;
				positions[i - 1].y = Math.max(-5119, Math.min(5119, positions[i - 1].y));
				render = false;
			}
			positions[i] = positions[i - 1].clone().plus(vel);
			if(render) r.drawLine3d(Color.YELLOW, positions[i - 1].toFramework(), positions[i].toFramework());
		}
	}
	
	public static Vector3 ballAfterSeconds(double seconds){
		int frame = (int)(seconds * fps);
		frame = Math.min(frame, Math.min(maxSeconds * fps, furthestCalculated));
		return positions[frame];
	}

}
