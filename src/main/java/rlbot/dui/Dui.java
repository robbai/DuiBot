package rlbot.dui;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import rlbot.Bot;
import rlbot.ControllerState;
import rlbot.boost.BoostManager;
import rlbot.dui.states.*;
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
	private final int steerThreshold = 14;

	/**The list of states for Dui to work with*/
    public static ArrayList<State> states = new ArrayList<State>();

    /**The dodge timer used to determine which point of the dodge we are in, and whether we are eligible to dodge*/
    private long dodgeTimer = 0L;

	public static CarData[] cars;

    /**Team index of the current car*/
    public static int team = -10;

    /**Dui's home*/public static Vector2 ownGoal;
    /**Dui's target*/public static Vector2 enemyGoal;
    
    public static float lastSteeringFrame = 0F;

    public Dui(int playerIndex){
        this.playerIndex = playerIndex;           
        
        //Initialise all of the states
        new AttackState();
        new KickoffState();
        new BoostState();
        new DefendState();
        new ReturnState();
        new InterceptState();
        new WallState();
        new CentraliseState();
        new ChaseState();
        new DemoState();
        
        new TestState();
    }

    private ControlsOutput processInput(DataPacket input){
    	
    	//Get a renderer to show graphics for Dui
        Renderer r = BotLoopRenderer.forBotLoop(this);
    	
        //Set the goal vectors if not already set correctly
        if(team != input.car.team){
        	ownGoal = new Vector2(0, input.car.team == 0 ? -5120 : 5120);
            enemyGoal = new Vector2(0, input.car.team == 0 ? 5120 : -5120);
            team = input.car.team;
        }
        
        //Make our own little data file so we don't have to recalculate angles and such
    	DuiData d = new DuiData(input, r);
    	
    	//Prediction updating
        DuiPrediction.update(input.ball, r);
        if(DuiPrediction.isDanger()){
        	r.drawString3d("!", Color.red, d.car.position.plus(new Vector3(0, 0, 150)).toFramework(), 4, 4);
        }else if(DuiPrediction.isNice()){
        	r.drawString3d(":)", Color.green, d.car.position.plus(new Vector3(0, 0, 150)).toFramework(), 4, 4);
        }
    	
    	//Aerialing
    	if(!DuiAerial.aerialing){
    		DuiAerial.setTarget(d);
    		boolean start = DuiAerial.shouldStartAerial(d, false);
    		if(start){
    			DuiAerial.aerialing = true;
    			DuiAerial.timer = System.currentTimeMillis();
    		}
    	}else{
    		DuiAerial.aerialing = DuiAerial.shouldContinueAerial(d);
    	}
    	if(DuiAerial.aerialing){
    		System.out.print("Aerialing: "); //Start the line
        	
    		final int size = 1;
    		final int y = 10;
    	    r.drawString2d("Pitch: " + r(d.car.orientation.getPitch()), Color.cyan, new Point(0, 0), size, size);
            r.drawString2d("Yaw: " + r(d.car.orientation.getYaw()), Color.cyan, new Point(0, y), size, size);
            r.drawString2d("Roll: " + r(d.car.orientation.getRoll()), Color.cyan, new Point(0, y * 2), size, size);
            r.drawString2d("Nose: " + d.car.orientation.noseVector.toString(), Color.cyan, new Point(0, y * 3), size, size);
            r.drawString2d("Right: " + d.car.orientation.rightVector.toString(), Color.cyan, new Point(0, y * 4), size, size);
            r.drawString2d("Roof: " + d.car.orientation.roofVector.toString(), Color.cyan, new Point(0, y * 5), size, size);
            	                  
    		ControlsOutput a = DuiAerial.controller(d);
    		System.out.println(); //End the line
    		return a;
    	}
    	
    	//Game state setting
//    	if(KickoffState.isKickoff(d.input.ball)){
//	    	GameState s = new GameState().withBallState(new BallState().withPhysics(new PhysicsState().withVelocity(new DesiredVector3(0F, 0F, 1000F))));
//	    	RLBotDll.setGameState(s.buildPacket());
//    	}
    	
//	    GameState gameState = new GameState().withBallState(new BallState().withPhysics(new PhysicsState().withVelocity(new DesiredVector3(0F, 0F, 0F)).withLocation(new DesiredVector3(-1000F, -1000 + (float)(System.currentTimeMillis() % 10000) / 5, (float)(1000F + 500F * Math.sin((double)System.currentTimeMillis() / 1000D))))));	        
//	    Vector3 face = DuiAerial.getAngle(d);	        
//	    //(System.currentTimeMillis() % 10000) / 1000F
//	    gameState = gameState.withCarState(playerIndex, new CarState().withPhysics(new PhysicsState().withLocation(new DesiredVector3(0F, 0F, 1000F)).withVelocity(new DesiredVector3(0F, 0F, 0F))
//	      		.withRotation(new DesiredRotation((float)(face.x + d.car.orientation.getPitch()), (float)(face.y + d.car.orientation.getYaw()), 0F))));	        
//	    RLBotDll.setGameState(gameState.buildPacket());
//	    return new ControlsOutput().withSteer(0F).withBoost(false).withJump(false);   
        
        //Start printing, deal with the weights and the decided angle
        System.out.print(this.playerIndex + ": ");
        double totalWeight = 0, greatestWeight = 1;
        double[] angles = new double[states.size()];
        double chosen = 0;
          
        cars = input.cars;
        
        final long timerChange = System.currentTimeMillis() - dodgeTimer;

        if(timerChange > 1150 || timerChange < 220 || WallState.isOnWall(d.car)){
	        //Go through each state to calculate its output and weight, and to determine the total weight and greatest weight
	        for(byte i = 0; i < states.size(); i++){
	        	State s = states.get(i);
	        	angles[i] = s.getOutput(d);
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
        float throttle = d.ballDistance > 400 || WallState.isOnWall(d.car) ? 1 : Math.max(0F, (float)(1F - (d.ballPosition3.z - 94) / 90F));
        throttle *= throttle; //Square up
        
        //Controller to send at the end
        ControlsOutput control = new ControlsOutput().withSteer(steer).withThrottle(throttle).withSlide(Math.abs(chosen) > 76 && d.car.position.z < 70);
        
        //Boosting is determined by how little we are turning, whether are are on the ground, and whether we are wanting to go fast
        boolean boost = d.car.hasWheelContact && (kickoff || (Math.abs(steer) < 0.25 && control.getThrottle() > 0.8 && (dif(d.steerBall, d.steerEnemyGoal) < 10 || d.ballDistance > 120) && timerChange > 1700 && !WallState.isOnWall(d.car)));
        control = control.withBoost(boost);
        if(boost) System.out.print("Zoom & ");

        //Dealing with whether we should dodge
        boolean dodge; 
        if(kickoff){
        	dodge = d.car.boost < 4; //Dodge earlier in a kickoff than normal
        }else{
        	if(d.car.boost < 10){
        		dodge = Math.abs(steer) < (d.ballDistance < 320 ? 0.3 : 0.2) && ((d.ballDistance > 1100 && d.car.velocity.magnitude() > 400) || (d.ballDistance < 190 && dif(d.steerBall, d.steerEnemyGoal) < 32));
        	}else{
        		dodge = (d.ballDistance < 700 && Math.abs(d.ballPosition3.z) < 160 && (DuiPrediction.isDanger() || dif(d.steerBall, d.steerEnemyGoal) < 26 || input.ball.velocity.magnitude() + 500 < d.car.velocity.magnitude()));
        	}
        }
        System.out.print(dodge ? "Dodge" : "Go");

        //Dealing with the actual process of dodging (where we are in the action of dodging)
        if(dodge || timerChange <= 1000){
        	if(kickoff){
        		control = dodge(control, (float)(d.steerBall * 2.3F), timerChange);
        	}else{
        		control = dodge(control, (float)(d.ballDistance > 1400 ? chosen : d.steerBall), timerChange);
        	}
        }
        
        //Car correction when we're falling
        if(!d.car.hasWheelContact && !boost && (d.car.position.z > 220 || d.car.velocity.z > 650 || d.car.velocity.z < -175)){
        	r.drawString3d("Correcting...", Color.gray, d.car.position.toFramework(), 2, 2);
        	control = control.withYaw((float)DuiAerial.yaw(d.ballPosition3.minus(input.car.position), d) / 10F);
        	control = control.withRoll((float)((d.car.orientation.getRoll() > 0 ? -1 : 1) * Math.log(1 + Math.abs(d.car.orientation.getRoll()))));
        	control = control.withPitch((float)((d.car.orientation.getPitch() > 0 ? -1 : 1) * Math.log(1 + Math.abs(d.car.orientation.getPitch()))));
        }

        lastSteeringFrame = steer;
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

    /**Return controls for dodging purposes*/
	private ControlsOutput dodge(ControlsOutput control, float steer, long timerChange){
		float angle = (steer / 90F);
    	if(timerChange > 2200){
    		dodgeTimer = System.currentTimeMillis(); //We can now dodge again (recharged)
    	}else if(timerChange <= 100){
    		control.withJump(true); //Jump
    		control.withPitch(-1 + Math.abs(angle)); //Tip forward
    	}else if(timerChange <= 250){
    		control.withJump(false); //Stop jumping
    		control.withPitch(-1 + Math.abs(angle)); //Keep tipping
    	}else if(timerChange <= 1100){
    		control.withJump(true); //Dodge
    		
    		//Still keep tipping
    		double w = 1D - (double)(timerChange - 150) / 750D;
    		control.withPitch((float)(((-1 + Math.abs(angle)) * w + -1) / (1 + w))); 
//    		control.withPitch(-1);
    		
    		control.withYaw(-angle); //Point the way we intend to go
    	}
    	return control;
	}
    
}
