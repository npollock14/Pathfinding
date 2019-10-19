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

	// ============== end of settings ==================

	public void paint(Graphics g) {
		super.paintComponent(g);

	}

	public void update() throws InterruptedException {

	}

	private void init() {
		ArrayList<Node> blocked = new ArrayList<Node>();
		Grid g = new Grid(blocked);
		g.getPath(new Point(0,0), new Point(0,10));
		
		
	}

	// ==================code above ===========================

	@Override
	public void actionPerformed(ActionEvent arg0) {

		try {
			update();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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

class Node implements Comparable<Node>{
	Point pos;
	boolean blocked, target, start;
	int fCost, gCost, sCost;
	Node parent;

	public Node(Point pos, boolean blocked, boolean target, boolean start, int gCost, int sCost,
			Node parent) {
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
	
	

	public Grid(ArrayList<Node> blocked) {
		super();
		this.blocked = blocked;
	}

	public void getPath(Point a, Point b) {
		open.add(new Node(a, false, false, true, 0, 0, null));

		while (open.size() > 1) {
			Node curr = getLoF(open);
			open.remove(curr);
			closed.add(curr);

			if (curr.target) {
				printNodePath(curr);
				System.out.println("here");
				break;
			}
			ArrayList<Node> neighbors = getNeighbors(curr, a, b);
			for (Node n : neighbors) {
				if (n.blocked || closed.contains(n)) {
					continue;
				}
				Node temp = n;
				temp.parent = curr;
				temp.sCost = getSCost(temp);
				if (!open.contains(n) || temp.sCost < n.sCost) {
					n.sCost = getSCost(n);
					n.fCost = n.sCost + n.gCost;
					n.parent = curr;
					
					if (!open.contains(n)) {
						open.add(n);
					}
				}

			}

		}
	}

	private void printNodePath(Node n) {
		ArrayList<Node> nodeTree = new ArrayList<Node>();
		for(; !n.start; n = n.parent) {
			nodeTree.add(n);
		}
		for(Node node : nodeTree) {
			System.out.println(node.pos);
		}
		
	}

	private ArrayList<Node> getNeighbors(Node curr, Point a, Point b) {
		ArrayList<Node> neighbors = new ArrayList<Node>();
		for (Node n : open) {
			if (n.pos.isAdjacentTo(curr.pos)) {
				neighbors.add(n);
			}
		}
		for (Node n : closed) {
			if (n.pos.isAdjacentTo(curr.pos)) {
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
							 getGCost(tempPos, b), (int)(curr.sCost + 10*tempPos.distanceTo(curr.pos)), curr);
					neighbors.add(n);
				}
			}

		}
		
		//sort neighbors by f cost
		Collections.sort(neighbors);

		return neighbors;
	}

	
	private int getGCost(Point curr, Point b) {
		return (Math.abs(curr.x - b.x) - Math.abs(curr.y - b.y)) * 10 + (14
				* (Math.abs(curr.x - b.x) < Math.abs(curr.y - b.y) ? Math.abs(curr.x - b.x) : Math.abs(curr.y - b.y)));
	}
	private int getSCost(Node n) {
		
		int SCost = 0;
		for(; !n.start; n = n.parent) {
			SCost += (int)(n.parent.sCost + 10*n.pos.distanceTo(n.parent.pos));
		}
		
		return SCost;
		
	}

	private Node getLoF(ArrayList<Node> open) {
		Node lowF = open.get(0);
		for (Node n : open) {
			if (n.fCost < lowF.fCost) {
				lowF = n;
			}
		}
		return lowF;
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