package org.vle.aid.metadata.exception;

public class UnknownException extends java.rmi.RemoteException 
{
	public UnknownException(String message, Throwable org) 
	{
		super(message,org); 		
	}

	private static final long serialVersionUID = -2814650374460798806L;
	
}
