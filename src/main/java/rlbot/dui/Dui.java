package rlbot.dui;

import java.awt.Color;
import java.util.ArrayList;

import rlbot.Bot;
import rlbot.ControllerState;
import rlbot.boost.BoostManager;
import rlbot.dui.states.AttackState;
import rlbot.dui.states.BoostState;
import rlbot.dui.states.CentraliseState;
import rlbot.dui.states.DefendState;
import rlbot.dui.states.KickoffState;
import rlbot.dui.states.ReturnState;
import rlbot.dui.states.WallState;
import rlbot.flat.GameTickPacket;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.manager.BotLoopRenderer;
import rlbot.obj.Vector2;
import rlbot.obj.Vector3;
import rlbot.output.ControlsOutput;
import rlbot.render.Renderer;

public class Dui implements Bot {

	// https://github.com/RLBot/RLBot/wiki/Useful-Game-Values
	// https://github.com/RLBot/RLBot/blob/master/src/main/flatbuffers/rlbot.fbs	

    private int playerIndex;

	/**The threshold at which the car should make smoother turns in degrees, degree turns below this will not be jerky*/
	private final int steerThreshold = 16;

	/**The list of states for Dui to work with*/
    public static ArrayList<State> states = new ArrayList<State>();

    /**The dodge timer used to determine which point of the dodge we are in, and whether we are eligible to dodge*/
    private long dodgeTimer = 0L;

    /**Team index of the current car*/
    private int team = -10;

    /**Dui's home*/public static Vector2 ownGoal;
    /**Dui's target*/public static Vector2 enemyGoal;

    public Dui(int playerIndex){
        this.playerIndex = playerIndex;           
        
        //Initialise all of the states
        new AttackState();
        new KickoffState();
        new BoostState();
        new DefendState();
        new ReturnState();
        new WallState();
        new CentraliseState();
    }

