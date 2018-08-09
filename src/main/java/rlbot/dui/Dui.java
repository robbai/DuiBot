package rlbot.dui;

import java.awt.Color;
import java.util.ArrayList;

import rlbot.Bot;
import rlbot.ControllerState;
import rlbot.boost.BoostManager;
import rlbot.dui.states.*;
import rlbot.flat.GameTickPacket;
import rlbot.input.CarData;
import rlbot.input.DataPacket;
import rlbot.manager.BotLoopRenderer;
import rlbot.output.ControlsOutput;
import rlbot.render.Renderer;
import rlbot.obj.*;

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

    /**Dui's goal*/public Vector2 ownGoal;
    /**Dui's target*/public Vector2 enemyGoal;

    public Dui(int playerIndex){
        this.playerIndex = playerIndex;           
        
        //Initialise all the states
        new AttackState();
        new KickoffState();
        new BoostState();
        new DefendState();
        new ReturnState();
        new WaitState();
    }

    private ControlsOutput processInput(DataPacket input){
    	
    	//Get the car (very useful)
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
        
        //Write the ball height on the ball
        r.drawString3d((int)ballPosition3.z + "", Color.white, ballPosition3.toFramework(), 2, 2);
        
        //Go through each state to calculate its output and weight, and to determine the total weight and greatest weight
        for(byte i = 0; i < states.size(); i++){
        	State s = states.get(i);
        	angles[i] = s.getOutput(input, ballPosition3, ballPosition, car, carPosition, carDirection, ownGoal, enemyGoal, ballDistance, ownGoalDistance, steerBall, steerEnemyGoal, r);
        	if(s.getWeight() > 0) totalWeight += s.getWeight(); //Negative weights should be ignored
        	greatestWeight = Math.max(greatestWeight, s.getWeight());
        }
        
        //Go through all the states again in order to determine which way Dui should steer
        for(byte i = 0; i < states.size(); i++){
        	State s = states.get(i);
        	if(s.getWeight() >= 0.01){ //Negative weights should be ignored
        		chosen += (angles[i] * ((s.getWeight() / greatestWeight) / (totalWeight / greatestWeight)));
        		System.out.print(s.toString(greatestWeight) + ", "); //Print useful weighted outputs
        	}
        }
        
        //Steering output, determined by what the weights have given us
        final float steer = ((chosen > 0 ? -1 : 1)) * (float)Math.min((1D / steerThreshold) * Math.abs(chosen), 1D);
        
        //Controller to send at the end
        ControlsOutput control = new ControlsOutput().withSteer(steer).withThrottle(ballPosition3.z > 110 ? Math.min(1F, (float)ballDistance / 2800F) : 1F).withSlide(Math.abs(chosen) > 84);
        
        //Boosting is determined by how little we are turning, whether are are on the ground, and whether we are wanting to go fast
        control = control.withBoost(Math.abs(steer) < 0.28 && car.hasWheelContact && control.getThrottle() > 0.6);
        
        //Dealing with whether we should dodge
        boolean dodge; 
        if(KickoffState.isKickoff(input.ball)){
        	dodge = (ballDistance < 1200 && car.boost < 30) || !car.hasWheelContact; //Dodge earlier in a kickoff than normal
        }else{
        	dodge = (((ballDistance > 3000 && car.velocity.magnitude() > 600) || (ballDistance < 250 && ballPosition3.z < 220 && Math.abs(steerBall) < 12)) && car.position.z < 140 && Math.abs(chosen) < 30) || !car.hasWheelContact;
        }
        
        System.out.print(dodge ? "Dodge" : "Go");
                
        //Dealing with the actual process of dodging (where we are in the action of dodging)
        if(dodge){
        	control = dodge(control, steer);
        }else if(car.position.z > 800){ 
        	//If we get too high up the wall, it is useful to simply jump down
        	control.withJump(System.currentTimeMillis() % 100 >= 50); 
        }
        
        System.out.println(); //End the line we printed
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
    
    /**Return controls that are dodging*/
	private ControlsOutput dodge(ControlsOutput control, float steer){
		final long timerChange = System.currentTimeMillis() - dodgeTimer;
    	if(timerChange > 2200){
    		dodgeTimer = System.currentTimeMillis(); //We can now dodge again (recharged)
    	}else if(timerChange <= 100){
    		control.withJump(true); //Jump
    		control.withPitch(-1); //Tip forward
    	}else if(timerChange <= 150){
    		control.withJump(false); //Stop jumping
    		control.withPitch(-1); //Keep tipping
    	}else if(timerChange <= 1000){
    		control.withJump(true); //Dodge
    		control.withPitch(-1); //Still keep tipping
    		control.withYaw(steer); //Point the way we intend to go
    	}
    	return control;
	}
    
}
