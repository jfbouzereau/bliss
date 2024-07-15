package	bliss;

import	java.awt.*;
import	java.awt.event.*;
import	java.text.*;
import	java.util.*;

import	org.jivesoftware.smack.*;
import	org.jivesoftware.smack.packet.*;

public	class	Chatter	extends	Widget	
	implements GlyphReceiver,
		ComponentListener, MouseListener, MouseMotionListener {

/*****************************************************************************/
//	CONSTANTS

static	final	int	WIDTH = 650;
static	final	int	HLINE = 80;
static	final	int	HHELP = 20;

/*****************************************************************************/
//	FIELDS

Chat	chat = null;

int 	nline = 6;	// number of lines per page
int	ifirst = 0;	// first displayed line

Image offscreen = null;
String title = null;
String helptext = "";
Font bfont = null;
Font tinyfont = null;

Color sndcolor = null;
Color rcvcolor = null;

SimpleDateFormat format = null;

public TypeWriter writer = null;

Messenger messenger = null;

Vector history = null;	// list of all the phrases exchanged
Phrase phrase = null;	// current phrase being typed

String me;
String you;
 
/*****************************************************************************/
//	CONSTRUCTOR

public	Chatter(Messenger messenger, String me, String you, Chat chat,
	int left, int top)
{
this.messenger = messenger;
this.me = me;
this.you = you;
this.title = me + "  ::  " +you;
this.chat = chat;

this._left = left;
this._top = top;
this._width = WIDTH;
this._height = 20+nline*HLINE+HHELP;


setInterface();

history = new Vector();
phrase = new Phrase();

myColor = chatColor;
myDarkColor = chatDarkColor;
myLightColor = chatLightColor;

sndcolor = new Color(0xFF,0xEE,0xFF);
rcvcolor = new Color(0xEE,0xFF,0xFF);

bfont = new Font("Verdana",Font.BOLD,14);
tinyfont = new Font("Verdana",Font.PLAIN,9);

format = new SimpleDateFormat("HH:mm");

setLayout(null);
setBounds(_left,_top,_width+1,_height+1);
setVisible(false);

addMouseListener(this);
addMouseMotionListener(this);
addComponentListener(this);

}

/*****************************************************************************/

public	void	paint(Graphics g)
{
if(offscreen==null)
	offscreen = createImage(_width+1,_height+1);
if(offscreen==null)
	return;

if(writer==null)
	openWriter();

Graphics og = offscreen.getGraphics();

// title bar
drawTitle(og,title);

// close box
drawClose(og);

// content
og.setColor(Color.white);
og.fillRect(0,MARGIN,_width-MARGIN,_height-MARGIN);

// history
int iline = 0;
for(int i=0;i<history.size();i++)
	{
	Phrase p = (Phrase)history.elementAt(i);
	iline = drawPhrase(og,p,iline);
	}

// phrase being typed
iline = drawPhrase(og,phrase,iline);


// help area
int y = MARGIN +  nline*HLINE;
og.setColor(ltgray);
og.fillRect(0,y,_width-MARGIN,HHELP);
og.setColor(Color.black);
og.drawLine(0,y,_width-MARGIN,y);
        
og.setFont(textfont);
int ls = og.getFontMetrics().stringWidth(helptext);
og.drawString(helptext,_width/2-ls/2,y+HHELP-5);
        

// scroll bars
drawScroll(og,rUp,action==ACTION_UP);
drawScroll(og,rDown,action==ACTION_DOWN);

// resize box
drawResize(og);

drawBorders(og);

g.drawImage(offscreen,0,0,this);

}	//	End of method	paint

/******************************************************************************/

int	drawPhrase(Graphics g, Phrase p, int iline)
{

int x = 32; // left margin
int y = MARGIN;


if(p.time==null)
	g.setColor(action==ACTION_DROPW ? acgray: Color.white);	// msg not sent
else if(p.user==null)
	g.setColor(sndcolor);		// msg sent by me
else 
	g.setColor(rcvcolor);		// msg received

g.fillRect(0,y+iline*HLINE,_width-MARGIN,HLINE);

// draw sky and ground lines
g.setColor(ltgray);
g.fillRect(0,y+iline*HLINE+32,_width-MARGIN,2);
g.fillRect(0,y+iline*HLINE+64,_width-MARGIN,2);

// display time if any
g.setFont(tinyfont);
g.setColor(Color.black);
if(p.time!=null)
	g.drawString(format.format(p.time),10,y+iline*HLINE+14);
else
	g.drawString("...",10,y+iline*HLINE+14);

for(int j=0;j<p.bwords.size();j++)
	{
	BlissWord bword = (BlissWord)p.bwords.elementAt(j);
	if(x+(bword.limits.xmax-bword.limits.xmin)>_width-MARGIN)
		{
		iline++;
		x = 32;
		if(p.user!=null)
			{
			g.setColor(ltgray);
			g.fillRect(0,y,_width-MARGIN,HLINE);
			}
		}
	bword.left = x;
	bword.top = y+iline*HLINE;
	drawGlyph(g,bword.glyph,x,y+iline*HLINE);
	x += (bword.limits.xmax-bword.limits.xmin) + 32;
	}

iline++;
return iline;

}

/******************************************************************************/

void	hello()
{

try	{
	Message message = chat.createMessage();
	message.setBody("Hello !");
	chat.sendMessage(message);
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

}	//	End of method	hello

/*****************************************************************************/

void    drawGlyph(Graphics g, GlyphPart gl[], int left, int top)
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

}       //      End of method   drawGlyph

/*****************************************************************************/

void    openWriter()    
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
		title, _left+300,_top+300);

        // put it just below me
        index = bliss.getComponentIndex(this);
        bliss.add(writer,index);
        writer.setVisible(true);
        }

}       //      openWriter

