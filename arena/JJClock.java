/*
   Version: 1.0beta (2002/03/05)
   JJClock written by:
   Alan Lund (alan.lund@acm.org)
   Walter Nistico' (walnist@infinito.it)

   Usage: this class can be used as a replacement for the natural timer
   System.getCurrentMillis().
   It provides a machine dependent clock, so that the simulation runs slower
   or faster than real time depending on the speed of the machine is running
   on versus the speed of a target machine chosen as a benchmark, insuring that
   robot threads get always the same amount of CPU time per virtual second,
   regardless of the speed of the computer they are running on.
   It can be used as an asynchronous clock generator on an independent thread,
   by calling start() on an instance of itself; alternatively, it can be
   synchronized with another thread, by calling periodically the tick() function
   instead of starting the JJClock object.

*/

public class JJClock extends Thread {

// constants
// these are perfomance tuning paramters
 private static final int IT_MSEC = 20;
 private static final int MULT = 1;

// private fields
 private long[] currentTime;
 private double resolution;

// constructor
 public JJClock() {
  super("JJClock");
  currentTime = new long[1];
 }

// private methods
 private void benchmark(){
// this function does nothing but generating a machine dependent delay
// here i used a rectangular to polar conversion function, i think every
// robot uses something like this
  double alpha;
  double x;
  double y;
  double a;
  double b;
  int i;
  x = 10*Math.random();
  y = 10*Math.random();
  a = 10*Math.random();
  b = 10*Math.random();
  if ((x == 0)&&(y > 0))
   alpha = 90;
  else if ((x == 0)&&(y <= 0))
   alpha = 180;
  else {
   alpha = Math.atan(y/x)*Math.PI; // slow fpu function
   if (x < 0)
    alpha += 180;
  }
  for (i=0; i<100; i++){
   x += a; // these 2 line are just to test peak fpu performance
   y *= b; // add and mul without data dependency
  }
 }
 private void loopStep(){
  for (int i=0; i<IT_MSEC; i++)
   benchmark();
  synchronized (currentTime){
   currentTime[0] += MULT;
  }
  Thread.yield();
 }

// set&get methods
 public void setResolution (double secondsPerTick) {
  resolution = secondsPerTick;
 }
 public double getResolution () {
  return resolution;
 }

// methods
 public double currentTimeSelectedRes(){
  return resolution*currentTimeMillis();
 }
 public long currentTimeMillis(){
  synchronized (currentTime){ //thread safe!
   return currentTime[0];
  }
 }

// running functions
// Asynchronous clock generator
 public void run(){
  Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
  while (true)
   loopStep();
 }
// Synchronous clock generator
 public void tick(){
  if (!this.isAlive())
   loopStep();
 }
}
