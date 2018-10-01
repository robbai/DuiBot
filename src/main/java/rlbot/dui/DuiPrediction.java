package rlbot.dui;

import java.awt.Color;
import java.io.IOException;

import rlbot.cppinterop.RLBotDll;
import rlbot.flat.BallPrediction;
import rlbot.input.BallData;
import rlbot.obj.Vector3;
import rlbot.render.Renderer;

public class DuiPrediction {
	
	
	/**Determines whether the ball is going into our Dui'shome goal*/private static boolean danger = false;
	/**Determines whether the ball is going into the opponent's goal*/private static boolean nice = false;
	/**The amount of seconds currently predicted for the ball*/private static int furthestCalculated = 0;
	/**The amount of seconds ideally predicted for the ball*/private final static int maxSeconds = 6;
	/**Rendering for the ball prediction*/private final static boolean render = false;
	/**The list of vectors for the ball's path*/private static Vector3[] positions;
	/**FPS for the ball prediction*/private final static int fps = 60;
	
	@SuppressWarnings("unused")
	public static void update(BallData ball, Renderer r){
		danger = false;
		nice = false;
		
		positions = new Vector3[fps * maxSeconds];
		furthestCalculated = 0;
		
		try {
			final BallPrediction ballPrediction = RLBotDll.getBallPrediction();

			for(int i = 0; i < ballPrediction.slicesLength(); i++){
				Vector3 location = Vector3.fromFlatbuffer(ballPrediction.slices(i).physics().location());
				positions[i] = location;
				furthestCalculated = i;
				if(i != 0 && render) r.drawLine3d(Color.WHITE, positions[i - 1].toFramework(), location.toFramework());

				if(Math.abs(positions[i].y) > 5120 + 93){
					danger = (positions[i].y < 0 == (Dui.team == 0));
					nice = (positions[i].y > 0 == (Dui.team == 0));
				}
			}

		}catch(IOException e){
			e.printStackTrace();
		}
		
		if(furthestCalculated == 0) positions[0] = ball.position.clone();
	}
	
	public static Vector3 ballAfterSeconds(double seconds){
		int frame = (int)(Math.max(0, seconds) * 15) * (fps / 15);
		frame = Math.min(frame, furthestCalculated);
		return positions[frame];
	}
	
	public static boolean isDanger(){return danger;}
	public static boolean isNice(){return nice;}

}
