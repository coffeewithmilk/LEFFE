package model;

import java.io.BufferedInputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Sound {

	public static void main(String[] args) throws InterruptedException {
		playFailure();
		Thread.sleep(10000);
	}
	
	public static void playFailure() {
		playSound("failure.wav");
	}
	
	private static synchronized void playSound(final String url) {
		new Thread(new Runnable() {
			public void run() {
				try {
					Clip clip = AudioSystem.getClip();
					AudioInputStream inputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(
							Sound.class.getResourceAsStream(url)));
					clip.open(inputStream);
					clip.start(); 
				} catch (Exception e) {
					System.err.println(e.toString());
				}
			}
		}).start();
	}

}
