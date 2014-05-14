
package br.ufam.xpeoplebus.connection;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

// XMPP
// Classe de comunicação utilizando o protocolo XMPP
// Atentar para as classes públicas e métodos a serem sobreescritos (@Override)

public class XMPP
{
	
	private final String TAG = "XMPP_";
	private final int RECONNECT_INTERVAL = 5000;
	private final int MAX_ATTEMPTS_ALLOWED = Integer.MAX_VALUE;
	
	private int counterReconnectAttempts = 0;
	private String serverAddress;
	private XMPPConnection mXmppConnection;
	private MessageListener messageListener;
	
	// private MultiUserChat muc;
	
	public XMPP(String serverAddress)
	{
		this.serverAddress = serverAddress;
	}
	
	public XMPP(XMPPConnection conn)
	{
		this.serverAddress = conn.getHost();
		this.mXmppConnection = conn;
	}
	
	public boolean isConnected()
	{
		return mXmppConnection.isConnected();
	}
	
	public XMPPConnection getConnection()
	{
		return mXmppConnection;
	}
	
	// De forma assíncrona, conecta com o servidor definido utilizando login e
	// senha especificados
	public void connect(final String loginUser, final String passwordUser)
	{
		Log.d(TAG, String.format("Trying to establish a connection with %s", serverAddress));
		
		SmackConfiguration.setPacketReplyTimeout(5000);
		AsyncTask<Void, Void, Boolean> XMPPConnection = new AsyncTask<Void, Void, Boolean>()
		{
			protected void onPreExecute()
			{
				super.onPreExecute();
			}
			
			protected Boolean doInBackground(Void... params)
			{
				return internalConnect(loginUser, passwordUser);
			}
			
			protected void onPostExecute(Boolean itWorks)
			{
				super.onPostExecute(itWorks);
				
				if (!itWorks)
					attemptReconnect(loginUser, passwordUser);
				else
					counterReconnectAttempts = 0;
				
				registerChatListener();
				afterConnect();
			}
		};
		
		XMPPConnection.execute();
	}
	
	// Conecta com o servidor XMPP
	private void connectWithServer() throws XMPPException
	{
		mXmppConnection.connect();
	}
	
	// Efetua o login no servidor XMPP utilizando os params
	private void loginUser(String userName, String password)
	{
		try
		{
			if (mXmppConnection.isConnected())
			{
				mXmppConnection.login(userName, password);
			}
		} catch (XMPPException e)
		{
			if (e.getXMPPError() == null)
			{
				String errorMessage = e.getMessage();
				if (errorMessage.contains("SASL authentication failed using mechanism DIGEST-MD5"))
					onError("Nome de usuário e/ou senha inválidos. Tente novamente.");
			} else
			{
				onError(e.getXMPPError().getMessage());
			}
		}
	}
	
	// Método interno de conexão e login
	// O retorno desse método deve indicar se uma reconexão deve ser feita.
	private boolean internalConnect(String loginUser, String passwordUser)
	{
		boolean shouldReconnect = false;
		mXmppConnection = new XMPPConnection(serverAddress);
		
		try
		{
			connectWithServer();
			
			if (!mXmppConnection.isConnected())
			{
				Log.e(TAG, "Could not connect to the Xmpp server.");
			} else
			{
				loginUser(loginUser, passwordUser);
				Log.i(TAG, String.format("Connection established with %s as %s", serverAddress, loginUser));
				afterConnect();
				shouldReconnect = true;
			}
			
		} catch (final XMPPException e)
		{
			Log.e(TAG, "Could not connect to Xmpp server.\n" + e.getMessage());
		}
		
		return shouldReconnect;
	}
	
	// Método de reconexão
	private void attemptReconnect(final String loginUser, final String passwordUser)
	{
		Handler h = new Handler();
		h.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if (!mXmppConnection.isConnected() && counterReconnectAttempts <= MAX_ATTEMPTS_ALLOWED)
				{
					Log.d(TAG, String.format("Tentativa (%d) de reconexão com %s", ++counterReconnectAttempts, serverAddress));
					connect(loginUser, passwordUser);
				}
			}
		}, RECONNECT_INTERVAL);
	}
	
	// Listener para chats individuais
	private void registerChatListener()
	{
		messageListener = new MessageListener()
		{
			@Override
			public void processMessage(final Chat chat, final Message message)
			{
				if (message.getBody() != "" && message.getBody() != null)
				{
					messageIncoming(message.getFrom(), message.getBody());
				}
			}
		};
		
		ChatManagerListener chatListener = new ChatManagerListener()
		{
			@Override
			public void chatCreated(final Chat chat, final boolean createdLocally)
			{
				if (!createdLocally)
				{
					chat.addMessageListener(messageListener);
				}
			}
		};
		
		mXmppConnection.getChatManager().addChatListener(chatListener);
	}
	
	public void sendMessage(String from, String body)
	{
		ChatManager chatMrg = mXmppConnection.getChatManager();
		
		Chat chat = chatMrg.createChat(from, messageListener);
		
		Message msgObj = new Message(from, Message.Type.chat);
		msgObj.setBody(body);
		
		try
		{
			chat.sendMessage(msgObj);
		} catch (XMPPException e)
		{
			Log.e(TAG, "Message could not be sent, please try again!");
		}
	}
	
	// public void registerUser(String userName, String password, String email,
	// String fullName)
	// {
	// AccountManager mAccount = new AccountManager(mXmppConnection);
	//
	// if (mAccount.supportsAccountCreation())
	// {
	// HashMap<String, String> attributes = new HashMap<String, String>();
	// attributes.put("username", userName);
	// attributes.put("password", password);
	// attributes.put("email", email);
	// attributes.put("name", fullName);
	//
	// try
	// {
	// mAccount.createAccount(userName, password, attributes);
	// } catch (XMPPException e)
	// {
	// Log.e("Error", "XMPP - Account creating", e);
	// }
	// }
	// }
	
	// public void join()
	// {
	// muc = new MultiUserChat(mXmppConnection, "rota_1@conference.peoplebus");
	// try
	// {
	// muc.addMessageListener(new PacketListener()
	// {
	// @Override
	// public void processPacket(Packet arg0)
	// {
	// Message message = (Message) arg0;
	// if (message.getBody() != "" && message.getBody() != null)
	// addGroupMessageListener(message.getBody());
	// }
	// });
	//
	// DiscussionHistory history = new DiscussionHistory();
	// history.setMaxStanzas(0);
	//
	// if (!muc.isJoined())
	// muc.join(mXmppConnection.getUser().split("@")[0], "", history,
	// SmackConfiguration.getPacketReplyTimeout());
	// } catch (XMPPException e)
	// {
	// Log.e("XMPP", "Joining a room", e);
	// }
	// }
	
	// @Override
	// Callback para recebimento de mensagens
	// Não esquecer runOnUIThread quando houver manipulação na UI
	public void messageIncoming(String from, String message)
	{
	}
	
	// @Override
	// Callback para execução pós-conexão
	// Não esquecer runOnUIThread quando houver manipulação na UI
	public void afterConnect()
	{
	}
	
	// @Override
	// Callback para exibição de mensagens de erro, quando houver
	// Não esquecer runOnUIThread quando houver manipulação na UI
	public void onError(String message)
	{
	}
	
	// public void addGroupMessageListener(String messageBody)
	// {
	// }
	
}
