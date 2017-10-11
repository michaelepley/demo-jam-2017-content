/* JJVector class: see "extensions.txt" for details
 *
 * Author: Christo Fogelberg (doubtme@hotmail.com)
 * Additions: Alan Lund (alan.lund@acm.org)
 *
 * Built as an addon for JRobots, by Leonardo Boselli (boselli@uno.it)
 *
 * Planned additions: Set of static functions that have the same functionality
 * but don't return new JJVectors, instead taking one as an argument
 *
 * October, 2001. Please direct any comments or questions regarding this
 * class to Christo or Alan, comments regarding JRobots in general to
 * Leonardo :)
 */

// Used to print test output in the main function:
// import java.io.*;

public class JJVector {

  // Data Fields
  private double x, y, t;

  // Factory Methods
  public static JJVector Polar(double r, double a) {
    JJVector result = new JJVector(r, 0);

    result.rotateSelf(a);

    return result;
  }

  public static JJVector Polar(double r, double a, double t) {
    JJVector result = new JJVector(r, 0, t);

    result.rotateSelf(a);

    return result;
  }

  // Constructors
  public JJVector() {
    this.x = 0;
    this.y = 0;
    this.t = 1.0;
  }

  public JJVector(double x, double y) {
    this.x = x;
    this.y = y;
    this.t = 1.0;
  }

  public JJVector(double x, double y, double t) {
    this.x = x;
    this.y = y;
    this.t = t;
  }

  public JJVector(JJVector v) {
    x = v.x();
    y = v.y();
    t = v.t();
  }



  // Basic Mutator/Accessor Functions:
  public final double t()             { return t; }
  public final double x()             { return x; }
  public final double y()             { return y; }
  public final double r()             { return mag(); }
  public final double a()             { return angle(); }

  public final void   set_x(double x) { this.x = x; }
  public final void   set_y(double y) { this.y = y; }
  public final void   set_t(double t) { this.t = t; }

  //  -- Polar coordinates
  public final void set(double x, double y) { 
    this.x = x; 
    this.y = y; 
  }

  public final void set(double x, double y, double t) { 
    this.x = x; 
    this.y = y; 
    this.t = t;
  }

  public final void set(JJVector v) { 
    x = v.x();
    y = v.y();
    t = v.t();
  }


  // Basic Mathematical Functions
  public final double mag() { 
    return Math.sqrt(x*x + y*y); 
  }

  public final double angle() {
    return 180.0 * Math.atan2(y, x) / Math.PI;
  }

  public final double dot(JJVector v) { 
    return x * v.x() + y * v.y(); 
  }

  public final double speed() {
    return mag() / t;
  }

  public final JJVector velocity(JJVector result) {
    mult(1.0d / t, result);
    result.set_t(1.0);
    return result;
  }

  public final JJVector velocity() {
    return velocity(new JJVector());
  }

  public final JJVector plus(JJVector v, JJVector result) {
    result.set(x + v.x(), y + v.y(), t + v.t());
    return result;
  }

  public final JJVector plus(JJVector v) {
    return plus(v, new JJVector());
  }

  public final JJVector minus(JJVector v, JJVector result) {
    result.set(x - v.x(), y - v.y(), t - v.t());
    return result;
  }

  public final JJVector minus(JJVector v) {
    return minus(v, new JJVector());
  }

  public final JJVector mult(double k, JJVector result) {
    result.set(x * k, y * k, t * k);
    return result;
  }

  public final JJVector mult(double k) {
    return mult(k, new JJVector());
  }

  public final JJVector rotate(double degrees, JJVector result) {
    result.set(this);
    result.rotateSelf(degrees);
    return result;
  }

  public final JJVector rotate(double degrees) {
    return rotate(degrees, new JJVector());
  }


  // Useful Functions
  public final double dist(JJVector v) {
    double dx = x - v.x();
    double dy = y - v.y();
    return Math.sqrt( dx*dx + dy*dy );
  }

  public final JJVector unit(JJVector result) {
    double invMag = 1.0d / mag();
    return mult(invMag, result);
  }

  public final JJVector unit() {
    return unit(new JJVector());
  }

  private final void rotateSelf(double degrees) {
    double radians = Math.PI * degrees / 180.0; 
    double newX    = Math.cos(radians) * x - Math.sin(radians) * y;
    double newY    = Math.sin(radians) * x + Math.cos(radians) * y;
    x = newX;
    y = newY;
  }


