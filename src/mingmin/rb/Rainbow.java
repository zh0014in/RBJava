package mingmin.rb;

import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Rainbow {

	private String[] words;
	private int rounds;
	private HashMap<String, String> rainbowTable;
	private int chainCount;
	private static HashSet<String> uniqueWords;
	private static int index = 0;

	public Rainbow() {
		words = new String[16777216];
		for (int i = 0; i < words.length; i++) {
			words[i] = padZeros(Integer.toHexString(i));
		}
		rainbowTable = new HashMap<String, String>();
	}

	public Rainbow(int rounds) {
		this();
		this.rounds = rounds;
		this.chainCount = 38500000 / rounds;
	}

	public static void main(String args[]) {
		int rounds = 300;
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
			if (index % 100000 == 0) {
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
		Date date = new Date();
		long start = date.getTime();
		try {
			uniqueWords = new HashSet<String>();
			String input = getNextWord();
			List<String> uniqueWordsInAChain = new ArrayList<String>(rounds);
			int successiveFailure = 0;
			int minUniqueWordSize = rounds * 19 / 20;
			int[] adjustments = { 2, 5 };
			int tableIndex = 0;
			int adjustment = adjustments[tableIndex];
			do {
				input = getNextWord();
				if (input == null) {
					break;
				}
				String chainStart = input;
				uniqueWordsInAChain.clear();
				for (int i = 0; i < rounds - 1; i++) {
					String output = sha1(input);
					input = reduce(output, i, adjustment);
					if (!uniqueWords.contains(input)) {
						uniqueWordsInAChain.add(input);
					}
				}

				if (uniqueWordsInAChain.size() <= minUniqueWordSize) {
					if (successiveFailure++ >= chainCount) {
						adjustment = adjustments[++tableIndex % adjustments.length];
						successiveFailure = 0;
						System.out.println("change of reduce function " + adjustment);
					}
					continue;
				}
				String output = sha1(input);
				rainbowTable.put(chainStart, output);

				for (String word : uniqueWordsInAChain) {
					uniqueWords.add(word);
					if (uniqueWords.size() % 50000 == 0) {
						printResult();
					}
				}
				int nonZeroCount = uniqueWords.size();
				int totalCount = words.length;
				minUniqueWordSize = (int) (rounds * (long) (totalCount - nonZeroCount) / (long) totalCount - 1);
				if (minUniqueWordSize < rounds / 10) {
					minUniqueWordSize = rounds / 10;
				}
				if (rainbowTable.size() % chainCount == 0) {
					break;
				}
			} while (true);

			date = new Date();
			long end = date.getTime();
			System.out.println("tasks ends in " + (end - start) + " ms");

			// write to file
			Set set = rainbowTable.entrySet();
			Iterator i = set.iterator();
			try (FileOutputStream os = new FileOutputStream("rainbowTable")) {
				while (i.hasNext()) {
					Map.Entry me = (Map.Entry) i.next();

					os.write((hexStringToByteArray((String) me.getKey())));
					os.write((hexStringToByteArray(me.getValue().toString().substring(0, 8))));
				}
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		// printResult();
		return (long) 0.0;
	}

	void printResult() {
		int nonZeroCount = uniqueWords.size();
		int totalCount = words.length;
		System.out.print(rainbowTable.size() + " chains, ");
		System.out.print(nonZeroCount + " / " + totalCount + " = " + nonZeroCount * 100 / totalCount + " %, ");
		System.out.println("valid words " + nonZeroCount + " / " + rainbowTable.size() + " / " + this.rounds + " = "
				+ nonZeroCount * 100 / rainbowTable.size() / this.rounds + " %");
	}

	static String reduce(String input, int round, int adjustment) {
		String result = input;
		result = result.substring((adjustment + round) % 35, ((adjustment + round) % 35) + 6);
		long resultInLong = Long.parseLong(result, 16);
		resultInLong = Math.abs(resultInLong + round * round * round * adjustment) % 16777216;
		result = Long.toHexString(resultInLong);
		result = padZeros(result);
		return result;
	}

	static String sha1(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(hexStringToByteArray(input));
		return byteArrayToHexString(result);
	}

	static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	static String byteArrayToHexString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			sb.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
}
