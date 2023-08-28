package com.vhc.ec.contract.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	private static final String DATE_COLUMN_FORMAT = "dd/MM/yyyy"; 
	
	public static Date addDate(Date d, int day) {
		Calendar cal = Calendar.getInstance();
		
		cal.setTime(d);
		cal.add(Calendar.DAY_OF_MONTH, day);
		
		return cal.getTime();
	}
	
	public static boolean isValid(String dateStr) {
        DateFormat sdf = new SimpleDateFormat(DATE_COLUMN_FORMAT);
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

	public static LocalDate convertToLocalDateViaMilisecond(Date dateToConvert) {
		return Instant.ofEpochMilli(dateToConvert.getTime())
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}
	
}
