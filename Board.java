package bliss;

import	java.awt.*;
import	java.awt.event.*;
import	java.net.*;
import	javax.swing.*;

public	class	Board extends	Widget
	implements Constants,
		MouseListener, MouseMotionListener {

/******************************************************************************/
//	STATIC VARIABLES

static	final	int	HDISPLAY = 162;
static	final	int	HSHAPE = 354;
static	final	int	HCONTROL = 32;
static	final	int	WIDTH = 530;

/******************************************************************************/
//	GLOBAL VARIABLES

Color barcolor = null;


GlyphPart fglyph[] = null;	// fixed glyph
GlyphPart mglyph[] = null;	// moving glyph
GlyphPart dglyph[] = null;	// glyph being dragged

int yshape1,yshape2;

Font font = null;
Image offscreen = null;
TextField field = null;

Rectangle rClear = null;
Rectangle rCancel = null;
Rectangle rAdd = null;

Component target = null;	// target of drop

int iword = -1;		// word being dropped

/******************************************************************************/
//	CONSTRUCTOR

public	Board(int left, int top)
{

setLayout(null);

_left = left;
_top = top;
_width = WIDTH;
_height = MARGIN+HDISPLAY+HSHAPE+HCONTROL+2;

setInterface();

rClear = new Rectangle(2,MARGIN+HDISPLAY+HSHAPE,HCONTROL,HCONTROL);
rCancel = new Rectangle(2+HCONTROL+2,MARGIN+HDISPLAY+HSHAPE,HCONTROL,HCONTROL);
rAdd = new Rectangle(_width-HCONTROL-2,MARGIN+HDISPLAY+HSHAPE,
	HCONTROL,HCONTROL);


myColor = boardColor;
myDarkColor = boardDarkColor;
myLightColor = boardLightColor;

barcolor = new Color(0x88,0x88,0x88);

font = new Font("Monospaced",Font.BOLD,18);

field = new TextField(20);
field.setBackground(Color.white);
add(field);
field.setBounds(2*HCONTROL+8,MARGIN+HDISPLAY+HSHAPE+2,_width-3*HCONTROL-12,20);
//field.setText("\u0420\u0443\u0441\u0441\u043A\u0438\u0439");
//field.setText("\u0395\u03BB\u03BB\u03B7\u03BD\u03B9\u03BA\u03AC");
//field.setText("\u05E2\u05D1\u05E8\u05D9\u05EA");


addMouseListener(this);
addMouseMotionListener(this);

setBounds(_left,_top,_width+1,_height+1);

setVisible(false);

}

/******************************************************************************/

public	void	paint(Graphics g)
{
if(offscreen==null)
	offscreen = createImage(_width+1,_height+1);
if(offscreen==null)
	return;

Graphics og = offscreen.getGraphics();

og.setColor(dkgray);
og.fillRect(0,0,_width,_height);

// title
drawTitle(og,"");

// close box
drawClose(og);

// display
int top = MARGIN;
og.setColor(Color.white);
og.fillRect(0,top,_width,HDISPLAY);

og.setColor(ltgray);
int nbar = _width/32;
for(int ibar=1;ibar<=nbar;ibar++)
	og.fillRect(32*ibar-2,top,4,HDISPLAY);	

// tall indicator line
og.setColor(ltgray);
og.fillRect(0,top,_width,4);

// indicator line
og.setColor(ltgray);
og.fillRect(0,top+32,_width,4);

// sky line
og.setColor(barcolor);
og.fillRect(0,top+64,_width,4);

// midline
og.setColor(ltgray);
og.fillRect(0,top+96,_width,4);

// ground line
og.setColor(barcolor);
og.fillRect(0,top+128,_width,4);

// glyph
og.setClip(0,top,_width,HDISPLAY);
og.setColor(Color.black);
drawGlyph(og,fglyph);
drawGlyph(og,mglyph);
og.setClip(0,0,9999,9999);

// shapes
top += HDISPLAY;
og.setColor(dkgray);
og.fillRect(0,top,_width,HSHAPE);
for(int i=0;i<96;i++)
	{
	int x = (i%12)*44 + 2;
	int y = top+(i/12)*44 + 2;
	int w = SHAPEDIM[i*2]*4+2;
	int h = SHAPEDIM[i*2+1]*4+2;
	og.setColor(ltgray);
	og.fillRect(x,y,42,42);
	x = x + (42-w)/2;
	y = y + (42-h)/2;
	og.drawImage(Bliss.images[i],x,y,w,h,null);
	}

// controls
drawControl(og,rClear,"<<",action==ACTION_CLEAR);
drawControl(og,rCancel,"<",action==ACTION_CANCEL);
drawControl(og,rAdd,"+",action==ACTION_ADDW);

drawBorders(og);

g.drawImage(offscreen,0,0,null);

}	//	End of method	paint

/******************************************************************************/

void	drawGlyph(Graphics g, GlyphPart gl[])
{
if(gl==null) return;

for(int i=0;i<gl.length;i++)
	{
	int is = gl[i].ishape;
	int w = SHAPEDIM[is*2]*8+4;
	int h = SHAPEDIM[is*2+1]*8+4;
	int x = gl[i].x*4 -2;
	int y = MARGIN + (gl[i].y)*4 ;
	g.drawImage(Bliss.images[is],x,y,w,h,this);
	}

}

/******************************************************************************/

void	drawControl(Graphics g, Rectangle r, String txt,
	boolean pressed)
{
g.setFont(font);

g.setColor(ltgray);
g.setColor(myColor);
g.fillRect(r.x,r.y,r.width,r.height);

g.setColor(pressed ? Color.black : Color.white);
g.drawLine(r.x,r.y+r.height,r.x,r.y);
g.drawLine(r.x,r.y,r.x+r.width,r.y);

g.setColor(pressed ? Color.white : Color.black);
g.drawLine(r.x+r.width,r.y,r.x+r.width,r.y+r.height);
g.drawLine(r.x+r.width,r.y+r.height,r.x,r.y+r.height);
	
g.setColor(Color.black);
int l = g.getFontMetrics().stringWidth(txt);
g.drawString(txt,r.x+(r.width-l)/2,r.y+4+r.height/2);

}

/******************************************************************************/

public	void	dropWord(int num)
{

if(num>=0)
	{
	action = ACTION_DROPW;
	iword = num;
	mglyph = Util.buildGlyph(Dict.words[iword].code,2);
	}
else
	{
	action = ACTION_NONE;
	iword = -1;
	}

}	//	End of method	dropWord

/******************************************************************************/

void	checkTarget(MouseEvent e)
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
	target = c;
	if(target instanceof GlyphReceiver)
		{	
		((Widget)target).setAction(ACTION_DROPW);
		target.paint(target.getGraphics());
		}
	}

}	//	End of method	checkTarget

