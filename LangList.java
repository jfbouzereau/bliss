package	bliss;

import	java.awt.*;
import	java.awt.event.*;
import	java.text.*;

public	class	LangList	extends	Widget
	implements Constants, MouseListener, MouseMotionListener {

/*****************************************************************************/
//	CONSTANTS

static	final	int	WIDTH = 200;
static	final	int	WL = 13;	// height of one line

/*****************************************************************************/
//	FIELDS


Image offscreen = null;

int	ifirst = 0;	// first word displayed
int	npage = 30;	// number of words per page

int	nlang = 0;	// number of languages

Font	ufont = null;

String	title = null;
String	code = null;

Collator collator = null;
Comparator comparator = null;

/*****************************************************************************/
//	CONSTRUCTOR

public	LangList(int left, int top)
{
this._left = left;
this._top = top;
this._width = WIDTH;
this._height = MARGIN + npage*WL;
this.code = code;

setInterface();

ufont = new Font("Monospaced",Font.PLAIN,10);

nlang = Dict.langs.length;

myColor = langColor;
myDarkColor = langDarkColor;
myLightColor = langLightColor;

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
String title = ""+nlang;
drawTitle(og,title);

// close box
drawClose(og);

// content
og.setColor(dkgray);
og.fillRect(0,MARGIN,_width,_height-MARGIN);

og.setColor(Color.black);

int top = MARGIN;
for(int i=ifirst;i<nlang;i++)
	{
	og.setColor(i==Dict.ilang ? acgray : ltgray);
	og.fillRect(0,top,_width,WL);
	og.setColor(i==Dict.ilang ? Color.white : Color.black);

	og.setFont(textfont);
	og.drawString(Dict.langs[i].name,10,top+WL-3);

	og.setFont(ufont);
	og.drawString(Dict.langs[i].locale,_width-60,top+WL-3);

	top += WL;
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

public	void	close()
{
setVisible(false);
}

/*****************************************************************************/

void	openTranslator(int il)
{

System.out.println("open translator "+il);

String title = Dict.langs[il].name;
Translator translator = new Translator(title,_left+300,_top+300);

Bliss bliss = (Bliss)getParent();
int index = bliss.getComponentIndex(this);
bliss.add(translator,index);
translator.setVisible(true);

}	//	End of method	openTranslator

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
        int il = (yclick-MARGIN)/WL + ifirst;
        if((il>=0)&&(il<nlang)&&(e.getClickCount()>1))
		{
		if(e.isControlDown())
			{
			openTranslator(il);
			repaint();
                        }
                else
			{
			Dict.setLanguage(this,il);
			repaint();
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

	_height = (_height-MARGIN+WL-1)/WL*WL+MARGIN;
	int h2 = (_height-40)/2;
	npage = (_height-MARGIN)/WL;

	setInterface();

	setBounds(_left,_top,_width+1,_height+1);

	// force recreation
	offscreen = null;

	paint(getGraphics());
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
else if(action==ACTION_CLOSE)
	{
	if(rClose.contains(e.getX(),e.getY()))
		{
		action = ACTION_NONE;
		close();
		return;
		}
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
		if(ifirst>nlang-1)
			ifirst = nlang-1;
		}
	paint(getGraphics());
	try	{ sleep(200); } catch(Exception ex) {}
	}

}	//	End of method	run

/*****************************************************************************/

}	//	End of class	Scroller

/*****************************************************************************/
/*****************************************************************************/

}	//	End of class	LangList
