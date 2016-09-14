package mingmin.rb;

import java.security.NoSuchAlgorithmException;

public class Test {

	public static void main(String args[]){
		String input = "0c5fc0";
		try {
			for(int i = 0; i < 499;i++){
				String o = Rainbow.sha1(input);
				System.out.println(o);
				input = Rainbow.reduce(o, i);
			}
			System.out.println(Rainbow.sha1(input));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
