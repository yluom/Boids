package boids;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Random;

import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeySizeException;

/**
 * Boids --- class to represent a "flock" of bird-like objects and it's
 * behaviour.
 * 
 * @author fr05td3su
 */
class Boids {
	KDTree kd; // kd-tree structure is used to find bird's neighbours fast
	Bird[] birds;
	int N; // number of boids to process
	int xRes; // maximum x-coordinate of field
	int yRes; // maximum y-coordinate of field
 
	// Font to draw birds
	private Font font = new Font(null, Font.PLAIN, 10);
	
	private boolean birdsAreDots = false;


	/**
	 * Initialize the array of bird-like objects with random coordinates within
	 * certain area, determined by width and height, so each bird will be asigned
	 * position vector (from 0 to width, from 0 to height) and zero velocity vector.
	 *
	 * @param amount Number of boids to create. width Maximum value of x-coordinate
	 *               of position. height Maximum value of y-coordinate of position.
	 */
	public Boids(int amount, int width, int height) {
		N = amount;
		xRes = width;
		yRes = height;
		kd = new KDTree(2);
		birds = new Bird[N];
		Random rand = new Random();

		for (int i = 0; i < N - 1; i++) {
			birds[i] = new Bird(new Vector(rand.nextInt(xRes), rand.nextInt(yRes)), new Vector(0, 0));
			try {
				kd.insert(birds[i].position.data, birds[i]);
			} catch (Exception e) {
				System.out.println("Init Exception caught: " + e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Updates each boid's position and velocity depending on it's neighbours.
	 *
	 * @param distance Number of neighbours, which positions and velocities are used
	 *                 to calculate corresponding vectors of cohesion, alignment,
	 *                 separation of a bird. cohesionCoefficient Value affects speed
	 *                 at which bird moves towards the perceived centre of mass e.g
	 *                 100 means that in each iteration bird moves 1% to the
	 *                 perceived centre alignmentCoefficient Value affects velocity
	 *                 increase of bird with respect to the perceived centre of mass
	 *                 separationCoefficient If bird is within this distance from
	 *                 other birds, it will move away
	 * @return No return value.
	 */
	public void move(int distance, double cohesionCoefficient, int alignmentCoefficient, double separationCoefficient) {
		try {
			for (int i = 0; i < N - 1; i++) {
				double[] coords = birds[i].position.data;
				Bird[] nbrs = new Bird[distance];
				kd.nearest(coords, distance).toArray(nbrs);
				try {
					kd.delete(coords);
				} catch (Exception e) {
					// we ignore this exception on purpose
					System.out.println("KeyMissingException deleting caught: " + e + e.getMessage());
				}
				birds[i].updateVelocity(nbrs, xRes, yRes, cohesionCoefficient, alignmentCoefficient,
						separationCoefficient);
				birds[i].updatePosition();
				kd.insert(birds[i].position.data, birds[i]);
			}

			// the implementation of deletion in KdTree does not actually delete nodes,
			// but only marks them, that affects performance, so it's necessary to rebuild
			// the tree
			// after long sequences of insertions and deletions
			kd = new KDTree(2);
			for (int i = 0; i < N - 1; i++)
				kd.insert(birds[i].position.data, birds[i]);
		} catch (KeySizeException | KeyDuplicateException e) {
			System.out.println("KeySizeException/KeyDuplicateException caught: " + e + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Unknown exception caught: ");
			e.printStackTrace();
		}
	}

	/**
	 * Draws each boid as a point on the graphics object.
	 *
	 * @param g Graphics object to draw on.
	 * @return No return value.
	 */
	public void draw(Graphics g) {
		for (int i = 0; i < N - 1; i++) {
			int x = (int) birds[i].position.data[0];
			int y = (int) birds[i].position.data[1];
			if(birdsAreDots) {
				g.drawLine(x, y, x, y);
			} else { 
				// calculate angle from velocity
				double angle = Math.atan2(birds[i].velocity.data[1], birds[i].velocity.data[0]);
				// we are drawing a "v" that's pointed to the bottom, so we need to turn by (-90�) -PI/2 to align "V" to the bird velocity
				angle = angle - Math.PI / 2;
				drawBirdWithText((Graphics2D) g, x, y, angle, "V");
			}
		}
	}
	/**
	 * Allows to draw a bird with a text
	 * @param g2d
	 * @param x
	 * @param y
	 * @param angle
	 * @param text
	 */
    private void drawBirdWithText(Graphics2D g2d, double x, double y, double angle, String text) {
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(angle, 0, 0);
        Font rotatedFont = font.deriveFont(affineTransform);
        g2d.setFont(rotatedFont);
        g2d.drawString(text, (int) x, (int) y);
    }
}