  // Miscellaneous Functions
  public String toString() {
    return "(" + x + ", " + y + ", " + t + ")";
  }


/*
  // Unit tests
  private static int failureCount = 0;

  private static void assertEquals(double expected,
                                   double actual,
                                   String what) {
    double epsilon = 1.0e-10;                               

    if ((expected == 0.0 && Math.abs(expected) > epsilon) ||
        (expected != 0.0 && Math.abs((actual - expected) / expected) > epsilon)) {
      System.out.println(what + ": expected [" + expected + "], actual [" + actual + "]");
      failureCount++;
    }
  }

  private static void assertEquals(JJVector expected,
                                   JJVector actual,
                                   String   what) {
    assertEquals(expected.x(), actual.x(), what + ".x()");
    assertEquals(expected.y(), actual.y(), what + ".y()");
    assertEquals(expected.t(), actual.t(), what + ".t()");
  }

  public static void selfTest()
  {
    JJVector a = new JJVector(3.0,  4.0, 0.5);
    JJVector b = new JJVector(3.0, -1.0, 1.0);

    assertEquals(5.0,           a.mag(),   "a.mag()");
    assertEquals(10.0,          a.speed(), "a.speed()");
    assertEquals(Math.sqrt(10), b.speed(), "b.speed()");

    assertEquals(new JJVector( 6.0,  3.0,  1.5), a.plus(b),    "a.plus(b)");
    assertEquals(new JJVector( 0.0,  5.0, -0.5), a.minus(b),   "a.minus(b)");
    assertEquals(new JJVector( 9.0, 12.0,  1.5), a.mult(3.0),  "a.mult(3)");
    assertEquals(new JJVector( 6.0,  8.0,  1.0), a.velocity(), "a.velocity()");

    assertEquals(new JJVector( 0.0,  1.0,  0.2), 
                 new JJVector( 1.0,  0.0,  0.2).rotate(90), 
                 "[1, 0, 0.2].rotate(90)");

    assertEquals(new JJVector(-1.0,  0.0,  0.4), 
                 new JJVector( 0.0,  1.0,  0.4).rotate(90), 
                 "[0, 1, 0.4].rotate(90)");

    assertEquals(new JJVector(-3.0,  0.0,  1.0), 
                 Polar(3.0, 180.0, 1.0),
                 "Polar(3, 180)");

    assertEquals(  45.0, new JJVector( 1.0,  1.0).angle(), "[ 1, 1].angle()");
    assertEquals( 135.0, new JJVector(-1.0,  1.0).angle(), "[-1, 1].angle()");
    assertEquals(-135.0, new JJVector(-1.0, -1.0).angle(), "[-1,-1].angle()");
    assertEquals( -45.0, new JJVector( 1.0, -1.0).angle(), "[ 1,-1].angle()");
    assertEquals(   0.0, new JJVector( 0.0,  0.0).angle(), "[ 0, 0].angle()");


    if (failureCount == 0)
      System.out.println("Passed.");
  }

  public static void main(String[] args) {
    selfTest();
  }
*/


/*
	// Test Function - You can ignore this.
	public static void main(String [] args) {

		// Read in two vectors:
		BufferedReader input = new BufferedReader( new InputStreamReader(System.in) );
		double x;
		double y;
		JJVector a;
		JJVector b;
		JJVector tmp = new JJVector(0, 0);

		try {
			System.out.print("Vector 'a': Enter the x component: ");
			x = Double.parseDouble(input.readLine());

			System.out.print("Vector 'a': Enter the y component: ");
			y = Double.parseDouble(input.readLine());

			a = new JJVector(x, y);

			System.out.print("Vector 'b': Enter the x component: ");
			x = Double.parseDouble(input.readLine());

			System.out.print("Vector 'b': Enter the x component: ");
			y = Double.parseDouble(input.readLine());

			b = new JJVector(x, y);
		}
		catch (IOException e) {
			System.out.println("IO Error: " + e);
			System.out.println("Building default vectors...");
			a = new JJVector(1, 2);
			b = new JJVector(3, 4);
		}

		// Now start doing the tests:
		System.out.println("a = " + a);
		System.out.println("b = " + b +"\n");

		System.out.println("|a| = " + a.mag());
		System.out.println("|b| = " + b.mag() + "\n");

		System.out.println("a + b = " + a.plus(b, tmp));
		System.out.println("a - b = " + a.minus(b, tmp));
		System.out.println("b - a = " + b.minus(a, tmp));
		System.out.println("a * 1.5 = " + a.mult(1.5, tmp));
		System.out.println("b * -1 = " + b.mult(-1, tmp) + "\n");

		System.out.println("a . b = " + a.dot(b) + "\n");

		System.out.println("unit a = " + a.unit(tmp));
		System.out.println("unit b = " + b.unit(tmp) +"\n");

		System.out.println("distance of a from b = " + a.dist(b));
	} // end main
*/
} // end JVector