/*****************************************************************************/

public	void	receive(GlyphPart g[])
{

action = ACTION_NONE;

if(g.length==0)
	{
	sendPhrase();
	}
else
	{
	phrase.bwords.addElement(new BlissWord(g));
	repaint();
	}

}	//	End of method	receive
	
/*****************************************************************************/

void	sendPhrase()
{

// convert glyph to text

StringBuffer sb = new StringBuffer();
sb.append("<bliss>");
for(int i=0;i<phrase.bwords.size();i++)
	{
	sb.append("<w>");
	BlissWord bword = (BlissWord)phrase.bwords.elementAt(i);
	sb.append(bword.code);
	sb.append("</w>\n");
	}

sb.append("</bliss>\n");

// create and send message
try	{
	/*
	Message message = chat.createMessage();
	message.setBody(sb.toString());
	*/
	chat.sendMessage(sb.toString());
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

// put the phrase just sent to the history and begin a new one

phrase.time = new Date();
history.addElement(phrase);

phrase = new Phrase();

repaint();

}	//	End of method	sendPhrase

/*****************************************************************************/

public	void	close()
{
writer.setVisible(false);
setVisible(false);
}

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
	BlissWord bw = getBlissWordAt(xclick,yclick);
	if((bw!=null)&&(e.getClickCount()>1))
		{
		Bliss bliss = (Bliss)getParent();
		int iw = Dict.getWordWithCode(bw.code);
		if(iw<0)
			bliss.editWord(bw.code,	"");
		else
			bliss.editWord(Dict.words[iw].code,Dict.words[iw].name);
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

	nline = (_height-20)/HLINE;
        if(nline<1)
                nline = 1;
	_height = 20 + nline*HLINE;

	setInterface();

        setBounds(_left,_top,_width+1,_height+1);

        paint(getGraphics());
        }

}       //      End of method   mouseDragged

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

action = ACTION_NONE;
repaint();

}       //      End of method   mouseReleased

/*****************************************************************************/

// try to translate the word the mouse is over

public void mouseMoved(MouseEvent e)
{
int xmove = e.getX();
int ymove = e.getY();
String msg = "";

BlissWord bw = getBlissWordAt(xmove,ymove);

if(bw!=null)
	{
	int iw = Dict.getWordWithCode(bw.code);
	msg = (iw<0) ? "???" : Dict.words[iw].name;
	}

if(!msg.equals(helptext))
        {
        helptext = msg;
        paint(getGraphics());
        }

}       //      End of method   mouseMoved

/*****************************************************************************/

public  void    componentHidden(ComponentEvent e) {}
public  void    componentMoved(ComponentEvent e) {}
public  void    componentResized(ComponentEvent e) {}

public  void    componentShown(ComponentEvent e)
{


// if we are back on screen, check if some arriving packets
// are to be displayed

Packet packet = null;
while((packet = messenger.getComingPacket(you))!=null)
	{
	processPacket(packet);
	}

}       //      End of method   componentShow

/*****************************************************************************/

BlissWord getBlissWordAt(int x, int y)
{

// check history

for(int j=0;j<history.size();j++)
	{
	Phrase p = (Phrase)history.elementAt(j);
	for(int i=0;i<p.bwords.size();i++)
		{
		BlissWord bw = (BlissWord)p.bwords.elementAt(i);
		if((x>bw.left) &&
                        (x<bw.left + bw.limits.xmax - bw.limits.xmin) &&
                        (y>bw.top) &&
                        (y<bw.top + HLINE))
			return bw;
		}
	}


// check current phrase

for(int i=0;i<phrase.bwords.size();i++)
	{
	BlissWord bw = (BlissWord)phrase.bwords.elementAt(i);
	if((x>bw.left) &&
		(x<bw.left + bw.limits.xmax - bw.limits.xmin) &&
		(y>bw.top) &&
		(y<bw.top + HLINE))
		return bw;
	}

return null;

}	//	End of method	getBlissWordAt

/*****************************************************************************/

public	void	processPacket(Packet packet)
{

System.out.println("packet processed by chatter "+packet);

if(!(packet instanceof Message))
	return;

Message message = (Message)packet;

String s = message.getBody();

// look for the <bliss> tag
int ib1,ib2,iw1,iw2;

ib1 = s.indexOf("<bliss>");
ib2 = s.indexOf("</bliss>",ib1);

Phrase phrase = new Phrase();

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
                phrase.bwords.addElement(new BlissWord(code));
                ib1 = iw2+3;
                }
        }

phrase.time = new Date();
phrase.user = packet.getFrom();

history.addElement(phrase);

repaint();

}	//	End of method	processPacket

/*****************************************************************************/
/*****************************************************************************/

class	Phrase 	{

/*****************************************************************************/
//	FIELDS

Vector	bwords = new Vector();
Date time = null;
String	user = null;

/*****************************************************************************/
/*****************************************************************************/

}

/*****************************************************************************/
/*****************************************************************************/

}	//	End of method	Chatter
