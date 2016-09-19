package mingmin.rb;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Test {

	public static void main(String args[]){
//		byte[] input = {0x00,0x01,0x02};
		String input = "000102";
		try {
			
			System.out.println(Arrays.toString(Rainbow.hexStringToByteArray(input)));
			
			long l = 16777216;
			System.out.println(Long.toHexString(l));
			
			long i = 999;
			System.out.println(999/100 * 100);
			
			byte[] x = {0x00,0x56,(byte) 0xff};
			System.out.println(Rainbow.byteArrayToHexString(x));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
