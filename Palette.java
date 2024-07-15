package	bliss;

import	java.awt.*;
import	java.awt.event.*;
import	java.awt.image.*;
import	java.net.*;
import	java.util.*;

public	class	Palette	extends	Widget
	implements Constants, MouseListener {

/*****************************************************************************/
//	CONSTANTS

static	final	int	TOOL_NONE = -1;
static	final	int	TOOL_LIST = 0;
static	final	int	TOOL_PAGE = 1;
static	final	int	TOOL_CHAT = 2;
static	final	int	TOOL_BOARD = 3;
static	final	int	TOOL_SEARCH = 4;
static	final	int	TOOL_LANG = 5;

static	final	int	NTOOL = 6;
static	final	int	SIZE = 36;

/*****************************************************************************/
//	FIELDS

int	_left,_top,_width,_height;

int	itool = TOOL_NONE;	// pressed button

Image	icons[];

/*****************************************************************************/
//	CONSTRUCTOR

public	Palette(int left, int top)
{
this._left = left;
this._top = top;
this._width = SIZE;
this._height = NTOOL*SIZE;

loadIcons();

setBounds(_left,_top,_width,_height);
setVisible(false);

addMouseListener(this);

}

/*****************************************************************************/

void	loadIcons()
{

icons = new Image[NTOOL];

MediaTracker mt = new MediaTracker(this);
for(int i=0;i<NTOOL;i++)
	{
	String name = "images/icon"+i+".png";
	URL url = this.getClass().getResource(name);
	icons[i] = Toolkit.getDefaultToolkit().createImage(url);
	mt.addImage(icons[i],i,32,32);
	}

try	{
	mt.waitForAll();
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}


for(int i=0;i<NTOOL;i++)
	icons[i] = Util.fixImage(icons[i],32,32);

}	//	End of method	loadIcons

/*****************************************************************************/

public	void	paint(Graphics g)
{
int top = 0;


drawButton(g,TOOL_LIST,top,listColor,listDarkColor,listLightColor);
top += _width;
drawButton(g,TOOL_PAGE,top,pageColor,pageDarkColor,pageLightColor);
top += _width;
drawButton(g,TOOL_CHAT,top,chatColor,chatDarkColor,chatLightColor);
top += _width;
drawButton(g,TOOL_BOARD,top,boardColor,boardDarkColor,boardLightColor);
top += _width;
drawButton(g,TOOL_SEARCH,top,searchColor,searchDarkColor,searchLightColor);
top += _width;
drawButton(g,TOOL_LANG,top,langColor,langDarkColor,langLightColor);

}	//	End of method	paint


/*****************************************************************************/

void	drawButton(Graphics g, int index, int top, Color color,
	Color darkColor, Color lightColor)
{

g.setColor(color);
g.fillRect(2,top+2,SIZE-4,SIZE-4);
g.drawImage(icons[index],2,top+2,color,this);

g.setColor((itool==index) ? lightColor : darkColor);
g.drawRect(1,top+1,SIZE-3,SIZE-3);
g.drawRect(0,top,SIZE-1,SIZE-1);

g.setColor((itool==index) ? darkColor : lightColor);
g.drawLine(0,top+SIZE,0,top);
g.drawLine(0,top,SIZE,top);
g.drawLine(1,top+SIZE-2,1,top+1);
g.drawLine(1,top+1,SIZE-2,top+1);

}	//	End of method	drawIcon

/*****************************************************************************/

public	void	mouseClicked(MouseEvent e) {}
public	void	mouseEntered(MouseEvent e) {}
public	void	mouseExited(MouseEvent e) {}

public	void	mousePressed(MouseEvent e)
{
itool = e.getY()/_width;
paint(getGraphics());
}

public	void	mouseReleased(MouseEvent e)
{
int i = e.getY()/_width;

if((e.getX()>0)&&(e.getX()<_width))
if(i==itool)
	{
	Bliss bliss = (Bliss)getParent();
	switch(itool)
		{
		case TOOL_LIST:
			bliss.openWordList();
			break;

		case TOOL_BOARD:
			bliss.openBoard();
			break;
		
		case TOOL_SEARCH:
			bliss.openSearch();
			break;

		case TOOL_PAGE:
			bliss.openPage();
			break;

		case TOOL_CHAT:
			bliss.openMessenger();
			break;

		case TOOL_LANG:
			bliss.openLangList();
			break;
		}
	}

itool = TOOL_NONE;
repaint();
}

/*****************************************************************************/

}	//	End of class	Palette
