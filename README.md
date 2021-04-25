# Conways
 
My own implementation of Conway's Game of Life, written purely in Java. This implementation checks all cells in the matrix doing all the work on CPU, including dead cells, but the matrix is divided in chunks that are loaded when are alive cells inside or near to them, so performance isn't bad, even tested in 2k and 4k displays.

It's coded thinking on the possibility to add multiple renderers, for example Swing and Console renderers. Currently I've worked only on Swing renderer. I'll think in the future to add the possibility of adding multiple algorithms to compute GoL, and calc them on CPU or GPU just choosing it from a file config.

I'm working currently on multithreading support (each thread can calculate different chunks), zoom/move features and the possibility to read GoL standard files. Also, I'll work in the future on a pattern editor to draw custom shapes inside the program.

## Known issues

Currently has a few bugs, like tries loading non-existent chunks when the number is so high, or simulation does strange calculations when, as same, you configure like +7k chunks.

# Controls

|Key|Action|
|---|---|
|m|Toggle fullscreen|
|p|Toggle pause|
|z|Show simulation stats|
|c|Show chunks|

# Screenshots

![image](https://user-images.githubusercontent.com/33585530/115991807-8f3c4280-a5ca-11eb-9549-2ca0e20d9962.png)

![image](https://user-images.githubusercontent.com/33585530/115991825-a24f1280-a5ca-11eb-9b96-314df22674b0.png)
