package	bliss;

import	java.awt.*;
import	java.awt.image.*;
import	java.util.*;
import	java.text.*;

public	class	Util implements Constants {

/*****************************************************************************/

static	Image	fixImage(Image oldimage, int w, int h)
{
Image newimage = null;

// pre java 1.4 trick to force image with black transparent background

int pixels[] = new int[w*h];
PixelGrabber pg = new PixelGrabber(oldimage,0,0,w,h,pixels,0,w);
        
try     {
	pg.grabPixels();

	for(int i=0;i<pixels.length;i++)
		if(pixels[i] == 0xFF000000)
			pixels[i] = 0;

	newimage = Toolkit.getDefaultToolkit().createImage(
		new MemoryImageSource(w,h,pg.getColorModel(),
			pixels,0,w));
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

return newimage;
        
}	//	End of method	fixImage

/*****************************************************************************/

static	int compareGlyphPart(GlyphPart g1, GlyphPart g2)
{
if(g1.x < g2.x) return -1;
if(g1.x > g2.x) return 1;
if(g1.y < g2.y) return -1;
if(g1.y > g2.y) return 1;
if(g1.ishape < g2.ishape) return -1;
if(g1.ishape > g2.ishape) return 1;
return 0;
}

/*****************************************************************************/

// Build code from the glyph.
// Scale=1 means glyph unit is 32 pixels
// Scale=2 means glyph unit is 64 pixels

static String buildCode(GlyphPart g[], int scale)
{
// sort parts
int np = g.length;
for(int i=0;i<np-1;i++)
	for(int j=i+1;j<np;j++)
		if(compareGlyphPart(g[i],g[j])>0)
			{
			GlyphPart temp = g[i];
			g[i] = g[j];
			g[j] = temp;
			}


int xmin = g.length>0 ? g[0].x : 0;
int xcur = xmin;

String s= "";
for(int i=0;i<np;i++)
	{
	if(g[i].x!=xcur)
		s += (char)((g[i].x-xmin)/scale+'A');
	s += (char)(g[i].y/scale+'A');
	s += ""+g[i].ishape;
	xcur = g[i].x;
	}

return s;

}	//	End of method	buildCode

/*****************************************************************************/

static GlyphPart[] buildGlyph(String code, int scale)
{
char c[] = code.toCharArray();

// count the number of parts 
int np = 0;
int nc = c.length;
for(int i=1;i<nc;i++)
	if((c[i]>='0')&&(c[i]<='9')&&((c[i-1]<'0')||(c[i-1]>'9')))
		np++;


GlyphPart gl[] = new GlyphPart[np];
for(int i=0;i<np;i++)
	gl[i] = new GlyphPart(0,0,0);

int ip = -1;
int xcur = 0;
for(int i=0;i<nc;)
	{
	if((i<nc-1)&&(c[i+1]>='A')&&(c[i+1]<='Z'))
		{
		ip++;
		gl[ip].x = (c[i]-'A')*scale;
		gl[ip].y = (c[i+1]-'A')*scale;
		i+=2;
		}
	else 
		{
		ip++;
		gl[ip].x = xcur;
		gl[ip].y = (c[i]-'A')*scale;
		i+=1;
		}
	xcur = gl[ip].x;

	// first digit of shape number
	if(i<nc)
		{
		gl[ip].ishape = c[i]-'0';	
		i+=1;
		}

	// possibly second digit of shape number
	if((i<nc)&&(c[i]>='0')&&(c[i]<='9'))
		{
		gl[ip].ishape = gl[ip].ishape*10 + c[i]-'0';
		i+=1;
		}
	}

return gl;

}	//	End of method	buildGlyph

/*****************************************************************************/

static	GlyphPart[] copyGlyph(GlyphPart gl[])
{
GlyphPart gg[] = new GlyphPart[gl.length];

for(int i=0;i<gl.length;i++)
	gg[i] = new GlyphPart(gl[i].ishape,gl[i].x,gl[i].y);

return gg;
}	//	End of method	copyGlyph

/*****************************************************************************/

static	GlyphPart[] concatGlyph(GlyphPart g1[], GlyphPart g2[])
{
GlyphPart gg[] = new GlyphPart[g1.length+g2.length];

int j = 0;
for(int i=0;i<g1.length;i++)
	gg[j++] = new GlyphPart(g1[i].ishape,g1[i].x,g1[i].y);

for(int i=0;i<g2.length;i++)
	gg[j++] = new GlyphPart(g2[i].ishape,g2[i].x,g2[i].y);

return gg;
	
}	//	End of method	concatGlyph

/*****************************************************************************/

static	GlyphPart[] concatGlyph(GlyphPart g1[], GlyphPart g2[],
	GlyphPart g3[])
{
int n = ((g1==null)?0:g1.length) +
	((g2==null)?0:g2.length) +
	((g3==null)?0:g3.length);

GlyphPart gg[] = new GlyphPart[n];

int j = 0;

if(g1!=null)
for(int i=0;i<g1.length;i++)
	gg[j++] = new GlyphPart(g1[i].ishape,g1[i].x,g1[i].y);

if(g2!=null)
for(int i=0;i<g2.length;i++)
	gg[j++] = new GlyphPart(g2[i].ishape,g2[i].x,g2[i].y);

if(g3!=null)
for(int i=0;i<g3.length;i++)
	gg[j++] = new GlyphPart(g3[i].ishape,g3[i].x,g3[i].y);

return gg;

}	//	End of method	concatGlyph

/*****************************************************************************/

//	returns a glyph displaying the given digit

static	GlyphPart[] digitGlyph(int digit)
{
GlyphPart gg[] = new GlyphPart[1];

gg[0] = new GlyphPart(84+digit,0,12);

return gg;

}	//	End of method	digitGlyph

/*****************************************************************************/

// compute Glyph limits in pixels

static Limits  computeGlyphLimits(GlyphPart gl[],int scale)
{
Limits l = new Limits();

l.xmin = 9999;
l.xmax = -9999;
l.ymin = 9999;
l.ymax = - 9999;

for(int i=0;i<gl.length;i++)
        {
        int w = (SHAPEDIM[gl[i].ishape*2]*4+2)*scale;
        int h = (SHAPEDIM[gl[i].ishape*2+1]*4+2)*scale;
        if(gl[i].x*4<l.xmin) l.xmin = gl[i].x*4;
        if(gl[i].x*4+w>l.xmax) l.xmax = gl[i].x*4 + w;
        if(gl[i].y*4<l.ymin) l.ymin = gl[i].y*4;
        if(gl[i].y*4+h>l.ymax) l.ymax = gl[i].y*4+ h;
        }

return l;

}       //      End of method   computeGlyphLimits

/*****************************************************************************/

static	void	printGlyph(GlyphPart gl[])
{

for(int i=0;i<gl.length;i++)
	System.out.println(gl[i].ishape+" "+gl[i].x+" "+gl[i].y);
System.out.println("");

}	//	End of method	printGlyph

/*****************************************************************************/

// quick sort of index array

public	static	void	quickSort(int ind[], Comparator comparator) 
{
if(ind.length>0)
	quickSort(ind,0,ind.length-1,comparator);
}


private static void quickSort(int ind[], int lowIndex, int highIndex,
	Comparator comparator)
{
int lowToHighIndex;
int highToLowIndex;
int pivotIndex;
int pivotValue;  
int lowToHighValue;
int highToLowValue;
int parking;
int newLowIndex;
int newHighIndex;
int compareResult;

lowToHighIndex = lowIndex;
highToLowIndex = highIndex;
pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
pivotValue = ind[pivotIndex];

newLowIndex = highIndex + 1;
newHighIndex = lowIndex - 1;
while ((newHighIndex + 1) < newLowIndex) 
	{ 
	lowToHighValue = ind[lowToHighIndex];
	while ((lowToHighIndex < newLowIndex) &&
		(comparator.compare(lowToHighValue,pivotValue)<0 ))
		{
		newHighIndex = lowToHighIndex; 
		lowToHighIndex ++;
		lowToHighValue = ind[lowToHighIndex];
		}

	highToLowValue = ind[highToLowIndex];
	while ((newHighIndex <= highToLowIndex) &&
		(comparator.compare(highToLowValue,pivotValue)>0))
		{	
		newLowIndex = highToLowIndex; 
		highToLowIndex --;
		highToLowValue = ind[highToLowIndex];
		}

	if (lowToHighIndex == highToLowIndex) 
		{
		newHighIndex = lowToHighIndex; 
		}
	else if (lowToHighIndex < highToLowIndex) 
		{
		compareResult = comparator.compare(lowToHighValue,highToLowValue);
		if (compareResult >= 0) 
			{
			parking = lowToHighValue;
			ind[lowToHighIndex] = highToLowValue;
			ind[highToLowIndex] = parking;

			newLowIndex = highToLowIndex;
			newHighIndex = lowToHighIndex;

			lowToHighIndex ++;
			highToLowIndex --;
			}
		}
	}

if (lowIndex < newHighIndex)
	quickSort(ind, lowIndex, newHighIndex, comparator);
if (newLowIndex < highIndex)
	quickSort(ind, newLowIndex, highIndex, comparator);

}	//	End of method	quickSort

/*****************************************************************************/


}	//	End of class	Util
