package mingmin.rb;

import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Rainbow {

	private String[] words;
	private int rounds;
	private HashMap<String, String> rainbowTable;
	private int chainCount;
	private static HashSet<String> uniqueWords;
	private static int index = 0;
	private static int indexReverse;

	public Rainbow() {
		words = new String[16777216];
		for (int i = 0; i < words.length; i++) {
			words[i] = padZeros(Integer.toHexString(i));
		}
		indexReverse = words.length - 1;
		rainbowTable = new HashMap<String, String>();
	}

	public Rainbow(int rounds) {
		this();
		this.rounds = rounds;
		// this.chainCount = 20000000 / rounds;
		// this.rWords = new ArrayList<String>(chainCount);
	}

	public static void main(String args[]) {
		int rounds = 1300;
		int factor = 15000000;
		Rainbow rb = new Rainbow(rounds);
		rb.chainCount = factor / rounds;
		rb.buildTable();
	}

	private static String padZeros(String input) {
		String result = input;
		while (result.length() < 6) {
			result = "0" + result;
		}
		return result;
	}

	private String getNextWord() {
		if (index >= words.length) {
			return null;
		}
		String result;
		do {
			result = words[index++];
			if(index % 100000 == 0){
				System.out.println(index + " words as head.");
			}
		} while (rainbowTable.containsKey(result));
		String nextWord = result;
		if (!uniqueWords.contains(nextWord)) {
			uniqueWords.add(nextWord);
		}
		return nextWord;
	}

	long buildTable() {
		System.out.println("building with chain count: " + chainCount);
		try {
			uniqueWords = new HashSet<String>();
			String input = getNextWord();
			int tableCount = 0;
			List<String> uniqueWordsInAChain = new ArrayList<String>(rounds);
			do {
				if (rainbowTable.containsKey(input)) {
					input = getNextWord();
					if (input == null) {
						break;
					}
				}
				String chainStart = input;
				uniqueWordsInAChain.clear();
				for (int i = 0; i < rounds - 1; i++) {
					String output = sha1(input);
					if (tableCount == 0) {
						input = reduceA(output, i);
					} else if (tableCount == 1) {
						input = reduceB(output, i);
					}
					if(!uniqueWords.contains(input))
					{
						uniqueWordsInAChain.add(input);
					}
//					if (!uniqueWords.contains(input)) {
//						uniqueWords.add(input);
//						if (uniqueWords.size() % 50000 == 0) {
//							printResult(tableCount);
//						}
//					}
				}
				if(uniqueWordsInAChain.size() <= rounds/2){
//					System.out.println("chain dropped because unique words not enough");
					continue;
				}
				//uniqueWords.addAll(uniqueWordsInAChain);
				for(String word : uniqueWordsInAChain){
					uniqueWords.add(word);
					if (uniqueWords.size() % 50000 == 0) {
						printResult(tableCount);
					}
				}
				String output = sha1(input);				
				rainbowTable.put(chainStart, output);
				if (rainbowTable.size() % chainCount  == 0) {
					tableCount++;
				}
			} while (tableCount < 2);

			// write to file
			Set set = rainbowTable.entrySet();
			Iterator i = set.iterator();
			try (PrintWriter writer = new PrintWriter("rainbowTable.txt", "UTF-8")) {
				while (i.hasNext()) {
					Map.Entry me = (Map.Entry) i.next();

					writer.print(me.getKey());
					writer.print(",");
					writer.println(me.getValue());
				}
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// printResult();
		return (long) 0.0;
	}

	void printResult(int tableCount) {
		int nonZeroCount = uniqueWords.size();
		int totalCount = words.length;
		System.out.print("Table " + tableCount + ", ");
		System.out.print(rainbowTable.size() + " chains, ");
		System.out.print(nonZeroCount + " / " + totalCount + " = " + nonZeroCount * 100 / totalCount + " %, ");
		System.out.println("valid words " + nonZeroCount + " / " + rainbowTable.size() + " / " + this.rounds + " = " + nonZeroCount * 100 / rainbowTable.size() / this.rounds + " %");
	}

	static String reduceA(String input, int round) {
		String result = input;
		result = result.substring(round % 35, (round % 35) + 6);
		long resultInLong = Long.parseLong(result, 16);
		resultInLong = Math.abs((resultInLong + round * round * round)) % 16777216;
		result = Long.toHexString(resultInLong);
		result = padZeros(result);
		if (result.length() > 6) {
			System.out.println("invalid reduce result: " + result);
		}
		return result;
	}

	static String reduceB(String input, int round) {
		String result = input;
		result = result.substring(round % 35, (round % 35) + 6);
		long resultInLong = Long.parseLong(result, 16);
		resultInLong = Math.abs((resultInLong + round * round * round) + round + 1) % 16777216;
		result = Long.toHexString(resultInLong);
		result = padZeros(result);
		if (result.length() > 6) {
			System.out.println("invalid reduce result: " + result);
		}
		return result;
	}
	
	static String reduceC(String input, int round, int adjustment){
		String result = input;
		result = result.substring(round % 35, (round % 35) + 6);
		long resultInLong = Long.parseLong(result, 16);
		resultInLong = Math.abs((resultInLong + round * round * round) + adjustment) % 16777216;
		result = Long.toHexString(resultInLong);
		result = padZeros(result);
		if (result.length() > 6) {
			System.out.println("invalid reduce result: " + result);
		}
		return result;
	}

	static String sha1(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(hexStringToByteArray(input));
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}

	static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
}
