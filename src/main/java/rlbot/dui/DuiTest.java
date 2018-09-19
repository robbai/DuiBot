package rlbot.dui;

import rlbot.obj.Vector3;

/**
 * For this whole class: send my thanks to Chip and Kip
*/

public class DuiTest {
	
	private final static Vector3 vectorOne = new Vector3(1, 1, 1);
	private final static float ALPHA_MAX = 9F;

	public static Vector3 AerialRollPitchYaw(Vector3 omega_start, Vector3 omega_end, Matrix theta_start, float dt){
        final float T_r = -36.07956616966136F; // torque coefficient for roll
        final float T_p = -12.14599781908070F; // torque coefficient for pitch
        final float T_y = 8.91962804287785F; // torque coefficient for yaw
        final float D_r = -4.47166302201591F; // drag coefficient for roll
        final float D_p = -2.798194258050845F; // drag coefficient for pitch
        final float D_y = -1.886491900437232F; // drag coefficient for yaw

        // net torque in world coordinates
        Vector3 tau = omega_end.minus(omega_start).scaled(1F / dt);

        // net torque in local coordinates
        tau = theta_start.transpose().multiply(tau);

        // beginning-step angular velocity, in local coordinates
        Vector3 omega_local = theta_start.transpose().multiply(omega_start);

        Vector3 rhs = new Vector3(tau.x - D_r * omega_local.x, tau.y - D_p * omega_local.y, tau.z - D_y * omega_local.z);

        // user inputs: roll, pitch, yaw
        Vector3 u = new Vector3(rhs.x / T_r, rhs.y / (T_p + Math.signum(rhs.y) * omega_local.y * D_p), rhs.z / (T_y - Math.signum(rhs.z) * omega_local.z * D_y));

        // ensure that values are between -1 and +1 
        u.x = clamp(u.x, -1, 1);
        u.y = clamp(u.y, -1, 1);
        u.z = clamp(u.z, -1, 1);

        return u;
    }

	public static Vector3 Step(Matrix theta, Matrix target, Vector3 angularVelocity, float dt){
        Matrix relativeRotation = theta.transpose().times(target);
        Vector3 geodesicLocal = relativeRotation.toRotationAxis();

        // figure out the axis of minimal rotation to target
        Vector3 geodesicWorld = theta.multiply(geodesicLocal);

        // get the angular acceleration
        Vector3 alpha = new Vector3(Controller(geodesicWorld.x, angularVelocity.x, dt), Controller(geodesicWorld.y, angularVelocity.y, dt), Controller(geodesicWorld.z, angularVelocity.z, dt));

        // reduce the corrections for when the solution is nearly converged
        Vector3 error = geodesicWorld.abs().plus(angularVelocity.abs());
        alpha = q(error).multiply(alpha);

        // set the desired next angular velocity
        Vector3 omega_next = angularVelocity.plus(alpha.scaled(dt));

        // determine the controls that produce that angular velocity
        Vector3 rollPitchYaw = AerialRollPitchYaw(angularVelocity, omega_next, theta, dt);

        return rollPitchYaw;
    }

    private static float Controller(float delta, float v, float dt){
        float ri = r(delta, v);

        float alpha = Math.signum(ri) * ALPHA_MAX;

        float rf = r(delta - v * dt, v + alpha * dt);

        // use a single step of secant method to improve
        // the acceleration when residual changes sign
        if(ri * rf < 0) alpha *= (2F * (ri / (ri - rf)) - 1);

        return alpha;
    }

    private static float r(float delta, float v){
        return delta - 0.5f * Math.signum(v) * v * v / ALPHA_MAX;
    }

    private static Vector3 q(Vector3 x){
        return vectorOne.minus(vectorOne.divide(vectorOne.plus(x.multiply(x).scaled(500D))));
    }
    
    private static double clamp(double x, double i, double j){
		return Math.max(Math.min(i, j), Math.min(x, Math.max(i, j)));
	}
    
    private static float Controller(double delta, double v, float dt){
    	return Controller((float)delta, (float)v, dt);
    }
    
}