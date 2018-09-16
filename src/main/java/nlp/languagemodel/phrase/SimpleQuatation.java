package nlp.languagemodel.phrase;

import nlp.languagemodel.BaseSimpleLanguageModel;
import tools.util.file.BufferedIterator;

import java.io.PrintWriter;
import java.io.Reader;

public class SimpleQuatation {
	
	static double divSmoothValue=0.000001;
	
	static String nameExtensionCount=".normalizeBigramWithCount";
	public static void normalizeBigramWithCount(String bigramDfFileAddress) throws NumberFormatException, Exception{		
		BaseSimpleLanguageModel languageModel= BaseSimpleLanguageModel.getLanguageModelFromBigram(bigramDfFileAddress);
		String resultFileAddress = bigramDfFileAddress+nameExtensionCount;
		PrintWriter printWriter = tools.util.file.Write.getPrintWriter(resultFileAddress, false);
		Reader reader=tools.util.file.Reader.getFileBufferReader(bigramDfFileAddress);
		tools.util.file.BufferedIterator bufferedIterator=new BufferedIterator(reader);
		while(bufferedIterator.hasNext()){
			String newLine = bufferedIterator.next();
			if(newLine.length()<=0)
				break;
			String[] splite1 = newLine.split("\t");
			String[] tokens = splite1[0].split(" ");
			double ttf=Double.valueOf(splite1[1]);
			double div1=languageModel.getUnigramTTF(tokens[1]);
			double div2=languageModel.getUnigramTTF(tokens[0]);
			double div=div1+div2;
			if(div<2)
				System.out.println(div+"\t"+newLine);
			printWriter.println(splite1[0]+"\t"+ttf/div);
		}
		reader.close();
		bufferedIterator.close();
		printWriter.close();
		System.out.println("Operation complete, result file address: "+resultFileAddress);
	}
	
	static String nameExtensionCountMinesSelf=".normalizeBigramWithCountMinesSelf";
	public static void normalizeBigramWithCountMinesSelf(String bigramDfFileAddress) throws NumberFormatException, Exception{		
		BaseSimpleLanguageModel languageModel= BaseSimpleLanguageModel.getLanguageModelFromBigram(bigramDfFileAddress);
		String resultFileAddress = bigramDfFileAddress+nameExtensionCountMinesSelf;
		PrintWriter printWriter = tools.util.file.Write.getPrintWriter(resultFileAddress, false);
		Reader reader=tools.util.file.Reader.getFileBufferReader(bigramDfFileAddress);
		tools.util.file.BufferedIterator bufferedIterator=new BufferedIterator(reader);
		while(bufferedIterator.hasNext()){
			String newLine = bufferedIterator.next();
			if(newLine.length()<=0)
				break;
			String[] splite1 = newLine.split("\t");
			String[] tokens = splite1[0].split(" ");
			double ttf=Double.valueOf(splite1[1]);
			double div1=languageModel.getUnigramTTF(tokens[1]);
			double div2=languageModel.getUnigramTTF(tokens[0]);
			double div=div1+div2-ttf+divSmoothValue;
			if(div<2)
				System.out.println(div+"\t"+newLine);
			printWriter.println(splite1[0]+"\t"+ttf/div);
		}
		reader.close();
		bufferedIterator.close();
		printWriter.close();
		System.out.println("Operation complete, result file address: "+resultFileAddress);
	}
	
	
	static String nameExtensionPrefixPostfixCount=".normalizeBigramWithPrefixPostfixCount";
	public static void normalizeBigramWithPrefixPostfixCount(String bigramDfFileAddress) throws NumberFormatException, Exception{		
		BaseSimpleLanguageModel languageModel= BaseSimpleLanguageModel.getLanguageModelFromBigram(bigramDfFileAddress);
		String resultFileAddress = bigramDfFileAddress+nameExtensionPrefixPostfixCount;
		PrintWriter printWriter = tools.util.file.Write.getPrintWriter(resultFileAddress, false);
		Reader reader=tools.util.file.Reader.getFileBufferReader(bigramDfFileAddress);
		tools.util.file.BufferedIterator bufferedIterator=new BufferedIterator(reader);
		while(bufferedIterator.hasNext()){
			String newLine = bufferedIterator.next();
			if(newLine.length()<=0)
				break;
			String[] splite1 = newLine.split("\t");
			String[] tokens = splite1[0].split(" ");
			double ttf=Double.valueOf(splite1[1]);
			double div1=languageModel.getBigramPrefixCount(tokens[1]);
			double div2=languageModel.getBigramPostfixCount(tokens[0]);
			double div=div1+div2;
			if(div<2)
				System.out.println(div+"\t"+newLine);
			printWriter.println(splite1[0]+"\t"+ttf/div);
		}
		reader.close();
		bufferedIterator.close();
		printWriter.close();
		System.out.println("Operation complete, result file address: "+resultFileAddress);
	}
	
