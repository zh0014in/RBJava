package mingmin.rb;

import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class Generate {

	private String[] digests;

	public Generate() {

	}

	public Generate(int count) {
		digests = new String[count];
	}

	public static void main(String args[]) {
		int count = Integer.parseInt(args[0]);
		Generate g = new Generate(count);
		g.start();
	}

	private static String padZeros(String input) {
		String result = input;
		while (result.length() < 6) {
			result = "0" + result;
		}
		return result;
	}

	int nextNumber() {
		double d = Math.random();
		return (int) (d * Math.pow(2, 24));
	}

	public void start() {
		try {
			for (int i = 0; i < digests.length; i++) {
				int number = nextNumber();
				String word = Integer.toHexString(number);
				word = padZeros(word);
				System.out.print(word + ", ");
				String result = Rainbow.sha1(word);
				System.out.println(result);
				digests[i] = result;
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try (PrintWriter writer = new PrintWriter("input.txt", "UTF-8")) {
			for (int i = 0; i < digests.length; i++) {
				writer.println(digests[i]);
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
