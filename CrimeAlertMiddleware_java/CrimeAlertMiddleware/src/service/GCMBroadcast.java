package service;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;

import org.bson.Document;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;

import exceptions.BroadcastFailedException;

/**
 * Servlet implementation class GCMBroadcast
 */
public class GCMBroadcast extends HttpServlet {
	public String connect(List<String> deviceIds, Document crimeEvent) throws BroadcastFailedException, IOException{
		List<String> androidTargets =  deviceIds;
		Sender sender = new Sender(Constants.SENDER_ID);
		Message message = new Message.Builder()
		.timeToLive(1000).delayWhileIdle(true)
		.addData("message",crimeEvent.toJson())
		.build();

		try {
			MulticastResult result = sender.send(message, androidTargets, 0); // message, regid, retries

			if (result.getResults() != null) {
				System.out.println("sent broadcast");
			} else {
				int error = result.getFailure();
				throw new BroadcastFailedException("Broadcast failed");
			}

		} catch (BroadcastFailedException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}		
		return Constants.SUCCESS;
	}
}