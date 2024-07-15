package	bliss;

import	java.awt.*;
import	java.awt.event.*;
import	java.text.*;
import	java.util.*;

public	class	Mouser extends Widget	
	implements Constants, 
		MouseListener, MouseMotionListener {

/*****************************************************************************/
//	CONSTANTS

static	final	int	WDISPLAY = 300;	// width of display
static	final	int	HDISPLAY = 400;	// height of display

static	final	char	ellipsis = '\u2026';

static	final	int	X1A = 0;
static	final	int	X1B = 20;
static	final	int	X2A = 60;
static	final	int	X2B = 100;

/*****************************************************************************/
//	FIELDS


String title = "";

Collator collator = null;
WordComparator comparator = null;

int wnum[] = null;

GlyphReceiver bucket = null;			// to whom I must send the typing

int	xmouse;
int	ymouse;

List	list1 = null;
List	list2 = null;
List	list3 = null;
List	list4 = null;
List	list5 = null;

Font font1 = null;
Font font2 = null;
Font font3 = null;
Font fontb = null;
int height1, height2, height3;
int descent1, descent2, descent3;

/*****************************************************************************/
//	CONSTRUCTOR

public	Mouser(GlyphReceiver bucket, Color myColor, Color myDarkColor,
	Color myLightColor, String title, int left, int top)
{
setLayout(null);

font1 = new Font("Helvetica",Font.PLAIN,12);
font2 = new Font("Helvetica",Font.PLAIN,12);
font3 = new Font("Helvetica",Font.PLAIN,28);
fontb = new Font("Helvetica",Font.BOLD,12);

FontMetrics fm;

fm = Toolkit.getDefaultToolkit().getFontMetrics(font1);
height1 = fm.getHeight();
descent1 = fm.getDescent();

fm = Toolkit.getDefaultToolkit().getFontMetrics(font2);
height2 = fm.getHeight();
descent2 = fm.getDescent();

fm = Toolkit.getDefaultToolkit().getFontMetrics(font2);
height3 = fm.getHeight();
descent3 = fm.getDescent();

this.bucket = bucket;
this.myColor = myColor;
this.myDarkColor = myDarkColor;
this.myLightColor = myLightColor;
this.myColor = myColor;
this.title = title;
this._left = left;
this._top = top;
this._width = WDISPLAY;
this._height = 20+HDISPLAY;

collator = Collator.getInstance();
collator.setStrength(Collator.PRIMARY);	// ignore case and accents

comparator = new WordComparator(collator);

// create index list
wnum = new int[Dict.words.length];
for(int i=0;i<wnum.length;i++)
        wnum[i] = i;
                
Util.quickSort(wnum,comparator);

list1 = new List();
list2 = new List();
list3 = new List();
list4 = new List();
list5 = new List();

buildList(list1,"");

setInterface();


setBounds(_left,_top,_width+1,_height+1);
setVisible(true);

addMouseListener(this);
addMouseMotionListener(this);

setVisible(false);

}

/*****************************************************************************/

void	buildList(List list, String prefix)
{
int lprefix = prefix.length();

String olds = "";

list.isel = -1;
list.n = 0;

for(int i=0;i<wnum.length;i++)
	{
	int k = comparator.comparePrefix(wnum[i],prefix);
	if(k==0)
		{
		String news = Dict.words[wnum[i]].name.substring(0,lprefix+1);
		if(comparator.compare(olds,news)!=0)
			{
			list.entries[list.n].string = news;
			list.n++;
			olds = news;
			}
		}
	/*
	else if(k>0)
		break;
	*/
	}


System.out.println(list.n+" entries for "+prefix);

}	//	End of method	buildList

/*****************************************************************************/

void	drawList(Graphics g, List list, int x, int yc)
{
int y;

/*
if(list.isel>=0)
	{
	int k = list.isel;
	y = yc + (k-list.n/2)*height1;
	g.setFont(font3);
	g.drawString(list.entries[k].string,x,y+height3/2-descent3);

	if(k>0)
		{
		g.setFont(font2);
		y = y-height3/2-height2/2;
		g.drawString(list.entries[k-1].string,x,y+height2/2-descent2);
		}

	y = y-height2/2-height1/1;

	g.setFont(font1);
	for(int j=k-2;j>=0;j--)
		{
		g.drawString(list.entries[j].string,x,y+height1/2-descent1);
		y = y-height1;
		}

	y = yc + (k-list.n/2)*height1;
	if(k<list.n-2)
		{
		g.setFont(font2);
		y = y+height3/2+height2/2;
		g.drawString(list.entries[k+1].string,x,y+height2/2-descent2);
		}

	y = y + height2/2+height1/2;
	g.setFont(font1);
	for(int j=k+2;j<list.n;j++)
		{
		g.drawString(list.entries[j].string,x,y+height1/2-descent1);
		y = y+height1;
		}
	}
else
	{
*/


	int k = list.n/2;
	y = yc;

	g.setFont(k==list.isel?fontb:font1);
	g.drawString(list.entries[k].string,x,y+height1/2-descent1);

	for(int j=k-1;j>=0;j--)
		{
		y = y-height1;
		g.setFont(j==list.isel?fontb:font1);
		g.drawString(list.entries[j].string,x,y+height1/2-descent1);
		}

	y = yc;
	for(int j=k+1;j<list.n;j++)
		{
		y = y+height1;
		g.setFont(j==list.isel?fontb:font1);
		g.drawString(list.entries[j].string,x,y+height1/2-descent1);
		}

/*
	}
*/

}	//	End of method	drawList

/*****************************************************************************/

public	void	paint(Graphics g)
{

g.setColor(dkgray);
g.fillRect(0,0,_width,_height);

g.setColor(Color.black);
g.setFont(font1);

int x = 10;
int yc = MARGIN + (_height-MARGIN)/2;
int y = 0;

drawList(g,list1,X1A+10,yc);

if(list1.isel>=0)
	drawList(g,list2,X2A+10,yc);

// title bar
drawTitle(g,title);

// close box
drawClose(g);

drawBorders(g);

}	//	End of method	paint

/*****************************************************************************/

void	drawGlyph(Graphics g, GlyphPart gl[])
{
/*
if(gl==null) return;


for(int i=0;i<gl.length;i++)
        {
        int is = gl[i].ishape;
        int w = SHAPEDIM[is*2]*4+2;
        int h = SHAPEDIM[is*2+1]*4+2;
        int x = WMODIFIER + MARGIN + gl[i].x*4 -2;
        int y = MARGIN + HINDIC + (gl[i].y)*4 ;
        g.drawImage(Bliss.images[is],x,y,w,h,this);
        }
*/
}	//	End of method	drawGlyph

/*****************************************************************************/

GlyphPart[] addGlyph(GlyphPart gold[], GlyphPart gnew[])
{
GlyphPart gg[]=null;

if(gold==null)
	{
	// no glyph yet
	gg = Util.copyGlyph(gnew);
	}
else
	{
	// else put the new one it at 1/4 of unit on the right of the old one
	gg = Util.copyGlyph(gnew);

	Limits l = Util.computeGlyphLimits(gold,1);

	for(int i=0;i<gg.length;i++)
		gg[i].x += l.xmax/4 + 2;

	gg = Util.concatGlyph(gold,gg);
	}

return gg;

}	//	End of method	addGlyph

/*****************************************************************************/

void	adjustGlyph()
{
/*
Limits l;

// make sure prefix, main part and suffix are properly espaced
int left = 0;

if(pglyph!=null)
	{
	l = Util.computeGlyphLimits(pglyph,1);

	for(int i=0;i<pglyph.length;i++)
		pglyph[i].x += (left-l.xmin)/4;

	left += l.xmax-l.xmin + 8;
	}

if(glyph!=null)
	{
	l = Util.computeGlyphLimits(glyph,1);

	for(int i=0;i<glyph.length;i++)
		glyph[i].x += (left-l.xmin)/4;
	
	left += l.xmax-l.xmin + 8;
	}

if(sglyph!=null)
	{
	l = Util.computeGlyphLimits(sglyph,1);

	for(int i=0;i<sglyph.length;i++)
		sglyph[i].x += (left-l.xmin)/4;

	left = l.xmax-l.xmin + 8;
	}

*/
}	//	End of method	adjustGlyph

/*****************************************************************************/

void	removeGlyphPart()
{
/*
if(glyph==null) return;
if(glyph.length==0) return;

// look for the last character of the glyph
int k = -1;
int xmin = -9999;
int xmax = -9999;

for(int i=0;i<glyph.length;i++)
	{
	if(glyph[i].x> xmax)
		{
		k = i;
		int is = glyph[i].ishape;	
		xmin = glyph[i].x;
		xmax = xmin + SHAPEDIM[is*2];
		}
	else
		{
		int is = glyph[i].ishape;
		if(glyph[i].x+SHAPEDIM[is*2]>xmax)
			xmax = glyph[i].x + SHAPEDIM[is*2];
		}
	}

GlyphPart gl[] = new GlyphPart[k];
for(int i=0;i<k;i++)
	gl[i] = glyph[i];

glyph = gl;

repaint();
*/

}	//	End of method	removeGlyphPart

/*****************************************************************************/

void	clear()
{

repaint();

}	//	End of method	clear

/*****************************************************************************/

void	sendToGlyphReceiver()
{
if(bucket==null) return;

//bucket.receive(Util.concatGlyph(pglyph,glyph,sglyph));

clear();
repaint();

}	//	End of method	sendToGlyphReceiver

/*****************************************************************************/

public	void	close()
{
System.out.println("Closing!");
((Bliss)getParent()).remove(this);
}

/*****************************************************************************/

int	xclick, yclick;

public	void 	mouseClicked(MouseEvent e) {}
public	void	mouseEntered(MouseEvent e) {}
public	void	mouseExited(MouseEvent e) {}
public	void	mouseMoved(MouseEvent e)
{
int yc = MARGIN + (_height-MARGIN)/2;

int x = e.getX();

if((x>X1A)&&(x<X1B))
	{
	int iy  = list1.n/2 + (e.getY()-yc)/height1;

	if(iy<0)
		{
		list1.isel = -1;
		list2.n = 0;
		}
	else if(iy>=list1.n)
		{
		list1.isel = -1;
		list2.n = 0;
		}
	else
		{
		list1.isel = iy;
		buildList(list2,list1.entries[list1.isel].string);
		}
	}

if((x>X2A)&&(x<X2B)&&(list2.n>0))
	{
	int iy = list2.n/2 + (e.getY()-yc)/height1;

	if(iy<0)
		list2.isel = -1;
	else if(iy>=list2.n)
		list2.isel = -1;
	else
		list2.isel = iy;
	}

repaint();

}


/*****************************************************************************/

public	void	mousePressed(MouseEvent e) 
{
action = ACTION_NONE;

xclick = e.getX();
yclick = e.getY();

if(rClose.contains(xclick,yclick))
	{
	action = ACTION_CLOSE;
	paint(getGraphics());
	}
else if(yclick<MARGIN)
	{
	action = ACTION_DRAG;
	}

}	//	End of method	mousePressed

/*****************************************************************************/

public	void	mouseDragged(MouseEvent e)
{
if(action==ACTION_DRAG)
	{
        int xmove = e.getX()-xclick;
        int ymove = e.getY()-yclick;
        _left += xmove;
        _top += ymove;
        setLocation(_left,_top);
        xclick = e.getX()-xmove;
        yclick = e.getY()-ymove;;
	}
}

/*****************************************************************************/

public	void	mouseReleased(MouseEvent e)
{

if(action==ACTION_DRAG)
        {
        ((Bliss)getParent()).bringToFront(this);
        }
else if(action==ACTION_CLOSE)
	{
	if(rClose.contains(e.getX(),e.getY()))
		{
		close();
		return;
		}
	}

action = ACTION_NONE;

repaint();

}	//	End of method	mouseReleased

/*****************************************************************************/
/*****************************************************************************/

class	Entry	{

String string;
int	count;
Word	word;

Entry(String string, int count, Word word)
{
this.string = string;
this.count = count;
this.word = word;
}

}	//	End of class	Entry

/*****************************************************************************/
/*****************************************************************************/

class	List	{

Entry entries[] = null;
int n = 0;
int isel = -1;

List()
{
int n = 0;
entries = new Entry[100];
for(int i=0;i<entries.length;i++)
	entries[i] = new Entry("",0,null);
}

}	//	End of class	List

/*****************************************************************************/
/*****************************************************************************/

}	//	End of class	Mouser

