package	bliss;

public	class	BlissWord	{

/*****************************************************************************/
//	FIELDS

GlyphPart	glyph[];
Limits		limits;
String		code;
public	int		left,top;	// position on the page if any

/*****************************************************************************/
//	CONSTRUCTOR

public	BlissWord(GlyphPart glyph[])
{
this.glyph = glyph;
this.limits = Util.computeGlyphLimits(glyph,1);

// move to x=0
for(int i=0;i<glyph.length;i++)
	glyph[i].x -= limits.xmin/4;

this.code = Util.buildCode(glyph,1);

}


public	BlissWord(String code)
{
this.glyph = Util.buildGlyph(code,1);
this.code = code;
this.limits = Util.computeGlyphLimits(glyph,1);
}

/*****************************************************************************/

}	//	End of class	BlissWord
