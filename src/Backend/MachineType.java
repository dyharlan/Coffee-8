/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package Backend;

/**
 *
 * @author dyhar
 */
public enum MachineType {
//cosmac VIP settings
//     vfOrderQuirks = false;
//     shiftQuirks = false;
//     logicQuirks = true;
//     loadStoreQuirks = false;
//     clipQuirks = true;
//     vBlankQuirks = true;
//     IOverflowQuirks = true;
//     jumpQuirks = false;

    COSMAC_VIP("COSMAC VIP",64,32,new boolean[]{false,false,true,false,true,true,true,false}),
    SUPERCHIP_1_1("Super-Chip 1.1",64,32,new boolean[]{false,true,false,true,true,false,true,true});

    private String machineName;
    private int displayWidth;
    private int displayHeight;
    private boolean quirks[];


    public String getMachineName() {
        return machineName;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public boolean getQuirks(int i) {
        return quirks[i];
    }
    
    MachineType(String machineName,int displayW, int displayH, boolean quirks[]){
        this.machineName = machineName;
        displayWidth = displayW;
        displayHeight = displayH;
        this.quirks = quirks;
    }
    
    
}
