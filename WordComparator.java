package	bliss;

import	java.text.*;

public	class   WordComparator  extends Comparator      {

/*****************************************************************************/
//	FIELDS

Collator collator;

/*****************************************************************************/
//	CONSTRUCTOR

public	WordComparator(Collator collator)
{
this.collator = collator;
}

/*****************************************************************************/

public int compare(int i1, int i2)
{
return collator.compare(Dict.words[i1].name,Dict.words[i2].name);
}

/*****************************************************************************/

public	int compare(int i1, int i2, int len)
{
int l1 = Dict.words[i1].name.length();
int l2 = Dict.words[i2].name.length();
if(l1<len) len = l1;
if(l2<len) len = l2;
return collator.compare(
	Dict.words[i1].name.substring(0,len),
	Dict.words[i2].name.substring(0,len));
}

/*****************************************************************************/

public int compare(String s1, String s2)
{
return collator.compare(s1,s2);
}

/*****************************************************************************/

public int comparePrefix(int i1, String prefix)
{
int l = Dict.words[i1].name.length();
if(l>prefix.length())
	return collator.compare(
		Dict.words[i1].name.substring(0,prefix.length()),
		prefix);
else 
	{
	int k = collator.compare(
		Dict.words[i1].name,
		prefix.substring(Dict.words[i1].name.length()));
	if(k!=0)
		return k;	// word is different from beginning of prefix
	else
		return -1;	// word is shorter than prefix
	}

}

/*****************************************************************************/

}	//	End of class	WordComparator


