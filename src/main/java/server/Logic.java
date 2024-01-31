package server;

import lombok.AllArgsConstructor;

import java.net.Socket;

@AllArgsConstructor
public class Logic extends Thread {
	private final Socket sock;

	@Override
	public void run() {
		super.run();
	}
}
