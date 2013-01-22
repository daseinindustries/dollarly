package ly.dollar.tx;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyUtils {
	
	 public static String formatDecimal(BigDecimal b)
	 {

	      NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
	      double doublePayment = b.doubleValue();
	      String s;
	      if(doublePayment < 0)
	      {
		      s = n.format(doublePayment * -1);
		      s  = "-" + s;
	      }
	      else
	     {
		      s = n.format(doublePayment);
	      }
	     return s;
	 }

}
