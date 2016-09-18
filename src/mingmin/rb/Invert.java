package mingmin.rb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Invert implements InvertEventListener {

	private HashMap<String, String> rainbowTable;
	private List<String> inputs;
	private int rounds;
	private int totalFound;
	private int totalSha1;

	public Invert() {
		rainbowTable = new HashMap<String, String>();
		inputs = new ArrayList<String>();
		totalFound = 0;
		totalSha1 = 0;
	}

	public Invert(int rounds) {
		this();
		this.rounds = rounds;
	}

	public static void main(String args[]) {
		// int rounds = Integer.parseInt(args[0]);
		Invert i = new Invert(500);
		i.start();
	}

	public void start() {
		readInput();
		readTable();
		decode();
	}

	private void decode() {
		try {
			int taskPerThread = 20;
			for (int i = 0; i < 10; i++) {
				InvertThread it = new InvertThread(inputs.subList(i * taskPerThread, i * taskPerThread + taskPerThread));
				it.setInvertEventListener(this);
				it.start();
			}
			// System.out.println("Accuracy C is " + totalFound / 5000);
			// System.out.println("Speed up factor F is " + 5000 / totalSha1 *
			// 8388608);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onInvertComplete(int totalFound, int totalSha1) {
		// TODO Auto-generated method stub
		this.totalFound += totalFound;
		this.totalSha1 += totalSha1;
		System.out.println(this.totalFound + ", " + this.totalSha1);
	}

	private void readTable() {
		try (BufferedReader br = new BufferedReader(new FileReader("rainbowTable.txt"))) {

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				String[] line = sCurrentLine.split(",");
				rainbowTable.put(line[1], line[0]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readInput() {
		try (BufferedReader br = new BufferedReader(new FileReader("SAMPLE_INPUT.data.txt"))) {

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				// String[] line = sCurrentLine.split(",");
				sCurrentLine = sCurrentLine.replace(" ", "").trim().toLowerCase();
				inputs.add(sCurrentLine);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class InvertThread extends Thread {
		private List<String> input;
		int totalFound = 0;
		int totalNotFound = 0;
		int totalSha = 0;
		InvertEventListener listener;

		public InvertThread() {
		}

		public InvertThread(List<String> input) {
			this.input = input;
		}

		public void setInvertEventListener(InvertEventListener listener) {
			this.listener = listener;
		}

		public void run() {
			long threadId = Thread.currentThread().getId();
			System.out.println("invert for " + input.size() + " records in thread "+  threadId);
			int[] adjustments = {2,3};
			for (String in : this.input) {
				try {
					String digest = in.toLowerCase();
					int i;
					boolean found = false;
					for(int j = 0; j < adjustments.length; j++){
						int adjustment = adjustments[j];
						for (int round = rounds - 1; round >= 0; round--) {
							String output = digest;
							for (i = round; i < rounds - 1; i++) {
								String input = Rainbow.reduceC(output, i, adjustment);
								output = Rainbow.sha1(input);
								totalSha++;
							}
							output = output.substring(0, 6);
							if (rainbowTable.containsKey(output)) {
								String chainStart = rainbowTable.get(output);
								String input = chainStart;
								
								for (i = 0; i < round; i++) {
									String d = Rainbow.sha1(input);
									totalSha++;
									input = Rainbow.reduceC(d, i,adjustment);
								}
								String result = Rainbow.sha1(input);
								if (result.equals(digest)) {
									System.out.println("word found: " + input + ": " + digest + ", " + ++totalFound + " in thread " + threadId);
									found = true;
									break;
								}
							}
						}
						if(found){
							break;
						}
					}
					if (!found) {
						System.out.println("word not found: " + digest + " in thread " + threadId);
						totalNotFound++;
					}
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (this.listener != null) {
				this.listener.onInvertComplete(totalFound, totalSha);
			}
		}
	}

}

interface InvertEventListener {
	void onInvertComplete(int totalFound, int totalSha1);
}
