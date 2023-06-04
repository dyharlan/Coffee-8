package Frontend;


import java.io.*;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author dyhar
 */
public class CPUDebugger {

    public static void cls() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException ex) {
            System.out.println(ex.toString());
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        File rom = new File("C:\\Users\\dyhar\\Downloads\\4-flags.ch8");;
        //Chip8SOC chip8CPU = new Chip8SOC(rom);
        while (true) {
             if (System.getProperty("os.name").contains("Windows")) {
                    cls();
                } else {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                }
            //chip8CPU.updateTimers();
            //chip8CPU.cpuExec();
           
            Thread.sleep(170);
        }

    }
}
