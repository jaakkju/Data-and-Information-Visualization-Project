package big.marketing.controller;

import org.apache.log4j.Logger;

public class Player extends Thread{
	static Logger logger = Logger.getLogger(Player.class);
	private DataController dataController;
	private int currentTime, stepSize, sleepMillis;
	private volatile boolean isPlaying;
	
	
	public Player(DataController dc, int startTime, int stepSize, int sleepMillis) {
		super("PlayThread");
		logger.info("Created 1 play object");
	   this.dataController = dc;
	   this.currentTime = startTime;
	   this.stepSize = stepSize;
	   this.sleepMillis = sleepMillis;
	   this.isPlaying = false;
   }
	
	public void stopPlaying(){
		this.isPlaying = false;
	}
	
	public void startPlaying(){
		this.isPlaying = true;
		this.start();
	}

	@Override
	public void run() {
		while (isPlaying){
			currentTime += stepSize;
			logger.info("Move to "+currentTime);
			dataController.setTime(currentTime);
			
			try {
	         Thread.sleep(sleepMillis);
         } catch (InterruptedException e) {
	         e.printStackTrace();
         }
			
		}
		

	}

}
