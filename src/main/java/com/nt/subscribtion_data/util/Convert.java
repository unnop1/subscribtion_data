package com.nt.subscribtion_data.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.SQLException;

public class Convert {
    public static String clobToString(Clob clob) throws IOException, SQLException {
        try (Reader reader = clob.getCharacterStream();
             StringWriter writer = new StringWriter()) {
            
            char[] buffer = new char[4096]; // Define an appropriate buffer size
            int charsRead;
            
            while ((charsRead = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, charsRead);
            }
            
            return writer.toString();
        }
    }

    public static Clob stringToClob(String data) throws IOException, SQLException {
        try{
            Clob dataClob = new javax.sql.rowset.serial.SerialClob(data.toCharArray());
            return dataClob;
        } catch (Exception clobE){
            clobE.printStackTrace();
        }
        return null;
    }
}
