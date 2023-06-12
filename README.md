<h1 align="center">Coffee-8</h1>
<h2 align="center"><img src="https://raw.githubusercontent.com/dyharlan/Coffee-8/main/src/Frontend/icon.png"/></h2>

<h2 align="center">A Chip-8 emulator that aims to be Octo compliant in terms of accuracy. Currently simulates both the SuperChip 1.1, and the COSMAC VIP interpreters.</h2>

<h3 align="center">
<img alt="Car Race by Klaus von Sengbusch" src="https://raw.githubusercontent.com/dyharlan/Coffee-8/main/Screenshots/Race.jpg" width="420" height="240"/>
<img alt="Space Invaders by David Winter" src="https://raw.githubusercontent.com/dyharlan/Coffee-8/main/Screenshots/Space%20Invaders.PNG" width="420" height="240"/>
<img alt="Spacefight 2091 by Carsten Soerensen" src="https://raw.githubusercontent.com/dyharlan/Coffee-8/main/Screenshots/Spacefight%202091.PNG" width="420" height="240"/>
</h3>

## Accuracy
Coffee-8 passes [Timedus'](https://github.com/Timendus/chip8-test-suite) and [metteo's](https://github.com/metteo/chip8-test-rom) Test Suite for the Chip-8 and the Super-Chip.

## Installation
For now, builds are not available due to some base features missing. You need to compile this on your own.

Compilation is pretty simple. You need at least Netbeans 13, and Java JDK 17 to work. 

Then run ``git clone https://github.com/dyharlan/Coffee-8.git``, open the project on NetBeans, go to Run > Clean and Build Project, and open ``Coffee-8.jar`` inside of the ``Coffee-8/dist`` directory.

You can also just use the command-line by going into the ``Coffee-8/src`` directory and typing:
``javac Frontend/*.java``. 

This will generate class files that you can use by typing ``java Frontend/SwingDisplay``.

## Todo
- Implement saving user settings
- Implement toggleable quirks
- Implement XO-Chip
- Implement debugging capabilities

## Acknowledgements
- [michaelarnauts' chip8 emulator](https://github.com/michaelarnauts/chip8-java) - derived coffee-8's cpu and sound code.
- [JohnEarnest's Octo](https://github.com/JohnEarnest/Octo) - a huge help in fixing bugs in the emulator code.
- [lesharris' Dorito](https://github.com/lesharris/dorito) - the VBlank code used by the COSMAC VIP interpreter.
- [Tobias V. Langhoff's guide to a Chip8 emulator](https://tobiasvl.github.io/blog/write-a-chip-8-emulator/) - an excellent guide that got me started to creating a Chip-8 emulator
- [Awesome Chip-8](https://chip-8.github.io/extensions/) - for disambiguating multiple different C8 interpreters
- [BrokenProgrammer's Chip-8 emulator](https://github.com/brokenprogrammer/CHIP-8-Emulator) - for being a reference early in the project.
- [AlfonsoJLuna's chip8swemu](https://github.com/AlfonsoJLuna/chip8swemu) - for being another early reference in fixing emulation bugs.
- [The EmuDev Discord](https://discord.com/invite/7nuaqZ2) - Specifically @Gulrak and @Janitor Raus for answering my questions.
## License
Coffee-8 is licensed under the MIT License. See License.md for more details

## Contributing
Do take note that any form of contributions to this project will fall under the MIT License. With that said, any form of contributions are welcome. 
As I'm not confident with myself in certain aspects of this app's code.





