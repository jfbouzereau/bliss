package bliss;

import	java.awt.*;
import	java.awt.event.*;

public	abstract class	Widget	extends	Panel implements Constants {

/*****************************************************************************/
//	GLOBAL VARIABLES

protected	int	_left,_top,_width,_height;
protected	int	action = ACTION_NONE;

protected	Color		myColor = null;
protected	Color		myLightColor = null;
protected	Color		myDarkColor = null;

protected	Rectangle	rUp = null;
protected	Rectangle	rDown = null;
protected	Rectangle	rSize = null;
protected	Rectangle	rClose = null;

/*****************************************************************************/

void	setInterface()
{
int h2 = (_height-2*MARGIN)/2;
rUp = new Rectangle(_width-MARGIN,MARGIN,MARGIN,h2);
rDown = new Rectangle(_width-MARGIN,MARGIN+h2+1,MARGIN,h2);
rSize = new Rectangle(_width-MARGIN,MARGIN+2*h2+2,MARGIN,MARGIN);
rClose = new Rectangle(_width-13,3,10,10);
}

/*****************************************************************************/

void	drawScroll(Graphics g, Rectangle r, boolean pressed)
{

g.setColor(myColor);
g.fillRect(r.x,r.y,r.width,r.height);

g.setColor( pressed ? myDarkColor : myLightColor);
g.drawLine(r.x,r.y+r.height,r.x,r.y);
g.drawLine(r.x,r.y,r.x+r.width,r.y);

g.setColor( pressed ? myLightColor : myDarkColor);
g.drawLine(r.x+r.width,r.y,r.x+r.width,r.y+r.height);
g.drawLine(r.x+r.width,r.y+r.height,r.x,r.y+r.height);

}       //      End of method   drawScroll

/*****************************************************************************/

void	drawTitle(Graphics g, String title)
{

g.setColor(myColor);
g.fillRect(0,0,_width,MARGIN);

g.setFont(titlefont);
int lt = g.getFontMetrics().stringWidth(title);
g.setColor(Color.white);
g.drawString(title,(_width-lt)/2+1,12);

}	//	End of method	drawTitle

/*****************************************************************************/

void	drawClose(Graphics g)
{        

if(action==ACTION_CLOSE)
	{
	g.setColor(clgray);
	g.fillRect(rClose.x,rClose.y,rClose.width-2,rClose.height-2);
	}

g.setColor(myDarkColor);
g.drawRect(rClose.x,rClose.y,rClose.width-2,rClose.height-2);
g.setColor(myLightColor);
g.drawRect(rClose.x+1,rClose.y+1,rClose.width-2,rClose.height-2);

}	//	End of method	drawClose

/*****************************************************************************/

void	drawResize(Graphics g)
{

g.setColor(myColor);
g.fillRect(rSize.x,rSize.y,rSize.width,rSize.height);

g.setColor(myDarkColor);
g.drawRect(rSize.x+3,rSize.y+2,6,6);
g.drawRect(rSize.x+6,rSize.y+5,6,6);

g.setColor(myLightColor);
g.drawRect(rSize.x+4,rSize.y+3,6,6);
g.drawRect(rSize.x+7,rSize.y+6,6,6);
g.drawLine(rSize.x,rSize.y,rSize.x,rSize.y+rSize.height);

}	//	End of method	drawResize

/*****************************************************************************/

void	drawBorders(Graphics g)
{
g.setColor(myDarkColor);
g.setColor(Color.black);
g.drawRect(0,0,_width,MARGIN-1);

g.setColor(myDarkColor);
g.setColor(Color.black);
g.drawRect(0,0,_width,_height);

g.setColor(myLightColor);
g.setColor(Color.white);
g.drawLine(0,_height,0,0);
g.drawLine(0,0,_width,0);

}	//	End of method	drawBorders

/*****************************************************************************/

void	setAction(int a)
{
this.action = a;
}

/*****************************************************************************/

public	void	close()
{
}	//	End of method	close

/*****************************************************************************/

}	//	End of class	Widget
