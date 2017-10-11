//
// JJRobots (c) 2000 L.Boselli - boselli@uno.it
//
public class __Phalanx_ extends JJRobot {

private static int counter;
private static int firstCorner;

private static int[] cornerX = {50,950,950,50};
private static int[] cornerY = {50,50,950,950};
private static double lastTargetX;
private static double lastTargetY;
private static double lastTargetSpeedX;
private static double lastTargetSpeedY;
private static double lastTargetTime;
private static int locX[] = new int[8];
private static int locY[] = new int[8];

private double oldTargetX;
private double oldTargetY;
private double targetX;
private double targetY;
private double speedX;
private double speedY;
private double lastTime;
private double lastShotTime;
private boolean foundEnemy;
private int resolution;
private int corner;
private int driveStatus;
private int range;
private int scan;
private int drive;
private int id;

void main() {
  if((id = id()) == 0) {
    counter = 1;
    firstCorner = rand(4);
    lastTargetX = lastTargetY = -1000;
    lastTargetSpeedX = lastTargetSpeedY = 0;
    lastTargetTime = 0;
  } else {
    counter = id+1;
  }
  targetX = targetY = -1000;
  speedX = speedY = 0;
  lastTime = 0;
  resolution = 8;
  corner = firstCorner;
  stopAndGo();
  while(farFromCorner()) {
    findAndShoot();
    changeDrive();
  }
  while(true) {
    if(time() - lastShotTime < 30) {
      if(++corner == 4) corner = 0;
    } else {
      if((corner += 2) >= 4) corner -= 4;
    }
    stopAndGo();
    while(farFromSide()) {
      findAndShoot();
      changeDrive();
    }
  }
}

private void stopAndGo() {
  drive(drive,0);
  while(speed() >= 50) findAndShoot();
  int dx = cornerX[corner]-(locX[id] = loc_x());
  int dy = cornerY[corner]-(locY[id] = loc_y());
  if(dx == 0) {
    drive = dy > 0? 90: 270;
  } else {
    drive = atan(dy*100000/dx);
    if(dx < 0) drive += 180;
  }
  drive(drive,100);
  driveStatus = 0;
}

private void changeDrive() {
  int speed = speed();
  switch(driveStatus) {
    default:
    case 0: {
      if(speed >= 100) {
        driveStatus++;
        drive(drive,48);
      }
      break;
    }
    case 1: {
      if(speed <= 49) {
        driveStatus++;
        drive(drive+10,100);
      }
      break;
    }
    case 2: {
      if(speed >= 100) {
        driveStatus++;
        drive(drive,48);
      }
      break;
    }
    case 3: {
      if(speed <= 49) {
        driveStatus = 0;
        drive(drive-10,100);
      }
      break;
    }
  }
}

private boolean findEnemy() {
  if((range = scan(scan,resolution)) == 0) return false;
  double time;
  double deltaT = (time = time()) - lastTime;
  targetX = (locX[id]=loc_x())+range*cos(scan)/100000.0;
  targetY = (locY[id]=loc_y())+range*sin(scan)/100000.0;
  if(isTargetAFriend()) {
    scan += resolution;
    return false;
  }
  if(resolution == 1 && deltaT > 0.5) {
    double theSpeedX = (targetX-oldTargetX)/deltaT;
    double theSpeedY = (targetY-oldTargetY)/deltaT;
    lastTargetX = oldTargetX = targetX;
    lastTargetY = oldTargetY = targetY;
    double speed2 = theSpeedX*theSpeedX + theSpeedY*theSpeedY;
    if(speed2 > 0) {
      if(speed2 < 1600) {
        lastTargetSpeedX = speedX = theSpeedX;
        lastTargetSpeedY = speedY = theSpeedY;
      } else {
        lastTargetSpeedX = lastTargetSpeedY = speedX = speedY = 0;
      }
    }
    lastTargetTime = lastTime = time;
  }
  return true;
}

private void findAndShoot() {
  if(findEnemy()) {
    if(resolution > 1) resolution /= 2;
    foundEnemy = true;
  } else {
    if(foundEnemy) {
      if((scan -= resolution) < 0) scan += 360;
      foundEnemy = false;
    } else {
      if((scan += resolution) > 360) scan -= 360;
      if(resolution < 8) resolution *= 2;
    }
  }
  if(range > 40 && range <= 740) {
    if(!isTargetAFriend()) fireToTarget();
  } else {
    if(range > 740) {
      scan += resolution*2;
    } else {
      if(!isLastTargetAFriend()) fireToLastTarget();
    }
  }
}

private void fireToTarget() {
  fireTo(targetX,targetY,speedX,speedY,lastTime);
}

private void fireToLastTarget() {
  fireTo(
    lastTargetX,lastTargetY,lastTargetSpeedX,lastTargetSpeedY,lastTargetTime
  );
}

private void fireTo(double x, double y, double sx, double sy, double t) {
  double deltaT = time()-t;
  double Dx, Dy;
  if(deltaT > 0) {
    x += sx*deltaT;
    y += sy*deltaT;
    Dx = x-(locX[id]=loc_x());
    Dy = y-(locY[id]=loc_y());
  } else {
    Dx = x-locX[id];
    Dy = y-locY[id];
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
    if(cannon(degrees,(int)(d_sqrt(r2)+0.5)) != 0) lastShotTime = time();
  }
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

private boolean isLastTargetAFriend() {
  if(counter > 1) {
    for(int ct = 0; ct < counter; ct++) {
      if(ct != id) {
        int dx = (int)(lastTargetX-locX[ct]);
        int dy = (int)(lastTargetY-locY[ct]);
        if(dx*dx+dy*dy < 6400) return true;
      }
    }
  }
  return false;
}

private boolean farFromCorner() {
  switch(corner) {
    default:
    case 0: return locX[id] > 150 || locY[id] > 150;
    case 1: return locX[id] < 850 || locY[id] > 150;
    case 2: return locX[id] < 850 || locY[id] < 850;
    case 3: return locX[id] > 150 || locY[id] < 850;
  }
}

private boolean farFromSide() {
  switch(corner) {
    default:
    case 0: return locY[id] > 150;
    case 1: return locX[id] < 850;
    case 2: return locY[id] < 850;
    case 3: return locX[id] > 150;
  }
}

}
