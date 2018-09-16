package nlp.normalizer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class TermSplitterUniqeNormalizerMaker {

	public static void main(String[] args) throws IOException {
		FileInputStream fstream = new FileInputStream("/home/rahmani/Data/charDist/from hdfs/93-08-07-559m_undup_93-08-22_result/titleContent.termSplitter.lowerThan200000TTF.code");
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		PrintWriter pw = new PrintWriter("TermSplitterUniqeNormalizer.java");
		String line;
		String output = "public class UniqeNormalizer { \n\n";
		output += "\tpublic static String getNormalizedString(String input)\n";
		output += "\t{\n";
		output += "\t\tStringBuilder sb = new StringBuilder(input.length());\n";
		output += "\t\tfor (int i = 0 ; i < input.length(); i ++) {\n";
		output += "\t\t\tchar ch = input.charAt(i);\n";
		output += "\t\t\tsb.append(getNormalizedChar(ch));\n";
		output += "\t\t}\n";
		output += "\t\treturn sb.toString();\n";
		output += "\t}\n\n";
		output += "\tpublic static char getNormalizedChar(char ch) {\n";
		output += "\t\tswitch(ch) {\n";
		int charCode;
		while((line = br.readLine() ) != null) {
			charCode=Integer.parseInt(line);
		    output += "\t\t\tcase " + charCode +": return ' ';";
			output += "\n";
		}
		br.close();
		output += "\t\t}\n";
		output += "\treturn ' ';\n";
		output += "\t}\n}";
		pw.write(output);
		System.out.println("TermSplitterUniqeNormalizer.java has just been created!!!!");
		pw.close();
	}
	
	private static boolean isEscape(char ch) {
		if(ch == '\b' || ch == '\t' || ch == '\n' || ch == '\r' || ch == '\"' || ch == '\'' || ch == '\\')
			return true;
		 return false;
	}
}
