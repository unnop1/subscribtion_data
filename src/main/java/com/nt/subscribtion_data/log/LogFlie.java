package com.nt.subscribtion_data.log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogFlie {

	public void WriteLogFile(String className, String methodName, String path, String subFolder, String orderId,String messageLog) {
		try {
			Logger logger = Logger.getLogger(className);  
			Date date = new Date() ;
			SimpleDateFormat df = new SimpleDateFormat("MMyyyy");
			String pathLog = "/data/"+path+"/"+subFolder+"/"+df.format(date);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String fileName = dateFormat.format(date)+"_"+orderId+".txt";
			File file = new File(pathLog+"/"+fileName);
			
			if(!file.exists()) {
				FileHandler fh = new FileHandler(file.getAbsolutePath(), true);  
		        
//		        SimpleFormatter formatter = new SimpleFormatter();  
		        fh.setFormatter(new Formatter() {
					@Override
					public String format(LogRecord record) {
						// TODO Auto-generated method stub
						SimpleDateFormat logTime = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
		                Calendar cal = new GregorianCalendar();
		                cal.setTimeInMillis(record.getMillis());
		                return record.getLevel()+" "+logTime.format(cal.getTime())+" "+className+"."+methodName+"() : "+record.getMessage() + "\n";
					}
				});  
		        logger.addHandler(fh);
			}
			
	        logger.info(messageLog);  
	        //remove the console handler, use
	        //logger.setUseParentHandlers(false);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
