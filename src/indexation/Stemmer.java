package indexation;

/*
 * Porter stemmer
 * */

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  * Stemmer, implementing the Porter Stemming Algorithm
  *
  * The Stemmer class transforms a word into its root form.  The input
  * word can be provided a character at time (by calling add()), or at once
  * by calling one of the various stem(something) methods.
  */

public class Stemmer extends TextRepresenter
{

	private static final long serialVersionUID = 1L;

   private ArrayList<String> stopWords;
    private char[] b;
   private int i,     /* offset into b */
               i_end, /* offset to end of stemmed word */
               j, k;
   private static final int INC = 50;
                     /* unit of size whereby b is increased */
   public Stemmer()
   {  b = new char[INC];
      i = 0;
      i_end = 0;
      stopWords= new ArrayList<String>();
      this.setStopWords();

   }

   public HashMap<String,Integer> getTextRepresentation(String text){
   	HashMap<String,Integer> h=porterStemmerHash(text);
	h.remove(" * ");
	return h;
   }
   

   /**
    * Add a character to the word being stemmed.  When you are finished
    * adding characters, you can call stem(void) to stem the word.
    */

   public void add(char ch)
   {  if (i == b.length)
      {  char[] new_b = new char[i+INC];
         for (int c = 0; c < i; c++) new_b[c] = b[c];
         b = new_b;
      }
      b[i++] = ch;
   }


   /** Adds wLen characters to the word being stemmed contained in a portion
    * of a char[] array. This is like repeated calls of add(char ch), but
    * faster.
    */

   public void add(char[] w, int wLen)
   {  if (i+wLen >= b.length)
      {  char[] new_b = new char[i+wLen+INC];
         for (int c = 0; c < i; c++) new_b[c] = b[c];
         b = new_b;
      }
      for (int c = 0; c < wLen; c++) b[i++] = w[c];
   }

   /**
    * After a word has been stemmed, it can be retrieved by toString(),
    * or a reference to the internal buffer can be retrieved by getResultBuffer
    * and getResultLength (which is generally more efficient.)
    */
   public String toString() { return new String(b,0,i_end); }

   /**
    * Returns the length of the word resulting from the stemming process.
    */
   public int getResultLength() { return i_end; }

   /**
    * Returns a reference to a character buffer containing the results of
    * the stemming process.  You also need to consult getResultLength()
    * to determine the length of the result.
    */
   public char[] getResultBuffer() { return b; }

   /* cons(i) is true <=> b[i] is a consonant. */

   private final boolean cons(int i)
   {  switch (b[i])
      {  case 'a': case 'e': case 'i': case 'o': case 'u': return false;
         case 'y': return (i==0) ? true : !cons(i-1);
         default: return true;
      }
   }

   /* m() measures the number of consonant sequences between 0 and j. if c is
      a consonant sequence and v a vowel sequence, and <..> indicates arbitrary
      presence,

         <c><v>       gives 0
         <c>vc<v>     gives 1
         <c>vcvc<v>   gives 2
         <c>vcvcvc<v> gives 3
         ....
   */

   private final int m()
   {  int n = 0;
      int i = 0;
      while(true)
      {  if (i > j) return n;
         if (! cons(i)) break; i++;
      }
      i++;
      while(true)
      {  while(true)
         {  if (i > j) return n;
               if (cons(i)) break;
               i++;
         }
         i++;
         n++;
         while(true)
         {  if (i > j) return n;
            if (! cons(i)) break;
            i++;
         }
         i++;
       }
   }

   /* vowelinstem() is true <=> 0,...j contains a vowel */

   private final boolean vowelinstem()
   {  int i; for (i = 0; i <= j; i++) if (! cons(i)) return true;
      return false;
   }

   /* doublec(j) is true <=> j,(j-1) contain a double consonant. */

   private final boolean doublec(int j)
   {  if (j < 1) return false;
      if (b[j] != b[j-1]) return false;
      return cons(j);
   }

   /* cvc(i) is true <=> i-2,i-1,i has the form consonant - vowel - consonant
      and also if the second c is not w,x or y. this is used when trying to
      restore an e at the end of a short word. e.g.

         cav(e), lov(e), hop(e), crim(e), but
         snow, box, tray.

   */

   private final boolean cvc(int i)
   {  if (i < 2 || !cons(i) || cons(i-1) || !cons(i-2)) return false;
      {  int ch = b[i];
         if (ch == 'w' || ch == 'x' || ch == 'y') return false;
      }
      return true;
   }

