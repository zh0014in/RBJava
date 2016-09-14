package mingmin.rb;

import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Rainbow {

	private String[] words;
	private int rounds;
	private HashMap<String, String> hashMap;
	private List<String> rWords;
	private int chainCount;
	private static HashMap<String, Integer> reduceWords;
	private static int index = 0;

	public Rainbow() {
		reduceWords = new HashMap<String, Integer>();
		words = new String[16777216];
		for (int i = 0; i < words.length; i++) {
			words[i] = padZeros(Integer.toHexString(i));
			reduceWords.put(words[i], 0);
		}
		hashMap = new HashMap<String, String>();
	}

	public Rainbow(int rounds) {
		this();
		this.rounds = rounds;
		this.chainCount = 20000000 / rounds;
		this.rWords = new ArrayList<String>(chainCount);
	}

	public static void main(String args[]) {
		int rounds = Integer.parseInt(args[0]);
		Rainbow rb = new Rainbow(rounds);
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
		} while (rWords.contains(result));
		return words[index++];
	}

	void buildTable() {
		try {
			String input = getNextWord();
			do {
				String chainStart = input;
				for (int i = 0; i < rounds - 1; i++) {
					String output = sha1(input);
					input = reduce(output, i);
				}
				String output = sha1(input);
				if (rWords.contains(output)) {
					//System.out.println("Collsion: " + output);
					input = getNextWord();
					if (input == null) {
						break;
					}
					continue;
				}
				rWords.add(output);
				hashMap.put(chainStart, output);
				//System.out.println(rWords.size() + " chains generated: " + output);
				if (rWords.size() >= chainCount) {
					break;
				}
			} while (true);

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
			int zeroCount = Collections.frequency(new ArrayList<Integer>(reduceWords.values()), 0);
			int totalCount = reduceWords.size();
			System.out.println(zeroCount + " / " + totalCount + " = " + zeroCount*100/totalCount + " %");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static String reduce(String input, int round) {
		String result = input;
		result = result.substring(round % 35, (round % 35) + 6);
		long resultInLong = Long.parseLong(result, 16);
		resultInLong = (resultInLong + round * round * round) % 16777216;
		result = Long.toHexString(resultInLong);
		result = padZeros(result);
//		if (reduceWords.containsKey(result)) {
//			int count = reduceWords.get(result) + 1;
//			reduceWords.put(result, count);
//		} else {
//			reduceWords.put(result, 1);
//		}
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
