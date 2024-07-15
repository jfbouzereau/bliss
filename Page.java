package	bliss;

import	java.awt.*;
import	java.awt.event.*;
import	java.awt.datatransfer.*;

import	java.net.*;
import	java.util.*;

public	class	Page	extends	Widget
	implements Constants, GlyphReceiver ,
		 MouseListener, MouseMotionListener {

/*****************************************************************************/
//	CONSTANTS

static	final	int	HLINE = 80;		// height of one line
static	final	int	HICON = 32;		// height of icons
static	final	int	HHELP = 0;		// height of help area

static	final	int	CMD_FIRST = 30;

static	final	int	CMD_WRITER = 30;
static	final	int	CMD_MOUSER = 31;
static	final	int	CMD_PASTE = 32;
static	final	int	CMD_COPY = 33;
static	final	int	CMD_CLEAR = 34;

static	final	int	NCMD = 5;

/*****************************************************************************/
//	FIELDS

Image icons[] = null;
Clipboard clipboard = null;
Image offscreen = null;

String title = "";
String	helptext = "";

int nline = 6;	//	number of lines par page

Vector	bwords = null;

int icmd = -1;	//	command being selected

Mouser mouser = null;
TypeWriter writer = null;
Pad pad = null;

/*****************************************************************************/
//	CONSTRUCTOR

public	Page(String title, int left, int top)
{
this.title = title;
this._left = left;
this._top = top;
this._width = 650;
this._height = 20 + HICON+ nline*HLINE + HHELP;

myColor = pageColor;
myDarkColor = pageDarkColor;
myLightColor = pageLightColor;

setInterface();

bwords = new Vector();

loadIcons();

setBounds(_left,_top,_width+1,_height+1);
setVisible(false);

addMouseListener(this);
addMouseMotionListener(this);

}

/*****************************************************************************/

void    loadIcons()
{

icons = new Image[NCMD];

MediaTracker mt = new MediaTracker(this);
for(int i=0;i<NCMD;i++)
        {
        String name = "images/icon"+(CMD_FIRST+i)+".png";
        URL url = this.getClass().getResource(name);
        icons[i] = Toolkit.getDefaultToolkit().createImage(url);
        mt.addImage(icons[i],i,32,32);
        }

try     {
        mt.waitForAll();
        }
catch(Exception ex)
        {
        ex.printStackTrace();
        }

for(int i=0;i<NCMD;i++)
	icons[i] = Util.fixImage(icons[i],32,32);

}	//	End of method	loadIcons

/*****************************************************************************/

public	void	paint(Graphics g)
{
if(offscreen==null)
	offscreen = createImage(_width+1,_height+1);
if(offscreen==null)
	return;

if((writer==null)&&(mouser==null)&&(pad==null))
	openWriter();

Graphics og = offscreen.getGraphics();

Limits l;

// background
og.setColor(action==ACTION_DROPW ? acgray : Color.white);
og.fillRect(0,0,_width,_height);

// title bar
drawTitle(og,title);

// close box
drawClose(og);

// help area
int y = MARGIN;
int left = NCMD*HICON;

og.setColor(ltgray);
og.fillRect(0,y,_width-MARGIN,HICON);

og.setColor(Color.black);
og.setFont(textfont);
int ls = og.getFontMetrics().stringWidth(helptext);
og.drawString(helptext,left+(_width-left-MARGIN)/2-ls/2,y+18);


// icons
for(int i=0;i<NCMD;i++)
	{
	og.setColor(myColor);
	og.fillRect(0+i*HICON,y,HICON,HICON);

	og.drawImage(icons[i],0+i*HICON,y,myColor,this);

	og.setColor((icmd-CMD_FIRST==i) ? Color.white : Color.black);
	og.drawRect(0+i*HICON,y,HICON,HICON);

	og.setColor((icmd-CMD_FIRST==i) ? Color.black : Color.white);
	og.drawLine(0+i*HICON+1,y+HICON,0+i*HICON+1,y);
	og.drawLine(0+i*HICON,y,i*HICON+HICON,y);
	}

og.setColor(Color.black);
og.drawLine(0,y+HICON,_width-MARGIN,y+HICON);

// content
y += HICON;
for(int i=0;i<nline;i++)
	{
	og.setColor(ltgray);
	// skyline
	og.fillRect(0,y+i*HLINE+32,_width,2);
	// ground line
	og.fillRect(0,y+i*HLINE+64,_width,2);
	}

left = 16; // left margin 
int iline = 0;
for(int i=0;i<bwords.size();i++)
	{
	BlissWord bword = (BlissWord)bwords.elementAt(i);
	if(left+(bword.limits.xmax-bword.limits.xmin)>_width-MARGIN)
		{
		iline++;
		left=16;
		}

	bword.left = left;
	bword.top = y+iline*HLINE;
	drawGlyph(og,bword.glyph,left,y+iline*HLINE);
	left += (bword.limits.xmax-bword.limits.xmin) + 32;
	}

/*
// help area
y += nline*HLINE;
og.setColor(ltgray);
og.fillRect(0,y,_width-MARGIN,_height);
og.setColor(Color.black);
og.drawLine(0,y,_width-MARGIN,y);

og.setFont(textfont);
int ls = og.getFontMetrics().stringWidth(helptext);
og.drawString(helptext,_width/2-ls/2,y+HHELP-5);
*/


// scroll bars
drawScroll(og,rUp,action==ACTION_UP);
drawScroll(og,rDown,action==ACTION_DOWN);

// resize box
drawResize(og);

drawBorders(og);

g.drawImage(offscreen,0,0,this);

}	//	End of method	paint

/*****************************************************************************/

void	drawGlyph(Graphics g, GlyphPart gl[], int left, int top)
{
if(gl==null) return;

for(int i=0;i<gl.length;i++)
	{
	int is = gl[i].ishape;
	int w = SHAPEDIM[is*2]*4+2;
	int h = SHAPEDIM[is*2+1]*4+2;
	int x = left +gl[i].x*4;
	int y = top + gl[i].y*4;
	g.drawImage(Bliss.images[is],x,y,w,h,this);
	}

}	//	End of method	drawGlyph

/*****************************************************************************/

public void	receive(GlyphPart g[])
{

action = ACTION_NONE;

if(g==null) return;
if(g.length==0) return;

bwords.addElement(new BlissWord(g));
repaint();

}	//	End of method	receive

/*****************************************************************************/

void	openPad()
{
int index;
Bliss bliss = (Bliss)getParent();

if(pad!=null)
	{
	// check if previous pad has closed
	index = bliss.getComponentIndex(pad);
	if(index<0)
		pad = null;
	}

if(pad==null)	
	{
	pad = new Pad(this,myColor,title,_left+300,_top+300);

	// put it just below me
	index = bliss.getComponentIndex(this);
	bliss.add(pad,index);
	pad.setVisible(true);
	}
}	//	End of method	openPad

/*****************************************************************************/

void	openWriter()
{
int index;
Bliss bliss = (Bliss)getParent();

if(writer!=null)
	{
	// check if previous writer has closed
	index = bliss.getComponentIndex(writer);
	if(index<0)
		writer = null;
	}

if(writer==null)	
	{
	writer = new TypeWriter(this,myColor,myDarkColor,myLightColor,
		title,_left+300,_top+300);

	// put it just below me
	index = bliss.getComponentIndex(this);
	bliss.add(writer,index);
	writer.setVisible(true);
	}

}	//	openWriter

/*****************************************************************************/

void	closeWriter()
{

System.out.println("closeWriter writer="+writer);

Bliss bliss = (Bliss)getParent();

if(writer!=null)
	{
	int index = bliss.getComponentIndex(writer);
	System.out.println(" index="+index);

	if(bliss.getComponentIndex(writer)>=0)
		bliss.remove(writer);

	writer = null;
	}

}	//	End of method	closeWriter

/*****************************************************************************/

void	openMouser()
{
int index;
Bliss bliss = (Bliss)getParent();

if(mouser!=null)
	{
	// check if previous mouser has closed
	index = bliss.getComponentIndex(mouser);
	if(index<0)
		mouser = null;
	}

if(mouser==null)	
	{
	mouser = new Mouser(this,myColor,myDarkColor,myLightColor,
		title,_left+300,_top+300);

	// put it just below me
	index = bliss.getComponentIndex(this);
	bliss.add(mouser,index);
	mouser.setVisible(true);
	}

}	//	End of method	openMouser

/*****************************************************************************/

void	closeMouser()
{

Bliss bliss = (Bliss)getParent();

if(mouser!=null)
	{
	if(bliss.getComponentIndex(mouser)>=0)
		bliss.remove(mouser);

	mouser = null;
	}

}	//	End of method	closeMouser
	
/*****************************************************************************/

void	doCopy()
{
if(clipboard==null)
	clipboard = getToolkit().getSystemClipboard();

// convert glyph to text
StringBuffer sb = new StringBuffer();
sb.append("<bliss>\n");

for(int i=0;i<bwords.size();i++)
	{
	sb.append("<w>");
	BlissWord bword = (BlissWord)bwords.elementAt(i);
	sb.append(bword.code);
	sb.append("</w>\n");
	}

sb.append("</bliss>\n");

// convert text to clipboard selection
StringSelection data = new StringSelection(sb.toString());

// put into the clipboard
clipboard.setContents(data,data);

}	//	End of method	doCopy

/*****************************************************************************/

void	doPaste()
{
bwords = new Vector();

if(clipboard==null)
	clipboard = getToolkit().getSystemClipboard();

// get the content of the clipboard in text format

Transferable data = clipboard.getContents(this);
String s = "";

try	{	
	s = (String)data.getTransferData(DataFlavor.stringFlavor);
	}
catch(Exception ex)
	{
	}

// look for the <bliss> tag
int ib1,ib2,iw1,iw2;

ib1 = s.indexOf("<bliss>");
ib2 = s.indexOf("</bliss>",ib1);

if((ib1>=0)&&(ib2>ib1))
	{
	while(true)
		{
		iw1 = s.indexOf("<w>",ib1);
		if(iw1<0) break;
		if(iw1>ib2) break;

		iw2 = s.indexOf("</w>",iw1);
		if(iw2<0) break;
		if(iw2>ib2) break;

		String code = s.substring(iw1+3,iw2);
		bwords.addElement(new BlissWord(code));
		ib1 = iw2+3;
		}	
	}

repaint();

}	//	End of method	doPaste

/*****************************************************************************/

void	doClear()
{

bwords = new Vector();

repaint();

}	//	End of method	doClear

/*****************************************************************************/

public	void	close()
{
Bliss bliss = (Bliss)getParent();

closeWriter();

bliss.remove(this);

}	//	End of method	close

/*****************************************************************************/

BlissWord	getWordAt(int x, int y)
{

for(int i=0;i<bwords.size();i++)
        {
        BlissWord bw = (BlissWord)bwords.elementAt(i);
        if((x>bw.left) &&
		(x<bw.left + bw.limits.xmax - bw.limits.xmin) &&
		(y>bw.top) &&
		(y<bw.top + HLINE))
		return bw;
	}

return null;

}	//	End of method	getWordAt

/*****************************************************************************/

public void mouseClicked(MouseEvent e) {}
public void mouseEntered(MouseEvent e) {}
public void mouseExited(MouseEvent e) {}


int xclick, yclick;
int oldw,oldh;
int scrollid = 0;

/*****************************************************************************/

public void mousePressed(MouseEvent e)
{
action = ACTION_NONE;
xclick = e.getX();
yclick = e.getY();

if(rClose.contains(xclick,yclick))
        {
        action = ACTION_CLOSE;
	paint(getGraphics());
        }
else if(yclick<20)
        {
        action = ACTION_DRAG;
        }
else if(yclick<20+HICON)
	{
	int i = xclick/HICON;
	if(i<NCMD)
		{
		action = ACTION_CMD;
		icmd = CMD_FIRST+i;
		paint(getGraphics());
		}
	}
else if(rUp.contains(xclick,yclick))
        {
        action = ACTION_UP;
        paint(getGraphics());
        }
else if(rDown.contains(xclick,yclick))
        {
        action = ACTION_DOWN;
        paint(getGraphics());
        }
else if(rSize.contains(xclick,yclick))
        {
        oldw = _width;
        oldh = _height;
        action = ACTION_SIZE;
        }
else
	{
	BlissWord bw = getWordAt(xclick,yclick);
	if((bw!=null)&&(e.getClickCount()>1))
		{
		int iw = Dict.getWordWithCode(bw.code);
		if(iw<0)
			{
			((Bliss)getParent()).editWord(bw.code,"");
			}
		else
			{
			((Bliss)getParent()).editWord(
				Dict.words[iw].code,
				Dict.words[iw].name);
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

        nline  = (_height-20-HICON-HHELP)/HLINE;
	if(nline<1)
		nline = 1;
	_height = 20+HICON+nline*HLINE+HHELP;
        int h2 = (_height-40)/2;

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
		close();
		return;
		}
        }
else if(action==ACTION_CMD)
	{
	if(icmd==CMD_WRITER)
		{
		if(e.isControlDown())
			openPad();
		else
			{
			closeMouser();
			openWriter();
			}
		}
	else if(icmd==CMD_MOUSER)
		{
		closeWriter();
		openMouser();
		}
	else if(icmd==CMD_COPY)
		doCopy();
	else if(icmd==CMD_PASTE)
		doPaste();
	else if(icmd==CMD_CLEAR)
		doClear();
	icmd = -1;
	}

action = ACTION_NONE;
repaint();

}	//	End of method	mouseReleased

/*****************************************************************************/

// try to translate the word the mouse is over

public void mouseMoved(MouseEvent e) 
{
int xmove = e.getX();
int ymove = e.getY();
String msg = "";

BlissWord bw = getWordAt(xmove,ymove);
if(bw!=null)
	{
	// try to find the code in the thesaurus
	msg =  "???";
	int iw = Dict.getWordWithCode(bw.code);
	if(iw>=0)
		msg = Dict.words[iw].name;
	}	

if(!msg.equals(helptext))
	{
	helptext = msg;
	paint(getGraphics());
	}

}	//	End of method	mouseMoved

/*****************************************************************************/

}	//	End of class	Page
