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
	private List<String> chainHeads;
	private int rounds;
	private HashMap<String, String> hashMap;
	private List<String> chainTails;
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
		hashMap = new HashMap<String, String>();
		chainHeads = new ArrayList<String>();
	}

	public Rainbow(int rounds) {
		this();
		this.rounds = rounds;
		// this.chainCount = 20000000 / rounds;
		// this.rWords = new ArrayList<String>(chainCount);
	}

	public static void main(String args[]) {
		int rounds = 1000;
		int factor = 15000000;
		long percentage = 0;
		Rainbow rb = new Rainbow(rounds);
		do {
			rb.chainCount = factor / rounds;
			rb.chainTails = new ArrayList<String>(rb.chainCount);
			System.out.println("building with chain count: " + rb.chainCount);
			long p = rb.buildTable();
			if (p < percentage) {
				break;
			}
			percentage = p;
			factor += 500000;
		} while (true);
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
		} while (chainHeads.contains(result));
		String nextWord = result;
		if (!uniqueWords.contains(nextWord)) {
			uniqueWords.add(nextWord);
		}
		return nextWord;
	}

	long buildTable() {
		try {
			uniqueWords = new HashSet<String>();
			String input = getNextWord();
			int tableCount = 0;
			do {
				String chainStart = input;
				if(chainHeads.contains(input)){
					System.out.println("wasted: " + input);
				}
				chainHeads.add(input);
				for (int i = 0; i < rounds - 1; i++) {
					String output = sha1(input);
					if (tableCount == 0) {
						input = reduceA(output, i);
					} else if (tableCount == 1) {
						input = reduceB(output, i);
					}
					if (!uniqueWords.contains(input)) {
						uniqueWords.add(input);
						if (uniqueWords.size() % 50000 == 0) {
							printResult(tableCount);
						}
					}
				}
				String output = sha1(input);
				if (chainHeads.contains(input)) {
//					 System.out.println("Collsion: " + input + ", " + output);
					input = getNextWord();
					if (input == null) {
						break;
					}
					continue;
				}
				chainTails.add(output);
				hashMap.put(chainStart, output);
				if (chainTails.size() >= chainCount * (tableCount+1)) {
					tableCount++;
				}
			} while (tableCount < 2);

			// write to file
			Set set = hashMap.entrySet();
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
//		printResult();
		return (long) 0.0;
	}

	void printResult(int tableCount) {
		int nonZeroCount = uniqueWords.size();
		int totalCount = words.length;
		System.out.print("Table " + tableCount + ", ");
		System.out.print(chainTails.size() + " chains, ");
		System.out.print(nonZeroCount + " / " + totalCount + " = " + nonZeroCount * 100 / totalCount + " %");
		System.out.println(" valid words take " + nonZeroCount * 100 / chainTails.size() / this.rounds + " %");
	}

	static String reduceA(String input, int round) {
		String result = input;
		result = result.substring(round % 35, (round % 35) + 6);
		long resultInLong = Long.parseLong(result, 16);
		resultInLong = (resultInLong + round * round * round) % 16777216;
		result = Long.toHexString(resultInLong);
		result = padZeros(result);
		return result;
	}

	static String reduceB(String input, int round) {
		String result = input;
		result = new StringBuilder(result).reverse().toString();
		result = result.substring(round % 35, (round % 35) + 6);
		long resultInLong = Long.parseLong(result, 16);
		resultInLong = (long) ((resultInLong + Math.sqrt(round * round * round)) % 16777216);
		result = Long.toHexString(resultInLong);
		result = padZeros(result);
		return result;
	}

	static String sha1(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(input.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}
}
