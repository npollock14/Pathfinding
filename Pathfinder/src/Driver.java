import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Driver extends JPanel
		implements ActionListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	private static final long serialVersionUID = 1L;

	int screenWidth = 1000;
	int screenHeight = 1000;
	boolean[] keys = new boolean[300];
	boolean[] keysToggled = new boolean[300];
	boolean[] mouse = new boolean[200];
	Camera cam;
	Grid gr;
	static Point mPos;

	// ============== end of settings ==================

	public void paint(Graphics g) {
		super.paintComponent(g);
		//gr.draw(g);
	}

	public void update() throws InterruptedException {
		cam.update(keys, getMousePos());
	}

	private void init() {

	}

	// ==================code above ===========================

	private Node makeBlockerNode(int x, int y) {
		return new Node(new Point(x, y), true, false, false, 0, 0, null);
	}

	public Point getMousePos() {
		try {
			return new Point(this.getMousePosition().x, this.getMousePosition().y);
		} catch (Exception e) {
			return mPos;
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		try {
			update();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		repaint();
	}

	public static void main(String[] arg) {
		@SuppressWarnings("unused")
		Driver d = new Driver();
	}

	public Driver() {
		JFrame f = new JFrame();
		f.setTitle("Pathfinder");
		f.setSize(screenWidth, screenHeight);
		f.setBackground(Color.BLACK);
		f.setResizable(false);
		f.addKeyListener(this);
		f.addMouseMotionListener(this);
		f.addMouseWheelListener(this);
		f.addMouseListener(this);

		f.add(this);

		cam = new Camera(0, 0, 1, screenWidth, screenHeight);
		cam.update(keys, getMousePos());
		ArrayList<Node> blocked = new ArrayList<Node>();
		blocked.add(makeBlockerNode(3, -2));
		blocked.add(makeBlockerNode(4, -2));
		blocked.add(makeBlockerNode(5, -2));
		blocked.add(makeBlockerNode(2, -2));
		blocked.add(makeBlockerNode(1, -2));
		blocked.add(makeBlockerNode(0, -2));
		cam.toXScreen(100);
		// blocked.add(makeBlockerNode(-1, -10));
		// blocked.add(makeBlockerNode(5, -10));
		gr = new Grid(blocked, cam);
		gr.getPath(new Point(0, 0), new Point(3, -4));

		t = new Timer(15, this);
		t.start();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

		init();

	}

	Timer t;

	@Override
	public void keyPressed(KeyEvent e) {
		keys[e.getKeyCode()] = true;

	}

	@Override
	public void keyReleased(KeyEvent e) {

		keys[e.getKeyCode()] = false;

		if (keysToggled[e.getKeyCode()]) {
			keysToggled[e.getKeyCode()] = false;
		} else {
			keysToggled[e.getKeyCode()] = true;
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

		if (e.getWheelRotation() < 0) {
			cam.changeScale(.1f);
		} else {
			cam.changeScale(-.1f);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouse[e.getButton()] = true;

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouse[e.getButton()] = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouse[e.getButton()] = true;

	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

}

class Node implements Comparable<Node> {
	Point pos;
	boolean blocked, target, start;
	int fCost, gCost, sCost;
	Node parent;

	public Node(Point pos, boolean blocked, boolean target, boolean start, int gCost, int sCost, Node parent) {
		super();
		this.pos = pos;
		this.blocked = blocked;
		this.target = target;
		this.start = start;
		this.gCost = gCost;
		this.fCost = gCost + sCost;
		this.parent = parent;
	}

	@Override
	public int compareTo(Node n) {
		return -n.fCost + this.fCost;
	}

}

class Grid {
	ArrayList<Node> open = new ArrayList<Node>();
	ArrayList<Node> closed = new ArrayList<Node>();
	ArrayList<Node> blocked = new ArrayList<Node>();
	ArrayList<Node> pathTree = new ArrayList<Node>();
	Camera cam;

	public Grid(ArrayList<Node> blocked, Camera cam) {
		super();
		this.blocked = blocked;
	}

	public void getPath(Point a, Point b) {
		open.add(new Node(a, false, false, true, getGCost(a, b), 0, null));
		while (open.size() >= 1) {
			Node curr = getLoF(open);
			System.out.print("========================== Chosen: ");
			curr.pos.print();
			if(!curr.start) {
			System.out.print("Parent: ");curr.parent.pos.print();
			}
			System.out.println("OPEN: ");
			for (Node n : open) {
				n.pos.print();
				if(!n.start) {
					System.out.print("Parent: ");curr.parent.pos.print();
				}
				System.out.println("GCOST: " + n.gCost + " SCOST: " + n.sCost + " FCOST: " + n.fCost);
				
			}
			if (!curr.start) {
				curr.parent.pos.print();
			}
			open.remove(curr);
			closed.add(curr);

			if (curr.target) {
				System.out.println("====== FOUND PATH ======");
				setNodePath(curr);
				System.out.println("====== DONE ======");
				return;
			}
			ArrayList<Node> neighbors = getNeighbors(curr, a, b);
			for (Node n : neighbors) {

				if (n.blocked || closed.contains(n)) {
					continue;
				}

				//Node temp = n; //TODO (0,-1)'s neighbor is getting changed from (0,0) after being chosen by something other than (0,0)
				//temp.parent = curr;
				//temp.sCost = getSCost(temp);
				if (!open.contains(n)) {// || temp.sCost < n.sCost) {
					
					n.parent = curr;
					n.sCost = getSCost(n);
					n.fCost = n.sCost + n.gCost;
					System.out.println("Set (" + n.pos.x + ", " + n.pos.y + ")'s parent to: (" + n.parent.pos.x + ", " + n.parent.pos.y + ")");
					if (!open.contains(n)) {
						open.add(n);
					}
				}

			}

		}
		System.out.println("Couldn't find a path -_-");
	}

	private void setNodePath(Node n) {
		for (; !n.start; n = n.parent) {
			pathTree.add(n);
		}
		pathTree.add(n);

		for (Node node : pathTree) {
			node.pos.print();
		}

	}

	private ArrayList<Node> getNeighbors(Node curr, Point a, Point b) {

		ArrayList<Node> neighbors = new ArrayList<Node>();
		for (Node n : open) {

			if (n.pos.isAdjacentTo(curr.pos)) {

				n.sCost = getSCost(n);
				n.fCost = n.sCost + n.gCost;
				neighbors.add(n);
			}
		}
		for (Node n : closed) {
			if (n.pos.isAdjacentTo(curr.pos)) {
				if (!n.start) {

					n.sCost = getSCost(n);
					n.fCost = n.sCost + n.gCost;
				}
				neighbors.add(n);
			}
		}
		for (Node n : blocked) {
			if (n.pos.isAdjacentTo(curr.pos)) {
				neighbors.add(n);
			}
		}
		// add neighbors that are not on either closed or open and add them to open
		for (int y = -1; y < 2; y++) {
			outer: for (int x = -1; x < 2; x++) {
				if (!(x == 0 && y == 0)) {
					Point tempPos = new Point(curr.pos.x + x, curr.pos.y + y);
					for (Node n : neighbors) {
						if (n.pos.isSamePosition(tempPos)) {
							continue outer;
						}
					}
					// add new node
					Node n = new Node(tempPos, false, tempPos.isSamePosition(b), tempPos.isSamePosition(a),
							getGCost(tempPos, b), 0, curr);
					System.out.println("Created (" + n.pos.x + ", " + n.pos.y + ") and set their parent to: (" + curr.pos.x + ", " + curr.pos.y + ")");
					n.sCost = getSCost(n);
					n.fCost = n.sCost + n.gCost;
					neighbors.add(n);
				}
			}

		}

		// sort neighbors by f cost
		Collections.sort(neighbors);

		return neighbors;
	}

	private int getGCost(Point curr, Point b) {
		return (Math.abs(Math.abs(curr.x - b.x) - Math.abs(curr.y - b.y))) * 10 + (14
				* (Math.abs(curr.x - b.x) < Math.abs(curr.y - b.y) ? Math.abs(curr.x - b.x) : Math.abs(curr.y - b.y)));
	}

	private int getSCost(Node n) {
		int SCost = 0;
		for (; !n.start; n = n.parent) {
			SCost += (int) (10 * n.pos.distanceTo(n.parent.pos));
		}

		return SCost;
	}

	private Node getLoF(ArrayList<Node> open) {
		Node lowF = open.get(0);
		for (Node n : open) {
			if (n.fCost == lowF.fCost && n.gCost < lowF.gCost) {
				lowF = n;
			} else if (n.fCost < lowF.fCost) {
				lowF = n;
			}

		}
		return lowF;
	}

	public void draw(Graphics g) {

		// draw lines
		int w = 10;

		for (Node o : open) {
			System.out.println(o.sCost);
			g.setColor(Color.GREEN);
			g.fillRect(cam.toXScreen(o.pos.x * (w) - w / 2), cam.toYScreen((o.pos.y * (w)) - (w / 2)),
					(int) (w * cam.scale), (int) (w * cam.scale));
			g.drawString(o.sCost + "\n" + o.gCost + "\n" + o.fCost, cam.toXScreen(o.pos.x * (w) - w / 2),
					cam.toYScreen((o.pos.y * (w)) - (w / 2)));
		}
		for (Node o : closed) {
			g.setColor(Color.RED);
			g.fillRect(cam.toXScreen(o.pos.x * (w) - w / 2), cam.toYScreen((o.pos.y * (w)) - (w / 2)),
					(int) (w * cam.scale), (int) (w * cam.scale));
			g.drawString(o.sCost + "\n" + o.gCost + "\n" + o.fCost, cam.toXScreen(o.pos.x * (w) - w / 2),
					cam.toYScreen((o.pos.y * (w)) - (w / 2)));
		}
		for (Node o : blocked) {
			g.setColor(Color.BLACK);
			g.fillRect(cam.toXScreen(o.pos.x * (w) - w / 2), cam.toYScreen((o.pos.y * (w)) - (w / 2)),
					(int) (w * cam.scale), (int) (w * cam.scale));
			g.drawString(o.sCost + "\n" + o.gCost + "\n" + o.fCost, cam.toXScreen(o.pos.x * (w) - w / 2),
					cam.toYScreen((o.pos.y * (w)) - (w / 2)));
		}
		for (int i = 0; i <= 500 + 1; i++) {
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(cam.toXScreen(i * 10 - w / 2), cam.toYScreen(0 - w / 2), cam.toXScreen(i * 10 - w / 2),
					cam.toYScreen(10000 + 5));
			g.drawLine(cam.toXScreen(0 - w / 2), cam.toYScreen(i * 10 - w / 2), cam.toXScreen(10000 - w / 2),
					cam.toYScreen(i * 10 - w / 2));
		}
	}
}

class Camera {
	int xOff, yOff, screenW, screenH;
	double scale;
	Point center;
	float scaleNotches = 0;
	int moveSpeed = 10;

	public Camera(int xOff, int yOff, double scale, int screenW, int screenH) {
		super();
		this.xOff = xOff;
		this.yOff = yOff;
		this.scale = scale;
		this.screenW = screenW;
		this.screenH = screenH;
		center = new Point(screenW / 2, screenH / 2);
	}

	public void update(boolean[] keys, Point mousePos) {
		xOff += keys[65] ? moveSpeed / scale : 0;
		xOff -= keys[68] ? moveSpeed / scale : 0;
		yOff += keys[87] ? moveSpeed / scale : 0;
		yOff -= keys[83] ? moveSpeed / scale : 0;

	}

	public void focus(Point p) {
		// p = map coordinates
		// place point p in center of screen & determine how displaced that was
		xOff = screenW / 2 - p.x;
		yOff = screenH / 2 - p.y;

	}

	public void changeScale(float notches) {
		scaleNotches += notches;
		scale = Math.pow(2, scaleNotches);
	}

	public int toXScreen(int x) {
		int dx = (int) ((x + xOff - center.x) * scale);
		return (center.x + dx);

	}

	public int toYScreen(int y) {
		int dy = (int) ((y + yOff - center.y) * scale);
		return (center.y + dy);

	}

	public int toXMap(int x) {
		return (int) ((x - center.x) / scale) + center.x - xOff;

	}

	public int toYMap(int y) {
		return (int) ((y - center.y) / scale) + center.y - yOff;

	}

}

class Point {
	int x, y;

	public Point(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public double distanceTo(Point p2) {
		return Math.sqrt((this.x - p2.x) * (this.x - p2.x) + (this.y - p2.y) * (this.y - p2.y));
	}

	public double angleTo(Point p2) {
		try {
			return Math.atan2(this.y - p2.y, this.x - p2.x);
		} catch (Exception e) {

		}
		return 0;
	}

	public boolean isSamePosition(Point p) {
		return (p.x == this.x && p.y == this.y);
	}

	public boolean isAdjacentTo(Point p) {
		return (Math.abs(p.x - this.x) <= 1 && Math.abs(p.y - this.y) <= 1 && !p.equals(this));
	}

	public void print() {
		System.out.println("(" + x + ", " + y + ")");
	}
}