	static String nameExtensionPrefixPostfixCountMinesSelf=".normalizeBigramWithPrefixPostfixCountMinesSelf";
	public static void normalizeBigramWithPrefixPostfixCountMinesSelf(String bigramDfFileAddress) throws NumberFormatException, Exception{		
		BaseSimpleLanguageModel languageModel= BaseSimpleLanguageModel.getLanguageModelFromBigram(bigramDfFileAddress);
		String resultFileAddress = bigramDfFileAddress+nameExtensionPrefixPostfixCountMinesSelf;
		PrintWriter printWriter = tools.util.file.Write.getPrintWriter(resultFileAddress, false);
		Reader reader=tools.util.file.Reader.getFileBufferReader(bigramDfFileAddress);
		tools.util.file.BufferedIterator bufferedIterator=new BufferedIterator(reader);
		while(bufferedIterator.hasNext()){
			String newLine = bufferedIterator.next();
			if(newLine.length()<=0)
				break;
			String[] splite1 = newLine.split("\t");
			String[] tokens = splite1[0].split(" ");
			double ttf=Double.valueOf(splite1[1]);
			double div1=languageModel.getBigramPrefixCount(tokens[1]);
			double div2=languageModel.getBigramPostfixCount(tokens[0]);
			double div=div1+div2-ttf+divSmoothValue;
			if(div<2)
				System.out.println(div+"\t"+newLine);
			printWriter.println(splite1[0]+"\t"+ttf/div);
		}
		reader.close();
		bufferedIterator.close();
		printWriter.close();
		System.out.println("Operation complete, result file address: "+resultFileAddress);
	}
	
	static String nameExtensionPrefixPostfixCountMulTTF=nameExtensionPrefixPostfixCount+".MulTTF";
	public static void normalizeBigramWithPrefixPostfixCountMulTTF(String bigramDfFileAddress) throws NumberFormatException, Exception{		
		BaseSimpleLanguageModel languageModel= BaseSimpleLanguageModel.getLanguageModelFromBigram(bigramDfFileAddress);
		String resultFileAddress = bigramDfFileAddress+nameExtensionPrefixPostfixCountMulTTF;
		PrintWriter printWriter = tools.util.file.Write.getPrintWriter(resultFileAddress, false);
		Reader reader=tools.util.file.Reader.getFileBufferReader(bigramDfFileAddress);
		tools.util.file.BufferedIterator bufferedIterator=new BufferedIterator(reader);
		while(bufferedIterator.hasNext()){
			String newLine = bufferedIterator.next();
			if(newLine.length()<=0)
				break;
			String[] splite1 = newLine.split("\t");
			String[] tokens = splite1[0].split(" ");
			double ttf=Double.valueOf(splite1[1]);
			double div1=languageModel.getBigramPrefixCount(tokens[1]);
			double div2=languageModel.getBigramPostfixCount(tokens[0]);
			double div=div1+div2;
			if(div<2)
				System.out.println(div+"\t"+newLine);
			printWriter.println(splite1[0]+"\t"+ttf*(ttf/div));
		}
		reader.close();
		bufferedIterator.close();
		printWriter.close();
		System.out.println("Operation complete, result file address: "+resultFileAddress);
	}
	
	static String nameExtensionPrefixPostfixCountMulTTFMinesSelf=nameExtensionPrefixPostfixCountMinesSelf+".MulTTF";
	public static void normalizeBigramWithPrefixPostfixCountMinesSelfMulTTF(String bigramDfFileAddress) throws NumberFormatException, Exception{		
		BaseSimpleLanguageModel languageModel= BaseSimpleLanguageModel.getLanguageModelFromBigram(bigramDfFileAddress);
		String resultFileAddress = bigramDfFileAddress+nameExtensionPrefixPostfixCountMulTTFMinesSelf;
		PrintWriter printWriter = tools.util.file.Write.getPrintWriter(resultFileAddress, false);
		Reader reader=tools.util.file.Reader.getFileBufferReader(bigramDfFileAddress);
		tools.util.file.BufferedIterator bufferedIterator=new BufferedIterator(reader);
		while(bufferedIterator.hasNext()){
			String newLine = bufferedIterator.next();
			if(newLine.length()<=0)
				break;
			String[] splite1 = newLine.split("\t");
			String[] tokens = splite1[0].split(" ");
			double ttf=Double.valueOf(splite1[1]);
			double div1=languageModel.getBigramPrefixCount(tokens[1]);
			double div2=languageModel.getBigramPostfixCount(tokens[0]);
			double div=div1+div2-ttf+divSmoothValue;
			if(div<2)
				System.out.println(div+"\t"+newLine);
			printWriter.println(splite1[0]+"\t"+ttf*(ttf/div));
		}
		reader.close();
		bufferedIterator.close();
		printWriter.close();
		System.out.println("Operation complete, result file address: "+resultFileAddress);
	}
	
}
