package bliss;

import	java.awt.*;


public interface 	Constants {

/*****************************************************************************/

static final String VERSION = "0.85";

/*****************************************************************************/
//	ACTIONS ON THE WIDGETS

static  final   int     ACTION_NONE = 0;
static  final   int     ACTION_DRAG = 1;	// drag widget with title bar
static  final   int     ACTION_SHAPE = 2;	// drag shape (Board action)
static  final   int     ACTION_CLEAR = 3;	// clear display (Board action)
static  final   int     ACTION_CANCEL = 4;	// cancel (Board action)
static  final   int     ACTION_DRAGW = 5;	// drag word
static  final   int     ACTION_DROPW = 6;	// drop word
static  final   int     ACTION_UP = 7;		// scroll up
static  final   int     ACTION_DOWN = 8;	// scroll down
static  final   int     ACTION_SIZE = 9;	// resize
static  final   int     ACTION_CLOSE = 10;	// close 
static	final	int	ACTION_CMD = 11;	// click on icon (Page action)
static	final	int	ACTION_ADDW = 12;	// add word (Board action)
static	final	int	ACTION_DRAW = 13;	// draw curve (Pad action)
static	final	int	ACTION_MODIFY = 14;	// TypeWriter action

/*****************************************************************************/
//	SHAPE DIMENSIONS IN 1/8 OF UNIT

static final int SHAPEDIM[] = {
	0, 8 ,	//	FULL VERTICAL BAR
	8, 0 ,	//	FULL HORIZONTAL BAR
	8, 8 ,	//	FULL CIRCLE
	8, 8 ,	//	FULL SQUARE
	8, 2 ,	//	FULL ARC N
	8, 2 ,	//	FULL ARC S
	2, 8 ,	//	FULL ARC W
	2, 8 ,	//	FULL ARC E
	4, 8 ,	//	FULL EAR E
	4, 8 ,	//	FULL EAR W
	4, 4 ,	//	FULL ARC NW
	4, 4 ,	//	FULL ARC NE

	8, 4 ,	//	FULL DIAGONAL WNW-ESE
	8, 4 ,	//	FULL DIAGONAL ENE-WSW
	4, 8 ,	//	FULL DIAGONAL NNE-SSW
	4, 8 ,	//	FULL DIAGONAL NNW-SSE
	8, 8 ,	//	FULL DIAGONAL NW-SE
	8, 8 ,	//	FULL DIAGONAL NE-SW
	8, 4 ,	//	FULL ARC N
	8, 4 ,	//	FULL ARC S
	4, 8 ,	//	FULL ARC W
	4, 8 ,	//	FULL ARC E
	4, 4 ,	//	FULL ARC SW
	4, 4 ,	//	FULL ARC SE

	0, 4 ,	//	HALF VERTICAL BAR
	4, 0 ,	//	HALF HORIZONTAL BAR
	4, 4 ,	//	HALF CIRCLE
	4, 4 ,	//	HALF SQUARE
	4, 2 ,	//	HALF ARC N
	4, 2 ,	//	HALF ARC S
	2, 4 ,	//	HALF ARC W
	2, 4 ,	//	HALF ARC E
	4, 4 ,	//	HALF CROSS
	8, 8 ,	//	FULL DOTTED CIRCLE
	2, 2 ,	//	HALF ARC NW
	2, 2 ,	//	HALF ARC NE

	4, 2 ,	//	HALF DIAGONAL WNW-ESE
	4, 2 ,	//	HALF DIAGONAL ENE-WSW
	2, 4 ,	//	HALF DIAGONAL NNE-SSW
	2, 4 ,	//	HALF DIAGONAL NNW-SSE
	4, 4 ,	//	HALF DIAGONAL NE-SW
	4, 4 ,	//	HALF DIAGONAL NW-SE
	4, 2 ,	//	HALF ARC N
	4, 2 ,	//	HALF ARC S
	2, 4 ,	//	HALF ARC W
	2, 4 ,	//	HALF ARC E
	2, 2 ,	//	HALF ARC SW
	2, 2 ,	//	HALF ARC SE

	2, 0 ,	//	QUARTER VERTICAL BAR
	0, 2 ,	//	QUARTER HORIZONTAL BAR
	2, 2 ,	//	QUARTER CIRCLE
	2, 2 ,	//	QUARTER SQUARE
	2, 2 ,	//	QUARTER ARC N
	2, 2 ,	//	QUARTER ARC S 
	2, 2 ,	//	QUARTER ARC W
	2, 2 ,	//	QUARTER ARC E
	0, 0 ,	//	DOT
	2, 2 ,	//	COLON
	2, 2 ,	//	QUARTER ARC NW
	2, 2 ,	//	QUARTER ARC NE

	4, 2 ,	//	POINTER N
	2, 4 ,	//	POINTER E
	4, 2 ,	//	POINTER S
	2, 4 ,	//	POINTER W
	2, 2 ,	//	QUARTER DIAGONAL NE-SW
	2, 2 ,	//	QUARTER DIAGONAL NW-SE
	4, 2 ,	//	QUARTER ARC N
	4, 2 ,	//	QUARTER ARC S
	2, 4 ,	//	QUARTER ARC W
	2, 4 ,	//	QUARTER ARC E
	2, 2 ,	//	QUARTER ARC SW
	2, 2 ,	//	QUARTER ARC SE

	2, 2 ,	//	POINTER NW
	2, 2 ,	//	POINTER NE
	2, 2 ,	//	POINTER SE
	2, 2 ,	//	POINTER SW
	8, 8 ,	//	FULL CROSS
	4, 4 ,	//	HALF CROSS
	2, 2 ,	//	QUARTER CROSS
	2, 2 ,	//	QUARTER QUESTION MARK
	0, 4 ,	//	EXCLAMATION MARK
	2, 4 ,	//	QUESTION MARK
	2, 2 ,	//	QUARTER POINTER S
	2, 2 ,	//	QUARTER POINTER W

	2, 4 ,	//	DIGIT 2
	2, 4 ,	//	DIGIT 4
	2, 4 ,	//	DIGIT 3
	2, 4 ,	//	DIGIT 8
	2, 4 ,	//	DIGIT 5
	2, 4 ,	//	DIGIT 6
	2, 4 ,	//	DIGIT 7
	2, 4 ,	//	DIGIT 8
	2, 4 ,	//	DIGIT 9
	2, 4 ,	//	DIGIT 0
	2, 2 ,	//	QUARTER POINTER N
	2, 2 	//	QUARTER POINTER E
	};

/*****************************************************************************/
//	INDICATORS
/*
static	final	char	INDIC[] = {
	(char)94, 'a',	//	ACTION I.E. VERB
	(char)55, ')',	//	PAST
	(char)54, '(',	//	FUTURE
	(char)83, '<',	//	PASSIVE
	(char)95, '>',	//	ACTIVE
	(char)79, '?',	//	CONDITIONAL
	(char)-1, ' ',	//	CLEAR
	(char)82, 'v',	//	DESCRIPTION I.E. ADJECTIVE OR ADVERB
	(char)56, '.',	//	FACT
	(char)78, '*',	//	PLURAL
	(char)51, '@'	//	THING
	};
*/

/*****************************************************************************/
//	COLORS

static	final	Color	ltgray = new Color(0xCC,0xCC,0xCC);
static	final	Color	dkgray = new Color(0xAA,0xAA,0xAA);
static	final	Color	acgray = new Color(0x88,0x88,0x88);
static	final	Color	clgray = new Color(0x66,0x66,0x66);

static	final	Color	boardColor = new Color(0xFF,0xCC,0x00);
static	final	Color	boardDarkColor = new Color(0xA9,0x68,0x01);
static	final	Color	boardLightColor = new Color(0xFF,0xFF,0x9B);

static	final	Color	searchColor = new Color(0x00,0x99,0x66);
static	final	Color	searchDarkColor = new Color(0x01,0x65,0x02);
static	final	Color	searchLightColor = new Color(0x66,0xFF,0x97);

static	final	Color	writerColor = new Color(0x33,0x66,0xCC);
static	final	Color	writerDarkColor = new Color(0x33,0x98,0xCC);
static	final	Color	writerLightColor = new Color(0,0,0);

static	final	Color	listColor = new Color(0xCC,0x00,0x00);
static	final	Color	listDarkColor = new Color(0x9A,0x00,0x00);
static	final	Color	listLightColor = new Color(0xFE,0x66,0x65);

static	final	Color	chatColor = new Color(0x99,0x33,0xFF);
static	final	Color	chatDarkColor = new Color(0x67,0x00,0x9A);
static	final	Color	chatLightColor = new Color(0xE3,0xB1,0xFF);

static	final	Color	pageColor = new Color(0x66,0xCC,0xFF);
static	final	Color	pageDarkColor = new Color(0x33,0x98,0xCC);
static	final	Color	pageLightColor = new Color(0x9C,0xFE,0xFF);

static	final	Color	langColor = new Color(0x00,0x66,0xCC);
static	final	Color	langDarkColor = new Color(0x02,0x34,0xCB);
static	final	Color	langLightColor = new Color(0x67,0xCC,0xFD);


/*****************************************************************************/
//	FONTS

static	final	Font	textfont = new Font("Arial",Font.PLAIN,11);
static	final	Font	titlefont = new Font("Verdana",Font.BOLD,12);

/*****************************************************************************/
//	WIDGET

static	final	int	MARGIN = 16;	// title bar height and scroll bar width

}
