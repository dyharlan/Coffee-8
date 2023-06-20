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
package Frontend;

/**
 *
 * @author dyharlan
 */
import Backend.*;
import java.io.*;
import java.util.Scanner;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
/*
* This is an experimental console renderer for shits and giggles. It is provided AS IS.
* Anything here is quite literally unsupported till I say so. Use and modify at your own risk.
* What this does is render graphics in a brute force fashion to the console
* It's slow, a resource hog, and worst of all, a big walking race condition.
* Since reads from the renderer thread aren't synchronized with the main cpu thread.
*/
public class CPUDebugger implements Runnable{
    static Chip8SOC chip8CPU;
    static char[] planeColors = new char[4]; 
    
    
    public static void cls() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException ex) {
            System.out.println(ex.toString());
        }
    }
    
    @Override
    public void run() {
        while (true) {
            if (chip8CPU.graphics != null) {
                System.out.print("\033[H\033[2J");
                System.out.flush();
                for (int y = 0; y < chip8CPU.getMachineHeight(); y++) {
                    for (int x = 0; x < chip8CPU.getMachineWidth(); x++) {
                        int newPlane = (chip8CPU.graphics[1][(x) + ((y) * chip8CPU.getMachineWidth())] << 1 | chip8CPU.graphics[0][(x) + ((y) * chip8CPU.getMachineWidth())]) & 0x3;
                        //selectively update each pixel if the last frame exists
                        System.out.printf("%1s", planeColors[newPlane]);
                    }
                    System.out.printf("\n");
                }
            }
        }
    }
    
    
    public static void main(String[] args) throws IOException, InterruptedException, LineUnavailableException, UnsupportedAudioFileException {
        try {
            chip8CPU = new Chip8SOC(true, MachineType.XO_CHIP);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        planeColors[0] = '░';
        planeColors[1] = '█';
        planeColors[2] = '▒';
        planeColors[3] = '▓';
        Scanner input = new Scanner(System.in);
        System.out.print("Enter location of rom file: ");
        String location = input.nextLine();
        File rom = new File(location);
        
        
        double frameTime = 1000 / 60;
        long elapsedTimeFromEpoch = System.currentTimeMillis();
        double origin = elapsedTimeFromEpoch + frameTime / 2;
        CPUDebugger cd = new CPUDebugger();
        Thread renderThread = new Thread(cd);
        chip8CPU.enableSound();
        chip8CPU.loadROM(rom);
        chip8CPU.setCycles(500);
        renderThread.start();
        Boolean running = true;
        while (running) {
            synchronized (chip8CPU) {
                long diff = System.currentTimeMillis() - elapsedTimeFromEpoch;
                elapsedTimeFromEpoch += diff;
                for (long i = 0; origin < elapsedTimeFromEpoch - frameTime && i < 2; origin += frameTime, i++) {
                    for (int j = 0; j < chip8CPU.getCycles() && !chip8CPU.getWaitState(); j++) {
                        chip8CPU.cpuExec();
                    }
                    chip8CPU.updateTimers();
                }

                try {
                    Thread.sleep((int) frameTime);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                if (chip8CPU.getVBLankInterrupt() == 1) {
                    chip8CPU.setVBLankInterrupt(2);
                }
            }
                  
        }
        
        

    }

}
