package rlbot.dui;

import java.awt.Color;

import rlbot.dui.states.WallState;
import rlbot.obj.Vector3;
import rlbot.output.ControlsOutput;

public class DuiAerial {
	
	private Dui dui;
	public boolean enabled; 
	
	public DuiAerial(Dui dui, boolean enabled) {
		super();
		this.dui = dui;
		this.enabled = enabled;
	}

	/**Angle multiplier for pitch, yaw, and roll*/private final float[] angleHarshness = new float[] {1.2F, 0.6F, 0.1F};
	
	public Vector3 target;
	public double steerTarget = 0; 
	
	/**Telling whether we are going for an aerial*/public boolean aerialing = false;
	/**Timer for where we are in the aerial*/public long timer = 0L;
	
	private final double rightAngleRadians = Math.PI / 2;
	private final double fullCircleRadians = Math.PI * 2;
	private final double pitchMax = 0.925D;
	
	public boolean shouldStartAerial(DuiData d, boolean setup){
		if(!enabled) return false;
		return d.car.boost * 95D >= d.ballDistance && !WallState.isOnWall(d.car) && d.ballDistance < d.ballPosition3.z * (setup ? 7D : 6D) && d.ballDistance > d.ballPosition3.z * 1.5D && (setup || (Math.abs(steerTarget) < 10D / (d.ballDistance / 500D) && Math.abs(dui.lastSteeringFrame) <= 0.02F)) && d.car.hasWheelContact && (d.ballPosition3.z > (d.input.ball.velocity.isZero() ? 220 : 700) || Math.abs(d.input.ball.velocity.z) > 530);
	}
	
	public boolean shouldContinueAerial(DuiData d){
		return (!d.car.hasWheelContact && d.car.boost * 85D >= d.ballDistance) || (System.currentTimeMillis() - timer) < 800;
	}

	/**Pitch, yaw, roll*/
	public Vector3 getAngle(DuiData d){
		Vector3 ballDifference = target.minus(d.car.position);		
		d.r.drawLine3d(Color.white, d.car.position.toFramework(), target.withZ(d.car.position.z).toFramework());
		final double pitch = pitch(ballDifference, d);
		final double yaw = yaw(ballDifference, d);		
	    return new Vector3(pitch, yaw, -d.car.orientation.getRoll());
	}
	
	public double yaw(Vector3 ballDifference, DuiData d){
	    final double yaw = Math.atan2(ballDifference.x, ballDifference.y);
		d.r.drawLine3d(Color.yellow, target.withZ(d.car.position.z).toFramework(), d.car.position.withX(target.z).toFramework());
		d.r.drawLine3d(Color.yellow, d.car.position.toFramework(), d.car.position.withX(target.z).toFramework());		
		double correction = (yaw - (d.car.orientation.getYaw() - rightAngleRadians)) % fullCircleRadians;
		if(correction > Math.PI) correction = -Math.PI + (correction - Math.PI);
		if(correction < -Math.PI) correction = Math.PI - (-correction - Math.PI);
	    return correction;
	}

	public double pitch(Vector3 ballDifference, DuiData d){ 
		final double pitch = -Math.atan2(d.ballDistance, ballDifference.z) + rightAngleRadians;
		d.r.drawLine3d(Color.pink, d.car.position.toFramework(), target.toFramework());
		d.r.drawLine3d(Color.pink, target.toFramework(), target.withZ(d.car.position.z).toFramework());
		return (pitch - d.car.orientation.getPitch());
	}

	public ControlsOutput controller(DuiData d){
		final long timerChange = (System.currentTimeMillis() - timer);
		d.r.drawCenteredRectangle3d(Color.white, target.toFramework(), 28, 28, false);
		
		ControlsOutput control = new ControlsOutput().withThrottle(1F);
		
		if(timerChange < 200){
//			System.out.print("Jump");
			control = control.withJump(System.currentTimeMillis() % 100 > 50);
		}else if(timerChange < 350 + Math.min(400, 3000D / d.ballDistance)){
//			System.out.print("Tip");
			control = control.withJump(false).withPitch(1F).withBoost(d.car.orientation.getPitch() > 0.9);
		}else{
			Vector3 ang = getAngle(d);
//			System.out.print(ang.toString());
			
        	d.r.drawString3d(ang.toString(), Color.white, d.input.ball.position.toFramework(), 2, 2);
        	d.r.drawString3d((d.car.velocity.magnitude() * 100D) / 100D + "", Color.white, d.car.position.toFramework(), 2, 2);
        	
        	ang.x = ang.x * angleHarshness[0];
        	if(d.ballPosition3.z + 750D > d.car.position.z){
        		ang.x += rightAngleRadians * Math.min(1, (d.ballPosition3.z - d.car.position.z) / 570D);
        	}else if(ang.x < 0){
        		ang.x = Math.max(ang.x, 150D / (0.5D * d.ballDistance));
        	}
        	if(d.car.orientation.getPitch() + ang.x >= pitchMax) ang.x = (pitchMax - d.car.orientation.getPitch());
        	
        	ang.y = ang.y * angleHarshness[1];
        	if(Math.abs(ang.y) <= 0.1D) ang.y = 0;
        	
        	ang.z = ang.z * angleHarshness[2];
        	
			control = control.withPitch((float)ang.x).withYaw((float)ang.y).withRoll((float)ang.z).withBoost(Math.abs(ang.x) < 0.9D);
		}
		
		return control;
	}
	
	public void setTarget(DuiData d){
		if(d.input.ball.velocity.isZero()){
			target = d.ballPosition3;
		}else{
			Vector3 ballDifference = d.ballPosition3.minus(d.car.position);
			double seconds = (ballDifference.magnitude() / Math.min(2300, 1400 + d.car.velocity.magnitude()));
			if(d.car.hasWheelContact) seconds += Math.abs(d.steerBall) / 150D;
			target = dui.duiPrediction.ballAfterSeconds(seconds);
		}	
		
		steerTarget = Math.toDegrees(d.carDirection.correctionAngle(target.flatten().minus(d.carPosition)));
	}

}
