//
// JJRobots (c) 2000 L.Boselli - boselli@uno.it
//
public class __Stinger_ extends JJRobot {

private static int counter;

private static int locX[] = new int[8];
private static int locY[] = new int[8];

private static int driveAngle = 5;

private double oldTargetX;
private double oldTargetY;
private double targetX;
private double targetY;
private double speedX;
private double speedY;
private double lastTime;
private int range;
private int scan;
private int drive;
private int id;

void main() {
  if((id = id()) == 0) {
    counter = 1;
  } else {
    counter = id+1;
  }
  targetX = targetY = -1000;
  speedX = speedY = 0;
  lastTime = 0;
  drive(drive=rand(360),100);
  while(true) {
    do {
      if(findNearestEnemy(0)) shoot();
    } while(scan-driveAngle < drive && drive < scan+driveAngle);
    stopAndGo();
  }
}

private boolean findNearestEnemy(int minDistance) {
  int startAngle = 0;
  int endAngle = 360;
  int nearestAngle = 0;
  int nearestDistance = 0;
  for(int resAngle = 16; resAngle >= 1; resAngle /= 2) {
    nearestDistance = 2000;
    for(
      scan = startAngle;
      scan <= endAngle;
      scan += resAngle
    ) {
      range = scan(scan,resAngle);
      if(range > minDistance+40 && range < nearestDistance) {
        nearestDistance = range;
        nearestAngle = scan;
      }
    }
    startAngle = nearestAngle-resAngle;
    endAngle = startAngle+2*resAngle;
  }
  range = nearestDistance;
  scan = nearestAngle;
  if(range > 0) {
    double time;
    double deltaT = (time = time()) - lastTime;
    targetX = (locX[id]=loc_x())+range*cos(scan)/100000.0;
    targetY = (locY[id]=loc_y())+range*sin(scan)/100000.0;
    if(isTargetAFriend()) return findNearestEnemy(range);
    if(deltaT > 0.5) {
      double theSpeedX = (targetX-oldTargetX)/deltaT;
      double theSpeedY = (targetY-oldTargetY)/deltaT;
      oldTargetX = targetX;
      oldTargetY = targetY;
      double speed2 = theSpeedX*theSpeedX + theSpeedY*theSpeedY;
      if(speed2 > 0) {
        if(speed2 < 1600) {
          speedX = theSpeedX;
          speedY = theSpeedY;
        } else {
          speedX = speedY = 0;
        }
      }
      lastTime = time;
    }
    return true;
  }
  return false;
}

private boolean isTargetAFriend() {
  if(counter > 1) {
    for(int ct = 0; ct < counter; ct++) {
      if(ct != id) {
        int dx = (int)(targetX-locX[ct]);
        int dy = (int)(targetY-locY[ct]);
        if(dx*dx+dy*dy < 6400) return true;
      }
    }
  }
  return false;
}

private void stopAndGo() {
  drive(drive=scan,49);
  while(speed() >= 50) {
    findNearestEnemy(0);
    shoot();
  }
  int dx = (int)(targetX-(locX[id] = loc_x()));
  int dy = (int)(targetY-(locY[id] = loc_y()));
  if(dx == 0) {
    drive = dy > 0? 90: 270;
  } else {
    drive = atan(dy*100000/dx);
    if(dx < 0) drive += 180;
  }
  drive(drive,100);
}

private void shoot() {
  if(range > 50 && range <= 800) {
    fireToTarget(targetX,targetY,speedX,speedY,lastTime);
  } else if(range > 0 && range <= 50) {
    cannon(scan,45);
  }
}

private void fireToTarget(double x, double y, double sx, double sy, double t) {
  double Dx, Dy;
  double deltaT = time()-t;
  if(deltaT > 0) {
    x += sx*deltaT;
    y += sy*deltaT;
    Dx = x-(locX[id]=loc_x());
    Dy = y-(locY[id]=loc_y());
  } else {
    Dx = x-(locX[id]=loc_x());
    Dy = y-(locY[id]=loc_y());
  }
  double dxsymdysx = Dx*sy-Dy*sx;
  double tp =
    (d_sqrt((Dx*Dx+Dy*Dy)*90000-dxsymdysx*dxsymdysx)+Dx*sx+Dy*sy)/
    (90000-sx*sx-sy*sy)
  ;
  double rx = Dx+sx*tp;
  double ry = Dy+sy*tp;
  double r2 = rx*rx+ry*ry;
  if(r2 > 1600 && r2 < 547600) {
    double angle;
    if(rx == 0) {
      angle = ry > 0? 1.5708: 4.7124;
    } else {
      angle = d_atan(ry/rx);
      if(rx < 0) angle += 3.1416;
    }
    int degrees = (int)(angle*180/3.1416);
    cannon(degrees,(int)(d_sqrt(r2)+0.5));
  }
}

}
