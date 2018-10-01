package rlbot;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import rlbot.manager.BotManager;
import rlbot.pyinterop.PythonInterface;
import rlbot.pyinterop.PythonServer;
import rlbot.util.PortReader;

/**
 * See JavaAgent.py for usage instructions
 */

public class DuiJava {
	
	/**Port number*/private static Integer port;
	/**The beautiful frame!*/private static JFrame frame = null;
	/**For showing the bots on the frame*/private static ArrayList<Byte> bots = new ArrayList<Byte>();
	
    public static void main(String[] args){
    	
        BotManager botManager = new BotManager();
        PythonInterface pythonInterface = new DuiPythonInterface(botManager);
        port = PortReader.readPortFromFile("port.cfg");
        
        try{
        	
	        PythonServer pythonServer = new PythonServer(pythonInterface, port);
	        pythonServer.start();
	        
	        updateFrame();
	        System.out.println("Initialised the JFrame");
	        
        }catch(py4j.Py4JNetworkException e){
        	System.err.println("Port in use (" + port + ")");
        }catch(Exception e){
        	e.printStackTrace();
        }
        
    }
    
    public static void addBot(int index, int team){
    	System.out.println("Adding a Bot (team = " + team + ", index = " + index + ")");
    	bots.add((byte)(team + (index << 2)));
    	if(frame != null){
    		frame.setVisible(false);
    		frame.dispose();
    	}
    	updateFrame();
    }

	private static void updateFrame(){
		//Create the frame
        frame = new JFrame("DuiBot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Create the panel
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        //Add the text
        panel.add(new JLabel("Port: " + port + " ", JLabel.CENTER));
        
        //Add the button
        JButton b = new JButton("Terminate");  
		b.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){System.exit(0);}});  
		b.setBackground(new Color(255, 180, 180));
		b.setFocusPainted(false);
		panel.add(b);
				
		//List the bots
		if(bots.size() != 0){
			for(byte by : bots){
				panel.add(new JLabel("Dui - " + ((by & 3) == 0 ? "Blue" : "Orange") + ", Index = " + (by >> 2), JLabel.CENTER));
			}
		}
        
		//Finish the frame
        frame.add(panel);
        frame.pack();
//      frame.setSize(frame.getWidth() + 25, frame.getHeight());
        frame.setVisible(true);
        frame.setResizable(false);
	}
    
}