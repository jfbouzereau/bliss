package	bliss;

import	java.awt.*;
import	java.awt.event.*;
import	java.text.*;

public	class	WordList	extends	Widget
	implements Constants, MouseListener, MouseMotionListener {

/*****************************************************************************/
//	CONSTANTS

static	final	int	WIDTH = 250;
static	final	int	WH = 13;

/*****************************************************************************/
//	FIELDS

Font	font = null;

int	wnum[] = null;

Image offscreen = null;

int	ifirst = 0;	// first word displayed
int	npage = 30;	// number of words per page



String	title = null;
String	code = null;

Collator collator = null;
Comparator comparator = null;

/*****************************************************************************/
//	CONSTRUCTOR

public	WordList(String title, String code, int left, int top)
{
this._left = left;
this._top = top;
this._width = WIDTH;
this._height = MARGIN + npage*WH;
this.title = title;
this.code = code;

setInterface();

myColor = listColor;
myDarkColor = listDarkColor;
myLightColor = listLightColor;

font = textfont;

collator = Collator.getInstance();
comparator = new WordComparator(collator);

// create index list
createIndexList();
setBounds(_left,_top,_width+1,_height+1);

addMouseListener(this);
addMouseMotionListener(this);

setVisible(false);

}

/*****************************************************************************/

public	void	paint(Graphics g)
{
Graphics og = g;

// title bar
String s = title+"  ("+wnum.length+")";
drawTitle(og,s);

// close box
drawClose(og);

// content
og.setColor(action==ACTION_DROPW ? acgray : dkgray);
og.fillRect(0,MARGIN,_width,_height-MARGIN);

og.setFont(font);
og.setColor(Color.black);

int top = MARGIN;
for(int i=ifirst;i<wnum.length;i++)
	{
	og.setColor(action==ACTION_DROPW ? acgray : ltgray);
	og.fillRect(0,top,_width,WH);
	og.setColor(Color.black);
	og.drawString(Dict.words[wnum[i]].name,10,top+WH-3);
	if(wnum[i]==iword)
		{
		og.setColor(Color.black);
		og.drawLine(0,top+1,_width,top+1);
		og.setColor(Color.white);
		og.drawLine(0,top+2,_width,top+2);

		og.setColor(Color.black);
		og.drawLine(0,top+WH-2,_width,top+WH-2);
		og.setColor(Color.white);
		og.drawLine(0,top+WH-1,_width,top+WH-1);
		}
	top += WH;
	if(top>=_height) break;	
	}


// scroll bars
drawScroll(og,rUp,action==ACTION_UP);
drawScroll(og,rDown,action==ACTION_DOWN);

// resize box
drawResize(og);

// borders
drawBorders(og);

g.drawImage(offscreen,0,0,this);

}	//	End of method	paint

/*****************************************************************************/

void	createIndexList()
{
wnum = new int[Dict.words.length];
for(int i=0;i<wnum.length;i++)
	wnum[i] = i;

Util.quickSort(wnum,comparator);
}

/*****************************************************************************/

public	void	wordAdded()
{

createIndexList();
repaint();

}	//	End of method	addWord

/*****************************************************************************/

void	createNewList(int index)
{
System.out.println("create new list "+
	Dict.words[index].name+" "+Dict.words[index].code);

Bliss bliss = (Bliss)getParent();
bliss.createWordList(Dict.words[index].name,
	Dict.words[index].code,
	_left+50,_top+50);

}	//	End of method	createNewList

/*****************************************************************************/

public	void setAction(int a)
{
action = a;
}

/*****************************************************************************/

public	void	close()
{
Bliss bliss = (Bliss)getParent();
bliss.closeWordList(this);
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

/*****************************************************************************/

public void mouseClicked(MouseEvent e) {}
public void mouseEntered(MouseEvent e) {}
public void mouseExited(MouseEvent e) {}
public void mouseMoved(MouseEvent e) {}


int xclick, yclick;
int oldw,oldh;
int scrollid = 0;
Component target ;
GlyphPart dglyph[] = null;	// glyph being dragged
int	iword = -1;	// word being dragged or dropped

/*****************************************************************************/

public void mousePressed(MouseEvent e)
{
target = null;
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
	oldw = _width;
	oldh = _height;
	action = ACTION_SIZE;
	}
else if((yclick>MARGIN)&&(yclick<_height))
	{
	int iw = (yclick-MARGIN)/WH + ifirst;
	if((iw>=0)&&(iw<wnum.length))
		{
		iword = wnum[iw];
		if(e.getClickCount()>1)
			{
			// double click
			String code = Dict.words[iword].code;
			String name = Dict.words[iword].name;
			((Bliss)getParent()).editWord(code,name);
			iword = -1;
			repaint();
			}
		else
			{	
			dglyph = Util.buildGlyph(Dict.words[iword].code,1);
			action = ACTION_DRAGW;
			paint(getGraphics());
			}
		}
	}

}	//	End of method	mousePressed

/*****************************************************************************/

public void mouseDragged(MouseEvent e) 
{

if(action==ACTION_DRAG)
	{
	int xmove = e.getX()-xclick;
        int ymove = e.getY()-yclick;
        _left += xmove; 
        _top += ymove;  
        setLocation(_left,_top);
        xclick = e.getX()-xmove;
        yclick = e.getY()-ymove;
	return;
	}
else if(action==ACTION_SIZE)
	{
	int xmove = e.getX()-xclick;
	int ymove = e.getY()-yclick;
	_width = oldw + xmove;
	if(_width<80)
		_width = 80;
	_height = oldh + ymove;
	if(_height<80)
		_height = 80;

	_height = (_height-MARGIN+WH-1)/WH*WH+MARGIN;
	int h2 = (_height-40)/2;
	npage = (_height-MARGIN)/WH;

	setInterface();

	setBounds(_left,_top,_width+1,_height+1);

	// force recreation
	offscreen = null;

	paint(getGraphics());
	}
else if(action==ACTION_DRAGW)
	{
	checkTarget(e);
	}

}	//	End of method	mouseDragged

/*****************************************************************************/

public void mouseReleased(MouseEvent e)
{
// to stop the scroller if any
scrollid++;

if(action==ACTION_DRAG)
	{
	((Bliss)getParent()).bringToFront(this);
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
else if(action==ACTION_CLOSE)
	{
	if(rClose.contains(e.getX(),e.getY()))
		close();
	}

action = ACTION_NONE;
repaint();

}

/*****************************************************************************/
/*****************************************************************************/

//	class to autoscroll the list when the mouse is pressed on the bar

class	Scroller	extends	Thread	{

int id;

Scroller(int id)
{
this.id = id;
}

/*****************************************************************************/

public	void	run()
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
	try	{ sleep(200); } catch(Exception ex) {}
	}

}	//	End of method	run

/*****************************************************************************/

}	//	End of class	Scroller

/*****************************************************************************/
/*****************************************************************************/

}	//	End of class	WordList
