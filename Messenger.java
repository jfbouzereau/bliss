package	bliss;

import	java.awt.*;
import	java.awt.event.*;
import	java.util.*;

import	org.jivesoftware.smack.*;
import	org.jivesoftware.smack.packet.*;
import	org.jivesoftware.smack.packet.Presence.*;
import	org.jivesoftware.smack.filter.*;


public	class	Messenger	extends	Widget	
	implements PacketListener, RosterListener, KeyListener, 
		ComponentListener, 
		MouseListener, MouseMotionListener {

/****************************************************************************/
//	CONSTANTS

static	final	int	WIDTH = 260;
static	final	int	HEIGHT = 400;
static	final	int	HICON = 32;
static	final	int	HUSER = 18;

static	final	int	CMD_NONE = -1;
static	final	int	CMD_ADD = 0;
static	final	int	CMD_DELETE = 1;

/*****************************************************************************/
//	FIELDS

String	jabberServer = null;
String	jabberUser = null;
String	jabberPass = null;

XMPPConnection conn = null;
Roster roster = null;

User users[] = null;

int npage = 20;	//	number of users per page
int ifirst = 0;	//	first user displayed
int iuser = -1;	//	selected user

TextField field = null;

Checkbox checkbox = null;
TextField userfield = null;
TextField passfield = null;
String loginmsg = null;


Rectangle rAdd = null;	// to add contact
Rectangle rDel = null;	// to remove contact

Font wfont = null;	// font to warn that a message is coming
Font lfont = null;
Font bfont = null;

Hashtable chatters = null;  // list of the opened chat sessions
Hashtable comings = null;  // coming packets 

int icmd = CMD_NONE;

/******************************************************************************/
//	CONSTRUCTOR

public Messenger(int left, int top)
{

this._left = left;
this._top = top;
this._width = WIDTH;
this._height = 20+HICON+npage*HUSER;;

myColor = chatColor;
myDarkColor = chatDarkColor;
myLightColor = chatLightColor;

setInterface();

wfont = new Font("Arial",Font.BOLD,11);
lfont = new Font("Monospaced",Font.PLAIN,12);
bfont = new Font("Monospaced",Font.BOLD,24);

chatters = new Hashtable();
comings = new Hashtable();

field = new TextField("user@server");	// to add new contact
field.setBackground(Color.white);

checkbox = new Checkbox("New",false);

userfield = new TextField("user@server");
userfield.setBackground(Color.white);

passfield = new TextField("password");
passfield.setBackground(Color.white);
passfield.setEchoCharacter('x');

setLayout(null);

add(checkbox);
add(userfield);
add(passfield);
checkbox.setBounds(30,_height/2-24-2-24-10,_width-60,24);
userfield.setBounds(30,_height/2-24-2,_width-60,24);
passfield.setBounds(30,_height/2+2,_width-60,24);
userfield.setSelectionStart(0);
userfield.setSelectionEnd(userfield.getText().length());
userfield.requestFocus();

userfield.addKeyListener(this);
passfield.addKeyListener(this);

setBounds(_left,_top,_width+1,_height+1);
setVisible(false);

addMouseListener(this);
addMouseMotionListener(this);
addComponentListener(this);

field.addKeyListener(this);

}

/******************************************************************************/

void	createAccount(String user, String pass) throws Exception
{
AccountManager ac = conn.getAccountManager();
ac.createAccount(user,pass);
}	//	End of method	createAccount

/******************************************************************************/

void	openConnection(String user, String pass, boolean create)
{

// XMPPConnection.DEBUG_ENABLED = true;

Roster.setDefaultSubscriptionMode(Roster.SUBSCRIPTION_ACCEPT_ALL);

int i = user.indexOf('@');
if(i<0) return;

jabberUser = user.substring(0,i);
jabberServer  = user.substring(i+1);
jabberPass = pass;

try	{
	conn = new XMPPConnection(jabberServer);

	if(create)
		createAccount(jabberUser, jabberPass);

	conn.login(jabberUser,jabberPass);
	roster = conn.getRoster();
	roster.addRosterListener(this);
	setUsers();
	conn.addPacketListener(this, new MessageTypeFilter(Message.Type.CHAT));
	loginmsg = null;
	}
catch(Exception ex)
	{
	//ex.printStackTrace();
	loginmsg = ex.getMessage();
	conn = null;
	}

}	//	End of method	openConnection

/******************************************************************************/

void	setUsers()
{

if(roster==null)
	users = new User[0];
else
	{
	users = new User[roster.getEntryCount()];
	int i=0;
	for(Iterator it=roster.getEntries();it.hasNext();)
		{
		RosterEntry entry = (RosterEntry)it.next();
		Presence presence = roster.getPresence(entry.getUser());
		users[i++] = new User(entry,presence);
		}
	}

iuser = -1;

}	//	End of method	setUsers

/******************************************************************************/

public	void	paint(Graphics g)
{

g.setColor(dkgray);
g.fillRect(0,0,_width,_height);

// title bar
String title = jabberUser==null ? "" : jabberUser+"@"+jabberServer;
drawTitle(g,title);

// close box
drawClose(g);

if(loginmsg!=null)
	{
	int lmsg;

	g.setFont(lfont);
	if(loginmsg.length()<19)
		lmsg = g.getFontMetrics().stringWidth("* * * * * * * * * *");
	else
		lmsg = g.getFontMetrics().stringWidth(loginmsg);
	g.setColor(Color.black);
	g.drawString(loginmsg,_width/2-lmsg/2,_height/2+50);
	}

else
	{

	// icons
	int y = MARGIN;
	drawButton(g,rAdd,"+",icmd==CMD_ADD);
	drawButton(g,rDel,"-",icmd==CMD_DELETE);

	// users
	y += HICON+1;
	if(users!=null)
	for(int i=ifirst;i<users.length;i++) 
		{
		g.setColor(ltgray);
		g.fillRect(0,y+(i-ifirst)*HUSER,_width-MARGIN,HUSER-1);

		g.setColor(Color.white);
		g.drawOval(8-1,y+(i-ifirst)*HUSER+2-1,10,10);
		g.setColor(Color.black);
		g.drawOval(8+1,y+(i-ifirst)*HUSER+2+1,10,10);
		g.setColor(getUserColor(users[i]));
		g.fillOval(8,y+(i-ifirst)*HUSER+2,10,10);

		g.setColor(Color.white);
		g.drawOval(8+2,y+(i-ifirst)*HUSER+4,3,3);

		boolean b = hasComingPacket(users[i].entry.getUser());
		g.setFont(b ? wfont : textfont);
		g.setColor(Color.black);
		g.drawString(users[i].entry.getUser(),
			25,y+(i-ifirst)*HUSER+12);

		if(i==iuser)
			{
			g.setColor(Color.black);
			g.drawLine(0,y+(i-ifirst)*HUSER,
				_width-MARGIN,y+(i-ifirst)*HUSER);
			g.setColor(Color.white);
			g.drawLine(0,y+(i-ifirst+1)*HUSER-1,
				_width-MARGIN,y+(i-ifirst+1)*HUSER-1);
			}
		}
	}


// scroll bars
drawScroll(g,rUp,action==ACTION_UP);
drawScroll(g,rDown,action==ACTION_DOWN);

// resize box
drawResize(g);

drawBorders(g);

}	//	End of method	paint

/******************************************************************************/

void	drawButton(Graphics g, Rectangle r, String text, boolean pressed)
{
if(r==null) return;

g.setColor(ltgray);
g.setColor(myColor);
g.fillRect(r.x,r.y,r.width,r.height);

g.setColor(pressed ? Color.white : Color.black);
g.drawRect(r.x,r.y,r.width-1,r.height-1);

g.setColor(pressed ? Color.black : Color.white);
g.drawLine(r.x,r.y+r.width,r.x,r.y);
g.drawLine(r.x,r.y,r.x+r.width,r.y);

g.setColor(Color.black);
g.setFont(bfont);
int l = g.getFontMetrics().stringWidth(text);
g.drawString(text,r.x + r.width/2 - l/2, r.y + r.height-7);

}	//	End of method	drawButton

/******************************************************************************/

Color	getUserColor(User user)
{
if(user.presence==null)
	return acgray;
else 
	{
	Presence.Mode mode = user.presence.getMode();
	if(mode==Presence.Mode.AVAILABLE)
		return Color.green;
	else if(mode==Presence.Mode.CHAT)
		return Color.green;
	else if(mode==Presence.Mode.AWAY)
		return Color.yellow;
	else if(mode==Presence.Mode.EXTENDED_AWAY)
		return Color.yellow;
	else if(mode==Presence.Mode.DO_NOT_DISTURB)
		return Color.red;
	else
		return acgray;
	}

}	//	End of method	getUserColor

/******************************************************************************/

Chatter	openChatter(User user)
{
if(conn==null) return null;

if((user.presence==null)||(user.presence.getType()!=Presence.Type.AVAILABLE))
	{
	getToolkit().beep();
	return null;
	}	

// check if chatter already exists
Chatter chatter = (Chatter)chatters.get(user.entry.getUser());

if(chatter!=null)
	{
	chatter.setVisible(true);
	chatter.writer.setVisible(true);
	}

else
	{
	try	{
		Chat chat = conn.createChat(user.entry.getUser());
		chatter = new Chatter(this,
				jabberUser+"@"+jabberServer,
				user.entry.getUser(),
				chat,
				_left+50,_top+50);	

		// add to our list
		chatters.put(user.entry.getUser(),chatter);

		// add the widget and make it visible
		Bliss bliss = (Bliss)getParent();
		int index = bliss.getComponentIndex(this);
		bliss.add(chatter,index);
		chatter.setVisible(true);
		}
	catch(Exception ex)
		{
		ex.printStackTrace();
		}
	}

return chatter;

}	//	End of method	openChatter

/******************************************************************************/

public	void	close()
{
Bliss bliss = (Bliss)getParent();

// remove all existing chatters first

Enumeration e = chatters.elements();
while(e.hasMoreElements())
	{
	Chatter chatter = (Chatter)e.nextElement();
	bliss.remove(chatter.writer);
	bliss.remove(chatter);
	}

if(conn!=null)
	conn.close();

bliss.remove(this);

}

/******************************************************************************/

public	void	rosterModified()
{
}

/******************************************************************************/

public	void	presenceChanged(String jabberid)
{
int k = jabberid.indexOf('/');
if(k>=0)
	jabberid = jabberid.substring(0,k);

for(int i=0;i<users.length;i++)
	{
	if(users[i].entry.getUser().equals(jabberid))
		{
		users[i].presence = roster.getPresence(jabberid);
		break;
		}
	}

repaint();

}

/******************************************************************************/

void	addContact()
{
String text = field.getText();

int i = text.indexOf('@');
if(i<0) 
	{
	getToolkit().beep();
	return;
	}

String nickname = text.substring(0,i-1);

try	{
	roster.createEntry(text,nickname,null);
	field.setText("");
	}
catch(Exception ex)
	{
	getToolkit().beep();
	return;
	}

}	//	End of method	addContact

/******************************************************************************/

void	deleteContact()
{
if(users==null) return;
if((iuser<0)||(iuser>=users.length)) return;

try	{
	roster.removeEntry(users[iuser].entry);
	}
catch(Exception ex)
	{
	}

}	//	End of method	deleteContact

/******************************************************************************/

public	void	entriesAdded(Collection col) { setUsers(); repaint(); }
public	void	entriesUpdated(Collection col) { setUsers(); repaint(); }
public	void	entriesDeleted(Collection col) { setUsers(); repaint(); }

/******************************************************************************/

public	void	keyTyped(KeyEvent e) {}

public	void	keyPressed(KeyEvent e) 
{
int kode = e.getKeyCode();

if(kode==9)
	{
	// tab
	if(e.getSource()==userfield)
		{
		passfield.requestFocus();
		passfield.setSelectionStart(0);
		passfield.setSelectionEnd(passfield.getText().length());
		}
	else if(e.getSource()==passfield)
		{
		userfield.requestFocus();
		userfield.setSelectionStart(0);
		userfield.setSelectionEnd(userfield.getText().length());
		}
	}

}	//	End of method	keyPressed

/******************************************************************************/

public	void	keyReleased(KeyEvent e)
{
int kode = e.getKeyCode();

if(kode==10)
	{
	// enter
	if(e.getSource()==userfield)
		{
		passfield.requestFocus();
		passfield.setSelectionStart(0);
		passfield.setSelectionEnd(passfield.getText().length());
		}
	else if(e.getSource()==passfield)
		{
		openConnection(userfield.getText(),passfield.getText(),
			checkbox.getState());
		if(conn!=null)
			{
			remove(checkbox);
			remove(userfield);
			remove(passfield);
			checkbox = null;
			userfield = null;
			passfield = null;
			add(field);
			field.setBounds(2,MARGIN+1,_width-MARGIN-2*HICON-4,
				HICON-6);	
			rAdd = new Rectangle(
				_width-MARGIN-2*HICON,
				MARGIN,HICON,HICON);
			rDel = new Rectangle(
				_width-MARGIN-HICON,
				MARGIN,HICON,HICON);
			}
		repaint();
		}	
	else if(e.getSource()==field)
		{
		addContact();
		}
	}

}	//	End of method	keyReleased

/******************************************************************************/


public void mouseClicked(MouseEvent e) {}
public void mouseEntered(MouseEvent e) {}
public void mouseExited(MouseEvent e) {}
public void mouseMoved(MouseEvent e) {}

int xclick, yclick;
int oldw,oldh;
int scrollid = 0;

/*****************************************************************************/

public void mousePressed(MouseEvent e)
{
action = ACTION_NONE;
icmd = CMD_NONE;
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
else if((rAdd!=null)&&(rAdd.contains(xclick,yclick)))
	{
	action = ACTION_CMD;
	icmd = CMD_ADD;
	paint(getGraphics());
	}
else if((rDel!=null)&&(rDel.contains(xclick,yclick)))
	{
	action = ACTION_CMD;
	icmd = CMD_DELETE;
	paint(getGraphics());
	}
else if((yclick>MARGIN+HICON)&&(yclick<MARGIN+HICON+npage*HUSER))
	{
	iuser = (yclick-MARGIN-HICON)/HUSER + ifirst;
	paint(getGraphics());
	if(e.getClickCount()>1)
		if(users!=null)
			if(iuser<users.length)
				openChatter(users[iuser]);
	}

}	//	End of method	mousePressed

/******************************************************************************/

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
	/*
        _width = oldw + xmove;
        if(_width<80)
                _width = 80;
	*/
        _height = oldh + ymove;

	npage = (_height-20-HICON)/HUSER;
	if(npage<1)
		npage = 1;
	_height = 20 + HICON + npage*HUSER;

	setInterface();

        setBounds(_left,_top,_width+1,_height+1);

        paint(getGraphics());
        }

}	//	End of method	mouseDragged

