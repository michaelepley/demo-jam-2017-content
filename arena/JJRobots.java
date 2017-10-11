/*
  JJRobots.java version 4.00 alfa (2003/10/19)
    Copyright (C) 2001-2003 Leonardo Boselli (boselli@uno.it)

  Portions of this program were written by:
    Moray Goodwin (moray@jyra.com)
    Christo Fogelberg (doubtme@hotmail.com)
    Alan Lund (alan.lund@acm.org)
    Tim Strazny (timstrazny@gmx.de)
    Walter Nistico' (walnist@infinito.it)
    Samuel (a1rex@hotmail.com)
---

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the
    Free Software Foundation, Inc.,
    59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

  Please send remarks, questions and bug reports to
    boselli@uno.it
  or write to:
    Leonardo Boselli
    Via Paoletti, 33
    18100 Imperia
    ITALY
*/

// Changes by S.G.:
// Main changes from version 3.1 to 4.0
// 1. Step function has been rewritten to achive:
// 1.a) speed improvement - it is not calculating trigonometric functions any more
// 1.b) precision of the simulation it is step size independent now
// 1.c) bug fix for multi damages
// 3. Robot exceptions are handled as fatal damages reducing simulation time
// 4. Numerical reporting for damages introduced (invented by A.L.)
// 5. Aplication should run smoothly now, prority has been altered
// 6. Corrupted robots are detected and reported and will not skew the results any more

import java.awt.*;
import java.util.*;

public final class JJRobots extends Canvas {

