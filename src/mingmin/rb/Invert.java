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
		int rounds = Integer.parseInt(args[0]);
		Invert i = new Invert(rounds);
		i.start();
	}

	public void start() {
		readInput();
		readTable();
		decode();
	}

	private void decode() {
		try {
			for (String input : inputs) {
				String digest = input.toLowerCase();
				
				int i;
				
				for (int round = rounds - 1; round >= 0; round--) {
					String output = digest.toLowerCase();
					for (i = round; i < rounds - 1; i++) {
						input = Rainbow.reduce(output, i);
						output = Rainbow.sha1(input);
					}
					if (hashMap.containsKey(output)) {
						//System.out.println("match found");
						String chainStart = hashMap.get(output);
						input = chainStart;
						
//						for(i = 0; i < rounds; i++){
//							String o = Rainbow.sha1(input);
//							input = Rainbow.reduce(o, i);
//							System.out.println(input + ", " + o);
//						}
//						System.out.println("");
						
						for (i = 0; i < round+1; i++) {
							output = Rainbow.sha1(input);
							if (output.equals(digest)) {
								System.out.println("word found: " + input + ": " + digest);
							}
							input = Rainbow.reduce(output, i);
						}
						output = Rainbow.sha1(input);
						if (output.equals(digest)) {
							System.out.println("word found: " + input + ": " + digest);
						}
					}
				}
			}
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
		try (BufferedReader br = new BufferedReader(new FileReader("SAMPLE_INPUT.data.txt"))) {

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				sCurrentLine = sCurrentLine.replace(" ", "").trim();
				inputs.add(sCurrentLine);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
