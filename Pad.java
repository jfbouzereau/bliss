package bliss;

import	java.awt.*;
import	java.awt.event.*;
import	java.awt.image.*;

public	class	Pad extends Widget  
	implements MouseListener, MouseMotionListener {

/*****************************************************************************/
//	CONSTANTS

static	final	int	HEIGHT = 328;
static	final	int	WIDTH =  648;

/*****************************************************************************/
//	FIELDS

Image offscreen = null;

String title;

GlyphPart glyph[] = null;

int	xpt[];
int	ypt[];
int 	npt;

Pix	pix[];

/*****************************************************************************/
//	CONSTRUCTOR

public	Pad(GlyphReceiver bucket, Color color, String title, int left, int top)
{
setLayout(null);

this._left = left;
this._top = top;
this._width = WIDTH;
this._height = MARGIN + HEIGHT;

this.title = title;

this.myColor = color;

setInterface();

// to keep the current curve
xpt = new int[2000];
ypt = new int[2000];
npt = 0;

loadPixels();

setBounds(_left,_top,_width+1,_height+1);
setVisible(true);

addMouseListener(this);
addMouseMotionListener(this);

}

/*****************************************************************************/

void	loadPixels()
{

int np = Bliss.images.length;


pix = new Pix[np];

for(int i=0;i<np;i++)
	{
        int w = SHAPEDIM[i*2]*8+4;
        int h = SHAPEDIM[i*2+1]*8+4;
	
	pix[i] = new Pix(w,h);

	PixelGrabber pg = new PixelGrabber(Bliss.images[i],
		0,0,w,h,pix[i].pixels,0,w);

	try	{
		pg.grabPixels();
		}
	catch(Exception ex)
		{
		ex.printStackTrace();
		}

	// compute surface of the curve (number of white pixels)
	pix[i].surface = 0;
	for(int j=0;j<w;j++)
		for(int k=0;k<h;k++)
			if(pix[i].pixels[k*w+j]==0xFF000000)
				pix[i].surface++;
	}


}	//	End of method	loadPixels

/*****************************************************************************/

void	show(int pixels[], int ww, int hh)
{

for(int i=0;i<hh;i++)
	{
	for(int j=0;j<ww;j++)
		{
		System.out.print(pixels[i*ww+j]&1);
		System.out.print(" ");
		}
	System.out.println("");
	}

}	//	End of method	show

/*****************************************************************************/

public	void	paint(Graphics g)
{
if(offscreen==null)
	offscreen = createImage(_width+1,_height+1);
if(offscreen==null)
	return;

Graphics og = offscreen.getGraphics();

// drawing area
int y = MARGIN;
og.setColor(Color.white);
og.fillRect(0,y,WIDTH,HEIGHT);

// vertical bars
og.setColor(ltgray);
for(int i=0;i<16;i++)
	og.fillRect(i*64,y,8,HEIGHT);

// super indicator line
og.setColor(ltgray);
og.fillRect(0,y,WIDTH,8);

// indicator line
og.setColor(ltgray);
og.fillRect(0,y+64,WIDTH,8);

// skyline
og.setColor(dkgray);
og.fillRect(0,y+128,WIDTH,8);

// midline 
og.setColor(ltgray);
og.fillRect(0,y+192,WIDTH,8);

// ground line
og.setColor(dkgray);
og.fillRect(0,y+256,WIDTH,8);

// bottom line
og.setColor(ltgray);
og.fillRect(0,y+320,WIDTH,8);

// glyph
drawGlyph(og,glyph);

// current curve
og.setColor(Color.black);
for(int i=0;i<npt-1;i++)
	{
	og.drawLine(xpt[i],ypt[i],xpt[i+1],ypt[i+1]);
	}

// title bar
drawTitle(og,title);

// closebox
drawClose(og);

drawBorders(og);

g.drawImage(offscreen,0,0,this);

}	//	End of method	paint

/*****************************************************************************/

void	drawGlyph(Graphics g, GlyphPart gl[])
{
if(gl==null) return;

for(int i=0;i<gl.length;i++)
	{
	int is = gl[i].ishape;
	int w = SHAPEDIM[is*2]*16+8;
	int h = SHAPEDIM[is*2+1]*16+8;
	int x = gl[i].x*4;
	int y = MARGIN + gl[i].y*4 - 16;
	g.drawImage(Bliss.images[is],x,y,w,h,this);
	}

}	//	End of method	drawGlyph

/*****************************************************************************/

public	void	close()
{
((Bliss)getParent()).remove(this);
}	//	End of method	close

/*****************************************************************************/

void	guess()
{

int ibest = -1;
double scorebest = 0.0;

// look for the left/top corner of the curve
int xmin = 9999;
int ymin = 9999;
int xmax = -9999;
int ymax = -9999;
for(int i=0;i<npt;i++)
	{
	if(xpt[i]<xmin) xmin = xpt[i];
	if(xpt[i]>xmax) xmax = xpt[i];
	if(ypt[i]<ymin) ymin = ypt[i];
	if(ypt[i]>ymax) ymax = ypt[i];
	}

// round size to 1/8 of unit
int dx = ((xmax-xmin+4)/8+2)/8*8;
int dy = ((ymax-ymin+4)/8+2)/8*8;
System.out.println("dx="+dx+" dy="+dy);

// readjust curve
for(int i=0;i<npt;i++)
	{
	xpt[i] = xmin + (xpt[i]-xmin)*dx*8/(xmax-xmin);
	ypt[i] = ymin + (ypt[i]-ymin)*dy*8/(ymax-ymin);
	}


// round to 1/8 of unit
xmin = (xmin+4)/8;
ymin = (ymin)/8;


xmin *= 4;
ymin *= 4;


// pixels from the curve

int ww = 128;
int hh = 128;
int ppp[] = new int[ww*hh];
for(int j=0;j<ppp.length;j++)
	ppp[j] = 0xFFFFFF;

for(int j=0;j<npt;j++)
	{
	int x = xpt[j]/2 - xmin;
	int y = ypt[j]/2 - ymin;
	for(int xx=x-2;xx<=x+2;xx++)
		{
		if(xx<0) continue;
		if(xx>=ww) break;
		for(int yy=y-2;yy<=y+2;yy++)
			{
			if(yy<0) continue;
			if(yy>=hh) break;
			ppp[yy*ww+xx] = 0xFF000000;
			}
		}
	}


// compute score of each image
for(int i=0;i<pix.length;i++)
	{
	// pixels of the image covered by the curve
	int k1 = 0;
	int n1 = 0;
	for(int x=0;x<pix[i].width;x++)
		for(int y=0;y<pix[i].height;y++)
			if(pix[i].pixels[y*pix[i].width+x]==0xFF000000)
			{
			n1++;
			if((x<ww)&&(y<hh))
				if(ppp[y*ww+x]==0xFF000000)
					k1++;
			}

	// pixels of the curve covered by the image
	int k2 = 0;
	int n2 = 0;
	for(int x=0;x<ww;x++)
		for(int y=0;y<hh;y++)
			if(ppp[y*ww+x]==0xFF000000)
			{
			n2++;
			if((x<pix[i].width)&&(y<pix[i].height))
				if(pix[i].pixels[y*pix[i].width+x]==0xFF000000)
					k2++;
			}

	double score = k1*1.0/n1 + k2*1.0/n2;
	//System.out.println(i+" "+k1*1.0/n1+" "+k2*1.0/n2);
	if(score>scorebest)
		{
		ibest = i;
		scorebest = score;
		}	
	}

System.out.println("best = "+ibest);

if(ibest>=0)
	{
	GlyphPart gl[] = new GlyphPart[1];
	gl[0] = new GlyphPart(ibest,xmin/2,ymin/2);

	if(glyph==null)
		{
		glyph = Util.copyGlyph(gl);
		}
	else
		{
		glyph = Util.concatGlyph(glyph,gl);
		}
	}

repaint();

}	//	End of method	guess

/*****************************************************************************/

int	xclick, yclick;

/*****************************************************************************/

public	void	mouseMoved(MouseEvent e) {}
public	void	mouseClicked(MouseEvent e) {}
public	void	mouseEntered(MouseEvent e) {}
public	void	mouseExited(MouseEvent e) {}

/*****************************************************************************/

public	void	mousePressed(MouseEvent e) 
{
npt = 0;
action = ACTION_NONE;
xclick = e.getX();
yclick = e.getY();

if(rClose.contains(xclick,yclick))
	{
	action = ACTION_CLOSE;
	}
else if(yclick<MARGIN)
	{
	action = ACTION_DRAG;
	}
else
	{
	action = ACTION_DRAW;
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
	yclick = e.getY()-ymove;
	}
else if(action==ACTION_DRAW)
	{
	xpt[npt] = e.getX();
	ypt[npt] = e.getY();
	npt++;
	paint(getGraphics());
	}

}	//	End of method	mouseDragged

/*****************************************************************************/

public	void	mouseReleased(MouseEvent e)
{

if(action==ACTION_DRAG)
	{
	((Bliss)getParent()).bringToFront(this);
	}
else if(action==ACTION_CLOSE)
	{
	close();
	}
else if(action==ACTION_DRAW)
	{
	guess();
	}

}	//	End of method	mouseReleased

/*****************************************************************************/



/*****************************************************************************/
/*****************************************************************************/

class	Pix	{

int	pixels[];
int	width,height;
int	surface;

Pix(int w, int h)
{
this.width = w;
this.height = h;
this.surface = 0;
this.pixels = new int[width*height];
}

}

/*****************************************************************************/
/*****************************************************************************/

}	//	End of class	Pad
