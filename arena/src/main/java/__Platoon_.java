//
// JJRobots (c) 2000 L.Boselli - boselli@uno.it
//
public class __Platoon_ extends JJRobot {

private static int count;
private static int[] cornerX = {50,950,950,50};
private static int[] cornerY = {50,50,950,950};
private static int targetX = 500;
private static int targetY = 500;
private static int locX[] = new int[8];
private static int locY[] = new int[8];
private static int corner1;

private int nCorner;
private int scan;
private int id;

void main() {
  if((id = id()) == 0) {
    count = 1;
    corner1 = rand(4);
  } else {
    count = id+1;
  }
  nCorner = corner1;
  int dx = cornerX[nCorner]-(locX[id]=loc_x());
  int dy = cornerY[nCorner]-(locY[id]=loc_y());
  int angle;
  if(dx == 0) {
    angle = dy > 0? 90: 270;
  } else {
    angle = atan(dy*100000/dx);
  }
  if(dx < 0) angle += 180;
  drive(angle,100);
  switch(nCorner) {
    default:
    case 0: while(locX[id] > 150 || locY[id] > 150) fire2(); break;
    case 1: while(locX[id] < 850 || locY[id] > 150) fire2(); break;
    case 2: while(locX[id] < 850 || locY[id] < 850) fire2(); break;
    case 3: while(locX[id] > 150 || locY[id] < 850) fire2(); break;
  }
  do {
    drive(0,0);
    while(speed() >= 50) fire1();
    if(++nCorner == 4) nCorner = 0;
    dx = cornerX[nCorner]-loc_x();
    dy = cornerY[nCorner]-loc_y();
    if(dx == 0) {
      angle = dy > 0? 90: 270;
    } else {
      angle = atan(dy*100000/dx);
    }
    if(dx < 0) angle += 180;
    drive(angle,100);
    switch(nCorner) {
      default:
      case 0: while(locY[id] > 150) fire1(); break;
      case 1: while(locX[id] < 850) fire1(); break;
      case 2: while(locY[id] < 850) fire1(); break;
      case 3: while(locX[id] > 150) fire1(); break;
    }
  } while(true);
}

private void fire1() {
  switch(nCorner) {
    default:
    case 0: if(++scan > 470 || scan < 240) scan = 250; break;
    case 1: if(++scan > 200 || scan < -30) scan = -20; break;
    case 2: if(++scan > 290 || scan <  60) scan = 70; break;
    case 3: if(++scan > 380 || scan < 150) scan = 160; break;
  }
  fire();
}

private void fire2() {
  if(++scan > 360) scan = 0;
  fire();
}

private void fire() {
  locX[id] = loc_x();
  locY[id] = loc_y();
  int range;
  if((range = scan(scan,1)) > 40 && range <= 740) {
    if (count > 1) {
      boolean shot = true;
      int shotX = locX[id]+range*cos(scan)/100000;
      int shotY = locY[id]+range*sin(scan)/100000;
      for(int ct = 0; ct < count; ct++) {
        if(ct != id) {
          int dx = shotX-locX[ct];
          int dy = shotY-locY[ct];
          if(dx*dx+dy*dy < 1600) {
            shot = false;
            break;
          }
        }
      }
      if(shot) {
        targetX = shotX;
        targetY = shotY;
        cannon(scan,range);
        scan -= 10;
      } else {
        int dx = targetX-locX[id];
        int dy = targetY-locY[id];
        int dist2 = dx*dx+dy*dy;
        if(dist2 > 1600 && dist2 <= 547600) {
          int angle;
          if(dx == 0) {
            angle = dy > 0? 90: 270;
          } else {
            angle = atan(dy*100000/dx);
            if(dx < 0) angle += 180;
          }
          cannon(angle,sqrt(dist2));
        }
      }
    } else {
      cannon(scan,range);
      scan -= 10;
    }
  }
}


}