    private ControlsOutput processInput(DataPacket input){
    	
    	//Get the car (very useful!)
        final CarData car = input.car;
        
        //Set the goal vectors if not already set correctly
        if(team != car.team){
        	ownGoal = new Vector2(0, car.team == 0 ? -5120 : 5120);
            enemyGoal = new Vector2(0, car.team == 0 ? 5120 : -5120);
            team = car.team;
        }

        //Get vectors
    	final Vector3 ballPosition3 = input.ball.position;
        final Vector2 ballPosition = ballPosition3.flatten();        
        final Vector2 carPosition = car.position.flatten();
        final Vector2 carDirection = car.orientation.noseVector.flatten();
                
        //Get distances in unreal units
        final double ballDistance = carPosition.distance(ballPosition);
        final double ownGoalDistance = carPosition.distance(ownGoal);
        
        //Get degrees to turn to certain vectors from the car
        final double steerBall = Math.toDegrees(carDirection.correctionAngle(ballPosition.minus(carPosition)));
        final double steerEnemyGoal = Math.toDegrees(carDirection.correctionAngle(enemyGoal.minus(carPosition)));
        
        //Start printing, deal with the weights and the decided angle
        System.out.print(this.playerIndex + ": ");
        double totalWeight = 0, greatestWeight = 1;
        double[] angles = new double[states.size()];
        double chosen = 0;
        
        //Get a renderer to show graphics for Dui
        Renderer r = BotLoopRenderer.forBotLoop(this);
//      r.drawString3d(r(ballPosition3.z) + "", Color.white, ballPosition3.toFramework(), 2, 2);
        
        //Prediction updating
        DuiPrediction.update(input.ball, r);
        
        final long timerChange = System.currentTimeMillis() - dodgeTimer;

        if(timerChange > 1250 || timerChange < 200 || WallState.isOnWall(car)){
	        //Go through each state to calculate its output and weight, and to determine the total weight and greatest weight
	        for(byte i = 0; i < states.size(); i++){
	        	State s = states.get(i);
	        	angles[i] = s.getOutput(input, ballPosition3, ballPosition, car, carPosition, carDirection, ballDistance, ownGoalDistance, steerBall, steerEnemyGoal, r);
	        	if(s.getWeight() > 0) totalWeight += s.getWeight(); //Negative weights should be ignored
	        	greatestWeight = Math.max(greatestWeight, s.getWeight());
	        }
	        
	        //Go through all the states again in order to determine which way Dui should steer
	        for(byte i = 0; i < states.size(); i++){
	        	State s = states.get(i);
	        	if(r(s.getWeight() / greatestWeight) >= 0.01){ //Negative/negligible weights should be ignored
	        		chosen += (angles[i] * ((s.getWeight() / greatestWeight) / (totalWeight / greatestWeight)));
	        		System.out.print(s.toString(greatestWeight) + ", "); //Print useful weighted outputs
	        	}
	        }      
        }else{
        	System.out.print("Dodging = ");
        }
        
        //Steering output, determined by what the weights have given us
        final float steer = ((chosen > 0 ? -1 : 1)) * (float)Math.min((1D / steerThreshold) * Math.abs(chosen), 1D);
        
        final boolean kickoff = KickoffState.isKickoff(input.ball);
        
        //Speed for Dui to travel at
        float throttle = ballDistance > 400 || WallState.isOnWall(car) ? 1 : Math.max(0F, (float)(1F - Math.max(ballPosition3.z - 94, input.ball.velocity.z / 3) / 90F));
        throttle *= throttle;
        
        //Controller to send at the end
        ControlsOutput control = new ControlsOutput().withSteer(steer).withThrottle(throttle).withSlide(Math.abs(chosen) > 94 && car.position.z < 80);
        
        //Boosting is determined by how little we are turning, whether are are on the ground, and whether we are wanting to go fast
        boolean boost = kickoff || (Math.abs(steer) < 0.1 && car.hasWheelContact && control.getThrottle() > 0.85 && (!car.isSupersonic || dif(steerBall, steerEnemyGoal) < 36 || ballDistance > 2500) && timerChange > 2000);
        control = control.withBoost(boost);
        if(boost) System.out.print("Zoom & ");

        //Dealing with whether we should dodge
        boolean dodge; 
        if(kickoff){
        	dodge = ballDistance < 1800; //Dodge earlier in a kickoff than normal
        }else{
//        	dodge = (((ballDistance > 3000 && car.velocity.magnitude() > 600) || (ballDistance < 350 && ballPosition3.z < 220) && Math.min(Math.abs(steerBall), Math.abs(steer)) < 18) && car.position.z < 140);
        	dodge = (Math.abs(steer) < 0.1 && car.boost < 40 && ballDistance > 4000 && car.position.z < 60 && car.velocity.magnitude() > 1000) || (ballDistance < 480 && Math.abs(ballPosition3.z) < 130 && dif(steerBall, steerEnemyGoal) < 30);
        }
        System.out.print(dodge ? "Dodge" : "Go");

        //Dealing with the actual process of dodging (where we are in the action of dodging)
        if(dodge || (System.currentTimeMillis() - dodgeTimer) <= 1000){
        	control = dodge(control, (float)(ballDistance > 1600 ? chosen : steerBall), timerChange);
        }
        
        if(!car.hasWheelContact && (car.position.z > 240 || car.velocity.z > 900 || car.velocity.z < -250)){
        	r.drawString3d("Correcting...", Color.gray, car.position.toFramework(), 2, 2);
        	control = control.withYaw((float)((car.orientation.getYaw() > 0 ? -1 : 1) * car.orientation.getYaw() / 5F));
        	control = control.withRoll((float)((car.orientation.getRoll() > 0 ? -1 : 1) * car.orientation.getRoll() / 5F));
        	control = control.withPitch((float)((car.orientation.getPitch() > 0 ? -1 : 1) * car.orientation.getPitch() / 5F));
        }

        System.out.println(); //End the line we've been printing to
        return control;
    }

	@Override
    public int getIndex(){
        return this.playerIndex;
    }

    @Override
    public ControllerState processInput(GameTickPacket packet){
        if(packet.playersLength() <= playerIndex || packet.ball() == null) return new ControlsOutput();
        BoostManager.loadGameTickPacket(packet);
        DataPacket dataPacket = new DataPacket(packet, playerIndex);
        return processInput(dataPacket);
    }

    public void retire(){
        System.out.println("Retiring Dui (" + playerIndex + ")");
    }
    
    /**Rounds to two decimal places*/
    public static double r(double r){
    	return Math.round(r * 100D) / 100D;
    }
    
    /**Returns the difference between two values*/
    public static double dif(double one, double two){
    	return Math.max(one, two) - Math.min(one, two);
    }

    /**Return controls for dodging purposes
     * @param timerChange */
	private ControlsOutput dodge(ControlsOutput control, float steer, long timerChange){
		float angle = (steer / 90F);
    	if(timerChange > 2200){
    		dodgeTimer = System.currentTimeMillis(); //We can now dodge again (recharged)
    	}else if(timerChange <= 100){
    		control.withJump(true); //Jump
    		control.withPitch(-1 + Math.abs(angle)); //Tip forward
    	}else if(timerChange <= 150){
    		control.withJump(false); //Stop jumping
    		control.withPitch(-1 + Math.abs(angle)); //Keep tipping
    	}else if(timerChange <= 1000){
    		control.withJump(true); //Dodge
    		control.withPitch(-1 + Math.abs(angle)); //Still keep tipping
    		control.withRoll(-angle); //Point the way we intend to go
    	}
    	return control;
	}
    
}
