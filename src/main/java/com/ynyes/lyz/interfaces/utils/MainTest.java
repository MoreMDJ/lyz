package com.ynyes.lyz.interfaces.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MainTest {
	
//	@Autowired
//	TdOrderService tdOrderService;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			String newStr = getNumberStr("TTR");
			System.err.println("test -----:"+newStr);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
	}
	
	public static String XMLEncNA(String s) {
        int ln = s.length();
        for (int i = 0; i < ln; i++) {
            char c = s.charAt(i);
            if (c == '<' || c == '>' || c == '&' || c == '"') {
                StringBuffer b =
                        new StringBuffer(s.substring(0, i));
                switch (c) {
                    case '<': b.append("&lt;"); break;
                    case '>': b.append("&gt;"); break;
                    case '&': b.append("&amp;"); break;
                    case '"': b.append("&quot;"); break;
                }
                i++;
                int next = i;
                while (i < ln) {
                    c = s.charAt(i);
                    if (c == '<' || c == '>' || c == '&' || c == '"') {
                        b.append(s.substring(next, i));
                        switch (c) {
                            case '<': b.append("&lt;"); break;
                            case '>': b.append("&gt;"); break;
                            case '&': b.append("&amp;"); break;
                            case '"': b.append("&quot;"); break;
                        }
                        next = i + 1;
                    }
                    i++;
                }
                if (next < ln) b.append(s.substring(next));
                s = b.toString();
                break;
            } // if c ==
        } // for
        return s;
    }
	public static String getNumberStr(String headStr)
	{
		if (headStr == null)
		{
			headStr = "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Date now = new Date();
		String sDate = sdf.format(now);
		Random random = new Random();
		Integer suiji = random.nextInt(900) + 100;
		String orderNum = headStr + sDate + suiji;
		return orderNum;
	}

}
