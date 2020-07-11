import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayDeque;

public class ClientUDP implements Runnable {

	private DatagramSocket socket;	// Socket à travers laquelle on va envoyer nos paquet UDP
	private boolean running;	// Boolean qui sert à stopper le thread
	private ArrayDeque<Message> file;	//File de MESSAGE qu'on stocke
	private int portUDP;	// portUDP du Client
	private static int tailleBuff = 1024;
	
	public ClientUDP(int portUDP) throws SocketException {
		this.setPortUDP(portUDP);
		bind(portUDP);
		this.file = new ArrayDeque<Message>();
	}

	public void bind(int port) throws SocketException {
		socket = new DatagramSocket(port);
	}

	public void start() {
		Thread thread = new Thread(this);
		thread.start();
	}

	public void stop() {
		running = false;
		socket.close();
	}

	@Override
	public void run() {
		byte[] buffer = new byte[tailleBuff];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		running = true;
		while(running) {
			try {
				socket.receive(packet);

				String msg = new String(buffer, 0, packet.getLength());
//				System.out.println("DEBUG - " + msg);
				String[] message = msg.split(";");
				file.add(new Message(message[1], Integer.parseInt(message[2]), message[3],message[4]));
							} 
			catch (IOException e) {
				break;
			}
		}
	}

	public void sendTo(Adresse adresse, String msg) throws IOException {
		byte[] buffer = msg.getBytes();
		InetSocketAddress address = new InetSocketAddress(adresse.getAdresse(), adresse.getPortUDP());
//		System.out.println("DEBUG - Adresse dans sendTO : " + address.getHostName());

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		packet.setSocketAddress(address);

		socket.send(packet);
	}
	
	// Lis un message et le renvoie
	public Message readOne() {
		Message m = null;
		try {
			m = file.poll();
			System.out.println("Message envoyé par : "+ m.getPseudo());
			System.out.println(m.getMessage());
			System.out.println("Voulez vous répondre ?");
		} catch (NullPointerException e) {
			System.out.println("Votre liste de message est vide");
		}
		return m;
	}

	//Affiche tout les messages sans les supprimer de la file
	public void readAll() {
		ArrayDeque<Message> deque = file.clone();
		Message m;
		if(deque.isEmpty()) {
			System.out.println("Votre liste de message est vide");
		} else {
			while((m=deque.poll())!=null) {
				System.out.println("Message de " + m.getPseudo() + " : " + m.getMessage());
			}			
		}
	}
	public ArrayDeque<Message> getFile() {
		return file;
	}

	public void setFile(ArrayDeque<Message> file) {
		this.file = file;
	}

	public int getPortUDP() {
		return portUDP;
	}

	public void setPortUDP(int portUDP) {
		this.portUDP = portUDP;
	}

}
