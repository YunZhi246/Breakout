// BreakoutGame.java
// Judy Lin
// DON'T RUN THIS ONE

// imports everything needed
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.image.*; 
import java.io.*; 
import javax.imageio.*; 

// Breakout starts everything off, it creates a GamePanel, and is where the main is.
public class BreakoutGame extends JFrame implements ActionListener{
	javax.swing.Timer myTimer;
	GamePanel game;
		
	public BreakoutGame(Breakout menu,boolean fall){
		super ("Breakout - SPACE to exit");
		setSize(800,650);
		myTimer = new javax.swing.Timer(10, this);
		game = new GamePanel(this,menu,fall);
		add(game);
		
		setResizable(false);
		setVisible(true);
	}
	
	public void start(){
		myTimer.start();
	}

	public void actionPerformed(ActionEvent evt){
		game.move();
		game.repaint();
	}
		
//	public static void main(String [] args){
//		BreakoutGame game = new BreakoutGame(false);
//	}
}

//=====================================================================================================================

// GamePanel moves all the pieces, checks if items collide, checks if the game ends, and draws everything. Where
// everything happens.
class GamePanel extends JPanel implements KeyListener{
	Random dice = new Random();
	private ArrayList <Block> blocks = new ArrayList <Block>();		// blocks = list of blocks that are remaining
	private ArrayList <Block> specials = new ArrayList <Block>();	// specials = list of specials that have been released
	private ArrayList <Block> fall = new ArrayList <Block>();		// fall = list of the falling blocks
	private ArrayList <Ball> balls = new ArrayList <Ball>();		// balls = list of all the balls that are in play
	private int px,py,pSize,score;	// px = x co-ordinate of paddle (left)	py = y co-ordinate of paddle (top)
									// pSize = size (length) of paddle		score = how many blocks hit
	private boolean []keys;			// keys = list of keyboard keys
	private BreakoutGame mainFrame;		// mainFrame = the class above, allows us to reference it
	private boolean ironON, falling, end;	// ironON = if the balls have the iron ball feature (doesn't bounce when it hits the blocks)
											// falling = if the game is in the falling mode		end = if game has ended
	private Image brick, person, win, lose;	// brick = each block	person = the stick person on the screen
											// win = win screen		lose = "You Lose" screen
	private Breakout menu;		// menu = the original Breakout, allows us to reference the score list

	// CONSTRUCTOR
	public GamePanel(BreakoutGame m,Breakout men, boolean fall){
		keys = new boolean[KeyEvent.KEY_LAST+1];
		mainFrame = m;
		menu = men;
		falling = fall;
		
		for (int x = 93;x<700;x+=51){		// makes all the blocks, and randomly chooses specials
			for (int y = 90;y<300;y+=16){
				String spec = "";
				Color c = new Color(0,0,dice.nextInt(100)+156);
				if (falling == false){
					int rand = dice.nextInt(30);
					if (rand == 5){
						spec = "new ball";	// adds another ball
						c = new Color(255,83,183);
					}
					else if (rand == 6){
						spec = "iron ball";	// turns iron feature on
						c = new Color(255,223,0);
					}	
					else if (rand == 7){
						spec = "reg ball";	// make all the ball regular
						c = new Color(97,169,250);
					}	
					else if (rand == 8){
						spec = "large paddle";	// make the paddle larger
						c = new Color(222,7,7);
					}				
					else if (rand == 9){
						spec = "reg paddle";	// make the paddle regular size
						c = new Color(2,115,44);					
					}
				}
				Block b = new Block(x, y,spec,c);
				blocks.add(b);
			}
		}
		
		px = 370;
		py = 518;
		pSize = 60;
		score = 0;
		ironON = false;
		end = false;

		while(true){		// makes the ball, loops until an appropriate angle if chosen
			int i = dice.nextInt(90)-45;
			if (i<-30 || i>30){
				balls.add(new Ball(i, 5, 1, 4, px+(pSize/2), py-5, Color.black));
				break;
			}
		}
		
    	brick = new ImageIcon("pics/brick.png").getImage();
    	win = new ImageIcon("pics/win.png").getImage();
    	lose = new ImageIcon("pics/lose.png").getImage();
    	person = new ImageIcon("pics/person.png").getImage();
		
		setSize(800,650);
		addKeyListener(this);
	}

