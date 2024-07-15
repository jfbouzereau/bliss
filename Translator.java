package	bliss;

import	java.awt.*;
import	java.awt.event.*;
import	java.text.*;
import	java.util.*;

public	class	Translator	extends	Widget
	implements Constants, GlyphReceiver, 
		KeyListener,MouseListener, MouseMotionListener {

/*****************************************************************************/
//	CONSTANTS

static	final	int	HLANG = 42;	// height of language panel
static	final	int	WLOCALE = 60;	// width of locale field
static	final	int	WDISPLAY = 400;	// width of display
static	final	int	HDISPLAY = 180;	// height of display
static	final	int	HOLD = 24;	// height of old text
static	final	int	HNEW = 24;	// height of new text

/*****************************************************************************/
//	FIELDS

String	title = null;

Font ofont = null;

TextField langfield = null;
TextField localefield = null;
TextField field = null;

int	iword = -1;	// current word being translated
Hashtable mywords = null;
boolean modified = false;

String slanguage = null;
String slocale = null;

String name = "";
GlyphPart glyph[] = null;

/*****************************************************************************/
//	CONSTRUCTOR

public	Translator(String title, int left, int top)
{
this.title = title;
this._left = left;
this._top = top;
this._width = WDISPLAY;
this._height = MARGIN + HLANG + HDISPLAY + HOLD + HNEW +MARGIN;

myColor = langColor;
myDarkColor = langDarkColor;
myLightColor = langLightColor;

setLayout(null);

setInterface();

slanguage = Locale.getDefault().getDisplayLanguage();
langfield = new TextField(slanguage);
langfield.setBackground(Color.white);
add(langfield);

slocale = Locale.getDefault().toString();
localefield = new TextField(slocale);
localefield.setBackground(Color.white);
add(localefield);

field = new TextField("");
field.setBackground(Color.white);
add(field);

langfield.setBounds(40,MARGIN+10,_width-3*40-WLOCALE,20);
localefield.setBounds(_width-40-WLOCALE,MARGIN+10,WLOCALE,20);

field.setBounds(40,MARGIN+HLANG+HDISPLAY+HOLD,_width-80,HNEW);

ofont = new Font("Arial",Font.BOLD,14);

setBounds(_left,_top,_width+1,_height+1);

addMouseListener(this);
addMouseMotionListener(this);

field.addKeyListener(this);
localefield.addKeyListener(this);
langfield.addKeyListener(this);

setVisible(false);

loadMyWords();
nextWord();

}

/*****************************************************************************/

public	void	paint(Graphics g)
{

// title bar
drawTitle(g,title);

// close box
drawClose(g);

// content
int y = MARGIN + HLANG;
g.setColor(action==ACTION_DROPW ? acgray : ltgray);
g.fillRect(0,y,_width,_height-MARGIN);

g.setFont(textfont);
String info = (iword+1)+" / "+(Dict.words.length);
g.setColor(Color.black);
int l = g.getFontMetrics().stringWidth(info);
g.drawString(info,_width-l-MARGIN,y+10);

// skyline
g.setColor(dkgray);
g.fillRect(0,y+64,_width,4);

// groundline
g.fillRect(0,y+128,_width,4);

// glyph
drawGlyph(g,glyph);

// original word
y += HDISPLAY;
g.setColor(Color.black);
g.setFont(ofont);
l = g.getFontMetrics().stringWidth(name);
g.drawString(name,_width/2-l/2,y);

g.setColor(Color.black);
g.drawLine(0,MARGIN+HLANG-1,_width,MARGIN+HLANG-1);
g.setColor(Color.white);
g.drawLine(0,MARGIN+HLANG,_width,MARGIN+HLANG);

// borders
drawBorders(g);

}	//	End of method	paint

/*****************************************************************************/

void	drawGlyph(Graphics g, GlyphPart gl[])
{
if(gl==null) return;

Limits l = Util.computeGlyphLimits(gl,2);
int dx = _width/2 - (l.xmax-l.xmin)/2;

for(int i=0;i<gl.length;i++)
	{
	int is = gl[i].ishape;
	int w = SHAPEDIM[is*2]*8+4;
	int h = SHAPEDIM[is*2+1]*8+4;
	int x = dx+gl[i].x*4;
	int y = MARGIN + gl[i].y*4;
	g.drawImage(Bliss.images[is],x,y+HLANG,w,h,this);
	}

}	//	End of method	drawGlyph

/*****************************************************************************/


void	loadMyWords()
{

mywords = new Hashtable();

// load the words already translated from the temporary file

Word ww[] = Dict.loadWords(this,slanguage,slocale);
if(ww!=null)
	for(int i=0;i<ww.length;i++)
		mywords.put(ww[i].code,ww[i].name);


iword = Dict.words.length-1;

}	//	End of method	loadMyWords

/*****************************************************************************/

//	go to first word not yet translated

void	nextWord()
{

int k = Dict.words.length-1;

for(int i=0;i<Dict.words.length;i++)
	if(mywords.get(Dict.words[i].code)==null) 
		{
		k = i;
		break;
		}

setWord(k);

}	//	End of method	nextWord

/*****************************************************************************/

void	setWord(int index)
{
if((index<0)||(index>=Dict.words.length)) return;

iword = index;

// original name
name = Dict.words[iword].name;
glyph = Util.buildGlyph(Dict.words[iword].code,2);

// translated name if already set
String myname = (String)mywords.get(Dict.words[iword].code);
if(myname==null) myname = "";

field.setText(myname);

field.requestFocus();

repaint();

}	//	End of method	setWord

/*****************************************************************************/

void	enterWord()
{

String myname = field.getText();
if(myname.length()==0)
	{
	getToolkit().beep();
	return;
	}

mywords.put(Dict.words[iword].code,myname);

modified = true;	// dictionary must be saved 

nextWord();

}	//	End of method	enterWord

/*****************************************************************************/

void	saveMyWords()
{

// if no changes since last save
if(!modified) return;

Word ww[] = new Word[mywords.size()];

int j = 0;
for(int i=0;i<Dict.words.length;i++)
	{
	String code = Dict.words[i].code;
	String name = (String)mywords.get(code);
	if(name!=null)
		if(j<ww.length)
			ww[j++] = new Word(name,code);
	}

Dict.saveWords(ww,slanguage,slocale);

modified = false;

}	//	End of method	saveMyWords

/*****************************************************************************/

public	void	close()
{

saveMyWords();

((Bliss)getParent()).remove(this);

}	//	End of method	close

/*****************************************************************************/

public	void	receive(GlyphPart gl[])
{
action = ACTION_NONE;

String code = Util.buildCode(gl,1);
int iw = Dict.getWordWithCode(code);

if(iw>=0)
	setWord(iw);

repaint();
}	//	End of method	receive

/*****************************************************************************/

public void mouseClicked(MouseEvent e) {}
public void mouseEntered(MouseEvent e) {}
public void mouseExited(MouseEvent e) {}
public void mouseMoved(MouseEvent e) {}


int xclick, yclick;
int oldw,oldh;

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
else if(yclick<MARGIN)
	{
	action = ACTION_DRAG;
	}
else if(yclick<MARGIN+HDISPLAY)
	{
	if((iword>=0)&&(iword<Dict.words.length)&&(e.getClickCount()>1))
		{
		Bliss bliss = (Bliss)getParent();
		bliss.editWord(Dict.words[iword].code,Dict.words[iword].name);	
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

	setInterface();

	setBounds(_left,_top,_width+1,_height+1);

	paint(getGraphics());
	}

}	//	End of method	mouseDragged

/*****************************************************************************/

public void mouseReleased(MouseEvent e)
{
// to stop the scroller if any

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

}

/*****************************************************************************/

public	void	keyTyped(KeyEvent e){}
public	void	keyReleased(KeyEvent e){}

public	void	keyPressed(KeyEvent e)
{
int kode = e.getKeyCode();

if(e.getSource()==field)
	{
	if(kode==10)
		enterWord();
	}
else if(e.getSource()==localefield)
	{
	slocale = localefield.getText();
	if(kode==10)
		{
		// load a new dictionary
		loadMyWords();
		nextWord();
		}
	}
else if(e.getSource()==langfield)
	{
	slanguage = langfield.getText();
	}

}	//	End of method	keyPressed

/*****************************************************************************/

}	//	End of class	Translator
