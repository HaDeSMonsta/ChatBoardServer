import app.server.Admin;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class AdminClient {
	private static final Scanner scanner = new Scanner(System.in);
	private static final String END_OF_MESSAGE = Admin.END_OF_MESSAGE;

	public static void main(String[] args) {

		System.out.println("Starting Admin client");
		System.out.print("Enter the Adminserver IP-Address, or leave blank for \"localhost\": ");
		String target = scanner.nextLine();
		target = target.isBlank() ? "localhost" : target;
		System.out.print("Enter Port: ");
		final int port = Integer.parseInt(scanner.nextLine());
		System.out.print("Enter password: ");
		final String pwd = scanner.nextLine();

		try (Socket sock = new Socket(target, port);
		     BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))) {

			writeStream(out, pwd);

			while(true) {

				final String response = readStream(in);
				System.out.println("Answer:\n" + response);
				if(response.trim().equals("Goodbye")) break;
				System.out.print("Enter next command: ");

				final String request = scanner.nextLine();
				writeStream(out, request);

			}
		} catch(IOException e) {
			System.out.println(e.getMessage());
		} catch(NullPointerException npe) {
			System.out.println("Nullpointer should never happen: " + npe.getMessage());
		}
	}

	private static String readStream(BufferedReader in) throws IOException {
		StringBuilder builder = new StringBuilder();
		String read;
		while(!(read = in.readLine()).equals(END_OF_MESSAGE)) builder.append(read).append("\n");
		return builder.toString();
	}

	private static void writeStream(BufferedWriter out, String payload) throws IOException {
		out.write(payload);
		out.newLine();
		out.write(END_OF_MESSAGE);
		out.newLine();
		out.flush();
	}

}
