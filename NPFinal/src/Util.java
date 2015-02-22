/*License: GNU General Public License version 2*/

import java.util.Calendar;
public class Util {
    static String HEX[]={"0","1","2","3","4","5","6","7","8","9",
			 "A","B","C","D","E","F"};
    public static String byteToHex(byte[] b, int off, int len, String separator){
        if(b==null) return "NULL";
        String result="";
        for(int i=off; i<off+len; i++)
	    result = result+separator+HEX[(b[i]>>4)&0x0f]+HEX[b[i]&0x0f];
        return result;
    }
    public static String byteToHex(byte[] b, String sep){
        if(b==null) return "NULL";
        String result="";
        for(int i=0; i<b.length; i++)
	    result = result+sep+HEX[(b[i]>>4)&0x0f]+HEX[b[i]&0x0f];
        return result;
    }
    public static String byteToHex(byte[] b){
        return Util.byteToHex(b,"");
    }
    public static String getGeneralizedTime(){
        return Encoder.getGeneralizedTime(Calendar.getInstance());
    }
    public static Calendar getCalendar(String gdate) {
	Calendar date = Calendar.getInstance();
	if((gdate==null) || (gdate.length()<14)) {
	    return null;
	}
	date.set(Integer.parseInt(gdate.substring(0, 4)),
		 Integer.parseInt(gdate.substring(4, 6)),
		 Integer.parseInt(gdate.substring(6, 8)),
		 Integer.parseInt(gdate.substring(8, 10)),
		 Integer.parseInt(gdate.substring(10, 12)),
		 Integer.parseInt(gdate.substring(12, 14)));
	return date;
    }
}