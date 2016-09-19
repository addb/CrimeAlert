package exceptions;

public class BroadcastFailedException extends Exception{
	public BroadcastFailedException(){}
	public BroadcastFailedException(String message) {
		super(message);
	}
}