/******************************************************************************/

public  void    addWord(String name, String code)
{

Dict.addWord(name,code);

field.setText("");
fglyph = null;
mglyph = null;
repaint();

((Bliss)getParent()).wordAdded();

}	//	End of method	addWord

/******************************************************************************/

public	void  close()
{
setVisible(false);
}

/******************************************************************************/

public  void    mouseEntered(MouseEvent e) {}
public  void    mouseExited(MouseEvent e) {}
public  void    mouseClicked(MouseEvent e) {}
public  void    mouseMoved(MouseEvent e) {}

int	xclick, yclick;

/******************************************************************************/

public	void	mousePressed(MouseEvent e)
{
target = null;
xclick = e.getX();
yclick = e.getY();
action = ACTION_NONE;

if(rClose.contains(xclick,yclick))
	{
	action = ACTION_CLOSE;
	paint(getGraphics());
	}
else if(yclick<MARGIN)
	{
	// in title bar
	action = ACTION_DRAG;
	}
else if((yclick>MARGIN)&&(yclick<MARGIN+HDISPLAY))
	{
	// click on the display
	if(fglyph!=null)
		{
		action = ACTION_DRAGW;
		dglyph = Util.copyGlyph(fglyph);

		//  normal size
		for(int i=0;i<dglyph.length;i++)
			{
			dglyph[i].x /=2;
			dglyph[i].y /=2;
			}
		}
	}
else if((yclick>MARGIN+HDISPLAY)&&(yclick<MARGIN+HDISPLAY+HSHAPE))
	{
	// click on a shape

	int ix = xclick/44;
	int iy = (yclick-20-HDISPLAY)/44;
	int ishape = iy*12+ix;

	// build the moving glyph
	mglyph = new GlyphPart[1];
	mglyph[0] = new GlyphPart(ishape,0,0);

	dglyph = Util.copyGlyph(mglyph);

	action = ACTION_SHAPE;
	}
else if(rClear.contains(xclick,yclick))
	{
	
	if((fglyph!=null)&&(fglyph.length>0))
		{
		action = ACTION_CLEAR;
		paint(getGraphics());
		}
	}
else if(rCancel.contains(xclick,yclick))
	{
	if((fglyph!=null)&&(fglyph.length>0))
		{
		action = ACTION_CANCEL;
		paint(getGraphics());
		}
	}
else if(rAdd.contains(xclick,yclick))
	{
	if((fglyph!=null)&&(fglyph.length>0)&&(field.getText().length()>0))
		{
		action = ACTION_ADDW;
		paint(getGraphics());
		}
	}

return;
}

