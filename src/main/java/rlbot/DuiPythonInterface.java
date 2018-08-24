package rlbot;

import rlbot.dui.Dui;
import rlbot.manager.BotManager;
import rlbot.pyinterop.DefaultPythonInterface;

public class DuiPythonInterface extends DefaultPythonInterface {

    public DuiPythonInterface(BotManager botManager){
        super(botManager);
    }

    protected Bot initBot(int index, String botType, int team){
        return new Dui(index);
    }
    
}
