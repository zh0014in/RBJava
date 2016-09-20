package mingmin.rb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
//		Rainbow rb = new Rainbow(300);
//		rb.buildTable();
		Invert i = new Invert(300);
		i.start();
	}

	public void start() {
		readInput();
		readTable();
		decode();
	}

	private void decode() {
		try {
			int taskPerThread = 200;
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
		Path path = Paths.get("rainbowTable");
		try {
			byte[] data = Files.readAllBytes(path);
			for(int i = 0; i < data.length; i+=6){
				byte[] head = {data[i],data[i+1],data[i+2]};
				byte[] tail = {data[i+3],data[i+4],data[i+5]};
//				System.out.println(Rainbow.byteArrayToHexString(head) + "," + Rainbow.byteArrayToHexString(tail));
				rainbowTable.put(Rainbow.byteArrayToHexString(tail), Rainbow.byteArrayToHexString(head));
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void readInput() {
		try (BufferedReader br = new BufferedReader(new FileReader("SAMPLE_INPUT.data.txt"))) {

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
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
			int[] adjustments = {2,5};
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
								String input = Rainbow.reduce(output, i, adjustment);
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
									input = Rainbow.reduce(d, i,adjustment);
								}
								String result = Rainbow.sha1(input);
								if (result.equals(digest)) {
									System.out.println(input);
									totalFound++;
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
						System.out.println("0");
						totalNotFound++;
					}
				} catch (NoSuchAlgorithmException e) {
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