	// Starts everything
	public void addNotify() {
        super.addNotify();
        requestFocus();
        mainFrame.start();
    }
    
    // MOVE - moves everything on the screen
    public void move(){
    	if (keys[KeyEvent.VK_SPACE]){		// allows user to exit by pressing the space button
    		mainFrame.setVisible(false);
    	}
    	
    	if (end == false){
	    	checkEnd();
	    	
			if(keys[KeyEvent.VK_RIGHT] ){			// moves the paddle left and right
				px += 5;
				px = Math.min(800-pSize,px);
			}
			if(keys[KeyEvent.VK_LEFT] ){
				px -= 5;
				px = Math.max(0,px);
			}
			
			for (int i=0;i<balls.size();i++){		// moves each ball
				balls.get(i).chX((Math.sin(Math.toRadians(balls.get(i).getAng())))*(balls.get(i).getSpeed()));
				balls.get(i).chY((balls.get(i).getDirc()*Math.cos(Math.toRadians(balls.get(i).getAng())))*balls.get(i).getSpeed());
			}		
			for (int i=0;i<specials.size();i++){	// moves each special
				specials.get(i).chY(specials.get(i).getY()+1);
			}
			if (falling == true){	// moves each falling block, if it is in that mode
				for (int i=0;i<fall.size();i++){
					fall.get(i).chY(fall.get(i).getY()+1);
				}
			}
			for (int i=0;i<balls.size();i++){	// checks if each ball collides with a block
				checkCollide(balls.get(i).getX(), balls.get(i).getY(), balls.get(i).getDirc(),balls.get(i).getSize(),balls.get(i).getAng(), i,balls.get(i).getSpeed(),balls.get(i).getCol() );	
			}	
			for (int i=0;i<specials.size();i++){	// checks if any of the specials were hit
				checkSpecial(specials.get(i).getX(),specials.get(i).getY(),specials.get(i).getSpec(),i);
			} 
    	}
    }
    
    // checks if the game has ended or not
    public void checkEnd(){
    	if (blocks.size()==0){	// no more blocks remaining, therefore ends
    		end = true;
    		menu.addScore(score);
    	}
    	
    	for (int i=0;i<balls.size();i++){	// a ball dies, if there are no other balls, then also end
    		Ball b = balls.get(i);
    		if (b.getY()>py){
    			balls.remove(i);
    			if (balls.size()==0){
    				end = true;
    				menu.addScore(score);
    			}
    		}
    	}
    	
    	if (falling == true){	// checks to see if any of the falling blocks hit the paddle when in falling mode
    		for (int i=0;i<fall.size();i++){
    			Block f = fall.get(i);
    			if (f.getY()==py && f.getX()>px && f.getX()<px+pSize || f.getY()==py && f.getX()+50>px && f.getX()+50<px){
    				end = true;
    				menu.addScore(score);
    				for (int j=0;j<balls.size();j++){
			    		balls.remove(j);
			    	}
    			}	
    		}
    	}
    }
    
