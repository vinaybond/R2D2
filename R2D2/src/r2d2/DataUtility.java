package r2d2;
/*
 * A data generation utility to generate 
 */

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class DataUtility {
	Pattern pattern1;
	Matcher matcher ;
	public static void main(String[] args) {
		DataUtility d = new DataUtility();

		System.out.println(d.getDate("hh:mm:ss", 1));
		System.out.println(d.getTime("hh:mm:ss a", 600));
		
		//System.out.println("Date: "+d.getDate("dd MMM yyyy", -15));
	}

	/*
	 * Generate Data
	 */
	public String getDate(String format, int days) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        return(dateFormat.format(cal.getTime()));
	}
	
	/*
	 * Generate Time
	 */
	public String getTime(String format, int minutes) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.HOUR, minutes);
        cal.add(Calendar.MINUTE, minutes);
        return(dateFormat.format(cal.getTime()));
	}
	

	
	/*
	 * generate random number
	 */
	public int getRandomNumber(int upperRange) {
		// to generate random number from 0 - upper range
		int n  = (int) Math.round(Math.random() * upperRange);
		return n;
	}
	
	
	/*
	 * this method generates random string/number/alfa-num string
	 */
	public String getRandomString(int length) {
		String value="";

		String alf1 = "abcdefghijklmnopqrstuvwxyz";
		value = "";
		int cnt = length;
		while (cnt != 0) {
			cnt--;
			int n = (int) Math.round(Math.random() * (alf1.length() - 1));
			value = value + alf1.substring(n, n + 1);
		}

		
		return value;
	} // end method randomData

	/* 
	 * This method gives the no. of days between two dates
	*/
	public long getDateDifference(String startDate,String endDate) throws ParseException{
		
		SimpleDateFormat myFormat = new SimpleDateFormat("dd MMM yyyy");
			
		try {
		    Date date1 = myFormat.parse(startDate);
		    Date date2 = myFormat.parse(endDate);
		    long diff = date2.getTime() - date1.getTime();
		    long Days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		    System.out.println ("Diff Days: " +Days);
		    return Days;
		} catch (ParseException e) {
		    e.printStackTrace();
		   
		}
		return 0;	
	}//end of method getDateDifference
	
	
}
