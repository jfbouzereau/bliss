package	bliss;

import	java.awt.*;
import	java.awt.event.*;
import	java.text.*;
import	java.util.*;

public	class	TypeWriter extends Widget	
	implements Constants, KeyListener,ComponentListener,
		MouseListener, MouseMotionListener {

/*****************************************************************************/
//	CONSTANTS

static	final	int	WDISPLAY = 300;	// width of display
static	final	int	HDISPLAY = 81;	// height of display
static	final	int	HINDIC = 48;	// height of indicator icon
static	final	int	WINDIC = 24;	// width of indicator icon
static	final	int	HFIELD = 24;	// height of input field
static	final	int	HCHOICE = 13;	// height of choice item
static	final	int	HMODIFIER = 38;	// height of modifier icon
static	final	int	WMODIFIER = 76;	// width of modifier icon

static	final	int	NC = 14;	// number of choices displayed

static	final	int	MODE_NORMAL = 0;
static	final	int	MODE_BACK = 1;
static	final	int	MODE_INDIC = 2;

/*****************************************************************************/
//	FIELDS

TextField field = null;

String title = "";

Font	mfont = null;
char	cequiv[] = null;

Choice choices[] = null;
int	ichoice = -1;

Modifier prefix[] = null;
int	iprefix = -1;

Modifier suffix[] = null;
int 	isuffix = -1;

Modifier indic[] = null;
int	iindic = -1;

Limits	plimits;			// limits of the prefix glyph
Limits	glimits;			// limits of the whole word
Limits	slimits;			// limits of the suffix glyph
Limits	climits;			// limits of the last char entered

GlyphPart pglyph[] = null;		// prefix glyph
GlyphPart glyph[] = null;		// main glyph
GlyphPart sglyph[] = null;		// suffix glyph
GlyphPart iglyph[] = null;		// indicator glyph

Collator collator = null;

int 	mode = MODE_NORMAL;

GlyphReceiver bucket = null;			// to whom I must send the typing

Rectangle rIndic = null;

/*****************************************************************************/
//	CONSTRUCTOR

public	TypeWriter(GlyphReceiver bucket, Color myColor, Color myDarkColor,
	Color myLightColor, String title, int left, int top)
{
setLayout(null);

this.bucket = bucket;
this.myColor = myColor;
this.myDarkColor = myDarkColor;
this.myLightColor = myLightColor;
this.myColor = myColor;
this.title = title;
this._left = left;
this._top = top;
this._width = WMODIFIER + WDISPLAY + WMODIFIER;
this._height = 20+HINDIC+HDISPLAY + HFIELD + NC*HCHOICE;

rIndic = new Rectangle(0,0,0,0);

setInterface();

mfont = new Font("Monospaced",Font.BOLD,18);

cequiv = new char[1];

ichoice = -1;

collator = Collator.getInstance();
collator.setStrength(Collator.PRIMARY);	// ignore case and accents

setIndicators();
iindic =  -1;

setPrefixes();
iprefix =  -1;

setSuffixes();
isuffix = -1;

setBounds(_left,_top,_width+1,_height+1);
setVisible(true);

field = new TextField("");
field.setBackground(Color.white);
field.setBounds(WMODIFIER+2,20+HINDIC+HDISPLAY+2,WDISPLAY-4,HFIELD-4);
add(field);

addMouseListener(this);
addMouseMotionListener(this);
addComponentListener(this);
field.addKeyListener(this);

setChoices("");

setVisible(false);

}

/*****************************************************************************/

void	setIndicators()
{

indic = new Modifier[] {
	new Modifier("A94", '^'),  //      ACTION I.E. VERB
        new Modifier("A55", ')'),  //      PAST
        new Modifier("A54", '('),  //      FUTURE
        new Modifier("A83", '<'),  //      PASSIVE
        new Modifier("A95", '>'),  //      ACTIVE
        new Modifier("A79", '?'),  //      CONDITIONAL
        new Modifier("A82", '\\'),  //      DESCRIPTION I.E. ADJECTIVE OR ADVERB
        new Modifier("A56", '.'),  //      FACT
        new Modifier("A78", '*'),  //      PLURAL
        new Modifier("A51", '#')   //      THING
	};

}	//	End of method	setIndicators

/*****************************************************************************/

void	setPrefixes()
{
prefix = new Modifier[] {
	new Modifier("K81",'?'),	//	INTERROGATION
	new Modifier("K80",'!'),	//	EXCLAMATION
	new Modifier("M40M41",'*'),	//	AUGMENTATION
	new Modifier("I64CI0O64",'/'),	//	OPPOSITE
	new Modifier("I26M40M41",'&'),	//	GENERALISATION
	new Modifier("M25CK56O56",'%'),	//	PART OF
	new Modifier("Q25CO24",'+'),	//	POSSESSION
	new Modifier("Q25",'-')		//	MINUS
	};

}	//	End of method	setPrefixes

/*****************************************************************************/

void	setSuffixes()
{

suffix = new Modifier[] {
	new Modifier("K80",'!'),	//	EXCLAMATION
	new Modifier("Q25CO24",'+'),	//	POSSESSION
	new Modifier("M31",')'),	//	IN THE PAST, AGO
	new Modifier("M30",'('),	//	IN THE FUTURE, THEN
	};

}	//	End of method	setSuffixes

/*****************************************************************************/

public	void	paint(Graphics g)
{

g.setColor(dkgray);
g.fillRect(0,0,_width,_height);

int y = MARGIN;
int left = _width/2 - indic.length*WINDIC/2;

rIndic.x = left;
rIndic.y = y;
rIndic.width = indic.length*WINDIC;
rIndic.height = HINDIC;

// indicators
for(int i=0;i<indic.length;i++)
	{
	g.setColor(ltgray);
	g.fillRect(left+i*WINDIC,y,WINDIC,HINDIC);
	drawModifier(g,indic[i].glyph,left+i*WINDIC,y+HINDIC-WINDIC,WINDIC);
	drawShortcut(g,indic[i].equiv,left+i*WINDIC,y,WINDIC);

	g.setColor((i==iindic) ? Color.white : Color.black);
	g.drawRect(left+i*WINDIC,y,WINDIC+1,HINDIC+1);
	g.setColor((i==iindic) ? Color.black : Color.white);
	g.drawLine(left+i*WINDIC+1,y+HINDIC,left+i*WINDIC+1,y);
	g.drawLine(left+i*WINDIC+1,y,left+i*WINDIC+WINDIC,y);
	}
g.setColor(Color.black);
g.drawLine(WMODIFIER,y+HINDIC+1,_width-WMODIFIER,y+HINDIC+1);


y += HINDIC;

// display
g.setColor(Color.white);
g.fillRect(WMODIFIER,y+2,WDISPLAY,HDISPLAY);

// skyline
g.setColor(ltgray);
g.fillRect(WMODIFIER,y+32,WDISPLAY,2);

// ground line
g.fillRect(WMODIFIER,y+64,WDISPLAY,2);

drawGlyph(g,pglyph);
drawGlyph(g,glyph);
drawGlyph(g,sglyph);

y += HDISPLAY;
y += HFIELD+2;

// choices
if(choices!=null)
for(int i=0;i<choices.length;i++)
	{
	g.setColor(i==ichoice ? acgray : ltgray);
	g.fillRect(WMODIFIER,y+i*HCHOICE,WDISPLAY,HCHOICE);

	g.setFont(textfont);
	g.setColor(Color.black);
	int j = choices[i].windex;
	g.drawString(Dict.words[j].name,WMODIFIER+10,y+i*HCHOICE+10);
	}

// prefixes
y = MARGIN;
for(int i=0;i<prefix.length;i++)
	{
	g.setColor(ltgray);
	g.fillRect(0,y+i*HMODIFIER,WMODIFIER,HMODIFIER);
	drawModifier(g,prefix[i].glyph,WMODIFIER-HMODIFIER,
		y+i*HMODIFIER,HMODIFIER);
	drawShortcut(g,prefix[i].equiv,0,y+i*HMODIFIER,HMODIFIER);
	}

for(int i=0;i<=prefix.length;i++)
	{
	g.setColor((i-1)==iprefix ? Color.white : Color.black);
	g.drawLine(0,y+i*HMODIFIER-1,WMODIFIER,y+i*HMODIFIER-1);
	g.setColor(i==iprefix ? Color.black : Color.white);
	g.drawLine(0,y+i*HMODIFIER,WMODIFIER,y+i*HMODIFIER);
	}
g.setColor(Color.black);
g.drawLine(WMODIFIER,y,WMODIFIER,_width);

// suffixes
left = _width-WMODIFIER;
for(int i=0;i<suffix.length;i++)
	{
	g.setColor(ltgray);
	g.fillRect(left,y+i*HMODIFIER,WMODIFIER,HMODIFIER);
	drawModifier(g,suffix[i].glyph,_width-WMODIFIER,y+i*HMODIFIER,
		HMODIFIER);
	drawShortcut(g,suffix[i].equiv,left+HMODIFIER,y+i*HMODIFIER,HMODIFIER);
	}

for(int i=0;i<=suffix.length;i++)
	{
	g.setColor((i-1)==isuffix ? Color.white : Color.black);
	g.drawLine(left,y+i*HMODIFIER-1,_width,y+i*HMODIFIER-1);
	g.setColor(i==isuffix ? Color.black : Color.white);
	g.drawLine(left,y+i*HMODIFIER,_width,y+i*HMODIFIER);
	}
g.setColor(Color.white);
g.drawLine(_width-WMODIFIER,y,_width-WMODIFIER,_width);


// title bar
drawTitle(g,title);

// close box
drawClose(g);

drawBorders(g);

}	//	End of method	paint

/*****************************************************************************/

void	drawGlyph(Graphics g, GlyphPart gl[])
{
if(gl==null) return;


for(int i=0;i<gl.length;i++)
        {
        int is = gl[i].ishape;
        int w = SHAPEDIM[is*2]*4+2;
        int h = SHAPEDIM[is*2+1]*4+2;
        int x = WMODIFIER + MARGIN + gl[i].x*4 -2;
        int y = MARGIN + HINDIC + (gl[i].y)*4 ;
        g.drawImage(Bliss.images[is],x,y,w,h,this);
        }

}	//	End of method	drawGlyph

/*****************************************************************************/

void	drawModifier(Graphics g, GlyphPart gl[], int left, int top, int size)
{
Limits l = Util.computeGlyphLimits(gl,1);

// center of the glyph
int cx = (l.xmin+l.xmax)/2;
int cy = (l.ymin+l.ymax)/2;

// needed shift
int dx = left+size/2 - cx;
int dy = top+size/2 - cy;

for(int i=0;i<gl.length;i++)
        {
        int is = gl[i].ishape;
        int w = SHAPEDIM[is*2]*4+2;
        int h = SHAPEDIM[is*2+1]*4+2;
        g.drawImage(Bliss.images[is],4*gl[i].x+dx,4*gl[i].y+dy,w,h,this);
        }

}	//	End of method	drawModifier

/*****************************************************************************/

void	drawShortcut(Graphics g, char kar, int x, int y, int size)
{
cequiv[0] = kar;

g.setColor(Color.black);
g.setFont(mfont);
int l = g.getFontMetrics().charWidth(kar);

g.drawChars(cequiv,0,1,x + (size-l)/2, y + size - 10);


}	//	End of method	drawShortcut
	
/*****************************************************************************/

int	setChoices(String text)
{
StringTokenizer tk;
Vector v = new Vector();

int lt = text.length();

// retain all the words with keyword beginning with the typed text
if(lt>0)
for(int i=0;i<Dict.words.length;i++)
	{
	tk = new StringTokenizer(Dict.words[i].name,",");
	int ipref = 0;
	while(tk.hasMoreTokens())
		{
		ipref++;
		String keyword = tk.nextToken().trim();
		if((keyword.length()>=lt)&&
			collator.equals(keyword.substring(0,lt),text))
				{
				v.addElement(new Choice(i,keyword,ipref));
				break;
				}
		}
	}


int nc = v.size();
int ind[] = new int[nc];
for(int i=0;i<nc;i++)
	ind[i] = i;

if(nc>0)
	Util.quickSort(ind,new ChoiceComparator(v));


if(nc>NC)
	nc = NC;
choices = new Choice[nc];
for(int i=0;i<nc;i++)
	choices[i] = (Choice)v.elementAt(ind[i]);

if(nc==1)
	{
	// if only one choice, select it	
	ichoice = 0;
	}
else if((nc>1)&&collator.equals(choices[0].keyword,text))
	{
	// if first word match exactly the typed text
	ichoice = 0;
	}
else  if(nc>1)
	{
	ichoice = -1;
	}
else
	{
	// no choice
	ichoice = -1;
	}

repaint();

return nc;

}	//	End of method	setChoices

/*****************************************************************************/

GlyphPart[] addGlyph(GlyphPart gold[], GlyphPart gnew[])
{
GlyphPart gg[]=null;

if(gold==null)
	{
	// no glyph yet
	gg = Util.copyGlyph(gnew);
	}
else
	{
	// else put the new one it at 1/4 of unit on the right of the old one
	gg = Util.copyGlyph(gnew);

	Limits l = Util.computeGlyphLimits(gold,1);

	for(int i=0;i<gg.length;i++)
		gg[i].x += l.xmax/4 + 2;

	gg = Util.concatGlyph(gold,gg);
	}

return gg;

}	//	End of method	addGlyph

/*****************************************************************************/

void	adjustGlyph()
{
Limits l;

// make sure prefix, main part and suffix are properly espaced
int left = 0;

if(pglyph!=null)
	{
	l = Util.computeGlyphLimits(pglyph,1);

	for(int i=0;i<pglyph.length;i++)
		pglyph[i].x += (left-l.xmin)/4;

	left += l.xmax-l.xmin + 8;
	}

if(glyph!=null)
	{
	l = Util.computeGlyphLimits(glyph,1);

	for(int i=0;i<glyph.length;i++)
		glyph[i].x += (left-l.xmin)/4;
	
	left += l.xmax-l.xmin + 8;
	}

if(sglyph!=null)
	{
	l = Util.computeGlyphLimits(sglyph,1);

	for(int i=0;i<sglyph.length;i++)
		sglyph[i].x += (left-l.xmin)/4;

	left = l.xmax-l.xmin + 8;
	}

}	//	End of method	adjustGlyph

/*****************************************************************************/

void	addIndicator(int num)
{
if(glyph==null) return;

GlyphPart tglyph[] = Util.copyGlyph(glyph);
// sort parts from left to right
for(int i=0;i<tglyph.length-1;i++)
	for(int j=i+1;j<tglyph.length;j++)
		if(tglyph[i].x > tglyph[j].x )
			{
			GlyphPart temp = tglyph[i];
			tglyph[i] = tglyph[j];
			tglyph[j] = temp;
			}

// look for the last character of the glyph
int xmin = -9999;
int xmax = -9999;
int ymin = 9999;

for(int i=0;i<tglyph.length;i++)
	{
	if(tglyph[i].x> xmax)
		{
		int is = tglyph[i].ishape;	
		xmin = tglyph[i].x;
		xmax = xmin + SHAPEDIM[is*2];
		ymin = tglyph[i].y;
		}
	else
		{
		int is = tglyph[i].ishape;
		if(tglyph[i].x+SHAPEDIM[is*2]>xmax)
			xmax = tglyph[i].x + SHAPEDIM[is*2];
		if(tglyph[i].y<ymin)
			ymin = tglyph[i].y;
		}
	}


// glyph for the indicator
GlyphPart iglyph[] = Util.copyGlyph(indic[num].glyph);

// move to the center of the last character
int dx = (xmax+xmin)/2 - (indic[num].limits.xmax - indic[num].limits.xmin)/8;

for(int i=0;i<iglyph.length;i++)
	iglyph[i].x += dx;

// if not a high character, put the indicator on the normal line
if(ymin>=8)
	{
	for(int i=0;i<iglyph.length;i++)
		iglyph[i].y += 4;
	}

glyph = Util.concatGlyph(tglyph,iglyph);

repaint();

}	//	End of method	addIndicator

/*****************************************************************************/

boolean	isPrefix(KeyEvent e)
{

// no text must have been typed yet
if(field.getText().length()>0)
	return false;

// no char must have been entered yet
if(glyph!=null) return false;

char kar = e.getKeyChar();

if((kar>='0')&&(kar<='9'))
	{
	
	pglyph = addGlyph(pglyph,Util.digitGlyph(kar-'0'));
	adjustGlyph();
	return true;
	}

for(int i=0;i<prefix.length;i++)
	if(prefix[i].equiv==kar)
		{
		pglyph = addGlyph(pglyph,prefix[i].glyph);
		adjustGlyph();
		return true;	
		}

return false;

}	//	End of method	isPrefix


/*****************************************************************************/

boolean	isIndicator(KeyEvent e)
{

if(glyph==null)
	return false;

char kar = e.getKeyChar();

for(int i=0;i<indic.length;i++)
	if(indic[i].equiv==kar)
		{
		addIndicator(i);
		return true;
		}

return false;

}	//	End of method	isIndicator

/*****************************************************************************/

boolean	isSuffix(KeyEvent e)
{

char kar = e.getKeyChar();

if((kar>='0')&&(kar<='9'))
	{
	
	sglyph = addGlyph(sglyph,Util.digitGlyph(kar-'0'));
	adjustGlyph();
	return true;
	}

for(int i=0;i<suffix.length;i++)
	if(suffix[i].equiv==kar)
		{
		sglyph = addGlyph(sglyph,suffix[i].glyph);
		adjustGlyph();
		return true;	
		}

return false;
}	//	End of method	isSuffix

/*****************************************************************************/

void	removeGlyphPart()
{
if(glyph==null) return;
if(glyph.length==0) return;

// look for the last character of the glyph
int k = -1;
int xmin = -9999;
int xmax = -9999;

for(int i=0;i<glyph.length;i++)
	{
	if(glyph[i].x> xmax)
		{
		k = i;
		int is = glyph[i].ishape;	
		xmin = glyph[i].x;
		xmax = xmin + SHAPEDIM[is*2];
		}
	else
		{
		int is = glyph[i].ishape;
		if(glyph[i].x+SHAPEDIM[is*2]>xmax)
			xmax = glyph[i].x + SHAPEDIM[is*2];
		}
	}

GlyphPart gl[] = new GlyphPart[k];
for(int i=0;i<k;i++)
	gl[i] = glyph[i];

glyph = gl;

repaint();

}	//	End of method	removeGlyphPart

/*****************************************************************************/

void	clear()
{
mode = MODE_NORMAL;
field.setText("");
glyph = null;
pglyph = null;
sglyph = null;
ichoice = -1;
iindic = -1;
iprefix = -1;
isuffix = -1;

repaint();

}	//	End of method	clear

/*****************************************************************************/

void	sendToGlyphReceiver()
{
if(bucket==null) return;

bucket.receive(Util.concatGlyph(pglyph,glyph,sglyph));

clear();
repaint();

}	//	End of method	sendToGlyphReceiver

/*****************************************************************************/

public	void	close()
{
System.out.println("Closing!");
((Bliss)getParent()).remove(this);
}

/*****************************************************************************/

int	xclick, yclick;

public	void 	mouseClicked(MouseEvent e) {}
public	void	mouseEntered(MouseEvent e) {}
public	void	mouseExited(MouseEvent e) {}
public	void	mouseMoved(MouseEvent e) {}

/*****************************************************************************/

public	void	mousePressed(MouseEvent e) 
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
else if(xclick<WMODIFIER)
	{
	int k = (yclick-MARGIN)/HMODIFIER;
	if((k>=0)&&(k<prefix.length))
		{
		action = ACTION_MODIFY;
		iprefix = k;
		paint(getGraphics());
		}
	}
else if(xclick>_width-WMODIFIER)
	{
	int k = (yclick-MARGIN)/HMODIFIER;
	if((k>=0)&&(k<suffix.length))
		{
		action = ACTION_MODIFY;
		isuffix = k;
		paint(getGraphics());
		}
	}
else if(rIndic.contains(xclick,yclick))
	{
	int k = (xclick-rIndic.x)/WINDIC;
	if((k>=0)&&(k<indic.length))
		{
		action = ACTION_MODIFY;
		iindic = k;
		paint(getGraphics());
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
	}
}

/*****************************************************************************/

public	void	mouseReleased(MouseEvent e)
{

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
else if(action==ACTION_MODIFY)
	{
	if(iprefix>=0)
		{
		pglyph = addGlyph(pglyph,prefix[iprefix].glyph);
		adjustGlyph();
		iprefix = -1;
		}
	else if(isuffix>=0)
		{
		sglyph = addGlyph(sglyph,suffix[isuffix].glyph);
		adjustGlyph();
		isuffix = -1;
		}
	else if(iindic>=0)
		{
		addIndicator(iindic);
		iindic = -1;
		}
	}

action = ACTION_NONE;

repaint();

field.requestFocus();

}	//	End of method	mouseReleased


/*****************************************************************************/


public	void	keyTyped(KeyEvent e) {}

/*****************************************************************************/

public	void	keyPressed(KeyEvent e)
{

//System.out.println("keypressed "+e.getKeyCode());

switch(mode)
	{
	case MODE_NORMAL: keyPressedNormal(e);	break;
	case MODE_BACK: keyPressedBack(e);	break;
	case MODE_INDIC : keyPressedIndic(e);	break;
	}

}	//	End of method	keyPressed

/*****************************************************************************/

public	void	keyReleased(KeyEvent e)
{

//System.out.println("keyreleased "+e.getKeyCode());

if(mode!=MODE_NORMAL)
	{
	 e.consume();
	}
else
	keyReleasedNormal(e);

}	//	End of method	keyReleased

/*****************************************************************************/

String oldtext = "";

void	keyPressedNormal(KeyEvent e)
{
oldtext = field.getText();

int kode = e.getKeyCode();

if(kode==8)
	{
	if(field.getText().length()==0)
		removeGlyphPart();
	}
else if(kode==38)
	{
	if(ichoice>=0)
		{
		ichoice--;
		repaint();
		e.consume();
		}
	else
		{
		ichoice = -1;
		mode = MODE_BACK;
		}
	}
else if(kode==40)
	{
	if((ichoice<NC-1)&&(choices!=null)&&(ichoice<choices.length-1))
		{
		ichoice++;
		repaint();
		e.consume();
		}
	}
else if(isPrefix(e))
	{
	iprefix = -1;
	mode = MODE_NORMAL;
	e.consume();
	repaint();
	}
else if(isIndicator(e))
	{
	iindic = -1;
	mode = MODE_NORMAL;
	e.consume();
	repaint();
	}
else if(isSuffix(e))
	{
	isuffix = -1;
	mode = MODE_NORMAL;
	e.consume();
	repaint();
	}

}	//	End of method	keyPressedNormal

/*****************************************************************************/

void	keyReleasedNormal(KeyEvent e)
{
int kode = e.getKeyCode();

if(kode==27)
	{
	// escape
	clear();
	}
else if(kode==10)
	{
	// enter
	if(ichoice>=0)
		{
		System.out.println("choice code = "+
				Dict.words[choices[ichoice].windex].code);
		GlyphPart g[] = Util.buildGlyph(
			Dict.words[choices[ichoice].windex].code,1);
		glyph = addGlyph(glyph,g);
		adjustGlyph();
		repaint();
		field.setText("");
		ichoice = -1;
		choices = null;
		}
	else if(field.getText().length()==0)
		{	
		sendToGlyphReceiver();
		}
	}
else if(kode==38)
	{
	if(ichoice<0)
		{
		mode = MODE_INDIC;
		repaint();
		}
	}
else if(!field.getText().equals(oldtext))
	{
	oldtext = field.getText();
	setChoices(oldtext);
	}

}	//	End of method	keyReleasedNormal

/*****************************************************************************/

void	keyPressedBack(KeyEvent e)
{
mode = MODE_NORMAL;
}

/*****************************************************************************/

void	keyPressedIndic(KeyEvent e)
{
int kode = e.getKeyCode();

if(kode==10)
	{
	mode = MODE_NORMAL;
	repaint();
	}
else if(kode==37)
	{
	if(iindic>0)
		{
		iindic--;
		repaint();
		}
	}
else if(kode==38)
	{
	}
else if(kode==39)
	{
	if(iindic<indic.length-1)
		{
		iindic++;
		repaint();
		}
	}
else if(kode==40)
	{
	mode = MODE_BACK;
	repaint();
	}
else
	{
	char kar = e.getKeyChar();
	int j = -1;
	for(int i=0;i<indic.length;i++)
		if(indic[i].equiv==kar)
			{
			j = i;
			break;
			}
	if(j>=0)
		{
		addIndicator(j);	
		mode = MODE_NORMAL;
		repaint();
		}
	}

e.consume();

}	//	End of method	keyPressedIndic

/*****************************************************************************/

public	void	componentHidden(ComponentEvent e) {}

public	void	componentResized(ComponentEvent e) 
{
field.requestFocus();
}

public	void	componentMoved(ComponentEvent e) 
{
field.requestFocus();
}

public	void	componentShown(ComponentEvent e)
{
field.requestFocus();
}

/*****************************************************************************/
/*****************************************************************************/

class	Choice	{

int	windex;
String keyword;
int	pref;

Choice(int windex, String keyword, int pref)
{
this.windex = windex; // index in thesaurus
this.keyword = keyword; // keyword matching the user's text
this.pref = pref;	// preference (order of appearance in name)
}

}

/*****************************************************************************/
/*****************************************************************************/

class	Modifier	{

String	code;
char	equiv;
GlyphPart glyph[];
Limits limits;

public	Modifier(String code, char equiv)
{
this.code = code;
this.equiv = equiv;
glyph = Util.buildGlyph(code,1);
limits = Util.computeGlyphLimits(glyph,1);
}

}

/*****************************************************************************/
/*****************************************************************************/

class	ChoiceComparator	extends	Comparator	{

Vector v;

ChoiceComparator(Vector v)
{
this.v = v;
}

public int compare(int i1, int i2)
{
Choice c1 = (Choice)v.elementAt(i1);
Choice c2 = (Choice)v.elementAt(i2);

// compare text
int k = collator.compare(c1.keyword,c2.keyword);
if(k!=0) return k;

// in case of equality, compare preference :  lower is better
return c1.pref - c2.pref;
}	//	End of method	 compare

}

/*****************************************************************************/
/*****************************************************************************/

}	//	End of class	TypeWriter

