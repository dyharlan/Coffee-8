/*
 * The MIT License
 *
 * Copyright 2023 dyharlan.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package Backend;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author dyharlan
 */
public class WaveGenerator {
    AudioInputStream audioStream;
    AudioFormat audioFormat;
    ByteArrayInputStream bais;
    SourceDataLine sourceDataLine;
   
    Boolean isPlaying;
    Boolean isEnabled;
    float bufferpos = 0;
    byte[] buffer;
    byte[] scaledBuffer;
    static int systemFreq = 48000;
    static float sampleFreq;
    static int channels = 1;
    float pitch;
    public WaveGenerator(Boolean sound,float pitch, int[] pattern) throws IOException, LineUnavailableException, UnsupportedAudioFileException{
       buffer = new byte[128];
       scaledBuffer = new byte[800];
       this.pitch = pitch;
       this.sampleFreq = 4000;
       
       audioFormat = new AudioFormat(systemFreq, 8, channels, false, false);
       sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
       sourceDataLine.open(audioFormat);
       sourceDataLine.start();
       isPlaying = false;
       isEnabled = sound;
       setPitch(pitch);
       setBuffer(pattern);
    }
    
    public void setPitch(float value) {
        float exp = ((value-64)/48);
        float rate = (float) (4000*Math.pow(2, exp));
        sampleFreq = rate;
        pitch = value;
    }
    
    public void setBuffer(int[] pattern){
        //create an array given the formula: 8 * 16 = bits in a byte * number of samples (16)
        //create an array that will store a scaled wave form version of the above sound, so it can be played on higher frequencies that a modern system uses. (i.e. 48000hz or 48khz)
        //byte[] buffer2 = new byte[bufferSize];
        //compute the playback rate given a pitch value in the vX register
        for (int i = 0, j = 0; i < pattern.length; i++) {
            for (byte shift = 7; shift >= 0; shift--) {
                buffer[j++] = (byte) (((pattern[i] >> shift & 1) != 0) ? 255 : 0);
            }
        }
    }
    public void generateSquareWavePattern(int amount){
        //scale the waveform so that it can be played properly on a system with high frequency. 
        //buffer2 will store the scaled waveform given by this formula:
        //buffer2[i] = buffer[(i*(rate/128))/targetFrequency)%buffer_length]
        //where: 
        // i is the ith amplitude in the original untouched sample
        // rate = 4000*(2^((vx-64)/48)) from here: https://johnearnest.github.io/Octo/docs/XO-ChipSpecification.html
        // it is divided by 128 for mono, or 256 for stereo to get the actual freq of the tone
        // targetFrequency is the frequency you want the sounds to be scaled at. In our case it is 48khz
//        for(int k = 0; k < scaledBuffer.length; k++){
//            scaledBuffer[k] = buffer[(int)((k*sampleFreq/(channels == 2? 256 : 128)*buffer.length)/systemFreq)%buffer.length];
//        }

        if (scaledBuffer == null) {
            scaledBuffer = new byte[amount];
        }
        float rate = sampleFreq / systemFreq;
        for (int k = 0; k < amount; k++) {
            scaledBuffer[k] = buffer[(int) bufferpos];
            bufferpos = (bufferpos + rate) % buffer.length;
        }
//        for(int k = 0; k < scaledBuffer.length; k++){
//            scaledBuffer[k] = buffer[(int)((k*sampleFreq/(channels == 2? 256 : 128)*buffer.length)/systemFreq)%buffer.length];
//        }
        if(!sourceDataLine.isActive()){
            sourceDataLine.start();
        }
        sourceDataLine.write(scaledBuffer, 0, scaledBuffer.length);
        
    }
    
//    public void playSound() {
//        if (isPlaying || !isEnabled) 
//            return;
//        isPlaying = true;
//        sThread = new soundThread();
//        sThread.setPriority(Thread.MAX_PRIORITY);
//        sThread.start();
//    }
//    
//    public void pauseSound() {
//        isPlaying = false;
//        sourceDataLine.stop();
//        sourceDataLine.flush();
//    }
    public void stop(){
        //stop those clicks from happening
        sourceDataLine.flush();
        //sourceDataLine.stop();
    }
}
