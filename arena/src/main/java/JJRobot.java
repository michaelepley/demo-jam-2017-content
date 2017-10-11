/*
  JJRobot.java version 4.00 alfa (2003/10/19)
    Copyright (C) 2001-2003 Leonardo Boselli (boselli@uno.it)

  Portions of this program were written by:
    Christo Fogelberg (doubtme@hotmail.com)
    Alan Lund (alan.lund@acm.org)
    Tim Strazny (timstrazny@gmx.de)
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

abstract public class JJRobot implements Runnable {

static final private double DEG2RAD = Math.PI/180;
static final private double RAD2DEG = 180/Math.PI;

private JJRobots jjRobots;
private int id;

JJRobot() {
}

final public void run() {
  try {
    main();
  } catch(Exception e) {
    jjRobots.kre(this);   //sg! robot exits due to exception
    e.printStackTrace();
 }
finally
	{
    jjRobots.krf(this);   //sg! the robot may decide to exit on its own
	}
}

abstract void main();

final void init(int id, JJRobots jjRobots) {
  if(jjRobots.regID(this)) {
    this.id = id;
    this.jjRobots = jjRobots;
  }
}

final int getID() {
  return id;
}

final protected int id() {
  return jjRobots.id(this);
}

final protected int getFriendsCount() {
  return jjRobots.getFriendsCount();
}

final protected double time() {
  return jjRobots.time();
}

final protected int scan(int degree, int resolution) {
  return jjRobots.scan(this,degree,resolution);
}

final protected int cannon(int degree, int range) {
  return jjRobots.cannon(this,degree,range);
}

final protected void drive(int degree, int speed) {
  jjRobots.drive(this,degree,speed);
}

final protected int damage() {
  return jjRobots.damage(this);
}

final protected int speed() {
  return jjRobots.speed(this);
}

final protected int loc_x() {
  return i_rnd(jjRobots.loc_x(this));
}

final protected int loc_y() {
  return i_rnd(jjRobots.loc_y(this));
}

// - Additions by alan.lund@acm.org
final protected double d_loc_x() {
  return jjRobots.d_loc_x(this);
}

final protected double d_loc_y() {
  return jjRobots.d_loc_y(this);
}

final protected int cannon(JJVector v) {
  return jjRobots.cannon(this, i_rnd(v.angle()), i_rnd(v.mag()));
}

final protected JJVector vscan(int degree, int resolution) {
	int distance = jjRobots.scan(this,degree,resolution);

	if (distance > 0)
		return JJVector.Polar(distance, degree);
	else
		return null;
}

final protected void drive(JJVector v) {
  jjRobots.drive(this, i_rnd(v.angle()), i_rnd(100.0 * v.speed() / 30.0));
}

final protected double actual_speed() {
  // Returns 0.0 to 30.0, not 0 to 100 like speed()
  return jjRobots.actual_speed(this);
}

final protected double heading() {
  return jjRobots.heading(this);
}

final protected JJVector velocity() {
  return JJVector.Polar(jjRobots.actual_speed(this),
                        rad2deg(jjRobots.heading(this)),
                        1.0);
}

final protected JJVector location() {
  return new JJVector(jjRobots.d_loc_x(this),
                      jjRobots.d_loc_y(this),
                      jjRobots.time());
}
// - End additions by alan.lund@acm.org

static protected final int rand(int limit) {
  return (int)(Math.random()*limit);
}

static protected final int sqrt(int number) {
  return (int)Math.sqrt(Math.abs(number));
}

static final double deg2rad(int degree) {
  int deg = degree%360;
  if(deg > 180) deg -= 360;
  return deg*DEG2RAD;
}

static protected final int sin(int degree) {
  return (int)(100000*Math.sin(deg2rad(degree)));
}

static protected final int cos(int degree) {
  return (int)(100000*Math.cos(deg2rad(degree)));
}

static protected final int tan(int degree) {
  return (int)(100000*Math.tan(deg2rad(degree)));
}

static protected final int atan(int value) {
  return (int)(Math.atan(value/100000.0)/DEG2RAD);
}

static protected final double d_sqrt(double number) {
  return Math.sqrt(number);
}

static protected final double d_sin(double rad) {
  return Math.sin(rad);
}

static protected final double d_cos(double rad) {
  return Math.cos(rad);
}

static protected final double d_tan(double rad) {
  return Math.tan(rad);
}

static protected final double d_atan(double value) {
  return Math.atan(value);
}

// Additions Below Here By Christo Fogelberg (doubtme@hotmail.com)
// Send comments/bugs etc my way :)
static protected final double d_abs(double value) {
	return Math.abs(value);
}

static protected final double d_rnd(double value) {
	return Math.floor(value + 0.5d);
}

static protected final int i_rnd(double value) {
	return (int) Math.floor(value + 0.5d);
}

static protected final double rad2deg(double value) {
	double deg = value * RAD2DEG;
	while(deg < 0) {
		deg = 360 + deg;
	}
	return deg%360;
}

// Additions Below Here By Tim Strazny (timstrazny@gmx.de)

static protected final double exp(double value) {
  return Math.exp(value);
  // e^value
}

static protected final double log(double value) {
  return Math.log(value);
  // ln value
}

protected final void finalize() {
}

// Additions Below Here By Samuel J. Grabski (frostytower@hotmail.com)
// enjoy more math functions :)
static protected final double floor(double value) {
	return Math.floor(value);
}
static protected final double ceil(double value) {
	return Math.ceil(value);
}
static protected final double atan2(double x, double y) {
	return Math.atan2(x,y);
}
static protected final double asin(double x) {
	return Math.asin(x);
}
static protected final double acos(double x) {
	return Math.acos(x);
}
static protected final double min(double x,double y) {
	return Math.min(x,y);
}
static protected final double max(double x,double y) {
	return Math.max(x,y);
}
static protected final double round(double x) {
	return Math.round(x);
}
// - End additions by Samuel J. Grabski (frostytower@hotmail.com)

}