   private final boolean ends(String s)
   {  int l = s.length();
      int o = k-l+1;
      if (o < 0) return false;
      for (int i = 0; i < l; i++) if (b[o+i] != s.charAt(i)) return false;
      j = k-l;
      return true;
   }

   /* setto(s) sets (j+1),...k to the characters in the string s, readjusting
      k. */

   private final void setto(String s)
   {  int l = s.length();
      int o = j+1;
      for (int i = 0; i < l; i++) b[o+i] = s.charAt(i);
      k = j+l;
   }

   /* r(s) is used further down. */

   private final void r(String s) { if (m() > 0) setto(s); }

   /* step1() gets rid of plurals and -ed or -ing. e.g.

          caresses  ->  caress
          ponies    ->  poni
          ties      ->  ti
          caress    ->  caress
          cats      ->  cat

          feed      ->  feed
          agreed    ->  agree
          disabled  ->  disable

          matting   ->  mat
          mating    ->  mate
          meeting   ->  meet
          milling   ->  mill
          messing   ->  mess

          meetings  ->  meet

   */

   private final void step1()
   {  if (b[k] == 's')
      {  if (ends("sses")) k -= 2; else
         if (ends("ies")) setto("i"); else
         if (b[k-1] != 's') k--;
      }
      if (ends("eed")) { if (m() > 0) k--; } else
      if ((ends("ed") || ends("ing")) && vowelinstem())
      {  k = j;
         if (ends("at")) setto("ate"); else
         if (ends("bl")) setto("ble"); else
         if (ends("iz")) setto("ize"); else
         if (doublec(k))
         {  k--;
            {  int ch = b[k];
               if (ch == 'l' || ch == 's' || ch == 'z') k++;
            }
         }
         else if (m() == 1 && cvc(k)) setto("e");
     }
   }

   /* step2() turns terminal y to i when there is another vowel in the stem. */

   private final void step2() { if (ends("y") && vowelinstem()) b[k] = 'i'; }

   /* step3() maps double suffices to single ones. so -ization ( = -ize plus
      -ation) maps to -ize etc. note that the string before the suffix must give
      m() > 0. */

   private final void step3() { if (k == 0) return; /* For Bug 1 */ switch (b[k-1])
   {
       case 'a': if (ends("ational")) { r("ate"); break; }
                 if (ends("tional")) { r("tion"); break; }
                 break;
       case 'c': if (ends("enci")) { r("ence"); break; }
                 if (ends("anci")) { r("ance"); break; }
                 break;
       case 'e': if (ends("izer")) { r("ize"); break; }
                 break;
       case 'l': if (ends("bli")) { r("ble"); break; }
                 if (ends("alli")) { r("al"); break; }
                 if (ends("entli")) { r("ent"); break; }
                 if (ends("eli")) { r("e"); break; }
                 if (ends("ousli")) { r("ous"); break; }
                 break;
       case 'o': if (ends("ization")) { r("ize"); break; }
                 if (ends("ation")) { r("ate"); break; }
                 if (ends("ator")) { r("ate"); break; }
                 break;
       case 's': if (ends("alism")) { r("al"); break; }
                 if (ends("iveness")) { r("ive"); break; }
                 if (ends("fulness")) { r("ful"); break; }
                 if (ends("ousness")) { r("ous"); break; }
                 break;
       case 't': if (ends("aliti")) { r("al"); break; }
                 if (ends("iviti")) { r("ive"); break; }
                 if (ends("biliti")) { r("ble"); break; }
                 break;
       case 'g': if (ends("logi")) { r("log"); break; }
   } }

   /* step4() deals with -ic-, -full, -ness etc. similar strategy to step3. */

   private final void step4() { switch (b[k])
   {
       case 'e': if (ends("icate")) { r("ic"); break; }
                 if (ends("ative")) { r(""); break; }
                 if (ends("alize")) { r("al"); break; }
                 break;
       case 'i': if (ends("iciti")) { r("ic"); break; }
                 break;
       case 'l': if (ends("ical")) { r("ic"); break; }
                 if (ends("ful")) { r(""); break; }
                 break;
       case 's': if (ends("ness")) { r(""); break; }
                 break;
   } }

   /* step5() takes off -ant, -ence etc., in context <c>vcvc<v>. */

