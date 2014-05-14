
package br.ufam.xpeoplebus.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;
import br.ufam.xpeoplebus.R;
import br.ufam.xpeoplebus.Util;
import br.ufam.xpeoplebus.connection.ShellCommand;
import br.ufam.xpeoplebus.connection.XMPP;

public class MainActivity extends Activity
{
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		connectWithXMPP();
	}
	
	private void connectWithXMPP()
	{
		final XMPP xmppConnection = new XMPP("192.168.0.15")
		{
			@Override
			public void messageIncoming(final String from, final String message)
			{
				ShellCommand shell = new ShellCommand()
				{
					@Override
					public void executeCommand(final Command cmd)
					{
						switch (cmd.operationCode)
						{
							case Command.GET_HOUR :
								String hour = Util.getDateTime();
								sendMessage(from, hour);
								break;
							case Command.SHOW_TOAST_MESSAGE : 
								runOnUiThread(new Runnable()
								{
									@Override
									public void run()
									{
										Toast.makeText(getApplicationContext(), (String) cmd.parameters[0], Toast.LENGTH_SHORT).show();
									}
								});
								break;
						}
					}
				};
				
				shell.proccessCommand(message);
			}
		};
		
		xmppConnection.connect("richardaum", "123");
	}
}