package ly.dollar;

import java.math.BigDecimal;

public class Goof {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("goof");
		BigDecimal b = new BigDecimal("25.00").setScale(2);
		System.out.println(b);
	}

}