   private final void step5()
   {   if (k == 0) return; /* for Bug 1 */ switch (b[k-1])
       {  case 'a': if (ends("al")) break; return;
          case 'c': if (ends("ance")) break;
                    if (ends("ence")) break; return;
          case 'e': if (ends("er")) break; return;
          case 'i': if (ends("ic")) break; return;
          case 'l': if (ends("able")) break;
                    if (ends("ible")) break; return;
          case 'n': if (ends("ant")) break;
                    if (ends("ement")) break;
                    if (ends("ment")) break;
                    /* element etc. not stripped before the m */
                    if (ends("ent")) break; return;
          case 'o': if (ends("ion") && j >= 0 && (b[j] == 's' || b[j] == 't')) break;
                                    /* j >= 0 fixes Bug 2 */
                    if (ends("ou")) break; return;
                    /* takes care of -ous */
          case 's': if (ends("ism")) break; return;
          case 't': if (ends("ate")) break;
                    if (ends("iti")) break; return;
          case 'u': if (ends("ous")) break; return;
          case 'v': if (ends("ive")) break; return;
          case 'z': if (ends("ize")) break; return;
          default: return;
       }
       if (m() > 1) k = j;
   }

   /* step6() removes a final -e if m() > 1. */

   private final void step6()
   {  j = k;
      if (b[k] == 'e')
      {  int a = m();
         if (a > 1 || a == 1 && !cvc(k-1)) k--;
      }
      if (b[k] == 'l' && doublec(k) && m() > 1) k--;
   }

   /** Stem the word placed into the Stemmer buffer through calls to add().
    * Returns true if the stemming process resulted in a word different
    * from the input.  You can retrieve the result with
    * getResultLength()/getResultBuffer() or toString().
    */
   public void stem()
   {  k = i - 1;
      if (k > 1) { step1(); step2(); step3(); step4(); step5(); step6(); }
      i_end = k+1; i = 0;
   }
   
   
   public static String to_stems(String str){
	   Stemmer s = new Stemmer();
	   String ret="";
	   InputStream in = new ByteArrayInputStream( str.getBytes() );
	   char[] w = new char[501];
	   try
       { while(true)

         {  int ch = in.read();
            if (Character.isLetter((char) ch))
            {
               int j = 0;
               while(true)
               {  ch = Character.toLowerCase((char) ch);
                  w[j] = (char) ch;
                  if (j < 500) j++;
                  ch = in.read();
                  if (!Character.isLetter((char) ch))
                  {
                     /* to test add(char ch) */
                     for (int c = 0; c < j; c++) s.add(w[c]);

                     /* or, to test add(char[] w, int j) */
                     /* s.add(w, j); */

                     s.stem();
                     {  String u;

                        /* and now, to test toString() : */
                        u = s.toString();
                        ret+=u+" ";
                        /* to test getResultBuffer(), getResultLength() : */
                        /* u = new String(s.getResultBuffer(), 0, s.getResultLength()); */

                        //System.out.print(u);
                     }
                     break;
                  }
               }
            }
            if (ch < 0) break;
            //System.out.print((char)ch);
         }
       }
       catch (Exception e)
       {  System.out.println("error stemming "+e);
       }
	   return(ret);

   }
   


   public String porterStemmer(String s){
       Pattern p = Pattern.compile("([\\w]+)");
             Matcher m = p.matcher(s);
             String res="";
             while(m.find()){
                 String wo = m.group(1).toLowerCase();
                 if(! stopWords.contains(wo)){
                 this.add(wo.toCharArray(), wo.length());
                 this.stem();
                 res+=this.toString()+" ";}
             }
             if(res.endsWith(" ")){
                 return res.substring(0, res.length()-1);
             }
             else{
             return res.toLowerCase();}
   }

   public ArrayList<String> porterStemmerList(String s){
         ArrayList<String> res = new ArrayList<String>();
       Pattern p = Pattern.compile("([\\w]+)");
             Matcher m = p.matcher(s);
             while(m.find()){
                 String wo = m.group(1).toLowerCase();
                 if(! stopWords.contains(wo)){
                 this.add(wo.toCharArray(), wo.length());
                 this.stem();
                 res.add(this.toString().toLowerCase());}
             }
             return res;
   }



