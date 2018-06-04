
package com.excel.info2;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class Functions {

    public static String convertSingleToDouble(int i){
        String s1 = String.valueOf(i);
        String s = s1;
        if (s1.length() == 1)
        {
            s = (new StringBuilder("0")).append(s1).toString();
        }
        return s;
    }

    // Creating a Custom Dialog
    public static AlertDialog.Builder createCustomDialog( Context ct, String title, String message, View view ){
    	AlertDialog.Builder alert = new AlertDialog.Builder( ct );
    	alert.setTitle( title );
    	alert.setMessage( message );
    	alert.setCancelable( false );
    	alert.setView( view );

    	return alert;
    }


	// ----- Creating SharedPreference
	public static SharedPreferences createSharedPreference( Context ct, String name ){
		SharedPreferences spfs = ct.getSharedPreferences( name, Context.MODE_WORLD_READABLE );
		return spfs;
	}
	// ----- /Creating SharedPreference

	// ----- Editing SharedPreference
	public static void editSharedPreference( SharedPreferences spfs, String key, String value ){
		SharedPreferences.Editor spe = spfs.edit();
		spe.putString( key, value );
		spe.commit();
	}
	// ----- /Editing SharedPreference
		
	// ----- Retrieving From SharedPreference Starts Here
	public static Object getSharedPreference( SharedPreferences spfs, String name, String default_value ){
		Object value = spfs.getString( name, default_value );
		return value;
	}
	// ----- Accessing SharedPreference Ends Here
	

	public static void executeShellCommand( String command ){
		String cmd[] = command.split( " " );
		try{
			Runtime.getRuntime().exec( cmd );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}

	public static String executeShellCommandWithOP( String command ) {
		//String cmd[] = command.split( " " );
		String cmd = command;
		try {
			// Executes the command.
			Process process = Runtime.getRuntime().exec( cmd );
			// Reads stdout.
			// NOTE: You can write to stdin of the command using
			//       process.getOutputStream().
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			int read;
			char[] buffer = new char[4096];
			StringBuffer output = new StringBuffer();
			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
			}
			reader.close();

			// Waits for the command to finish.
			process.waitFor();

			return output.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

    public static String getCurrentDateTime()
    {
        Object obj = Calendar.getInstance();
        int i = ((Calendar) (obj)).get(5);
        int j = ((Calendar) (obj)).get(2);
        int k = ((Calendar) (obj)).get(1);
        int l = ((Calendar) (obj)).get(10);
        int i1 = ((Calendar) (obj)).get(12);
        int j1 = ((Calendar) (obj)).get(13);
        obj = (new StringBuilder(String.valueOf(convertSingleToDouble(i)))).append("-").append(convertSingleToDouble(j + 1)).append("-").append(convertSingleToDouble(k)).append("  ").append(convertSingleToDouble(l)).append(":").append(convertSingleToDouble(i1)).append(":").append(convertSingleToDouble(j1)).toString();
        Log.i("Functions", (new StringBuilder("getCurrentDateTime() : ")).append(((String) (obj))).toString());
        return ((String) (obj));
    }

    // create file
    public static File getFile( String dir_name, String file_name ){
    	File dir = Environment.getExternalStoragePublicDirectory( dir_name );
    	if( ! dir.exists() )
    		dir.mkdirs();

    	File file = new File( dir.getAbsolutePath() + File.separator + file_name );
    	try{
    		if( ! file.exists() )
    			file.createNewFile();
    	}
    	catch( Exception e ){
    		e.printStackTrace();
    	}

    	return file;
    }

    public static String executeShellCommandWithOp(String...strings) {
        String res = "exception occurred ";
        DataOutputStream outputStream = null;
        InputStream response = null;
        try{
            Process su = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(su.getOutputStream());
            response = su.getInputStream();

            for (String s : strings) {
                outputStream.writeBytes(s+"\n");
                outputStream.flush();
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            try {
                su.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            res = readFully(response);
        } catch (IOException e){
            e.printStackTrace();
        }
        return res;
    }
    
    public static String readFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString("UTF-8");
    }
	
	public static String getLocalIpAddressIPv4( Context ct ) {
		String address = "error";

		try{
			ConnectivityManager cm = (ConnectivityManager) ct.getSystemService( Context.CONNECTIVITY_SERVICE );
	
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if( ni == null ){
				Functions.logAndToast( ct, "functions", "ni is null" );
			}
			int network_type = -1;
			
			if( ni.isConnected() ){
				network_type = ni.getType();
			}
			
	
			if( network_type == ConnectivityManager.TYPE_WIFI ){
				WifiManager manager = (WifiManager ) ct.getSystemService( Context.WIFI_SERVICE );
				WifiInfo info = manager.getConnectionInfo();
				address = executeShellCommandWithOp( "ip addr show wlan0" ); //ip addr show wlan0 | awk '/inet / {print $2}' | cut -d/ -f 1
				String ip = trimIp( address );
				Log.i( null, "IP1 : "+ip );
				return ip;
			}
			else if( network_type == ConnectivityManager.TYPE_ETHERNET ){
				address = executeShellCommandWithOp( "ip addr show eth0" );  //ip addr show eth0 | awk '/inet / {print $2}' | cut -d/ -f 1
				String ip = trimIp( address );
				Log.i( null, "IP2 : "+ip );
				return ip;
			}
		}
		catch( Exception e ){
			// Toast.makeText( ct, String.valueOf( e.getLocalizedMessage() ), Toast.LENGTH_LONG ).show();
			Functions.logAndToast( ct, "functions", String.valueOf( e.getLocalizedMessage() ) );
		}
		return address;
    }
	
	public static String trimIp( String op ){
		//String op = executeShellCommandWithOP( "ip addr show eth0" ); //ip addr show wlan0 | awk '/inet / {print $2}' | cut -d/ -f 1
        String arr[] = op.split( "\n" );
        for( int i = 0 ; i < arr.length ; i++ ){
        	if( arr[ i ].contains( "inet" ) ){
        		String s = arr[ i ];
        		s = s.trim();
        		s = s.substring( s.indexOf( "inet" ) + 5, s.indexOf( "/" ) );
        		s = s.trim();
        		Log.i( null, "Output : "+s );
                return s;
        	}
        }
        return "error";
	}
	
	public static String trimEthMac( String op ){
		//String op = executeShellCommandWithOP( "ip addr show eth0" ); //ip addr show wlan0 | awk '/inet / {print $2}' | cut -d/ -f 1
        String arr[] = op.split( "\n" );
        for( int i = 0 ; i < arr.length ; i++ ){
        	if( arr[ i ].contains( "ether" ) ){
        		String s = arr[ i ];
        		s = s.trim();
        		s = s.substring( s.indexOf( "ether" ) + 6, s.indexOf( "ether" ) + 23 );
        		s = s.trim();
        		Log.i( null, "Output : "+s );
                return s;
        	}
        }
        return "error";
	}
	
	// Retrieve Mac Address
	public static String getMacAddress( Context ct ){
		String address = "";

		try{
		ConnectivityManager cm = (ConnectivityManager) ct.getSystemService( Context.CONNECTIVITY_SERVICE );

		NetworkInfo ni = cm.getActiveNetworkInfo();
		if( ni == null ){
			Functions.logAndToast( ct, "functions", "ni is null" );
		}
		int network_type;
		
		if( ni.isConnected() ){
			network_type = ni.getType();
		}
		else
			return address;

		if( network_type == ConnectivityManager.TYPE_WIFI ){
			WifiManager manager = (WifiManager ) ct.getSystemService( Context.WIFI_SERVICE );
			WifiInfo info = manager.getConnectionInfo();
			address = info.getMacAddress();
			Log.i( null, "wIfI mac : "+address );

		}
		else if( network_type == ConnectivityManager.TYPE_ETHERNET ){
			// File mac_bak = getFile( Environment.getExternalStorageDirectory().getAbsolutePath(), "mac.bak" );
			try{
				/*FileInputStream fis = new FileInputStream( "/mnt/sdcard/mac.bak" );
				address = "";
				int ch;
				for( int i = 0 ; ( i < 17 ) && ( ( ch = fis.read() ) != -1 ) ; i++ ){
					// ch = fis.read();
					address += (char) ch;
				}
				while( ( ch = fis.read() ) != -1 ){
					address += (char) ch;
				}
				Log.i( null, "eth mac : "+address+"----"+address.length() );*/
				
				if( address.equals( "" ) ){
					address = executeShellCommandWithOp( "ip addr show eth0" );  //ip addr show eth0 | awk '/inet / {print $2}' | cut -d/ -f 1
					address = trimEthMac( address );
					Log.i( null, "Forced eth mac retrieval : "+address );
				}
				
				// fis.close();
			}
			catch( Exception e ){
				e.printStackTrace();
			}
		}
		}
		catch( Exception e ){
			// Toast.makeText( ct, String.valueOf( e.getLocalizedMessage() ), Toast.LENGTH_LONG ).show();
			Functions.logAndToast( ct, "functions", String.valueOf( e.getLocalizedMessage() ) );
		}
		return address;
	}

 // Network Connection Detector
 	public static boolean isConnectedToInternet( Context context ){
         ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
           if ( connectivity != null ){
               NetworkInfo[] info = connectivity.getAllNetworkInfo();
               if ( info != null )
                   for ( int i = 0; i < info.length; i++ )
                       if ( info[i].getState() == NetworkInfo.State.CONNECTED ){
                           return true;
                       }
           }
           return false;
     }
 	// Network Connection Detector

 // Common Toast and Log recorder function
 	public static void logAndToast( Context context, String TAG, String message ){
 		Log.i( TAG, message );
 		// Toast.makeText( context, message, Toast.LENGTH_LONG ).show();
 	}

 // ----- Making GET/POST Request Starts Here
 	public static String makeRequestForData(String url, String request_method, String urlParameters){
 		StringBuffer response = null;
 		String resp ="";
 		
 		try{
 			URL obj = null;
 			HttpURLConnection con = null;
 			
 			if(request_method.equals("GET")){
				String encodedURL = url + "?" + urlParameters;
				//encodedURL = encodedURL.replaceAll("+", "%20");
				
				obj = new URL(encodedURL);
				con = (HttpURLConnection) obj.openConnection();
				//con.setRequestProperty("User-Agent", USER_AGENT);			
				con.setRequestMethod("GET");
				// con.setConnectTimeout(5000); //set timeout to 5 seconds
				con.setDoOutput(true);					
			}
			else{
				String encodedURL = url;//URLEncoder.encode(url, "UTF-8");
				//urlParameters     = URLEncoder.encode(urlParameters, "UTF-8");
				//urlParameters     = urlParameters.replaceAll("+", "%20");
				
				obj = new URL(encodedURL);
				con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("POST");
				// con.setConnectTimeout(5000); //set timeout to 5 seconds
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();
			}
 			
 			int responseCode = con.getResponseCode();
 			if(responseCode == 200){
 				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
 				String inputLine;
 				resp = "";
 				response = new StringBuffer();
 				char buff[] = new char[ 65535 ];
 				
 				FileOutputStream fos = new FileOutputStream( Functions.getFile( "Launcher", "temp.txt" ) );
 				
 				while ( (inputLine = in.readLine()) != null ) {
 				//while ( ( in.read( buff ) ) != -1 ) {
 					// System.out.println( "Response length before exception : "+inputLine );
 					//fos.write( String.valueOf( buff ).getBytes() );
 					//resp += String.valueOf( buff );
 					response.append(inputLine);
 				}
 				fos.close();
 				in.close();
 		 
 				//print result
 				System.out.println(response.toString());
 				
 			}
 			else{
 				throw new Exception("No Response from server.");
 			}
 			}
 			catch(Exception e){
 				Log.i(null,"Exception : " + e.toString());
 				return null;
 			}
 		Log.i("funcs", response.toString());
 		 return response.toString();
 		//return resp.toString();
 		
 	}
 	// ----- Making GET/POST Request Ends Here
 	
    /*public static String readData(String s)
    {
        String s2 = "";
        String s1 = s2;
        FileInputStream fileinputstream;
        int i;
        try
        {
            fileinputstream = new FileInputStream(s);
        }
        // Misplaced declaration of an exception variable
        catch (Exception e)
        {
            e.printStackTrace();
            return s1;
        }
        s = s2;
        s1 = s;
        i = fileinputstream.read();
        if (i != -1)
        {
            break MISSING_BLOCK_LABEL_38;
        }
        s1 = s;
        fileinputstream.close();
        return s;
        s1 = s;
        s = (new StringBuilder(String.valueOf(s))).append((char)i).toString();
        if (false)
        {
        } else
        {
            break MISSING_BLOCK_LABEL_16;
        }
    }
*/
    
    public static void saveDataToFile( File file, String data ){
		try{
			FileOutputStream fos = new FileOutputStream( file );
			fos.write( data.getBytes() );
			fos.close();
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}

    public static boolean saveFile( String directory, String file_name, String file_extension, byte[] file_data ){
		String command = "su -f mkdir /mnt/sdcard/"+directory;
		executeShellCommand( command );
		
		command = "su -f mkfile " +file_name+ "." +file_extension;
		executeShellCommand( command );
		
		try{
			File f_file = new File( "/mnt/sdcard/" + directory + File.separator + file_name + "." + file_extension );
				
			FileOutputStream fos = new FileOutputStream( f_file );
			fos.write( file_data );
			fos.close();
			return true;
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		
		return false;
		
	}

    public static String readData( String dir, String file_name ){
    	String data = "";
		try{
			FileInputStream fis = new FileInputStream( Functions.getFile( dir, file_name ) );
			int ch;
			data = "";
			while( ( ch = fis.read() ) != -1 ){
				data += ( char ) ch;
			}
		}
		catch( Exception e ){
			e.printStackTrace();
			data = "";
		}
		return data;
    }
    
    
	
	public static String getCMSIpFromTextFile(){
    	String ip = "";
		try{
			FileInputStream fis = new FileInputStream( Functions.getFile( "OTS", "ip.txt" ) );
			int ch;
			ip = "";
			while( ( ch = fis.read() ) != -1 ){
				ip += (char)ch;
			}
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		return ip;
    }
	
	public static void setAlarmForBroadcast( Context context, int millis_from_now, String action ){
    	AlarmManager am = (AlarmManager) context.getSystemService( "alarm" );
		Intent in = new Intent( action );
		PendingIntent pi = PendingIntent.getBroadcast( context, 0, in, 0 );
		am.set( 0, System.currentTimeMillis() + millis_from_now, pi );
    }
}