/******************************************************************************/

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
	if((icmd==CMD_ADD)&&(rAdd.contains(e.getX(),e.getY())))
		addContact();
	else if((icmd==CMD_DELETE)&&(rDel.contains(e.getX(),e.getY())))
		deleteContact();
	}


action = ACTION_NONE;
icmd = CMD_NONE;

repaint();

}	//	End of method	mouseReleased

/*****************************************************************************/

public	void	componentHidden(ComponentEvent e) {}
public	void	componentMoved(ComponentEvent e) {}
public	void	componentResized(ComponentEvent e) {}
public	void	componentShown(ComponentEvent e)
{
if(userfield!=null)
	{
	userfield.requestFocus();
	userfield.setSelectionStart(0);
	userfield.setSelectionEnd(userfield.getText().length());
	}
}	//	End of method	componentShown

/*****************************************************************************/

public	void	processPacket(Packet packet)
{
if(!(packet instanceof Message)) return;

String origin = packet.getFrom();
int i = origin.indexOf('/');
if(i>0)
	origin = origin.substring(0,i);

// check if we already have a chatter for this destination
Chatter chatter = (Chatter)chatters.get(origin);

if((chatter!=null)&&(chatter.isVisible()))
	{
	chatter.processPacket(packet);
	}
else
	{
	addComingPacket(origin,packet);
	getToolkit().beep();
	repaint();
	}	

}	//	End of method	processPacket