 int jjarenaPriority = 0;
 boolean draw = true;

//fields/static/final
static final private int PRIORITY  = 3;    // app priority
static final private int BF_SZ     = 1000; // field size
static final private int MS_RN     = 700;  // missile range
static final private int MS_SP     = 300;  // missile speed
static final private int MX_SP     = 30;   // max robot speed (when changing change MX_SP_H also!)
static final private int MX_SP_H   = 15;   // sg! half of the max robot speed MX_SP_H = 0.5*MX_SP
static final private int MX_RS     = 20;   // max resolution
static final private int JR_AC     = 5;    // robot acceleration (when changing change JR_AC_I/JR_AC_H also!)
static final private int CN_MS     = 4;    // missile number
static final private int CN_RT     = 1;    // cannon reload time

static final private int TM_OT      = 180;         // timeout
static final private double MX_SP_R = MX_SP/100.0; // sg!  conversion to real speed factor
static final private double MS_SP_I = 1/MS_SP;     // sg!  missile speed inverted

static final private double JR_AC_I = 0.2;         // sg! robot inverted acceleration JR_AC_I = 1/JR_AC; precomputed for precision
static final private double JR_AC_H = 2.5;         // sg! robot half of acceleration JR_AC_H  = JR_AC/2; precomputed for precision
static final private double MTM     = 0.25;			 	 // sg! PROTECTION minimum virtual time for a match

private static final int    maxTracks = 1000;
private static final double trackStep = 7;

static final int SINGLE = 0;
static final int DOUBLE = 1;
static final int TEAM = 2;

static final int N_SINGLE = 1;
static final int N_DOUBLE = 2;
static final int N_TEAM = 8;

static final Color cyan = Color.cyan;
static final Color orange = Color.orange;
static final Color green = Color.green;
static final Color pink = Color.pink;
static final Color darkYellow = Color.yellow.darker();
static final Color darkCyan = cyan.darker();
static final Color darkOrange = orange.darker();
static final Color darkGreen = green.darker();
static final Color darkPink = pink.darker();

//fields/static

static volatile private boolean isApplication = false; //

//fields/private
volatile private boolean fde = true;                    // fatal damage to a robot due to exception

private Dimension imageDimension;
private Image offscreenImage;

private int mode;
private String[] names;
private int winner = -1;
private StringBuffer status = new StringBuffer(80);
private String lastWinner=null;
private	boolean classException = false;                // robot class is corrupted

volatile private Thread[] tr;    // system  - robots thread
volatile private JJRobot[] jr;   // JJRobots
volatile private double[] lcx;   // robot   - loc_x, loc_y, drive(angle), speed(0-30)
volatile private double[] lcy;
volatile private double[] lcd;
volatile private double[] lcs;
volatile private double[] clcd;  //cos(robot_drive_angle)
volatile private double[] slcd;  //sin(robot_drive_angle)

volatile private double[] sts;   // robot   - speed, damage, shot time
volatile private double[] stt;
volatile private int[]    std;
volatile private int[] sd;       // robot   - scan degree, resolution, range
volatile private int[] sr;
volatile private int[] sl;
volatile private double[][] msx; // missile 0,1,2 - loc_x, loc_y, drive, range
volatile private double[][] msy;
volatile private double[][] msd;
volatile private double[][] msr;
volatile private double[][] cmsd; //cos(missile_drive_angle)
volatile private double[][] smsd; //sin(missile_drive_angle)

volatile private double tm;       // timer   - virtual time now

private int[] damageRemaining = new int[4];
private int[] kills           = new int[4];
private int[] damageInflicted = new int[4];

private static final byte trackVars = 3;
private int tracks;
volatile private int[] tracked; // [robot]
volatile private double[][][] track; // [robot][tracknr][0=x; 1=y; 2=speed]

volatile private int explosionX[] = new int[32];
volatile private int explosionY[] = new int[32];
volatile private int explosionCount[] = new int[32];
volatile private int maxExplosions;

volatile private boolean showTracks;

volatile private boolean showTraces;
volatile private boolean showScans;

//methods/set&get

final static void isApplication(boolean b) {
		isApplication = b;
}

final void setShowScans(boolean b) {
  showScans = b;
}

final void setShowTraces(boolean b) {
  showTraces = b;
}

final void setShowTracks(boolean b) {
  if (b != showTracks) {
    if (b) {
      if(jr == null) createTrack(8);
      else createTrack(jr.length);
    }
    else
      deleteTrack();
    showTracks = b;
  }
}

final void createTrack(int c) {
  if (c != 0) {
    tracks = maxTracks / c;
    tracked = new int[c];
    track = new double[c][tracks][trackVars];
  }
}

final void deleteTrack() {
  track = null;
  tracked = null;
}

//methods/package

final boolean regID(JJRobot r) {
  int ct;
  for(ct = 0; ct < jr.length && jr[ct] != r; ct++);
  if(ct == jr.length) return false;
  return true;
}


// final in JJRobot.java
//
double time() {
  Thread.yield();
  return tm;
}

int id(JJRobot r) {
  return r.getID()%mode;
}

int getFriendsCount() {
   return mode;
}

int scan(JJRobot r, int degree, int resolution) {
  Thread.yield();
  int id = r.getID();
  if(std[id] >= 100){
   Thread.yield();
	 return 0;
	 }
  if(resolution < 1) resolution = 1;
  else if(resolution > MX_RS) resolution = MX_RS;
  double res = JJRobot.deg2rad(resolution)*0.5;
  double rad = JJRobot.deg2rad(degree);
  double range = Double.MAX_VALUE;
  sd[id] = degree-resolution/2;
  sr[id] = resolution;
  for(int ct = 0; ct < jr.length; ct++) {
    if(ct != id && std[ct] < 100) {
      double dx = lcx[ct]-lcx[id];
      double dy = lcy[ct]-lcy[id];
      double ang = Math.atan2(dy,dx);
      double diff = Math.abs(rad-ang);
      if(diff >= Math.PI) diff = 2.0*Math.PI - diff;
      if(diff < res) range = Math.min(range,dx*dx+dy*dy);
    }
  }
  if(range == Double.MAX_VALUE) return 0;
  int misRange = (int)(Math.sqrt(range));
  if(misRange <= MS_RN) sl[id] = misRange;
  return misRange;
}

int cannon(JJRobot r, int degree, int range) {

  int id = r.getID();

  if(std[id] >= 100){                      // no rights to shoot
    Thread.yield();
	  return 0;
	}

  if(tm - stt[id] < CN_RT){                // too early to shoot
    Thread.yield();
	  return 0;
	}

  int mis;                                 // missile number
  int misWait = -1;

  for(mis = 0; mis < CN_MS; mis++) {       // check all missiles
    double m = msr[id][mis];               // get missile range
    if(m < 0) {                            //
      if(m < -1)                           // missile exploded
        break;                             // ready to be shoot
      else
        misWait = mis;                     // waiting to be shoot
    }
  }


  if(mis == CN_MS) {                       // all missiles checked
    if(misWait < 0)                        // nothing available
		{
     Thread.yield();
     return 0;
		}
    else
      mis = misWait;                       // fire this missile
  }

  msx[id][mis] = lcx[id];                  // from this position
  msy[id][mis] = lcy[id];                  //
  msd[id][mis] = JJRobot.deg2rad(degree);  // missile traveling angle
	cmsd[id][mis] = Math.cos(msd[id][mis]);  // just calculate it here
	smsd[id][mis] = Math.sin(msd[id][mis]);
  stt[id] = tm;                            // robot shoot time
  msr[id][mis] = Math.min(Math.abs(range),MS_RN);
  return 1;
}

void drive(JJRobot r, int degree, int speed) {

  if(speed > 100) speed = 100;
  else if(speed < 0) speed = 0;
  int id = r.getID();

  Thread.yield();                       // give time to others at this spot so hopefully we will not
																				// be interupted at the next few instructions
  if(lcs[id] <= MX_SP_H){               // only if speed less than half of maximum turn is allowed
	  lcd[id] = JJRobot.deg2rad(degree);  // new heading in radians
	 clcd[id] = Math.cos(lcd[id]);        // computational burden moved to robot
	 slcd[id] = Math.sin(lcd[id]);
	}

  sts[id] = speed*MX_SP_R;              // speed is expressed in percentage,
																				// sts[] is a real desired speed
  if((lcs[id] == 0) && (sts[id] > 0))   // if robot is stopped and wants to move
	    lcs[id] = MX_SP_R;                // give robot speed right away (adventage=0.06 sec head start)
}

int damage(JJRobot r) {
  Thread.yield();
  return std[r.getID()];
}

int speed(JJRobot r) {
  Thread.yield();
  return (int)(lcs[r.getID()]*100.0/MX_SP);
}

int loc_x(JJRobot r) {
  Thread.yield();
  return (int)lcx[r.getID()];
}

int loc_y(JJRobot r) {
  Thread.yield();
  return (int)lcy[r.getID()];
}

double d_loc_x(JJRobot r) {
  Thread.yield();
  return lcx[r.getID()];
}

double d_loc_y(JJRobot r) {
  Thread.yield();
  return lcy[r.getID()];
}

double actual_speed(JJRobot r) {
  // Returns 0 to MX_SP, not 0 to 100!
  return lcs[r.getID()];
}

double heading(JJRobot r) {
  return lcd[r.getID()];
}

//-----------------------------------------------------------------

/*synchronized*/ final void init(int mode, String[] name) {

  try {
    tm = 0;
    int count = name.length;
    this.mode = mode;
    int counter = 0;
    names = new String[count*mode];
    jr = new JJRobot[count*mode];

    classException = false;

    for (int i = 0; i < count; i++)
    {
      kills[i] = 0;
      damageInflicted[i] = 0;
    }

    for(int ct = 0; ct < count; ct++) {
      for(int ct1 = 0; ct1 < mode; ct1++) {
        if(name[ct].charAt(0) == '_') {
          names[counter] = name[ct].substring(2);
        } else {
          names[counter] = name[ct].substring(1);
        }
        names[counter] = names[counter].substring(0,names[counter].indexOf('_'));


        try {
          jr[counter] =
					(JJRobot)Class.forName(name[ct]).newInstance(); //sg! exceptions observed
          counter++;
        } catch(Exception e) {// e.g. classNotFoundException
					classException = true;
					fde = true;
          jr[counter] = null;
          counter++;                    // keeps names counter going!
         //e.printStackTrace();
        } catch(LinkageError e) {
					classException = true;
					fde = true;
          jr[counter] = null;
          counter++;                    // keeps names counter going!
          e.printStackTrace();
        }
      }
    }


    count *= mode;
    lcx = new double[count];
    lcy = new double[count];
    lcd = new double[count];
    lcs = new double[count];
		clcd = new double[count];
    slcd = new double[count];
    msx = new double[count][CN_MS];
    msy = new double[count][CN_MS];
    msd = new double[count][CN_MS];
    cmsd = new double[count][CN_MS];
    smsd = new double[count][CN_MS];
    msr = new double[count][CN_MS];
    sts = new double[count];
    stt = new double[count];
    std = new int[count];
    sd = new int[count];
    sr = new int[count];
    sl = new int[count];
    if (showTracks) createTrack(count);

    for(int ct = 0; ct < count; ct++) {

      if(jr[ct] != null) {
        jr[ct].init(ct,this);
        std[ct] = 0;
      } else {
        std[ct] = 100;
      }

      lcx[ct] = JJRobot.rand(BF_SZ);
      lcy[ct] = JJRobot.rand(BF_SZ);
      lcd[ct]  = 0;
			clcd[ct] = 1;
			slcd[ct] = 0;
      lcs[ct] = 0;
      sd[ct] = 0;
      sr[ct] = 0;
      sl[ct] = 0;
      for(int mis = 0; mis < CN_MS; mis++) msr[ct][mis] = -1;
      sts[ct] = 0;
      stt[ct] = 0;
    }
    if(jr.length > 4) maxExplosions = 32;
    else maxExplosions = jr.length*4;
    for(int ct = 0; ct < 32; ct++) explosionCount[ct] = 0;
  } catch(Exception e) {
    e.printStackTrace();
  }
}

final synchronized void start() {
  try{ Thread.sleep(1000); } catch(InterruptedException e) {}

	try{

  Thread[] current = new Thread[Thread.activeCount()];
  int count = Thread.enumerate(current);

  for (int i=0; i<count; i++){

	// The lines bellow works well for standalone applet and application
	// but not for direct invocation of jjr_match.html by Explorer

   if(isApplication)                      // comment out to if only application is going to benefit
	  if(current[i].isAlive())              // do it only on alive one!
	     current[i].setPriority(PRIORITY);  // very important to run above priority of the standard application!
	// ---------------------------------------------------------------

   if (current[i].getName().equals("JJClock"))
    Thread.currentThread().setPriority(current[i].getPriority());

		//sg! it is easy to build protection for extra unterminated threats here
	}

//  Thread.currentThread().setPriority(JJArena.getClockPriority());
//
  tr = new Thread[jr.length];

/* SERIAL START
  for(int ct = 0; ct < jr.length; ct++) {
    if(jr[ct] != null) {
      tr[ct] = new Thread(jr[ct], names[ct] + " - " + id(jr[ct]));
      tr[ct].start();
    }
  }
*/
// MIXED START (2003/09/04)
  for(int theMate = 0; theMate < mode; theMate++) {
    for(int theTeam = 0; theTeam < jr.length/mode; theTeam++) {
      int theBot = theMate+theTeam*mode;
      if(jr[theBot] != null) {
        tr[theBot] =                    // java.lang.NullPointerException observed
        new Thread(jr[theBot],
        names[theBot] +
	              " - " +
        id(jr[theBot]));

	      // The line bellow works well for standalone applet and application
        // but not for direct invocation of jjr_match.html by Explorer
         if(isApplication)
	        tr[theBot].setPriority(PRIORITY);  // very important to run above priority
																			  			// of the standard application!
         tr[theBot].start();
      }
    }
  }
	}catch(Exception e) {
    e.printStackTrace();
	}
}

final synchronized void stop() {
try{
 if(tr != null) {
 for(int ct = 0; ct < jr.length; ct++) {
   if(tr[ct] != null) {
 	   if(tr[ct].isAlive())           //sg! important dead threads would do exceptions
        tr[ct].stop();
        tr[ct] = null;
      }
     }
    tr = null;
   }
  } catch(Exception e){
    e.printStackTrace();
 }
}


final void setTrack(int id) {
    try {
      int p = tracked[id] % tracks;
      track[id][p][0] = lcx[id];
      track[id][p][1] = lcy[id];
      track[id][p][2] = lcs[id];
      p = (tracked[id]-1) % tracks;
      if((p < 0) ||
         (Math.abs(track[id][p][0]-lcx[id]) > trackStep) ||
         (Math.abs(track[id][p][1]-lcy[id]) > trackStep)) {
        tracked[id]++;
        p = tracked[id] % tracks;
        track[id][p][0] = lcx[id];
        track[id][p][1] = lcy[id];
        track[id][p][2] = lcs[id];
      }
    } catch (Exception e) {/* ignore */}
//  }
}

final void drawArena(boolean forcedStop) {

  Graphics og = offscreenImage.getGraphics();
  int width = imageDimension.width;
  int height = imageDimension.height;
  int size = Math.min(width,height);
  int off = 20;
  int side = size-off*2;

  og.setColor(Color.lightGray);
  og.fillRect(0,0,width,height);
  if(draw) {
    og.setColor(Color.white);
    og.fillRect(off,off,side,side);
    og.setColor(Color.red);
    og.drawRect(off,off,side,side);
  }

  status.setLength(0);

  if(lastWinner != null) {
    status.append("The last winner is : ").append(lastWinner)
    .append("   -  Time  ").append((int)tm);
  } else {
    status.append("Time  ").append((int)tm);
  }

	if( (forcedStop) && (winner > -1) ){
		winner = 0;           // forced stop break in the middle of the match
	}

  if(winner >= 0) {
    if(winner == 0) {
      status.append("  -  ").append(lastWinner = " Timeout or Draw"); //sg!
		 } else {
      status.append("  -  ").append(lastWinner = names[(winner-1)*mode])
      .append(" won");
    }
		winner = -1;
  }

  og.setColor(Color.darkGray);
//  og.drawString(str,3,13);
//  og.setColor(Color.gray);
  og.drawString(status.toString(),2,14);
	double constant = side*0.001;
  double constant2 = constant*2;

  if (draw){

    double expRad = side*0.005;
    int iExpRad = (int)(expRad*8+0.5);

    int ptx, pty;

    for(int ct = 0; ct < jr.length; ct++) {
      if(std[ct] < 100) {
        if(showScans && (sr[ct] != 0)) {
          ptx = (int)(lcx[ct]*constant+0.5)+off;
          pty = (int)(lcy[ct]*constant+0.5)+off;
          int rng = (int)(side*sl[ct]/BF_SZ+0.5);
          int rng2 = rng*2;
          og.setColor(Color.lightGray);
          og.fillArc(ptx-rng,pty-rng,rng2,rng2,-sd[ct],-sr[ct]);
          sr[ct] = 0;
        }
        for(int mis = 0; mis < CN_MS; mis++) {
          if(msr[ct][mis] < 0) {
            if(msr[ct][mis] < -1) {
              for(int expCt = 0; expCt < maxExplosions; expCt++) {
                if(explosionCount[expCt] == 0) {
                  explosionX[expCt] = (int)(msx[ct][mis]*constant+0.5)+off;
                  explosionY[expCt] = (int)(msy[ct][mis]*constant+0.5)+off;
                  explosionCount[expCt] = 4;
                  break;
                }
              }
              msr[ct][mis] = -1;
            }
          } else {

            ptx = (int)(msx[ct][mis]*constant+0.5)+off;
            pty = (int)(msy[ct][mis]*constant+0.5)+off;

            if(showTraces) {
              Color fillColor;
              Color borderColor;
              switch(ct/mode) {
                default:
                case 0: {
                  fillColor = Color.green;
                  borderColor = darkGreen;
                  break;
                }
                case 1: {
                  fillColor = Color.orange;
                  borderColor = darkOrange;
                  break;
                }
                case 2: {
                  fillColor = Color.cyan;
                  borderColor = darkCyan;
                  break;
                }
                case 3: {
                  fillColor = Color.pink;
                  borderColor = darkPink;
                  break;
                }
              }
              int endx = ptx+(int)(msr[ct][mis]*cmsd[ct][mis]*constant+0.5);
              int endy = pty+(int)(msr[ct][mis]*smsd[ct][mis]*constant+0.5);
             //og.setColor(fillColor);          // al
							og.setColor(Color.lightGray);     // al
              og.drawLine(ptx,pty,endx,endy);
             //og.fillOval(endx-iExpRad,endy-iExpRad,iExpRad*2,iExpRad*2);  // al
              og.fillOval(endx-3,endy-3,6,6);  // al
              og.setColor(borderColor);
              og.drawOval(endx-iExpRad,endy-iExpRad,iExpRad*2,iExpRad*2);
            }

            og.setColor(Color.darkGray);
            og.fillRect(ptx-1,pty-1,2,2);

          }
        }
      }
    }

    int jExpRad;
    og.setColor(darkYellow);
    jExpRad = iExpRad+1;
    for(int ct = 0; ct < maxExplosions; ct++) {
      if(explosionCount[ct] > 0) {
        og.drawOval(
          explosionX[ct]-jExpRad,explosionY[ct]-jExpRad,jExpRad*2,jExpRad*2
        );
      }
    }
    og.setColor(Color.yellow);
    jExpRad = iExpRad;
    for(int ct = 0; ct < maxExplosions; ct++) {
      if(explosionCount[ct] > 0) {
        og.fillOval(
          explosionX[ct]-jExpRad,explosionY[ct]-jExpRad,jExpRad*2,jExpRad*2
        );
      }
    }
    og.setColor(Color.orange);
    jExpRad = (int)(expRad*4+0.5);
    for(int ct = 0; ct < maxExplosions; ct++) {
      if(explosionCount[ct] > 0) {
        og.fillOval(
        explosionX[ct]-jExpRad,explosionY[ct]-jExpRad,jExpRad*2,jExpRad*2
        );
      }
    }
    og.setColor(Color.red);
    jExpRad = (int)(expRad+0.5);
    for(int ct = 0; ct < maxExplosions; ct++) {
      if(explosionCount[ct] > 0) {
        og.fillOval(
          explosionX[ct]-jExpRad,explosionY[ct]-jExpRad,jExpRad*2,jExpRad*2
        );
        explosionCount[ct]--;
      }
    }

    for(int ct = 0; ct < jr.length; ct++) {
      if(std[ct] < 100) {
        if (showTracks && (track != null)) {
          try {
            setTrack(ct);               // sg! setTrack belongs here
            int min = (tracked[ct] < tracks) ? 1 : (tracked[ct]+2)%tracks;
            int count = (tracked[ct] < tracks) ? tracked[ct] : tracks-1;
            for (int i = 0;  i < count; i++) {
              int i1 = (min+i) % tracks;
              int i2 = (i1 == 0) ? tracks-1 : i1-1;
              og.setColor(new Color(0, (float)(track[ct][i1][2]/MX_SP), 0));
              og.drawLine((int)(track[ct][i2][0]*constant+0.5)+off, (int)(track[ct][i2][1]*constant+0.5)+off,
                          (int)(track[ct][i1][0]*constant+0.5)+off, (int)(track[ct][i1][1]*constant+0.5)+off);
            }
          } catch (Exception e) {/* ignore */}
        }
        double lcxc = lcx[ct]*constant;
        double lcyc = lcy[ct]*constant;
        ptx = (int)(lcxc+0.5)+off;
        pty = (int)(lcyc+0.5)+off;
        Color fillColor;
        Color borderColor;
        switch(ct/mode) {
          default:
          case 0: {
            fillColor = green;
            borderColor = darkGreen;
            break;
          }
          case 1: {
            fillColor = orange;
            borderColor = darkOrange;
            break;
          }
          case 2: {
            fillColor = cyan;
            borderColor = darkCyan;
            break;
          }
          case 3: {
            fillColor = pink;
            borderColor = darkPink;
            break;
          }
        }
        og.setColor(Color.darkGray);
        og.fillOval(ptx-3,pty-3,6,6);
        double spx = lcs[ct]*clcd[ct];
        double spy = lcs[ct]*slcd[ct];
        double stepx = spx*constant2 * 0.5; // was 1.0
        double stepy = spy*constant2 * 0.5;
        int endx = (int)(lcxc+stepx+0.5)+off;
        int endy = (int)(lcyc+stepy+0.5)+off;

        if(lcs[ct] <= 15) {
          og.drawLine(ptx,pty,endx,endy);
        } else {
          double cts = 15/lcs[ct];
          int midx = (int)(lcxc+stepx*cts+0.5)+off;
          int midy = (int)(lcyc+stepy*cts+0.5)+off;
          og.drawLine(ptx,pty,midx,midy);
          og.setColor(Color.red);
          og.drawLine(midx,midy,endx,endy);
        }

        og.setColor(borderColor);
//        og.drawString(names[ct],ptx+6,pty-4);
//        og.setColor(fillColor);
//       og.drawString(names[ct],ptx+5,pty-5); // replaced by:
         og.drawString(names[ct] + "-" + (ct%mode) + ":" + std[ct],ptx+20,pty-20);

// colour damage bar
        int len = std[ct]/2;
        og.setColor(fillColor);
        og.fill3DRect(ptx+6+len,pty-2,50-len,2,true);
        if(len > 0) {
          og.setColor(Color.red);
          og.fill3DRect(ptx+6,pty-2,len,2,true);
        }
      }
    }

   og.setColor(Color.lightGray);
   int oneEnd   = (int) off;
   int otherEnd = (int) (1000 * constant + off);
   for (int i = 1; i < 10; i++)
   {
     int v = (int) (i * 100 * constant + off);

     og.drawLine(     v, oneEnd,        v, otherEnd);
     og.drawLine(oneEnd,      v, otherEnd,        v);
   }
  }

  if (draw)
  {
    for (int i = 0; i < jr.length / mode; i++)
      damageRemaining[i] = 0;

    for (int i = 0; i < jr.length; i++)
      if (std[i] < 100)
        damageRemaining[i / mode] += (100 - std[i]);

    og.setColor(Color.darkGray);

    for (int i = 0; i < jr.length / mode; i++)
    {
      int place = 1;
      for (int j = 0; j < jr.length / mode; j++)
      {
        if (damageRemaining[j] > damageRemaining[i])
          place++;
      }

      int y = (int) (800 * constant + off + (i+1) * 18);

			if( names[i * mode] != null)
       og.drawString(names[i * mode],             30, y);
			else
       og.drawString("???",                       30, y);

			if(jr[i * mode]!=null){
       og.drawString(": "  + damageRemaining[i], 120, y);
       og.drawString(""  + place,                160, y);
			}
			else{
       og.drawString(": corrupted!", 120, y);  // sg! for classes which do not load!!!
			}

			if(jr[i * mode]!=null){
       if (mode > 2)
       {
         og.drawString("" + damageInflicted[i],    180, y);
         og.drawString("" + kills[i],              220, y);
       }
			}
    }
   }

  og.dispose();
  paint(getGraphics());
}

Image getImage() {
  return offscreenImage;
}

//methods/public

final public void paint(Graphics g) {
  if(imageDimension == null) {
    imageDimension = size();//getSize();
    offscreenImage = createImage(imageDimension.width,imageDimension.height);
    return;
  }
  Dimension d = size();//getSize();
  if(d.width != imageDimension.width || d.height != imageDimension.height) {
    imageDimension = d;
    offscreenImage = createImage(imageDimension.width,imageDimension.height);
  }
  g.drawImage(offscreenImage,0,0,this);
}


//methods/private

final private int ot(int id) {
	// functions checks collosions with walls
  boolean hit = false;

  if(lcx[id] < 0) {
    hit = true;
		if(clcd[id]==0)
     lcx[id] = 0;
		else{
    lcy[id] -= lcx[id]*slcd[id]/clcd[id];  // tan(a) = sin(a)/cos(a)
    lcx[id] = 0;
		}
  } else if(lcx[id] > BF_SZ) {
    hit = true;
		if(clcd[id]==0)
     lcx[id] = BF_SZ;
		else{
    lcy[id] += (BF_SZ-lcx[id])*slcd[id]/clcd[id];
    lcx[id] = BF_SZ;
		}
  }

  if(lcy[id] < 0) {
    hit = true;
		if(slcd[id]==0)
     lcy[id] = 0;
		else{
     lcx[id] -= lcy[id]*clcd[id]/slcd[id];
     lcy[id] = 0;
		}
  } else if(lcy[id] > BF_SZ) {
     hit = true;
		 if(slcd[id]==0)
      lcy[id] = BF_SZ;
		 else{
      lcx[id] += (BF_SZ-lcy[id])*clcd[id]/slcd[id];
      lcy[id] = BF_SZ;
		 }
  }

  if(hit) {
    lcs[id] = 0;
    sts[id] = 0;              // to prevent robots self destruction
    std[id] += 2;
    if(std[id] >= 100)
  		return id;
	}
  return -1;
}

final private int kr() {
	// function
 int	w = -1;
 int count = jr.length/mode;

 if(tr!=null){
  for(int ct = 0; ct < jr.length/mode; ct++) {
    int ct_ = ct*mode;
    int ct1;
    for(ct1 = 0; ct1 < mode; ct1++) {
    	if(std[ct_+ct1] < 100) {
        w = ct;
        break;
      }
    }
    if(ct1 == mode) count--;
  }

  if(count <= 1) {
    return w+1;
  }
 }
 return -1;
}

