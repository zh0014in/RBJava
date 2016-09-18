package mingmin.rb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Invert {

	private HashMap<String, String> hashMap;
	private List<String> inputs;
	private int rounds;

	public Invert() {
		hashMap = new HashMap<String, String>();
		inputs = new ArrayList<String>();
	}

	public Invert(int rounds) {
		this();
		this.rounds = rounds;
	}

	public static void main(String args[]) {
//		int rounds = Integer.parseInt(args[0]);
		Invert i = new Invert(1000);
		i.start();
	}

	public void start() {
		readInput();
		readTable();
		decode();
	}

	private void decode() {
		try {
			int totalFound = 0;
			int totalNotFound = 0;
			int totalSha = 0;
			for (String in : inputs) {
				String digest = in.toLowerCase();
				
				int i;
//				System.out.println("start: " + digest);
				boolean found = false;
				for (int round = rounds - 1; round >= 0; round--) {
					String output = digest;
					for (i = round; i < rounds-1; i++) {
						String input = Rainbow.reduceA(output, i);
						output = Rainbow.sha1(input);
						totalSha++;
					}
					if (hashMap.containsKey(output)) {
						String chainStart = hashMap.get(output);
						String input = chainStart;
						for (i = 0; i < round; i++) {
							String d = Rainbow.sha1(input);
							totalSha++;
							input = Rainbow.reduceA(d, i);
						}
						String result = Rainbow.sha1(input);
						if (result.equals(digest)) {
//							System.out.println("word found: " + input + ": " + digest);
							totalFound++;
							found = true;
							System.out.println(totalFound);
							break;
						}
					}
				}
				if(!found){
					totalNotFound++;
				}
			}
			
			System.out.println("Accuracy C is " + totalFound/5000);
			System.out.println("Speed up factor F is " + 5000/totalSha * 8388608);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void readTable() {
		try (BufferedReader br = new BufferedReader(new FileReader("rainbowTable.txt"))) {

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				String[] line = sCurrentLine.split(",");
				hashMap.put(line[1], line[0]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readInput() {
		try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				//String[] line = sCurrentLine.split(",");
				sCurrentLine = sCurrentLine.replace(" ", "").trim().toLowerCase();
				inputs.add(sCurrentLine);
				System.out.println(sCurrentLine);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
