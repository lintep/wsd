package nlp.normalizer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class UniqeNormalizerMaker {

	public static void main(String[] args) throws IOException {
		FileInputStream fstream = new FileInputStream("VALID_NORMALIZER.txt");
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		PrintWriter pw = new PrintWriter("UniqeNormalizer.java");
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
		while((line = br.readLine() ) != null) {
			String[] chars = line.split("\t");
			if(chars.length == 3) {
				output += "\t\t\tcase '\\t': return '" + chars[2] + "';";
				output += "\n";
				continue;
			}
		    output += "\t\t\tcase '" + (isEscape(chars[0].charAt(0)) ? "\\" : "") + chars[0] + "': return '" + (isEscape(chars[1].charAt(0)) ? "\\" : "") + chars[1] + "';";
			output += "\n";
		}
		br.close();
		output += "\t\t}\n";
		output += "\treturn ' ';\n";
		output += "\t}\n}";
		pw.write(output);
		System.out.println("UniqeNormalizer.java has just been created!!!!");
		pw.close();
	}
	
	private static boolean isEscape(char ch) {
		if(ch == '\b' || ch == '\t' || ch == '\n' || ch == '\r' || ch == '\"' || ch == '\'' || ch == '\\')
			return true;
		 return false;
	}
}
