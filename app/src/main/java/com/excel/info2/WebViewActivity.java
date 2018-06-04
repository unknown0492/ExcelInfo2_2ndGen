package com.excel.info2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.excel.configuration.ConfigurationReader;
import com.excel.excelclasslibrary.UtilNetwork;
import com.excel.excelclasslibrary.UtilShell;
import com.excel.excelclasslibrary.UtilURL;

import java.io.File;

public class WebViewActivity extends Activity {

	WebView wv_open_page;
	Context context = this;
	SharedPreferences spfs;
	ConfigurationReader cr;
	
	final static String TAG = "WebViewActivity";
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_web_view );
		
		init();
		
		String web_view_url = "info2";

		String URL = "";
		String url_params = "";

		//	URL = "http://" + Functions.getCMSIpFromTextFile() + File.separator + "appstv" + File.separator + "webservice.php?";
		//	url_params = String.format( "what_do_you_want=%s&", web_view_url );

		URL = UtilURL.getWebserviceURL() + "?";//"http://" + Functions.getCMSIpFromTextFile() + File.separator + "appstv" + File.separator + "webservice.php?";

		String language_code = UtilShell.executeShellCommandWithOp( "getprop language_code" ).trim();
		url_params = String.format( "what_do_you_want=%s&url_type=%s&language_code=%s&", "url_forward", web_view_url, language_code );

		String params_arr[] = new String[]{ "mac_address" };
		for( int i = 0 ; i < params_arr.length ; i++ ){
			
			if( params_arr[ i ].equals( "mac_address" ) ){
				url_params += "mac_address=" + UtilNetwork.getMacAddress( context ) + "&";
			}
			
		}
		url_params = url_params.substring( 0, url_params.length() - 1 );
		URL = URL + url_params;
		
		wv_open_page.loadUrl( URL );
				
		setContentView( wv_open_page );
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		wv_open_page.clearCache( true );
		
		context.deleteDatabase( "webview.db" );
	    context.deleteDatabase( "webviewCache.db" );
	    
	    File dir = getCacheDir();

        if ( dir != null && dir.isDirectory() ){
            try{
                File[] children = dir.listFiles();
                if ( children.length > 0 ){
                    for ( int i = 0; i < children.length; i++ ){
                        File[] temp = children[ i ].listFiles();
                        for ( int x = 0; x < temp.length; x++ ){
                            temp[ x ].delete();
                        }
                    }
                }
            }
            catch ( Exception e ){
                Log.e( TAG, "failed cache clean" );
            }
        }
		Log.i( TAG, "onDestroy()" );
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	private void init(){
		wv_open_page = new WebView( this );
		wv_open_page.getSettings().setJavaScriptEnabled( true );
		wv_open_page.getSettings().setAppCacheEnabled( false );

		try {
			cr = ConfigurationReader.getInstance();
		}
		catch ( Exception e ){
			Log.e( TAG, "This is first gen" );
		}
		
		final Activity activity = this;

		wv_open_page.setWebViewClient( new WebViewClient() {
        	
        	ProgressDialog p;
        	
            public void onReceivedError( WebView view, int errorCode, String description, String failingUrl ) {
            	wv_open_page.loadUrl( "file:///android_asset/maintenance/maintenance.html" );
            }

			@Override
			public void onPageStarted( WebView view, String url, Bitmap favicon ) {
				super.onPageStarted( view, url, favicon );
			}

			@Override
			public void onPageFinished( WebView view, String url ) {
				super.onPageFinished( view, url );
			}
            
        });
	}
	public boolean onKeyDown( int i, KeyEvent keyevent ){
		KeyEvent.keyCodeToString( i );
		Log.d( TAG, "key : " + i );
		if( i == keyevent.KEYCODE_BACK ) {
			finish();
			return false;
		}

		return super.onKeyDown( i, keyevent );
	}
}
