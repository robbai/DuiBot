package rlbot.input;

import rlbot.flat.GameTickPacket;

public class DataPacket {

    public final CarData car;
    public final BallData ball;
    public final int team;
    public final int playerIndex;
    
    /**List of all the cars in the game*/ 
    public final CarData[] cars;

    public DataPacket(GameTickPacket request, int playerIndex){
        this.ball = new BallData(request.ball());
        this.playerIndex = playerIndex;
        
        rlbot.flat.PlayerInfo myPlayerInfo = request.players(playerIndex);
        this.car = new CarData(myPlayerInfo, request.gameInfo().secondsElapsed());
        this.car.dui = true;
        
        this.team = myPlayerInfo.team();
        
//        this.cars = new CarData[request.playersLength()];
//        for(int i = 0; i < request.playersLength(); i++){
//        	if(i != playerIndex) this.cars[i] = new CarData(request.players(i), request.gameInfo().secondsElapsed());
//        }
        
        this.cars = new CarData[0];
        
    }
    
}
