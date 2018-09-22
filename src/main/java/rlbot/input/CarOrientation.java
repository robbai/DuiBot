package rlbot.input;

import rlbot.flat.PlayerInfo;
import rlbot.obj.Vector3;

public class CarOrientation {
	
	private static double pitch = 0, yaw = 0, roll = 0;

	public Vector3 noseVector;
    public Vector3 roofVector;
    public Vector3 rightVector;

    public CarOrientation(Vector3 noseVector, Vector3 roofVector){
        this.noseVector = noseVector;
        this.roofVector = roofVector;
        this.rightVector = noseVector.crossProduct(roofVector);
    }

    public static CarOrientation fromFlatbuffer(PlayerInfo playerInfo){
        return convert(
                playerInfo.physics().rotation().pitch(),
                playerInfo.physics().rotation().yaw(),
                playerInfo.physics().rotation().roll());
    }

    /**
     * All params are in radians.
     */
    private static CarOrientation convert(double ipitch, double iyaw, double iroll){
    	pitch = ipitch;
    	yaw = iyaw;
    	roll = iroll;

        double noseX = -1 * Math.cos(pitch) * Math.cos(yaw);
        double noseY = Math.cos(pitch) * Math.sin(yaw);
        double noseZ = Math.sin(pitch);

        double roofX = Math.cos(roll) * Math.sin(pitch) * Math.cos(yaw) + Math.sin(roll) * Math.sin(yaw);
        double roofY = Math.cos(yaw) * Math.sin(roll) - Math.cos(roll) * Math.sin(pitch) * Math.sin(yaw);
        double roofZ = Math.cos(roll) * Math.cos(pitch);

        return new CarOrientation(new Vector3(noseX, noseY, noseZ), new Vector3(roofX, roofY, roofZ));
    }
    
    public double getPitch(){
		return pitch;
	}

	public double getYaw(){
		return yaw;
	}

	public double getRoll(){
		return roll;
	}
	
}
