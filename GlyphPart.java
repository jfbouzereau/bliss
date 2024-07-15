package	bliss;


class   GlyphPart       {

/*****************************************************************************/
//	FIELDS

int     ishape,x,y;

/*****************************************************************************/
//	CONSTRUCTOR

GlyphPart(int ishape, int x, int y)
{
this.ishape = ishape;
this.x = x;
this.y = y;
}

/*****************************************************************************/

public	String	toString()
{
return "ishape="+ishape+" x="+x+" y="+y;
}

/*****************************************************************************/

}	//	End of class	GlyphPart