    // checks if the paddle hit the special given. If yes, then changes conditions accordingly
    public void checkSpecial(int sx, int sy, String spec, int num){
    	if (sy>650){				// removes special if it is off the screen
    		specials.remove(num);
    	}
    	
    	else if (sy == py-5 && sx>=px && sx<=px+pSize){	// checks if it is hit
    		if (spec.equals("new ball")){	// adds another ball to the list
    			while(true){
					int l = dice.nextInt(90)-45;
					if (l<-30 || l>30){
						balls.add(new Ball(l, 5, 1, 4, px+(pSize/2), py-5, Color.black));
						break;
					}
				}
    		}
    		else if (spec.equals("iron ball")){		// turns on the iron feature
    			ironON = true;
    			for (int i=0; i<balls.size();i++){
    				balls.get(i).chCol(new Color (153, 101, 21));
    			}
    		}
    		else if (spec.equals("reg ball")){	// changes all balls into regular balls
    			ironON = false;
    			for (int i=0;i<balls.size();i++){
    				balls.get(i).chCol(Color.black);
    			}
    		}
    		else if (spec.equals("large paddle")){	// makes a larger paddle
    			pSize=120;
    		}
    		else if (spec.equals("reg paddle")){	// makes the paddle regular size again
    			pSize=60;
    		}
	   		specials.remove(num);
    	}
    }
    
    // checks if a certain ball has hit any of the sides or the blocks or the paddle, and removes the block that is hit
    public void checkCollide(int cx, int cy,int cDirc,int cSize,int cAng, int num, int cSpeed,Color bCol){
    	if(cx <= 0 || cx >= 800-cSize || cy<=0 || cy>=650-cSize){	// checks if the ball has hit the side
    		if(cy<=0 || cy>=650-cSize){
    			balls.get(num).chDirc(-1);
    		}
    		else{
    			balls.get(num).chAng(-1);
    		}
    	}
    	    	
	   	for (int n = cSpeed-1;n>0;n-=1){		// a loop of the speed to make sure no co-ordinates are skipped
	   		// checks if ball has hit paddle
	   		if (cy+(int)(cDirc*Math.cos(Math.toRadians(cAng))*n)==py-cSize && cx-(int)(Math.sin(Math.toRadians(cAng))*n)>=px && cx-(int)(Math.sin(Math.toRadians(cAng))*n)<=px+pSize){
	   			balls.get(num).chDirc(-1);
	   			break;
	   		}
	   		else{ 		// if ball hits blocks, changes the direction according to whether it has hit the horizontal
	   					// surface or vertical surface or if iron is on. Also adds block to specials or fall if needed
				for (int i=0;i<blocks.size();i++){
					String b = blocks.get(i).collideBlocks(cx-(int)(Math.sin(Math.toRadians(cAng))*n),cy+(int)(cDirc*Math.cos(Math.toRadians(cAng))*n));
		
					if (b.equals("hori")==true){
						if (blocks.get(i).getSpec().equals("")==false){
							blocks.get(i).chX(blocks.get(i).getX()+15);
							blocks.get(i).chY(blocks.get(i).getY()+15);
							specials.add(blocks.get(i));
						}
						if (falling == true){
							fall.add(blocks.get(i));
						}
						blocks.remove(i);
						score++;
						if (ironON == false){
							balls.get(num).chDirc(-1);
						}
						break;
					}
					else if (b.equals("vert")){
						if (blocks.get(i).getSpec().equals("")==false){
							blocks.get(i).chX(blocks.get(i).getX()+15);
							blocks.get(i).chY(blocks.get(i).getY()+15);
							specials.add(blocks.get(i));
						}
						if (falling == true){
							fall.add(blocks.get(i));
						}
						blocks.remove(i);
						score++;
						if (ironON == false){
							balls.get(num).chAng(-1);
						}
						break;
					}
			   	}
	   		}
	   	}
    }
    
