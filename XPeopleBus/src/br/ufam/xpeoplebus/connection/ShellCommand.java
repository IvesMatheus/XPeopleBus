
package br.ufam.xpeoplebus.connection;

public abstract class ShellCommand
{
	public void proccessCommand(String text)
	{
		if (isCommand(text))
		{
			Command cmd = Command.parseText(text);
			executeCommand(cmd);
		}
	}
	
	private boolean isCommand(String text)
	{
		return text.startsWith("-");
	}
	
	public abstract void executeCommand(Command cmd);
	
	public static class Command
	{
		public final static int NON_RECOGNIZED_OPERATION = -1;
		public final static int SHOW_TOAST_MESSAGE = 0;
		public final static int GET_HOUR = 1;
		
		public String operationName;
		public int operationCode;
		public Object[] parameters;
		
		public static Command parseText(String commandString)
		{
			Command newCmd = new Command();
			newCmd.operationCode = NON_RECOGNIZED_OPERATION;
			
			String[] splittedParts = commandString.split(" ");
			
			// toast Command
			if (splittedParts.length == 2 && splittedParts[0].equals("-toast"))
			{
				newCmd.operationName = "toast";
				newCmd.operationCode = SHOW_TOAST_MESSAGE;
				newCmd.parameters = new Object[1];
				
				System.arraycopy(splittedParts, 1, newCmd.parameters, 0, splittedParts.length - 1);
			}
			// hour command
			else if(splittedParts.length == 1 && splittedParts[0].equals("-hour"))
			{
				newCmd.operationName = "hour";
				newCmd.operationCode = GET_HOUR;
			}
			
			return newCmd;
		}
	}
	
}
