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

	private final int steerThreshold = 18;
    private int playerIndex;
    public static ArrayList<State> states = new ArrayList<State>();
    private long timer = 0L;

    public Dui(int playerIndex){
        this.playerIndex = playerIndex;           
        
        new AttackState();
        new KickoffState();
        new BoostState();
        new DefendState();
        new ReturnState();
    }

    private ControlsOutput processInput(DataPacket input){
    	final Vector3 ballPosition3 = input.ball.position;
        final Vector2 ballPosition = input.ball.position.flatten();        
        final CarData car = input.car;
        final Vector2 carPosition = car.position.flatten();
        final Vector2 carDirection = car.orientation.noseVector.flatten();
        final Vector2 ownGoal = new Vector2(0, car.team == 0 ? -5120 : 5120);
        final Vector2 enemyGoal = new Vector2(0, car.team == 0 ? 5120 : -5120);                
        final double ballDistance = carPosition.distance(ballPosition);
        final double ownGoalDistance = carPosition.distance(ownGoal);        
        final double steerBall = Math.toDegrees(carDirection.correctionAngle(ballPosition.minus(carPosition)));
        final double steerEnemyGoal = Math.toDegrees(carDirection.correctionAngle(enemyGoal.minus(carPosition)));
        
        System.out.print(this.playerIndex + ": ");
        double totalWeight = 0, greatestWeight = 1;
        double[] angles = new double[states.size()];
        double chosen = 0;
        
        Renderer r = BotLoopRenderer.forBotLoop(this);
        r.drawString3d((int)ballPosition3.z + "", Color.white, ballPosition3.toFramework(), 2, 2);
        
        for(byte i = 0; i < states.size(); i++){
        	State s = states.get(i);
        	angles[i] = s.getOutput(input, ballPosition3, ballPosition, car, carPosition, carDirection, ownGoal, enemyGoal, ballDistance, ownGoalDistance, steerBall, steerEnemyGoal, r);
        	if(s.getWeight() > 0) totalWeight += s.getWeight();
        	greatestWeight = Math.max(greatestWeight, s.getWeight());
        }
        
        for(byte i = 0; i < states.size(); i++){
        	State s = states.get(i);
        	if(s.getWeight() > 0){
        		chosen += (angles[i] * ((s.getWeight() / greatestWeight) / (totalWeight / greatestWeight)));
        		System.out.print(s.toString(greatestWeight) + ", ");
        	}
        }
        
        final float steer = ((chosen > 0 ? -1 : 1)) * (float)Math.min((1D / steerThreshold) * Math.abs(chosen), 1D);
        ControlsOutput control = new ControlsOutput().withSteer(steer).withThrottle(ballPosition3.z > 140 ? Math.min(1F, (float)ballDistance / 1200F) : 1F).withSlide(Math.abs(chosen) > 105);
        control = control.withBoost(Math.abs(steer) < 0.3 && car.hasWheelContact && control.getThrottle() > 0.5);
        
        boolean dodge; 
        if(KickoffState.isKickoff(input)){
        	dodge = (ballDistance < 1200 && car.boost < 20) || !car.hasWheelContact;
        }else{
        	dodge = (((ballDistance > 3000 && car.velocity.magnitude() > 800) || (ballDistance < 600 && ballPosition3.z < 220)) && (Math.abs(steerBall) < 25 || ballDistance < 400) && car.position.z < 200) || !car.hasWheelContact;
        }
        
        System.out.print(dodge ? "Dodge" : "Go");
        
        if(dodge){
        	long timerChange = System.currentTimeMillis() - timer;
        	if(timerChange > 2200){
        		timer = System.currentTimeMillis();
        	}else if(timerChange <= 100){
        		control.withJump(true);
        		control.withPitch(-1);
        	}else if(timerChange <= 150){
        		control.withJump(false);
        		control.withPitch(-1);
        	}else if(timerChange <= 1000){
        		control.withJump(true);
        		control.withPitch(-1);
        		control.withYaw(steer);
        	}
        }else if(car.position.z > 800){
        	control.withJump(System.currentTimeMillis() % 100 >= 50);
        }
        
        System.out.println();
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
    
    /*
     * Rounds to two decimal places
     */
    public static double r(double r){
    	return Math.round(r * 100D) / 100D;
    }
    
    /*
     * Returns the difference between two values
     */
    public static double dif(double one, double two){
    	return Math.max(one, two) - Math.min(one, two);
    }
    
}