    // Allows us to access whether a key on the keyboard has been used
    public void keyTyped(KeyEvent e) {}
    
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }
    
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }
	
	// DRAWS - draws everything
	public void paintComponent(Graphics g){ 
		if(end == true){			// draws the end win and lose screens
			if (blocks.size()==0){
        		g.drawImage(win,0,0,this);
			}
			else{
				g.drawImage(lose,0,0,this);
			}
			g.setColor(Color.white);						// prints the scores
			g.setFont(new Font("TimesRoman", Font.PLAIN, 24));
			g.drawString("Scores (out of 168 blocks)",250,300);
			for (int i=0;i<menu.getScores().size();i++){		
				String str = "";
				str = str+menu.getScores().get(i);
				g.setColor(Color.white);
				g.setFont(new Font("TimesRoman", Font.PLAIN, 24));
				g.drawString(str,410-(str.length()*15),350+(i*30));
			//	System.out.println(menu.getScores().get(i));
			}
		}
		else{
			g.setColor(new Color(240,240,240));  	// draws background
	        g.fillRect(0,0,getWidth(),getHeight());
	      
			for (int i=0;i<blocks.size();i++){		// draws the remaining blocks
				g.drawImage(brick,blocks.get(i).getX(),blocks.get(i).getY(),this);
			}
			if (falling == true){		// draws the falling blocks
				for (int i=0;i<fall.size();i++){
					g.drawImage(brick,fall.get(i).getX(),fall.get(i).getY(),this);
				}
			}
			for (int i=0;i<specials.size();i++){		// draws the specials
				g.setColor(specials.get(i).getCol());
				g.fillOval(specials.get(i).getX(),specials.get(i).getY(),10,10);
			}
			for (int i=0;i<balls.size();i++){		// draws the balls
				g.setColor(balls.get(i).getCol());
	      		g.fillOval(balls.get(i).getX()-balls.get(i).getSize(),balls.get(i).getY()-balls.get(i).getSize(),2*balls.get(i).getSize(),2*balls.get(i).getSize());
			}
			g.setColor(Color.GREEN);		// draws the paddle and person
			g.fillRect(px,py,pSize,5);
			g.drawImage(person,px+((pSize-50)/2),py+5,this);
		}
    }
}

//=====================================================================================================================

// Ball deals with the different properties of the ball and have functions to change these properties.
class Ball{
	private int ang,size,dirc, speed,x,y;		// ang = angle from vertical	size = radius of ball
												// dirc = up or down			speed = speed of ball
												// x = x co-ordinate (centre)	y = y co-ordinate (centre)
	private Color col;							// col = color of ball
	public Ball (int a, int s, int d, int spe, int bx,int by, Color c){
		ang = a;
		size = s;
		dirc = d;
		speed = spe;
		x = bx;
		y = by;
		col = c;
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public int getAng(){
		return ang;
	}
	public int getSize(){
		return size;
	}
	public int getDirc(){
		return dirc;
	}
	public Color getCol(){
		return col;
	}
	public int getSpeed(){
		return speed;
	}

	public void chCol(Color c){
		col = c;
	}
	public void chDirc(int i){
		dirc*=i;
	}
	public void chAng(int i){
		ang*=i;
	}
	public void chX(double bx){
		x += bx;
	}
	public void chY(double by){
		y -= by;
	}
}

//=====================================================================================================================

// Block deals with the different properties of the blocks (including the specials and falling blocks) and
// functions to change these properties.
class Block{
	private int bx,by;			// bx = x co-ordinate (top, left corner)	by = y co-ordinate (top left corner)
	private String special;		// special = the special this block has
	private Color col;			// col = the color of the special
	public Block(int x, int y, String spec, Color c){
		bx = x;
		by = y;
		special = spec;
		col = c;
	}
	public int getX(){
		return bx;
	}
	public int getY(){
		return by;
	}
	public Color getCol(){
		return col;
	}
	public String getSpec(){
		return special;
	}

	public void chCol(Color c){
		col = c;
	}
	public void chX(int x){
		bx = x;
	}
	public void chY(int y){
		by = y;
	}
	
	// checks if an x,y co-ordinate has hit the block, then returns whether it has hit the horizontal side,
	// vertical side, or not at all.
	public String collideBlocks(int x,int y){
		if (bx<=x && x<=bx+50 && by==y || bx<=x && x<=bx+50&& y==by+15){
			return "hori";
		}
		else if (bx==x && by<=y && y<=by+15 || x==bx+50 && by<=y && y<=by+15){
			return "vert";
		}
		return "";
	}
}