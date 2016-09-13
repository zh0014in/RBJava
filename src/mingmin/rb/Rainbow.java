package mingmin.rb;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class Rainbow {

	private String[] words;
	private int rounds;
	private HashMap<String, String> hashMap;
	private static int index = 0;
	public Rainbow(){
		 words = new String[16777216];
		 for(int i = 0; i < words.length; i++){
			 words[i] = padZeros( Integer.toHexString(i)); 
		 }
		 hashMap = new HashMap<String, String>();
	}
	
	public Rainbow(int rounds){
		this();
		this.rounds = rounds;
	}
	
	public static void main(String args[]){
		int rounds = Integer.parseInt(args[0]);
		Rainbow rb = new Rainbow(rounds);
		rb.buildTable();
	}
	
	private String padZeros(String input){
		String result = input;
		while(result.length() < 6){
			result = "0" + result;
		}
		return result;
	}
	
	private String getNextWord(){
		return words[index++];
	}
	
	void buildTable(){
		try {
			System.out.println(words[0]);
			System.out.println(sha1(words[0]));
			String input = getNextWord();
			for(int i = 0; i < rounds; i++){
				String output = sha1(input);
				input = reduce(output, i);
			}
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static String reduce(String input, int round){
		String result = input;
		return result.substring(round % 35, (round%35)+6);
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
