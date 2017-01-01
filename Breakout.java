// Breakout.java
// Judy Lin
// RUN THIS ONE

// imports everything
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.awt.image.*; 
import java.io.*; 
import javax.imageio.*; 

// makes the menu, allows the user to choose what mode and calls BreakoutGame
class Breakout extends JFrame implements ActionListener{
   	JButton reg, fall;	// reg = button that lets you into the regular mode		fall = falling mode
    Image back;		// back = background image
    ArrayList <Integer> scores = new ArrayList <Integer>();
   	
   	// CONSTRUCTOR
   	public Breakout(){
   		super ("Breakout");
   		setLayout(null);
   		reg = new JButton("Regular");
    	reg.addActionListener(this);
    	reg.setSize(150,40);
    	reg.setLocation(325, 340);
    	add(reg);
    	
    	fall = new JButton("Falling");
    	fall.addActionListener(this);
    	fall.setSize(150,40);
    	fall.setLocation(325, 400);
    	add(fall);
    	

    	back = new ImageIcon("pics/menu.png").getImage();
    	setSize (800,650);
		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		
		setVisible (true);
   	}
   	
    public void actionPerformed (ActionEvent evt){	// lets user choose which mode to use
    	Object source = evt.getSource ();
    	if(source == reg){		// regular mode
    		JOptionPane.showMessageDialog (null, "Specials:\nPink - new ball\nYellow - iron ball\nBlue - regular ball\nRed - large paddle\nGreen - regular paddle\n");
			BreakoutGame game = new BreakoutGame(this, false);		
		}
		else if(source == fall){	// falling mode
    		JOptionPane.showMessageDialog (null, "Avoid the falling bricks.");
    		BreakoutGame game = new BreakoutGame(this, true);			
		}
    }
    
    // adds value s to the arraylist scores
    public void addScore(int s){
    	scores.add(s);
 //   	Collections.sort(scores);
   // 	Collections.reverse(scores);
    	if (scores.size()>5){
    		scores.remove(0);
    	}
    }
    
    // returns arraylist scores
    public ArrayList<Integer> getScores(){
    	return scores;
    }
    
    // draws the background
    public void paint (Graphics g){		
    	if (back!=null){
    		g.drawImage(back,0,0,this);
    	}
    }
    
    public static void main(String []args){   
		Breakout bk = new Breakout();
	}
}