   public HashMap<String,Integer> porterStemmerHash(String s){
         HashMap<String,Integer> res = new HashMap<String,Integer>();
         res.put(" * ",0);
       Pattern p = Pattern.compile("([\\w]+)");
             Matcher m = p.matcher(s);
             while(m.find()){
                 String wo = m.group(1).toLowerCase();
                 if(! stopWords.contains(wo)){

                 this.add(wo.toCharArray(), wo.length());
                 this.stem();
                 //    System.out.println("XXX");
                 if(res.containsKey(this.toString()))res.put(this.toString(),res.get(this.toString())+1);
                else res.put(this.toString().toLowerCase(),1);
               // " * " : nbMots
                res.put(" * ",res.get(" * ")+1);}
             }
             return res;
   }


private void setStopWords(){
stopWords.add("a");
stopWords.add("able");
stopWords.add("about");
stopWords.add("above");
stopWords.add("according");
stopWords.add("accordingly");
stopWords.add("across");
stopWords.add("actually");
stopWords.add("after");
stopWords.add("afterwards");
stopWords.add("again");
stopWords.add("against");
stopWords.add("ain");
stopWords.add("all");
stopWords.add("almost");
stopWords.add("alone");
stopWords.add("along");
stopWords.add("already");
stopWords.add("also");
stopWords.add("although");
stopWords.add("always");
stopWords.add("am");
stopWords.add("among");
stopWords.add("amongst");
stopWords.add("amoungst");
stopWords.add("an");
stopWords.add("and");
stopWords.add("another");
stopWords.add("any");
stopWords.add("anybody");
stopWords.add("anyhow");
stopWords.add("anyone");
stopWords.add("anything");
stopWords.add("anyway");
stopWords.add("anyways");
stopWords.add("anywhere");
stopWords.add("ap");
stopWords.add("apart");
stopWords.add("are");
stopWords.add("aren");
stopWords.add("around");
stopWords.add("as");
stopWords.add("aside");
stopWords.add("at");
stopWords.add("available");
stopWords.add("away");
stopWords.add("awfully");
stopWords.add("b");
stopWords.add("back");
stopWords.add("be");
stopWords.add("because");
stopWords.add("been");
stopWords.add("before");
stopWords.add("beforehand");
stopWords.add("behind");
stopWords.add("being");
stopWords.add("below");
stopWords.add("beside");
stopWords.add("besides");
stopWords.add("best");
stopWords.add("better");
stopWords.add("between");
stopWords.add("beyond");
stopWords.add("both");
stopWords.add("bottom");
stopWords.add("brief");
stopWords.add("but");
stopWords.add("by");
stopWords.add("c");
stopWords.add("came");
stopWords.add("can");
stopWords.add("cannot");
stopWords.add("cant");
stopWords.add("certain");
stopWords.add("certainly");
stopWords.add("clearly");
stopWords.add("co");
stopWords.add("com");
stopWords.add("come");
stopWords.add("comes");
stopWords.add("con");
stopWords.add("concerning");
stopWords.add("consequently");
stopWords.add("could");
stopWords.add("couldn");
stopWords.add("couldnt");
stopWords.add("course");
stopWords.add("currently");
stopWords.add("d");
stopWords.add("de");
stopWords.add("definitely");
stopWords.add("despite");
stopWords.add("did");
stopWords.add("didn");
stopWords.add("different");
stopWords.add("do");
stopWords.add("does");
stopWords.add("doesn");
stopWords.add("doing");
stopWords.add("don");
stopWords.add("done");
stopWords.add("down");
stopWords.add("downwards");
stopWords.add("during");
stopWords.add("e");
stopWords.add("each");
stopWords.add("edu");
stopWords.add("eg");
stopWords.add("eight");
stopWords.add("either");
stopWords.add("else");
stopWords.add("elsewhere");
stopWords.add("empty");
stopWords.add("enough");
stopWords.add("entirely");
stopWords.add("especially");
stopWords.add("et");
stopWords.add("etc");
stopWords.add("even");
stopWords.add("ever");
stopWords.add("every");
stopWords.add("everybody");
stopWords.add("everyone");
stopWords.add("everything");
stopWords.add("everywhere");
stopWords.add("ex");
stopWords.add("exactly");
stopWords.add("except");
stopWords.add("f");
stopWords.add("far");
stopWords.add("few");
stopWords.add("fifth");
stopWords.add("first");
stopWords.add("five");
stopWords.add("for");
stopWords.add("former");
stopWords.add("formerly");
stopWords.add("forth");
stopWords.add("forty");
stopWords.add("four");
stopWords.add("from");
stopWords.add("front");
stopWords.add("full");
stopWords.add("further");
stopWords.add("furthermore");
stopWords.add("g");
stopWords.add("get");
stopWords.add("gets");
stopWords.add("getting");
stopWords.add("given");
stopWords.add("gives");
stopWords.add("go");
stopWords.add("goes");
stopWords.add("going");
stopWords.add("gone");
stopWords.add("got");
stopWords.add("gotten");
stopWords.add("greetings");
stopWords.add("gs");
stopWords.add("h");
stopWords.add("had");
stopWords.add("hadn");
stopWords.add("happens");
stopWords.add("hardly");
stopWords.add("has");
stopWords.add("hasn");
stopWords.add("hasnt");
stopWords.add("have");
stopWords.add("haven");
stopWords.add("having");
stopWords.add("he");
stopWords.add("hello");
stopWords.add("help");
stopWords.add("hence");
stopWords.add("her");
stopWords.add("here");
stopWords.add("hereafter");
stopWords.add("hereby");
stopWords.add("herein");
stopWords.add("hereupon");
stopWords.add("hers");
stopWords.add("herself");
stopWords.add("hi");
stopWords.add("him");
stopWords.add("himself");
stopWords.add("his");
stopWords.add("hither");
stopWords.add("hopefully");
stopWords.add("how");
stopWords.add("howbeit");
stopWords.add("however");
stopWords.add("hundred");
stopWords.add("i");
stopWords.add("ie");
stopWords.add("if");
stopWords.add("ignored");
stopWords.add("immediate");
stopWords.add("in");
stopWords.add("inasmuch");
stopWords.add("inc");
stopWords.add("inc.");
stopWords.add("indeed");
stopWords.add("inner");
stopWords.add("insofar");
stopWords.add("instead");
stopWords.add("interest");
stopWords.add("into");
stopWords.add("inward");
stopWords.add("is");
stopWords.add("it");
stopWords.add("its");
stopWords.add("itself");
stopWords.add("j");
stopWords.add("just");
stopWords.add("k");
stopWords.add("keep");
stopWords.add("keeps");
stopWords.add("kept");
stopWords.add("know");
stopWords.add("known");
stopWords.add("knows");
stopWords.add("l");
stopWords.add("last");
stopWords.add("lately");
stopWords.add("later");
stopWords.add("latter");
stopWords.add("latterly");
stopWords.add("least");
stopWords.add("less");
stopWords.add("lest");
stopWords.add("let");
stopWords.add("like");
stopWords.add("liked");
stopWords.add("likely");
stopWords.add("little");
stopWords.add("look");
stopWords.add("looking");
stopWords.add("looks");
stopWords.add("ltd");
stopWords.add("m");
stopWords.add("made");
stopWords.add("mainly");
stopWords.add("make");
stopWords.add("makes");
stopWords.add("many");
stopWords.add("may");
stopWords.add("maybe");
stopWords.add("me");
stopWords.add("mean");
stopWords.add("meantime");
stopWords.add("meanwhile");
stopWords.add("merely");
stopWords.add("might");
stopWords.add("mine");
stopWords.add("miss");
stopWords.add("more");
stopWords.add("moreover");
stopWords.add("most");
stopWords.add("mostly");
stopWords.add("move");
stopWords.add("mr");
stopWords.add("mrs");
stopWords.add("much");
stopWords.add("must");
stopWords.add("my");
stopWords.add("myself");
stopWords.add("n");
stopWords.add("name");
stopWords.add("namely");
stopWords.add("nd");
stopWords.add("near");
stopWords.add("nearly");
stopWords.add("necessary");
stopWords.add("need");
stopWords.add("needs");
stopWords.add("neither");
stopWords.add("never");
stopWords.add("nevertheless");
stopWords.add("new");
stopWords.add("next");
stopWords.add("nine");
stopWords.add("no");
stopWords.add("nobody");
stopWords.add("non");
stopWords.add("none");
stopWords.add("nonetheless");
stopWords.add("noone");
stopWords.add("nor");
stopWords.add("normally");
stopWords.add("not");
stopWords.add("nothing");
stopWords.add("novel");
stopWords.add("now");
stopWords.add("nowhere");
stopWords.add("o");
stopWords.add("obviously");
stopWords.add("of");
stopWords.add("off");
stopWords.add("often");
stopWords.add("oh");
stopWords.add("ok");
stopWords.add("okay");
stopWords.add("old");
stopWords.add("on");
stopWords.add("once");
stopWords.add("one");
stopWords.add("ones");
stopWords.add("only");
stopWords.add("onto");
stopWords.add("or");
stopWords.add("other");
stopWords.add("others");
stopWords.add("otherwise");
stopWords.add("ought");
stopWords.add("our");
stopWords.add("ours");
stopWords.add("ourselves");
stopWords.add("out");
stopWords.add("outside");
stopWords.add("over");
stopWords.add("overall");
stopWords.add("own");
stopWords.add("p");
stopWords.add("part");
stopWords.add("particular");
stopWords.add("particularly");
stopWords.add("per");
stopWords.add("perhaps");
stopWords.add("please");
stopWords.add("plus");
stopWords.add("possible");
stopWords.add("presumably");
stopWords.add("probably");
stopWords.add("provides");
stopWords.add("put");
stopWords.add("q");
stopWords.add("que");
stopWords.add("quite");
stopWords.add("qv");
stopWords.add("r");
stopWords.add("rather");
stopWords.add("rd");
stopWords.add("re");
stopWords.add("really");
stopWords.add("reasonably");
stopWords.add("recent");
stopWords.add("recently");
stopWords.add("regarding");
stopWords.add("regardless");
stopWords.add("regards");
stopWords.add("relatively");
stopWords.add("respectively");
stopWords.add("right");
stopWords.add("s");
stopWords.add("said");
stopWords.add("same");
stopWords.add("saw");
stopWords.add("say");
stopWords.add("saying");
stopWords.add("says");
stopWords.add("second");
stopWords.add("secondly");
stopWords.add("see");
stopWords.add("seeing");
stopWords.add("seem");
stopWords.add("seemed");
stopWords.add("seeming");
stopWords.add("seems");
stopWords.add("seen");
stopWords.add("self");
stopWords.add("selves");
stopWords.add("sensible");
stopWords.add("sent");
stopWords.add("serious");
stopWords.add("seriously");
stopWords.add("seven");
stopWords.add("several");
stopWords.add("shall");
stopWords.add("she");
stopWords.add("should");
stopWords.add("shouldn");
stopWords.add("show");
stopWords.add("side");
stopWords.add("since");
stopWords.add("sincere");
stopWords.add("six");
stopWords.add("so");
stopWords.add("some");
stopWords.add("somebody");
stopWords.add("somehow");
stopWords.add("someone");
stopWords.add("something");
stopWords.add("sometime");
stopWords.add("sometimes");
stopWords.add("somewhat");
stopWords.add("somewhere");
stopWords.add("soon");
stopWords.add("sorry");
stopWords.add("still");
stopWords.add("stop");
stopWords.add("sub");
stopWords.add("such");
stopWords.add("sup");
stopWords.add("sure");
stopWords.add("system");
stopWords.add("t");
stopWords.add("take");
stopWords.add("taken");
stopWords.add("taking");
stopWords.add("tell");
stopWords.add("tends");
stopWords.add("th");
stopWords.add("than");
stopWords.add("thank");
stopWords.add("thanks");
stopWords.add("thanx");
stopWords.add("that");
stopWords.add("thats");
stopWords.add("the");
stopWords.add("their");
stopWords.add("theirs");
stopWords.add("them");
stopWords.add("themselves");
stopWords.add("then");
stopWords.add("thencethere");
stopWords.add("there");
stopWords.add("thereafter");
stopWords.add("thereby");
stopWords.add("therefore");
stopWords.add("therein");
stopWords.add("theres");
stopWords.add("thereupon");
stopWords.add("these");
stopWords.add("they");
stopWords.add("thick");
stopWords.add("thin");
stopWords.add("think");
stopWords.add("third");
stopWords.add("thirty");
stopWords.add("this");
stopWords.add("thorough");
stopWords.add("thoroughly");
stopWords.add("those");
stopWords.add("though");
stopWords.add("three");
stopWords.add("through");
stopWords.add("throughout");
stopWords.add("thru");
stopWords.add("thus");
stopWords.add("to");
stopWords.add("together");
stopWords.add("too");
stopWords.add("took");
stopWords.add("top");
stopWords.add("toward");
stopWords.add("towards");
stopWords.add("tried");
stopWords.add("tries");
stopWords.add("truly");
stopWords.add("try");
stopWords.add("trying");
stopWords.add("twenty");
stopWords.add("twice");
stopWords.add("two");
stopWords.add("u");
stopWords.add("un");
stopWords.add("under");
stopWords.add("unfortunately");
stopWords.add("unless");
stopWords.add("unlike");
stopWords.add("unlikely");
stopWords.add("until");
stopWords.add("unto");
stopWords.add("up");
stopWords.add("upon");
stopWords.add("us");
stopWords.add("use");
stopWords.add("used");
stopWords.add("useful");
stopWords.add("uses");
stopWords.add("using");
stopWords.add("usually");
stopWords.add("uucp");
stopWords.add("v");
stopWords.add("value");
stopWords.add("various");
stopWords.add("very");
stopWords.add("vfor");
stopWords.add("via");
stopWords.add("viz");
stopWords.add("vs");
stopWords.add("w");
stopWords.add("wait");
stopWords.add("want");
stopWords.add("wants");
stopWords.add("was");
stopWords.add("wasn");
stopWords.add("way");
stopWords.add("we");
stopWords.add("welcome");
stopWords.add("well");
stopWords.add("wentwere");
stopWords.add("weren");
stopWords.add("what");
stopWords.add("whatever");
stopWords.add("when");
stopWords.add("whence");
stopWords.add("whenever");
stopWords.add("where");
stopWords.add("whereafter");
stopWords.add("whereas");
stopWords.add("whereby");
stopWords.add("wherein");
stopWords.add("whereupon");
stopWords.add("wherever");
stopWords.add("whether");
stopWords.add("which");
stopWords.add("while");
stopWords.add("whither");
stopWords.add("who");
stopWords.add("whoever");
stopWords.add("whole");
stopWords.add("whom");
stopWords.add("whomever");
stopWords.add("whose");
stopWords.add("why");
stopWords.add("will");
stopWords.add("willing");
stopWords.add("wish");
stopWords.add("with");
stopWords.add("within");
stopWords.add("without");
stopWords.add("won");
stopWords.add("wonder");
stopWords.add("would");
stopWords.add("wouldn");
stopWords.add("x");
stopWords.add("y");
stopWords.add("yes");
stopWords.add("yet");
stopWords.add("you");
stopWords.add("your");
stopWords.add("yours");
stopWords.add("yourself");
stopWords.add("yourselves");
stopWords.add("z");
stopWords.add("zero");
stopWords.add("people");
stopWords.add("tagnum");
stopWords.add("t1");
stopWords.add("t2");
stopWords.add("t3");
stopWords.add("t4");
stopWords.add("h1");
stopWords.add("h2");
stopWords.add("h3");
stopWords.add("h4");
stopWords.add("amp");
stopWords.add("lt");
stopWords.add("gt");
stopWords.add("section");
stopWords.add("cx");
   }
   
   
   /** Test program for demonstrating the Stemmer.  It reads text from a
    * a list of files, stems each word, and writes the result to standard
    * output. Note that the word stemmed is expected to be in lower case:
    * forcing lower case must be done outside the Stemmer class.
    * Usage: Stemmer file-name file-name ...
    */
   public static void main(String[] args)
   {
      char[] w = new char[501];
      Stemmer s = new Stemmer();
      for (int i = 0; i <1; i++)
      try
      {
         FileInputStream in = new FileInputStream("a.txt");

         try
         { while(true)

           {  int ch = in.read();
              if (Character.isLetter((char) ch))
              {
                 int j = 0;
                 while(true)
                 {  ch = Character.toLowerCase((char) ch);
                    w[j] = (char) ch;
                    if (j < 500) j++;
                    ch = in.read();
                    if (!Character.isLetter((char) ch))
                    {
                       /* to test add(char ch) */
                       for (int c = 0; c < j; c++) s.add(w[c]);

                       /* or, to test add(char[] w, int j) */
                       /* s.add(w, j); */

                       s.stem();
                       {  String u;

                          /* and now, to test toString() : */
                          u = s.toString();

                          /* to test getResultBuffer(), getResultLength() : */
                          /* u = new String(s.getResultBuffer(), 0, s.getResultLength()); */

                          System.out.print(u);
                       }
                       break;
                    }
                 }
              }
              if (ch < 0) break;
              System.out.print((char)ch);
           }
         }
         catch (IOException e)
         {  System.out.println("error reading " + args[i]);
            break;
         }
      }
      catch (FileNotFoundException e)
      {  System.out.println("file " + args[i] + " not found");
         break;
      }
   }
   
}