/******************************************************************************/

public	void	mouseReleased(MouseEvent e)
{
xclick = e.getX();
yclick = e.getY();

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
else if(action==ACTION_DRAGW)
	{
	if(target instanceof GlyphReceiver)
		((GlyphReceiver)target).receive(dglyph);
	}
else if(action==ACTION_SHAPE)
	{
	if(target instanceof GlyphReceiver)
		{
		// if shape dropped on a GlyphReceiver
		((GlyphReceiver)target).receive(mglyph);
		}
	else if((yclick>MARGIN)&&(yclick<MARGIN+HDISPLAY))
		{
		// if shape dropped on the display
		if(fglyph==null)
			fglyph = mglyph;
		else
			fglyph = Util.concatGlyph(fglyph,mglyph);
		}
	mglyph = null;
	}
else if(action==ACTION_DROPW)
	{
	if((yclick>MARGIN)&&(yclick<MARGIN+HDISPLAY))
		{
		// if word dropped on the display
		if(fglyph==null)
			fglyph = mglyph;
		else
			fglyph = Util.concatGlyph(fglyph,mglyph);
		}
	mglyph = null;
	}
else if(action==ACTION_CLEAR)
	{
	// clear the fixed glyph
	fglyph = null;
	}
else if(action==ACTION_CANCEL)
	{
	// delete the last part of the fglyph
	if(fglyph!=null)
		{
		int np = fglyph.length-1;
		if(np<0) np = 0;
		GlyphPart g[] = new GlyphPart[np];
		for(int i=0;i<np;i++)
			g[i] = fglyph[i];
		fglyph = g;
		}
	}
else if(action==ACTION_ADDW)
	{
	String name = field.getText();
	String code = Util.buildCode(fglyph,2);
	addWord(name,code);
	}

target = null;
action = ACTION_NONE;

repaint();

return;

}	//	End of method	mouseReleased

/******************************************************************************/

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
else if(action==ACTION_DRAGW)
	{
	checkTarget(e);
	}
else if((action==ACTION_SHAPE)||(action==ACTION_DROPW))
	{
	xclick = e.getX();
	yclick = e.getY();
	if((xclick>0)&&(xclick<_width)&&
		(yclick>MARGIN)&&(yclick<MARGIN+HDISPLAY))
		{
		// position in 1/16 of unit
		int ix = (xclick+2)/4;
		int iy = (yclick-MARGIN+2)/4;
		moveGlyph(mglyph,ix,iy);
		paint(getGraphics());
		}
	else
		checkTarget(e);
	return;
	}

return;
}

/******************************************************************************/

void	moveGlyph(GlyphPart gl[], int xpos, int ypos)
{
// limits in pixels
Limits l = Util.computeGlyphLimits(gl,2);

// center in 1/16 of unit
int xc = (l.xmin+l.xmax-800)/2/4;
int yc = (l.ymin+l.ymax-800)/2/4;

// move center of glyph to xpos, ypos
for(int i=0;i<gl.length;i++)
	{
	gl[i].x += xpos-xc;
	gl[i].y += ypos-yc;
	gl[i].x = gl[i].x/2*2;
	gl[i].y = gl[i].y/2*2;
	gl[i].x -=100;		// bias used to prevent problem when
	gl[i].y -=100;		// truncating negative values
	}

}	//	End of method	moveGlyph

/******************************************************************************/

public	void	editWord(String code, String name)
{

fglyph = Util.buildGlyph(code,2);

// move to the center of the display
for(int i=0;i<fglyph.length;i++)
	fglyph[i].x += 48;

mglyph = null;
field.setText(name);
repaint();
}

public	void	editWord(GlyphPart g[], String name)
{
editWord(Util.buildCode(g,1),name);
}	//	End of method	editWord

/******************************************************************************/

}	//	End of class	Board

