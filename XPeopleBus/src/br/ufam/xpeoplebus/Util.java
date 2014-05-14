package br.ufam.xpeoplebus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.text.format.DateFormat;

@SuppressLint("DefaultLocale")
public class Util
{
//	public static String peopleBusAddress = "10.208.2.187";
//	public static String peopleBusAddress = "192.168.0.15";
//	public static final String peopleBusService = "http://10.208.2.187/peoplebus/";
//	public static final String peopleBusService = "http://192.168.0.15/peoplebus/scripts/";
	
	public static String dictionaryToString(HashMap<String, Object> args)
	{
		return dictionaryToString(args, true);
	}
	
	public static String dictionaryToString(HashMap<String, Object> args, boolean needEncode)
	{
		String argsString = "";
		
		if (args != null)
		{
			argsString += "?";
			for (Entry<String, Object> item : args.entrySet())
			{
				String value = item.getValue().toString();
				
				if (needEncode)
					value = Uri.encode(value);
				
				argsString += String.format("%s=%s&", item.getKey(), value);
			}
			argsString = argsString.substring(0, argsString.length() - 1);
		}
		
		return argsString;
	}
	
	public static String streamToString(final InputStream inputStream) throws IOException
	{
		String stringAux = "";
		
		if (inputStream != null)
		{
			final StringBuilder stbuilder = new StringBuilder();
			String line;
			
			try
			{
				final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				
				while ((line = reader.readLine()) != null)
				{
					stbuilder.append(line);
				}
				
				reader.close();
			} finally
			{
				inputStream.close();
			}
			
			stringAux = stbuilder.toString();
		}
		
		return stringAux;
	}
	
	public static String formatKmOrMeters(double distance_km)
	{
		String responseString = "";
		if (distance_km >= 1.0F)
			responseString = String.format(Locale.getDefault(), "%.1f km", distance_km);
		else
			responseString = String.format(Locale.getDefault(), "%.0f m", distance_km * 1000);
		return responseString;
	}
	
	public static String getDateTime()
	{
		Date now = new Date();  
		return (String) DateFormat.format("yyyy-MM-dd kk:mm:ss", now);
	}
	
}