 final void kre(JJRobot r) {// called after robot terminates due to exception
	try{
	fde = true;                               //  thread terminated
	std[r.getID()]=100;
	}catch(Exception e) {}
 }

 final void krf(JJRobot r) {// called after robot exits
	try{
	fde = true;                               //  thread terminated
	std[r.getID()]=100;
	}catch(Exception e) {}
 }


 final int step(double dt) {
// Rewritten by Samuel J. Grabski send comments/bugs to frostytower@hotmail.com
// This function calculates:
// 1) new positions and speed for all robots
// 2) missile new positions
// 3) damages due to exploding missiles
// 4) damagages due to collisions with arena walls
// 5) simulation time tm in dt increments
//
  tm += dt;                       // increase simlation time

 //-----------------------------------------------------------------
  if(tm >= TM_OT) return winner = 0;    // timeout stop the match
 //-----------------------------------------------------------------

  double rs;                      // robot relocates due to its speed d = v *t
  double rd;                      // robot distance change;
  double vnext;                   // robot's speed after this step
  double t1;                      // over/understepped time
  double t2;                      // remaining time to finish this step (t1+t2 = dt)
  double range;                   // remaining missile distance to the target
  double sdif;					 				 	// robots speed difference between current and required speed
  double delta;                   // robots speed increase due to acceleration    v = a*t
  double ra;                      // robots distance increase due to acceletation d = 0.5*a*t*t
	double dte;                     // explosion time
  boolean fd = false;             // fatal damage to robot

 //-----------------------------------------------------------------
 // calculate robots damages due to the exploding missiles
 //-----------------------------------------------------------------

 double misds = dt*MS_SP;        // missile can move so match during full time step dt

 for(int ct = 0; ct < jr.length; ct++) {     // for all robots
    for(int mis = 0; mis < CN_MS; mis++) {   // check their missiles
      if((range = msr[ct][mis]) > 0) {       // if missile is alive
        if(range > misds) {                  // should it still fly?
          msr[ct][mis] -= misds;
          msx[ct][mis] += misds*cmsd[ct][mis];// missile new position
          msy[ct][mis] += misds*smsd[ct][mis];
        } else {                             // explode the missile
				  dte = range * MS_SP_I;             // expolding time
          msr[ct][mis] = -2;                 // missile inactive
          msx[ct][mis] += range*cmsd[ct][mis]; // x coordinate
          msy[ct][mis] += range*smsd[ct][mis]; // y coordinate

					// exploding missile may affect all robots
          for(int ct1 = 0; ct1 < jr.length; ct1++) {
            if(std[ct1] < 100) { // if robot is alive it can be damaged by missile
             double dx = lcx[ct1]-msx[ct][mis]; // last known robot position used
             double dy = lcy[ct1]-msy[ct][mis];
             double dist = dx*dx + dy*dy;
						 double ec = 40.0 + lcd[ct1]*dte + JR_AC_H*dte*dte;  // max robot escape range
						 int d=0;                   // damage taken by robot
						 if(dist < ec*ec)           // robot close enough for damage
							{d = damageByMissile(dte,ct1,ct,mis);}
						 if(d>0){                   // robot damaged
              std[ct1] +=d;
						  boolean killed = false;
              if (std[ct1] >= 100){
						 	killed = true;
              fd = true;
	            }
	            if(draw){ // do not keep count drawing not required
               int firingTeam  = ct  / mode;
               int damagedTeam = ct1 / mode;
               if (firingTeam != damagedTeam){
                damageInflicted[firingTeam] += d;
                if (killed)
                   kills[firingTeam] += 1;
               }      //not self inflicted damage
	            }      //draw information needed
             }      //damage happend
	          }      //if robots is alive
          }       //for all efected robots
        }        //for exploding missile
      }         //for all active missiles
    }          //for all robots missiles
  }           //for all robots
 // ----------------------------------------------------------------

 //
 //-----------------------------------------------------------------
 // calculate robots new locations and new speed
 //-----------------------------------------------------------------
 //
 delta = dt*JR_AC;                // robots speed increase due to acceleration    v = a*t
 ra = 0.5*delta*dt;               // robots distance increase due to acceletation d = 0.5*a*t*t

 for(int ct = 0; ct < jr.length; ct++) {
  if(std[ct] < 100) {             // move all alive robots
   rs  = dt*lcs[ct];              // distance increases due to robot speed d = v *t
   sdif  = sts[ct]-lcs[ct];       // robot speed difference between current and required speed
  if(sdif==0)                     // if robot reached required speed
  {
      lcx[ct] += rs*clcd[ct];     // increase along x axis
      lcy[ct] += rs*slcd[ct];     // increase along y axis
  }
  else                            // robot accelerates or deaccelerats
  {
	if( sdif > 0)                   // if robot accelerates
	{                               // check if we overstepped required speed sts[ct]
	 vnext = lcs[ct] + delta;       // next speed
	 if(vnext > sts[ct]){           // yes we overstepped
	   t1 = sdif*JR_AC_I;           // t1 time when we would overstep
	   t2 = dt - t1;                // remaining time to finish the step
	   rd = lcs[ct]*t1 + JR_AC_H*t1*t1 + sts[ct]*t2;
	   lcx[ct]+= rd*clcd[ct];       // relocation along x axis
	   lcy[ct]+= rd*slcd[ct];       // relocation along y axis
	   lcs[ct] = sts[ct];           // no speed overstepping
	 }
	 else{                          // no we would not overstepped
	  lcx[ct]+= (rs+ra)*clcd[ct];   // relocation along x axis
	  lcy[ct]+= (rs+ra)*slcd[ct];   // relocation along y axis
	  lcs[ct] = vnext;              // speed at the end of time step
	  }
	 }
	else                            // robot deaccelerates
	{
	 vnext = lcs[ct] - delta;       // next speed
	 if(vnext < sts[ct]){           // yes we overstepped
	  t1 = -sdif*JR_AC_I;           // sign change is faster than abs fun
	  t2 = dt - t1;                 // remaining time to finish the step
	  rd = lcs[ct]*t1 - JR_AC_H*t1*t1 + sts[ct]*t2;
	  lcx[ct]+= rd*clcd[ct];        // relocation along x axis
	  lcy[ct]+= rd*slcd[ct];        // relocation along y axis
	  lcs[ct] = sts[ct];            // no speed overstepping
	 }
	 else{                          // no we would not understepped
	 lcx[ct]+= (rs-ra)*clcd[ct];    // relocation along x axis
	 lcy[ct]+= (rs-ra)*slcd[ct];    // relocation along y axis
	 lcs[ct] = vnext;               // speed at the end of time step
	 }
	}
 }

	//----------------------------------------------------------------
	// potential damage due to colisions with walls
	//----------------------------------------------------------------
    if(ot(ct) >= 0) fd=true;
	//----------------------------------------------------------------
  }                               // robot is alive
 }                                // loop for all robots
 //-----------------------------------------------------------------
 //    in this section we check if we have a winner of the match
 // ----------------------------------------------------------------

 if((fd==false) && (fde==false))
   return -1;                      // no fatal damage to a robot

 if(tm<MTM) return -1;             // PROTECTION:

 if(fde)                           // hold it for recheck
    fde=false;                     // exception happened clear it!
																	 //
 if(classException)                // one of the robots is corrupted
    return winner = 0;             // draw no matches with corrupted robots

 boolean alld = true;
 for(int ct=0;ct<jr.length;ct++) { // check if at list one robot
 if(std[ct] < 100) {               // is alive
  alld = false;
  break;
  }
 }

 if(alld)                          // draw - all robots died
    return winner = 0;             // in this time step

  winner = kr();                   //  we may have a winner
  return winner;                   //  -1 no winner 0 draw
}

final private int damageByMissile(double dte, int id, int ct, int mis)
{
	// double dte  - exact time of the missile explosion
	// int id      - index of the robot exposed to the explosion
	// int ct      - missile belongs to robot of this id
	// int mis     - index of the missile
  double rd;                           // robot distance change;
  double vnext;                        // robot's speed after this step
  double t1;                           // over/understepped time
  double t2;                           // remaining time to finish this step (t1+t2 = dt)
	double lx;                           // robot position on the dte time
	double ly;                           //
  double delta = dte*JR_AC;            // robots speed increase due to acceleration    v = a*t
  double ra    = 0.5*delta*dte;        // robots distance increase due to acceletation d = 0.5*a*t*t
  double rs    = dte*lcs[id];          // distance increases due to robot speed d = v *t
  double sdif  = sts[id]-lcs[id];      // robot speed difference between current and required speed

  if(sdif==0)                          // if robot reached required speed
  {
  lx = lcx[id] + rs*clcd[id];          // increase along x axis
  ly = lcy[id] + rs*slcd[id];          // increase along y axis
  }
  else                                 // robot accelerates or deaccelerats
  {
	if( sdif > 0)                        // if robot accelerates
	{                                    // check if we overstepped required speed sts[id]
	 vnext = lcs[id] + delta;            // next speed
	 if(vnext > sts[id]){                // yes we overstepped
	   t1 = sdif*JR_AC_I;                // t1 time when we would overstep
	   t2 = dte - t1;                    // remaining time to finish the dte step
	   rd = lcs[id]*t1 + JR_AC_H*t1*t1 + sts[id]*t2;
	   lx = lcx[id] + rd*clcd[id];       // relocation along x axis
	   ly = lcy[id] + rd*slcd[id];       // relocation along y axis
	 }
	 else{                               // no we would not overstepped
	   lx = lcx[id] + (rs+ra)*clcd[id];  // relocation along x axis
	   ly = lcy[id] + (rs+ra)*slcd[id];  // relocation along y axis
	 }
	}
	else                                 // robot deaccelerates
	{
	 vnext = lcs[id] - delta;            // next speed
	 if(vnext < sts[id]){                // yes we overstepped
	  t1 = -sdif*JR_AC_I;                // sign change is faster than abs fun
	  t2 = dte - t1;                     // remaining time to finish the explosion step
	  rd = lcs[id]*t1 - JR_AC_H*t1*t1 + sts[id]*t2;
	  lx = lcx[id] + rd*clcd[id];        // relocation along x axis
	  ly = lcy[id] + rd*slcd[id];        // relocation along y axis
	 }
	 else{                               // no we would not understepped
	 lx = lcx[id] + (rs-ra)*clcd[id];    // relocation along x axis
	 ly = lcy[id] + (rs-ra)*slcd[id];    // relocation along y axis
	 }
	}
 }

 // check if robot hit the wall
  if(lx < 0) {
		if(clcd[id]==0)  lx = 0;
		else{
    ly -= lx*slcd[id]/clcd[id];  // tan(a) = sin(a)/cos(a)
    lx = 0;
		}
  } else if(lx > BF_SZ) {
		if(clcd[id]==0)  lx = BF_SZ;
		else{
    ly += (BF_SZ-lx)*slcd[id]/clcd[id];
    lx = BF_SZ;
		}
  }
  if(ly < 0) {
		if(slcd[id]==0)  ly = 0;
		else{
     lx -= ly*clcd[id]/slcd[id];
     ly = 0;
		}
  } else if(ly > BF_SZ) {
		 if(slcd[id]==0) ly = BF_SZ;
		 else{
      lx += (BF_SZ-ly)*clcd[id]/slcd[id];
      ly = BF_SZ;
		 }
  }
	//----------------------------------------------------------------

 double dx = lx-msx[ct][mis]; //real robot position used
 double dy = ly-msy[ct][mis]; //
 double dist = dx*dx + dy*dy;

 // calculate the damage:
 int d = dist<1600.0? (dist<400.0? (dist<25.0? 10: 5): 3): 0;
 return d;
}

}/*END*/
