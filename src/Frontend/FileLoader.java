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
public class FileLoader {
    public static void main(String[] args) throws IOException {
        File rom = new File("C:\\Users\\dyhar\\Downloads\\Floppy Bird (by Micheal Wales)(2014).ch8");
        byte[] romArray;
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(rom)));
        romArray = new byte[(int)rom.length()];
//        in.read(romArray);
//        for(int i = 0; i < romArray.length; i++){
//            System.out.print(Integer.toHexString(romArray[i]) + "\t");
//            
//        }
        short x = 0;
        while((x = (short) in.read())!= -1){
            System.out.print(x + "\t");
        }
    }

}
