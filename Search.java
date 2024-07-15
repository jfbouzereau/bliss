package	bliss;

import	java.awt.*;
import	java.awt.event.*;
import	java.text.*;
import	java.util.*;

public	class	Search	extends	Widget	
	implements Constants, GlyphReceiver,
		ComponentListener, MouseListener, MouseMotionListener,
		 TextListener {

/*****************************************************************************/
//	CONSTANTS

static	final	int	WTARGET = 132;		// width of target icon
static	final	int	HTARGET = 50;		// height of target icon

/*****************************************************************************/
//	FIELDS

TextField field = null;

Comparator comparator = null;
Collator collator = null;

GlyphPart glyph[] = null;		// glyph to be searched

int wnum[] = null;			// indices of results in word array
int ifirst = 0;				// first result display
int npage = 10;				// number of results per page

/*****************************************************************************/
//	CONSTRUCTOR

public	Search(int left, int top)
{
this._left = left;
this._top = top;
this._width = 380;
this._height = 20 + (npage+1)*HTARGET;

setInterface();

myColor = searchColor;
myDarkColor = searchDarkColor;
myLightColor = searchLightColor;

collator = Collator.getInstance();
comparator = new WordComparator(collator);

setLayout(null);

field = new TextField("");
field.setBackground(Color.white);
field.setBounds(WTARGET+2,20+10,_width-WTARGET-20-4,20);
add(field);

setBounds(_left,_top,_width+1,_height+1);
setVisible(false);

addMouseListener(this);
addMouseMotionListener(this);
addComponentListener(this);

field.addTextListener(this);

}

/*****************************************************************************/

public	void	paint(Graphics g)
{


// title
String s = (wnum==null) ? "" : (wnum.length+"");
drawTitle(g,s);

// close box
drawClose(g);

// background
g.setColor(action==ACTION_DROPW ? acgray : dkgray);
g.fillRect(0,MARGIN,_width,_height-MARGIN);


// target
g.setColor(myColor);
g.fillRect(0,MARGIN,WTARGET,HTARGET);
g.setColor(Color.black);
g.drawRect(0,MARGIN,WTARGET-1,HTARGET-1);
g.setColor(Color.white);
g.drawLine(0,MARGIN,WTARGET,MARGIN);
drawGlyph(g,glyph,0,MARGIN);


// results

if(wnum!=null)
	{
	g.setFont(textfont);

	int top = MARGIN;

	for(int i=0;i<npage;i++) if(ifirst+i<wnum.length)
		{
		g.setColor(action==ACTION_DROPW ? acgray : ltgray);
		g.fillRect(0,top+(i+1)*HTARGET,_width,HTARGET);

		// draw the glyph
		g.setColor(Color.black);
		GlyphPart gl[] = Util.buildGlyph(
			Dict.words[wnum[ifirst+i]].code,1);
		drawGlyph(g,gl,0,top+(i+1)*HTARGET);

		// corresponding name
		g.drawString(Dict.words[wnum[ifirst+i]].name,
			WTARGET+10,top+(i+2)*HTARGET-HTARGET/2+5);
		
		g.setColor(Color.white);
		g.drawLine(0,top+(i+1)*HTARGET,_width,top+(i+1)*HTARGET);
		}
	}

// scrollbars
drawScroll(g,rUp,action==ACTION_UP);
drawScroll(g,rDown,action==ACTION_DOWN);


// resize box
drawResize(g);

drawBorders(g);

}	//	End of method	paint

/*****************************************************************************/

void    drawGlyph(Graphics g, GlyphPart gl[], int left, int top)
{
if(gl==null) return;

Limits l = Util.computeGlyphLimits(gl,1);
int dx = left + WTARGET/2 - (l.xmin+l.xmax)/2;
int dy = top + HTARGET/2 - (l.ymin+l.ymax)/2;

for(int i=0;i<gl.length;i++)
        {
        int is = gl[i].ishape;
        int w = SHAPEDIM[is*2]*4+2;
        int h = SHAPEDIM[is*2+1]*4+2;
        int x = gl[i].x*4 + dx;
        int y = gl[i].y*4 + dy;
        g.drawImage(Bliss.images[is],x,y,w,h,this);
        }

}       //      End of method   drawGlyph

/*****************************************************************************/

public	void	receive(GlyphPart gl[])
{
if(gl!=null)
	{
	glyph = Util.copyGlyph(gl);

	setResult();
	}

action = ACTION_NONE;
repaint();

}	//	End of method	receive

/*****************************************************************************/

void	setResult()
{
// look for all words matching the search criteria

String text = field.getText();

Vector v = new Vector();

for(int i=0;i<Dict.words.length;i++)
	if(validWord(i,text))
		v.addElement(new Integer(i));

int nr = v.size();
wnum = new int[nr];
for(int i=0;i<nr;i++)
	wnum[i] = ((Integer)v.elementAt(i)).intValue();

Util.quickSort(wnum,comparator);

ifirst = 0;

}	//	End of method	setResult

/*****************************************************************************/

boolean	validWord(int num, String text)
{
boolean ok = false;

// if no criteria yet, no result
if((glyph==null)&&(text.length()==0))
	return false;

GlyphPart gl[] = Util.buildGlyph(Dict.words[num].code,1);

if((glyph!=null)&&(glyph.length==1))
	{
	// check if word glyph contains target glyph
	for(int i=0;i<gl.length;i++)
		if(gl[i].ishape == glyph[0].ishape)
			{
			ok = true;
			break;
			}

	if(!ok)
		return false;
	}

if((glyph!=null)&&(glyph.length>1))
	{
	ok = false;

	// check if word glyph contains target glyph

	// look for the first char
	for(int i=0;i<gl.length;i++)
		{
		if(gl[i].ishape == glyph[0].ishape)
			{
			// check if other chars are presents at same relative location
			for(int j=1;j<glyph.length;j++)
				{
				ok = false;
				for(int k=0;k<gl.length;k++)
					if((gl[k].ishape==glyph[j].ishape) &&
					(gl[k].x-gl[i].x == glyph[j].x -glyph[0].x) &&
					(gl[k].y-gl[i].y == glyph[j].y -glyph[0].y))
						{
						ok = true;
						break;
						}
				if(!ok) break;
				}
			}
		if(ok) break;
		}

	if(!ok) 
		return false;
	}

// check if word contains the typed text
if(text.length()>0)
	{
	ok = Dict.words[num].name.indexOf(text)>=0;

	if(!ok)
		return false;
	}

return true;
	
}	//	End of method	validWord

/*****************************************************************************/

public	void	close()
{
glyph = null;
field.setText("");
wnum = null;
((Bliss)getParent()).remove(this);
}

/*****************************************************************************/

void    checkTarget(MouseEvent e)
{
// check if object dropped on glyphReceiver

int ix = e.getX()+_left;
int iy = e.getY()+_top;

Bliss bliss = (Bliss)getParent();
Component c = bliss.getComponentAt(ix,iy);
if((c!=target)&&(c!=this))
        {
        if(target instanceof GlyphReceiver)
                {
                ((Widget)target).setAction(ACTION_NONE);
                target.paint(target.getGraphics());
                }
	if(target instanceof Board)
		((Board)target).dropWord(-1);

        target = c;

	if(target instanceof Board)
		((Board)target).dropWord(iword);
        if(target instanceof GlyphReceiver)
                {
                ((Widget)target).setAction(ACTION_DROPW);
                target.paint(target.getGraphics());
                }
        }

if(target instanceof Board)
	{
	Board board = (Board)target;
	e.translatePoint(_left-board._left,_top-board._top);	
	board.mouseDragged(e);
	}

}       //      End of method   checkTarget

/******************************************************************************/

public	void	mouseClicked(MouseEvent e) {}
public	void	mouseEntered(MouseEvent e) {}
public	void	mouseExited(MouseEvent e) {}
public	void	mouseMoved(MouseEvent e) {}

/*****************************************************************************/

int xclick, yclick;
int scrollid = 0;
int oldw,oldh;
int curw,curh;
int iword = -1;	// word being selected
Component target = null;
GlyphPart dglyph[] = null;		// glyph to be searched

/*****************************************************************************/

public	void	mousePressed(MouseEvent e)
{
iword = -1;
target = null;
xclick = e.getX();
yclick = e.getY();
action = ACTION_NONE;

if(rClose.contains(xclick,yclick))
	{
	action = ACTION_CLOSE;
	paint(getGraphics());
	}
else if(yclick<20)
	{
	action = ACTION_DRAG;
	}
else if(rUp.contains(xclick,yclick))
        {
        action = ACTION_UP;
        paint(getGraphics());
        (new Scroller(++scrollid)).start();
        }
else if(rDown.contains(xclick,yclick))
        {
        action = ACTION_DOWN;
        paint(getGraphics());
        (new Scroller(++scrollid)).start();
        }
else if(rSize.contains(xclick,yclick))
	{
	oldw = curw = _width;
	oldh = curh = _height;
	action = ACTION_SIZE;
	}
else 
	{
	// click in a word
	int i = (yclick-20)/HTARGET -1;
	if((i>=0)&&(i+ifirst>=0)&&(i+ifirst<wnum.length))
		{
		iword = wnum[i+ifirst];
		if(e.getClickCount()>1)
			{
			String code = Dict.words[iword].code;
			String name = Dict.words[iword].name;
			((Bliss)getParent()).editWord(code,name);
			}		
		else
			{
			// glyph to be dragged
			dglyph = Util.buildGlyph(Dict.words[iword].code,1);
			action = ACTION_DRAGW;	// drag word
			}
		}
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
        return;
        }
else if(action==ACTION_SIZE)
	{
	int xmove = e.getX()-xclick;
        int ymove = e.getY()-yclick;
        _height = oldh + ymove;

        npage = (_height-20-HTARGET/2)/HTARGET;
	if(npage<1)
		npage = 1;

	_height = 20 + (npage+1)*HTARGET;

	if((_width!=curw)||(_height!=curh))
		{
		curw = _width;
		curh = _height;

		setInterface();
		setBounds(_left,_top,_width+1,_height+1);
		
		paint(getGraphics());
		}
	}
else if(action==ACTION_DRAGW)
	{
	checkTarget(e);
	}

}	//	End of method	mouseDragged
	
/*****************************************************************************/

public	void	mouseReleased(MouseEvent e)
{
// to stop the scroller if any
scrollid++;

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
else if(action==ACTION_DRAGW)
	{
	if(target instanceof GlyphReceiver)
		((GlyphReceiver)target).receive(dglyph);
	else if(target instanceof Board)
		{
		Board board = (Board)target;
		e.translatePoint(_left-board._left,_top-board._top);	
		board.mouseReleased(e);
		}
	iword = -1;
	}

action = ACTION_NONE;
repaint();

}	//	End of method	mouseReleased

/*****************************************************************************/

public	void	textValueChanged(TextEvent e)
{
setResult();
repaint();
}	//	End of method	textValueChanged

/*****************************************************************************/


/*****************************************************************************/
                
public  void    componentHidden(ComponentEvent e) {}

public  void    componentResized(ComponentEvent e)
{ field.requestFocus(); }

public  void    componentMoved(ComponentEvent e) 
{ field.requestFocus(); }

public  void    componentShown(ComponentEvent e)
{ field.requestFocus(); }

/*****************************************************************************/
/*****************************************************************************/

//      class to autoscroll the list when the mouse is pressed on the bar

class   Scroller        extends Thread  {

int id;

Scroller(int id)
{
this.id = id;
}

/*****************************************************************************/

public  void    run()
{
while(scrollid==id)
        {
        if(action==ACTION_UP)
                {
                ifirst -= npage-1;
                if(ifirst<0)
                        ifirst = 0;
                }
        else if(action==ACTION_DOWN)
                {
                ifirst += npage-1;
                if(ifirst>wnum.length-1)
                        ifirst = wnum.length-1;
                }
        paint(getGraphics());
        try     { sleep(200); } catch(Exception ex) {}
        }

}       //      End of method   run

/*****************************************************************************/

}       //      End of class    Scroller

/*****************************************************************************/
/*****************************************************************************/

}	//	End of class	Search
