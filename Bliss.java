package	bliss;

import	java.awt.*;
import	java.awt.event.*;
import	java.util.*;
import	java.net.*;
import	java.io.*;

public	class	Bliss	extends	Frame
	implements Constants, MouseMotionListener, WindowListener {

/****************************************************************************/
//	GLOBAL VARIABLES

static Image images[] = null;

/****************************************************************************/
//	FIELDS

Palette palette = null;
Board board = null;
LangList langlist = null;
Color myColor = null;

int kstart = 0;
int kpage = 0;	// number of page widget opened so far

/****************************************************************************/
//	CONSTRUCTOR

public	Bliss()
{
super("BlissTool");

setLayout(null);


(new WordLoader()).start();

palette = new Palette(8,30);
add(palette);



Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
dim.height -=80;
setSize(dim);

(new ImageLoader(this)).start();

myColor = new Color(0xAA,0xAA,0xAA);
//myColor = new Color(0x90,0x90,0x90);

setBackground(myColor);

addWindowListener(this);

setVisible(true);
palette.setVisible(true);

}

/****************************************************************************/

void    loadImages()
{
images = new Image[96];

MediaTracker mt = new MediaTracker(this);

for(int i=0;i<96;i++)
        {
        int w = SHAPEDIM[i*2]*8+4;
        int h = SHAPEDIM[i*2+1]*8+4;
        String name = "images/" + ((i<10)?"0"+i:""+i) + ".png";
        URL url = this.getClass().getResource(name);
        images[i] = Toolkit.getDefaultToolkit().createImage(url);
        mt.addImage(images[i],i,w,h);
        }

try     {
        mt.waitForAll();
        }
catch(Exception ex)
        {
        ex.printStackTrace();
        }

}       //      End of method   loadImages

/******************************************************************************/

int nmove = 0;

public	void	mouseMoved(MouseEvent e)
{
nmove = (nmove+1)%100000;
}

public	void	mouseDragged(MouseEvent e)
{
}

/******************************************************************************/

public	int getComponentIndex(Component comp)
{
int nc = countComponents();
for(int i=0;i<nc;i++)
	if(getComponent(i)==comp)
		return i;

return -1;

}	//	End of method	getComponentIndex

/******************************************************************************/

// override default Container method which retrieves invisible components

public	Component getComponentAt(int x, int y)
{
int nc = countComponents();
Component cc = this;

for(int i=0;i<nc;i++)
	{
	Component c = getComponent(i);
	if(!c.isVisible()) continue;
	if(c.getBounds().contains(x,y))
		{
		cc = c;
		break;
		}
	}

return cc;

}	//	End of method	getComponentAt

/******************************************************************************/

public	void	openBoard()
{
if(board==null)
	{
	board = new Board(70,36);
	add(board,1);
	}
else 	
	{
	remove(board);
	add(board,1);
	}

if(!board.isVisible())
	board.setVisible(true);

}	//	End of method	openBoard

/******************************************************************************/

public	void	openWordList()
{

WordList wl = new WordList("","",70,36);
add(wl,1);
wl.setVisible(true);

}	//	End of method	openWordList

/******************************************************************************/

public	void	openLangList()
{
if(langlist==null)
	{
	langlist = new LangList(70,36);
	add(langlist,1);
	}
else 	
	{
	remove(langlist);
	add(langlist,1);
	}

if(!langlist.isVisible())
	langlist.setVisible(true);

}	//	End of method	openLangList

/******************************************************************************/

public	void	openPage()
{
kpage++;
Page page = new Page(""+kpage,70,36);
add(page,1);
page.setVisible(true);

}	//	End of method	openPage

/******************************************************************************/

public	void	openSearch()
{
Search search = new Search(70,36);
add(search,1);
search.setVisible(true);

}	//	End of method	openSearch

/******************************************************************************/

public	void	openMessenger()
{
Messenger messenger = new Messenger(70,36);
add(messenger,1);
messenger.setVisible(true);
}

/******************************************************************************/

public	void	createWordList(String title, String code, int left, int top)
{
WordList w = new WordList(title,code,left,top);
add(w,1);
w.setVisible(true);
}	//	End of method	createNewList

/******************************************************************************/

public	void	closeWordList(WordList w)
{
remove(w);
}

/******************************************************************************/

public	void	bringToFront(Component comp)
{
remove(comp);
add(comp,1);	// put it just below the palette
}

/******************************************************************************/

public	void	wordAdded()
{
// must rebuild and refresh all word lists
int nc = countComponents();
for(int i=0;i<nc;i++)
	{
	Component comp = getComponent(i);
	if(comp instanceof WordList)
		{
		((WordList)comp).wordAdded();
		}
	}

}	//	End of method	wordAdded

/******************************************************************************/

public	void	editWord(String code, String name)
{
openBoard();
board.editWord(code,name);
}

public	void	editWord(GlyphPart g[], String name)
{

openBoard();
board.editWord(g,name);

}	//	End of method	editWord

/******************************************************************************/

public	void	paint(Graphics g)
{

drawLogo(g);

paintComponents(g);

drawVersion(g);

}	//	End of method	paint

/******************************************************************************/

void	drawLogo(Graphics g)
{

Rectangle r = getBounds();
int sidex = r.width/10;
int sidey = r.height/6;
if(sidex<sidey)
	sidey = sidex;
else
	sidex = sidey;

int marginx = (int)((r.width-10*sidex)/2);
int marginy = (int)((r.height-6*sidey)/2);

g.setColor(new Color(0xAD,0xAD,0xAD));

drawLine(g,marginx+8*sidex,marginy+1*sidey,marginx+5*sidex,marginy+1*sidey);
drawLine(g,marginx+5*sidex,marginy+1*sidey,marginx+8*sidex,marginy+5*sidey);
drawLine(g,marginx+8*sidex,marginy+5*sidey,marginx+5*sidex,marginy+5*sidey);
drawLine(g,marginx+3*sidex,marginy+1*sidey,marginx+3*sidex,marginy+5*sidey);
drawLine(g,marginx+3*sidex,marginy+5*sidey,marginx+1*sidex,marginy+3*sidey);
drawLine(g,marginx+2*sidex,marginy+2*sidey,marginx+4*sidex,marginy+2*sidey);
drawLine(g,marginx+4*sidex,marginy+2*sidey,marginx+4*sidex,marginy+4*sidey);
drawLine(g,marginx+4*sidex,marginy+4*sidey,marginx+2*sidex,marginy+4*sidey);
drawLine(g,marginx+2*sidex,marginy+4*sidey,marginx+2*sidex,marginy+2*sidey);

}	//	End of method	drawLogo

/******************************************************************************/

void	drawLine(Graphics g, int x1, int y1, int x2, int y2)
{
int rad = 20;
int nx = 4*Math.abs(x2-x1)/rad;
int ny = 4*Math.abs(y2-y1)/rad;

int n = (nx<ny) ? ny : nx;
if(n<1) n = 1;

for(int i=0;i<=n;i++)
	{
	int x = x1 + (int)((x2-x1)*i/n);
	int y = y1 + (int)((y2-y1)*i/n);
	g.fillOval(x-rad/2,y-rad/2,rad,rad);
	}

}

/******************************************************************************/

void	drawVersion(Graphics g)
{

Rectangle r = getBounds();

g.setColor(Color.black);
g.setFont(textfont);
g.drawString(VERSION,5,r.height-5);

}	//	End of method	drawVersion

/******************************************************************************/

public	void	windowActivated(WindowEvent e) {}
public	void	windowClosed(WindowEvent e) {}
public	void	windowDeactivated(WindowEvent e) {}
public	void	windowIconified(WindowEvent e) {}
public	void	windowDeiconified(WindowEvent e) {}
public	void	windowOpened(WindowEvent e) {}

public	void	windowClosing(WindowEvent e)
{
System.exit(0);
//dispose();
}

/******************************************************************************/

//	give each widget a chance to terminate gracefully 

protected void finalize() throws Throwable 
{

int nc = countComponents();

for(int i=nc-1;i>=0;i--)
	{
	Component comp = getComponent(i);
	if(comp instanceof Widget)
		((Widget)comp).close();
	}

Dict.saveWords();

Dict.saveLanguages();

}	//	End of method	finalize

/******************************************************************************/

public	static void main(String args[])
{

// on macosx make menu bar unique
System.setProperty("apple.laf.useScreenMenuBar", "true");

// on macosx make set menu name
System.setProperty("com.apple.mrj.application.apple.menu.about.name",
		"BlissTool");

// make sure changed data are saved
System.runFinalizersOnExit(true);

for(int i=0;i<args.length;i++)
	{
	if(args[i].equals("-locale"))
		{
		System.out.println("set locale "+args[i+1]+" "+args[i+2]+" "+args[i+3]);
		Locale.setDefault(new Locale(args[i+1],args[i+2],args[i+3]));
		i+=3;
		}
	}

Bliss bliss = new Bliss();
}	//	End of method	main

/****************************************************************************/
/****************************************************************************/

class	ImageLoader	extends	Thread	{

/****************************************************************************/
//	FIELDS

Component comp;

/****************************************************************************/
//	CONSTRUCTOR

ImageLoader(Component comp)
{
this.comp = comp;
}

/****************************************************************************/

public	void	run()
{
images = new Image[96];


MediaTracker mt = new MediaTracker(comp);

for(int i=0;i<96;i++)
        {
        int w = SHAPEDIM[i*2]*8+4;
        int h = SHAPEDIM[i*2+1]*8+4;
        String name = "images/" + ((i<10)?"0"+i:""+i) + ".png";
        URL url = comp.getClass().getResource(name);
        images[i] = Toolkit.getDefaultToolkit().createImage(url);
        mt.addImage(images[i],i,w,h);
        }


try     {
        mt.waitForAll();
        }
catch(Exception ex)
        {
        ex.printStackTrace();
        }


}       //      End of method   run

/******************************************************************************/

}	//	End of class	ImageLoader

/******************************************************************************/
/******************************************************************************/

class	WordLoader	extends	Thread	{

public	void	run()
{
Dict.init(this);
}

}	//	End of class	WordLoader

/******************************************************************************/
/******************************************************************************/

}	//	End of class	Bliss
