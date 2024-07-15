package	bliss;

import	java.io.*;
import	java.net.*;
import	java.util.*;

public	class	Dict	{

/*****************************************************************************

Words are saved in the dictionary with the name and the glyph.
Glyph coordinates are in 1/8 of unit.

******************************************************************************/


/*****************************************************************************/
//	GLOBAL VARIABLES


static	Lang langs[] = null;
static	Word words[] = null;

static	boolean	dmodified = false;	// current dictionary has been modified
static	boolean	lmodified = false;	// languages have been changed

static	int ilang = -1;		// current language

/*****************************************************************************/
//	CONSTRUCTOR

private Dict()
{
}

/*****************************************************************************/

static	void	init(Object requester)
{

// load languages from the blisslang.txt resource
loadLanguages(requester);

// current locale
String locale = java.util.Locale.getDefault().toString();

// choose the language corresponding to the current locale
ilang = -1;
for(int i=0;i<langs.length;i++)
	if(langs[i].locale.equals(locale))
		{
		ilang = i;
		break;
		}

// otherwise choose english
if(ilang<0)
for(int i=0;i<langs.length;i++)
	if(langs[i].locale.equals("en_UK"))
		{
		ilang = i;
		break;
		}

// otherwise choose first one !
if(ilang<0)
	ilang = 0;

if(ilang>langs.length)
	{
	System.out.println("No dictionary found");
	System.exit(1);
	}

// load corresponding dictionary
words = loadWords(requester,langs[ilang].name,langs[ilang].locale);

if(words==null)
	{
	System.out.println("No dictionary found");
	System.exit(1);
	}

}	//	End of method	init

/*****************************************************************************/

static	void	loadLanguages(Object requester)
{
File f;
BufferedReader r;
String filename = "blisslang.txt";

// local file
try	{
	f = new File(filename);
	if(f.exists())
		{
		r = new BufferedReader(new InputStreamReader(
			new FileInputStream(f),"UTF-8"));
		loadLanguages(r);
		return;
		}

	}
catch(Exception ex) {}


// file in home directory
try	{
	f = new File(System.getProperty("user.home"),filename);
	if(f.exists())
		{
		r = new BufferedReader(new InputStreamReader(
			new FileInputStream(f),"UTF-8"));
		loadLanguages(r);
		return;
		}
	}
catch(Exception ex) {}


// built-in version
try	{
	URL url = requester.getClass().getResource(filename);
	r = new BufferedReader(new InputStreamReader(url.openStream(),"UTF-8"));
	loadLanguages(r);
	return;
	}
catch(Exception ex)
	{
	}

System.out.println("Cannot load language list");
System.exit(1);

}	//	End of method	loadLanguages

/*****************************************************************************/

static	void	loadLanguages(BufferedReader r)
{
Vector v = new Vector();
String line;
StringTokenizer tk;

try	{
	while(true)
		{
		line = r.readLine();
		if(line==null) break;
	
		tk = new StringTokenizer(line,"\t");
		if(tk.countTokens()<2) continue;

		Lang lang = new Lang(tk.nextToken(),tk.nextToken());
		v.addElement(lang);
		}
	r.close();
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

langs = new Lang[v.size()];
for(int i=0;i<langs.length;i++)
	langs[i] = (Lang)v.elementAt(i);

// sort language names
for(int i=1;i<langs.length;i++)
	for(int j=0;j<i;j++)
		if(langs[i].name.compareTo(langs[j].name)<0)
			{
			Lang temp = langs[i];
			langs[i] = langs[j];
			langs[j] = temp;
			}

}	//	End of method	loadLanguages

/*****************************************************************************/

static	void	setLanguage(Object requester, int index)
{
if((index<0)||(index>=langs.length)) return;

ilang = index;

words = loadWords(requester,langs[ilang].name,langs[ilang].locale);

if(words==null)
	{
	System.out.println("No dictionary found");
	System.exit(1);
	}

}	//	End of method	setLanguage

/*****************************************************************************/

static	void	addLanguage(String name, String locale)
{

int nl = langs.length;

Lang newlangs[]  = new Lang[nl+1];
for(int i=0;i<nl;i++)
	newlangs[i] = langs[i];

newlangs[nl] = new Lang(name,locale);

langs = newlangs;

lmodified = true;

}	//	End of method	addLanguage

/*****************************************************************************/

static	void	saveLanguages()
{

if(!lmodified) return;

String eol = System.getProperty("line.separator");

String filename = "blisslang.txt";

System.out.println("Saving "+langs.length+" languages into "+ filename);

try	{
	OutputStreamWriter w = new OutputStreamWriter(
		new FileOutputStream(filename),"UTF-8");
	for(int i=0;i<langs.length;i++)
		{
		String line = langs[i].name+"\t"+langs[i].locale+eol;
		w.write(line,0,line.length());
		}
	w.close();
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

}	//	End of method	saveLanguages

/*****************************************************************************/

static	Word[]	loadWords(Object requester, String name, String locale)
{
File f;
BufferedReader r;

String filename = "blissdict-"+locale+".txt";

// local file
try	{
	f = new File(filename);
	if(f.exists())
		{
		r = new BufferedReader(new InputStreamReader(
			new FileInputStream(f),"UTF-8"));
		return loadWords(r);
		}

	}
catch(Exception ex) {}


// file in home directory
try	{
	f = new File(System.getProperty("user.home"),filename);
	if(f.exists())
		{
		r = new BufferedReader(new InputStreamReader(
			new FileInputStream(f),"UTF-8"));
		return loadWords(r);
		}
	}
catch(Exception ex) {}


// built-in version
try	{
	URL url = requester.getClass().getResource(filename);
	r = new BufferedReader(new InputStreamReader(url.openStream()
		,"UTF-8"));
	return loadWords(r);
	}
catch(Exception ex)
	{
	}

return null;

}	//	End of method	loadWords

/****************************************************************************/

static Word[]	loadWords(BufferedReader r)
{
String line;
StringTokenizer tk;
Vector v = new Vector();

try	{
	while(true)
		{
		line = r.readLine();
		if(line==null) break;

		tk = new StringTokenizer(line,"\t");
		if(tk.countTokens()<2) continue;

		String name = tk.nextToken();
		String code = tk.nextToken();
		
		Word w = new Word(name,code);
		v.addElement(w);		
		}
	r.close();
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}


Word ww[] = new Word[v.size()];
for(int i=0;i<v.size();i++)
	ww[i] = (Word)v.elementAt(i);

dmodified = false;

return ww;

}	//	End of method	loadWords

/*****************************************************************************/

static	int	addWord(String name, String code)
{

// check if word already exists
int k = -1;
for(int i=0;i<words.length;i++)
	if(words[i].name.equals(name))
		k = i;

if(k<0)
for(int i=0;i<words.length;i++)
	if(words[i].code.equals(code))
		k = i;

if(k>=0)
	{
	words[k].name = name;
	words[k].code = code;
	}
else
	{
	Word ww[] = new Word[words.length+1];

	for(int i=0;i<words.length;i++)
		ww[i] = words[i];

	k = words.length;
	ww[k] = new Word(name,code);

	// swap new and old arrays
	words = ww;
	}

dmodified = true;	// dictionary must be saved

// index of added word
return k;

}	//	End of method	addWord

/*****************************************************************************/

static	void	saveWords()
{
if(!dmodified) return;

saveWords(words,langs[ilang].name,langs[ilang].locale);

}	//	End of method	saveWords

/*****************************************************************************/

static	void	saveWords(Word ww[], String language, String locale)
{

String eol = System.getProperty("line.separator");

String filename = "blissdict-"+locale+".txt";

System.out.println("Saving "+ww.length+" words into "+ filename);

try	{
	OutputStreamWriter w = new OutputStreamWriter(
		new FileOutputStream(filename),"UTF-8");
	for(int i=0;i<ww.length;i++)
		{
		String line = ww[i].name+"\t"+ww[i].code+eol;
		w.write(line,0,line.length());
		}
	w.close();
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}


// check if this language must be added to the list
int k = -1;
for(int i=0;i<langs.length;i++)
	if(langs[i].locale.equals(locale))
		k = i;

if(k<0)
	addLanguage(language,locale);

}	//	End of method	saveWords

/*****************************************************************************/

public	static int 	getWordWithCode(String code)
{


for(int i=0;i<words.length;i++)
	if(words[i].code.equals(code))
		return i;

return -1;

}	//	End of method	getWordWithCode

/*****************************************************************************/

}	//	End of class	Dict
