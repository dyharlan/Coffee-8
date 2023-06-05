/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Backend;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author dyhar
 */
public class ToneGenerator {
    InputStream is;
    BufferedInputStream bis;
    AudioInputStream audioStream;
    AudioFormat audioFormat;
    ByteArrayInputStream bais;
    SourceDataLine sourceDataLine;
    soundThread sThread;
    byte[] buf;
    byte[] abData;
    Boolean isPlaying;
    Boolean isEnabled;
    
    public ToneGenerator(Boolean sound) throws IOException, LineUnavailableException, UnsupportedAudioFileException{
        is = getClass().getResourceAsStream("tone.wav");
        bis = new BufferedInputStream(is);
        audioStream = AudioSystem.getAudioInputStream(bis);
        audioFormat = audioStream.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
        sourceDataLine.open(audioFormat);
        
       
        int nBytesRead = 0;
        abData = new byte[(int)audioStream.getFrameLength()];
        while (nBytesRead != -1) {

            try {
                nBytesRead = audioStream.read(abData, 0, abData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }

            
        }
       isPlaying = false;
       isEnabled = sound;
    }
    
    class soundThread extends Thread {
        public void run(){
            sourceDataLine.start();
            while(isPlaying){
                sourceDataLine.write(abData, 0, abData.length);
            }
            
        }
    }
    public void playSound() {
        if (isPlaying || !isEnabled) 
            return;
        isPlaying = true;
        sThread = new soundThread();
        sThread.setPriority(Thread.MAX_PRIORITY);
        sThread.start();
    }
    
    public void pauseSound() {
        isPlaying = false;
        sourceDataLine.stop();
        sourceDataLine.flush();
    }
}
