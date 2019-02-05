package rlbot.dui;

import java.awt.Color;
import java.util.ArrayList;

import rlbot.Bot;
import rlbot.ControllerState;
import rlbot.DuiJava;
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
	private final int steerThreshold = 24;

	/**The list of states for Dui to work with*/
    public ArrayList<State> states = new ArrayList<State>();

    /**The dodge timer used to determine which point of the dodge we are in, and whether we are eligible to dodge*/
    private long dodgeTimer = 0L;

	public CarData[] cars;

    /**Team index of the current car*/public int team = -10;

    /**Dui's home*/public Vector2 ownGoal;
    /**Dui's target*/public Vector2 enemyGoal;
    
    public float lastSteeringFrame = 0F;

	public DuiPrediction duiPrediction;
	public DuiAerial duiAerial;

    public Dui(int playerIndex){
        this.playerIndex = playerIndex;  
        this.duiPrediction = new DuiPrediction(this);
        this.duiAerial = new DuiAerial(this, false);
        
        //Initialise all of the states
        new AttackState(this);
        new KickoffState(this);
        new BoostState(this);
        new DefendState(this);
        new ReturnState(this);
        new InterceptState(this);
        new WallState(this);
        new CentraliseState(this);
        new ChaseState(this);
        new DemoState(this);
        new TakeoffState(this);
        
        new TestState(this);
        new TestState2(this);
    }

    private ControlsOutput processInput(DataPacket input){
//    	DuiJava.ding(); //Show the user we are ready!
    	
    	//Get a renderer to show graphics for Dui
        Renderer r = BotLoopRenderer.forBotLoop(this);
            	
        //Set the goal vectors if not already set correctly
        if(team != input.car.team){
        	ownGoal = new Vector2(0, input.car.team == 0 ? -5120 : 5120);
            enemyGoal = new Vector2(0, input.car.team == 0 ? 5120 : -5120);
            team = input.car.team;
        }
        
        //Make our own little data file so we don't have to recalculate angles and such
    	DuiData d = new DuiData(this, input, r);
    	
    	//Prediction updating
        duiPrediction.update(input.ball, r);
        if(duiPrediction.isDanger()){
        	r.drawString3d("!", Color.red, d.car.position.plus(new Vector3(0, 0, 150)).toFramework(), 4, 4);
        }else if(duiPrediction.isNice()){
        	r.drawString3d(":)", Color.green, d.car.position.plus(new Vector3(0, 0, 150)).toFramework(), 4, 4);
        }
    	
    	//Aerialing
        if(!duiAerial.aerialing){
        	duiAerial.setTarget(d);
        	boolean start = duiAerial.shouldStartAerial(d, false);
        	if(start){
        		duiAerial.aerialing = true;
        		duiAerial.timer = System.currentTimeMillis();
        	}
        }else{
        	duiAerial.aerialing = duiAerial.shouldContinueAerial(d);
        }
        if(duiAerial.aerialing){
        	System.out.print("Aerialing: "); //Start the line

        	ControlsOutput a = duiAerial.controller(d);
        	System.out.println(); //End the line
        	return a;
        }
        
        //Start printing, deal with the weights and the decided angle
        System.out.print(this.playerIndex + ": ");
        double totalWeight = 0, greatestWeight = 1;
        double[] angles = new double[states.size()];
        double chosen = 0;
          
        cars = input.cars;
        
        final long timerChange = (System.currentTimeMillis() - dodgeTimer);

        if(timerChange > 1150 || timerChange < 220 || d.car.hasWheelContact){
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
        final float steer = (chosen > 0 ? -1 : 1) * (float)Math.min((1D / steerThreshold) * Math.abs(chosen), 1D);
        
        final boolean kickoff = KickoffState.isKickoff(input.ball);
        
        //Speed for Dui to travel at
        float throttle = (d.ballDistance > 500 || WallState.isOnWall(d.car) || KickoffState.isKickoff(d.input.ball)) ? 1 : Math.max(0F, (float)(1.2F - (d.ballPosition3.z - 94) / 80F));
//      if(d.car.velocity.magnitude() > 1200) throttle = (float)Math.min(throttle, 1.2F - Math.abs(chosen / 110F));
//      d.r.drawString3d(r(throttle) + "", Color.white, d.car.position.withZ(d.car.position.z + 50).toFramework(), 2, 2);
//		d.r.drawString3d((int)d.car.velocity.magnitude() +  "uu/s", Color.white, d.car.position.toFramework(), 2, 2);
        
        //Controller to send at the end
        ControlsOutput control = new ControlsOutput().withSteer(steer).withThrottle(throttle).withSlide(Math.abs(chosen) > 70 && d.car.position.z < 70);
        
        //Boosting is determined by how little we are turning, whether are are on the ground, and whether we are wanting to go fast
        boolean boost = d.car.hasWheelContact && (kickoff || (Math.abs(steer) < 0.25 && control.getThrottle() > 0.95 && (dif(d.steerBall, d.steerEnemyGoal) < 10 || d.ballDistance > 120) && timerChange > 1700 && !WallState.isOnWall(d.car)));
        boost &= (d.car.velocity.magnitude() < 2250);
        control = control.withBoost(boost);
        if(boost){
        	System.out.print("Zoom & ");
        }

        //Dealing with whether we should dodge
        boolean dodge; 
        if(kickoff){
        	dodge = d.car.boost < 3; //Dodge earlier in a kickoff than normal
        }else{
        	if(d.ballDistance < 300){
        		dodge = d.steerBall < 170 && (duiPrediction.isDanger() || dif(d.steerBall, d.steerEnemyGoal) < 26);
        	}else{
	        	if(d.car.boost < 18){
	        		dodge = (Math.abs(steer) < 0.1F && d.ballDistance > (duiPrediction.isDanger() ? 500 : 2700));
	        	}else{
	        		dodge = (d.ballDistance > 3500 && (input.ball.velocity.magnitude() + 500 < d.car.velocity.magnitude()));
	        	}
        	}
        }
        System.out.print(dodge ? "Dodge" : "Go");

        //Dealing with the actual process of dodging (where we are in the action of dodging)
        if(dodge || timerChange <= 1000){
        	if(kickoff){
        		control = dodgeDegrees(control, (float)(d.steerBall * 2.3F), timerChange);
        	}else{
        		control = dodgeDegrees(control, (float)(d.ballDistance < 1300 || (dif(chosen, d.steerBall) < 30 && d.ballDistance < 5000) ? d.steerBall : chosen), timerChange);
        	}
        }
        
        //Car correction when we're falling
        if(!d.car.hasWheelContact && !boost && timerChange > 1150 && (d.car.position.z > 220 || d.car.velocity.z > 650 || d.car.velocity.z < -175)){
        	r.drawString3d("Correcting...", Color.gray, d.car.position.toFramework(), 2, 2);
        	if(Math.abs(d.car.orientation.getRoll()) + Math.abs(d.car.orientation.getPitch()) < 0.45F){
        		control = control.withYaw((float)duiAerial.yaw(d.ballPosition3.minus(input.car.position), d) / 2.25F);
        	}
        	control = control.withRoll((float)(-Math.signum(d.car.orientation.getRoll()) * Math.log(1 + Math.abs(d.car.orientation.getRoll()))));
            control = control.withPitch((float)(-Math.signum(d.car.orientation.getPitch()) * Math.log(1 + Math.abs(d.car.orientation.getPitch()))));
        }

        lastSteeringFrame = steer;
        System.out.println(); //End the line we've been printing to
        
//        r.drawString2d("Last Method: " + lastMethod , Color.cyan, new Point(0, 0), 2, 2);
        
        return control;
    }

	@Override
    public int getIndex(){
        return this.playerIndex;
    }
	
    /**Printing ready*/private boolean ready = false;

    @Override
    public ControllerState processInput(GameTickPacket packet){
        if(packet.playersLength() <= playerIndex || packet.ball() == null){
        	if(packet.playersLength() <= playerIndex && !ready){
        		System.out.println("Ready!");
        		ready = true;
        	}
        	return new ControlsOutput();
        }
        ready = false;
        
        BoostManager.loadGameTickPacket(packet);
        
//        final long timeStarted = System.currentTimeMillis();
        DataPacket dataPacket = new DataPacket(packet, playerIndex);
        ControllerState state = processInput(dataPacket);
        
//        final long timeTaken = (System.currentTimeMillis() - timeStarted);
//        if(timeTaken > 50){
//        	System.err.println(((float)timeTaken / 1000F) + "s"); //Slow
//        }else{
//        	System.out.println(((float)timeTaken / 1000F) + "s");
//        }
        
        return state;
    }

    public void retire(){
        System.out.println("Retiring Dui (" + playerIndex + ")");
        if(team == 0 || team == 1) DuiJava.removeBot(playerIndex, team);
    }
    
    /**Rounds to two decimal places*/
    public double r(double r){
    	return Math.round(r * 100D) / 100D;
    }
    
    /**Returns the difference between two values*/
    public double dif(double one, double two){
    	return Math.abs(Math.max(one, two) - Math.min(one, two));
    }
    
    /**Return controls for dodging purposes, using degrees*/
	private ControlsOutput dodgeDegrees(ControlsOutput control, float degrees, long timerChange){return dodge(control, -Math.toRadians(degrees), timerChange);}

    /**Return controls for dodging purposes*/
	private ControlsOutput dodge(ControlsOutput control, double radians, long timerChange){
    	if(timerChange > 2200){
    		dodgeTimer = System.currentTimeMillis(); //We can now dodge again (recharged)
    	}else if(timerChange <= 100){
    		control.withJump(true); //Jump
    		control.withPitch(-1); //Tip forward
    	}else if(timerChange <= 250){
    		control.withJump(false); //Stop jumping
    		control.withPitch(-1); //Keep tipping
    	}else if(timerChange <= 1100){
    		control.withJump(true); //Dodge
    		control.withYaw((float)Math.sin(radians)); //Still keep tipping
    		control.withPitch((float)-Math.abs(Math.cos(radians))); //Point the way we intend to go
    	}
    	return control;
	}
    
}