/*****************************************************************************/

synchronized void	addComingPacket(String user, Packet packet)
{

// get the list for this user

Vector v = (Vector)comings.get(user);
if(v==null)
	{
	v = new Vector();
	comings.put(user,v);
	}

v.addElement(packet);
System.out.println("now "+v.size()+" comings for "+user);

}	//	End of method	addPendingPacket

/*****************************************************************************/

boolean	hasComingPacket(String user)
{

Vector v = (Vector)comings.get(user);
if(v==null)
	return false;
else
	return v.size()>0;

}	//	End of method	hasComingPacker

/*****************************************************************************/

synchronized public	Packet	getComingPacket(String user)
{

System.out.println("getting coming for "+user);

// if no list for this user
Vector v = (Vector)comings.get(user);
if(v==null)
	return null;

// if empty list
if(v.size()==0)
	return null;

// return the first packet and remove it from the list
Packet packet = (Packet)v.elementAt(0);
v.removeElementAt(0);

if(v.size()==0)
	repaint();

System.out.println("returning packet "+packet);

return packet;

}	//	End of method	getComingPacket

/*****************************************************************************/

//      class to autoscroll the list when the mouse is pressed on the bar

class   Scroller        extends Thread  {

int id;

Scroller(int id)
{
this.id = id;
}

/*****************************************************************************/

public  void    run()
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
                if(ifirst>users.length-1)
                        ifirst = users.length-1;
                }
        paint(getGraphics());
        try     { sleep(200); } catch(Exception ex) {}
        }

}       //      End of method   run

/*****************************************************************************/

}       //      End of class    Scroller

/*****************************************************************************/
/*****************************************************************************/

class	User	{

/********************************************************************************/
//	FIELDS

RosterEntry entry = null;
Presence presence = null;

/******************************************************************************/

User(RosterEntry entry, Presence presence)
{
this.entry = entry;
this.presence = presence;
}

/******************************************************************************/

}	//	End of class	User

/******************************************************************************/
/******************************************************************************/

}	//	End of class	Messenger
