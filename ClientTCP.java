import java.io.*;
import java.net.*;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ClientTCP {
	//Instanciation des options du serveur auquel on se connecte
	private static int portTCPServer = 1027;

	//Instanciation des elements de communications avec le serveurs
	private SSLSocket socket;
	private PrintWriter printWriter;
	private BufferedReader bufferedReader;
	private Scanner inFromUser;
	private String pseudoCourant;
	private ClientUDP clientUDP;
	private boolean isConnected;

	//------------------------- CONSTRUCTEUR ---------------------------------------------------------------------------------
	
	public ClientTCP(String ip) {
		try {
			System.setProperty("javax.net.ssl.trustStore", "client.jsk");
            System.setProperty("javax.net.ssl.trustStorePassword", "123456");
            SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            this.socket = (SSLSocket)socketFactory.createSocket(ip,this.portTCPServer);

			//this.socket = new SSLSocket(ip, this.portTCPServer);
			this.printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.inFromUser = new Scanner(System.in);
			this.pseudoCourant = "invite";
			this.isConnected = false;
		} catch(Exception e) {
			System.out.println("Connexion au Serveur impossible");
		}	
	}

	//-------------------------------- MAIN ----------------------------------------------------------------------------------
	
	public static void main(String[] args) {
//		String ip = "localhost";
		ClientTCP clientTCP = null;
//		System.out.println("IP entre en parametre : " + args[0]);	//DEBUG
		try{
			clientTCP = new ClientTCP(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Veuillez entrer en parametre l'adresse IP du Serveur");
			System.exit(1);
		}
		if(clientTCP.socket==null)	return;
		clientTCP.inFromUser = new Scanner(System.in);
		boolean connected = true;
		String commande;
		int portUDP = 0;
		while(connected) {

			System.out.println("Entrez votre choix : 0 - Quitter l'application\n 1 - Connect\n 2 - Disconnect\n 3 - Add annonce\n"
					+ " 4 - Voir toutes les annonces\n 5 - Voir mes annonces\n 6 - Voir les annonces avec filtre\n"
					+ " 7 - Supprimer une annonce\n 8 - Envoyer un message à un utilisateur\n 9 - Lire un message\n "
					+ "10 - Lire tout ses messages");
			commande = clientTCP.inFromUser.nextLine();
			switch(commande) {
			case "0":
				clientTCP.quit();
				break;
			case "1":
				if(!clientTCP.isConnected) {
					portUDP = clientTCP.connect();
					//					System.out.println("DEBUG - le port UDP récupéré par le serveur est : " + portUDP);
				}
				else	System.out.println("Vous etes deja connecte");
				break;
			case "2":
				clientTCP.disconnect();
				break;
			case "3":
				clientTCP.addAnnonce();
				break;
			case "4":
				clientTCP.allAnnonce();
				break;
			case "5":
				clientTCP.myAnnonce();
				break;
			case "6":
				clientTCP.filterAnnonce();
				break;
			case "7":
				clientTCP.deleteAnnonce();
				break;
			case "8":
				if(clientTCP.isConnected) {
					Adresse adresse = clientTCP.sendMessage();
					if(adresse!=null)	clientTCP.sendUDP(adresse);
					else	System.out.println("L'annonce n'existe pas");					
				} else {
					System.out.println("Connectez vous pour envoyer un message");
				}
				break;
			case "9":
				if(clientTCP.isConnected) {
					Message m = clientTCP.clientUDP.readOne();
					if(m!=null)	clientTCP.repondre(m);					
				} else {
					System.out.println("Connectez vous pour lire des messages");
				}
				break;
			case "10":
				if(clientTCP.isConnected)	clientTCP.clientUDP.readAll();
				else	System.out.println("Connectez vous pour lire des messages");
				break;
			default:
				break;

			}
		}
	}

	//---------------------------------- FONCTIONS DU PROTOCOLE CLIENT --> SERVEUR -------------------------------------------
	
	public void quit() {
		try {
			// Ecriture et envoi du message
			String message = "QUIT";
			printWriter.println(message);
			printWriter.flush();
			System.out.println("Fermeture du programme");
			printWriter.close();
			bufferedReader.close();
			socket.close();
			System.exit(1);
		} catch (NullPointerException e) {
			System.err.println("Vous n'etes plus connecté au serveur");
			System.exit(1);
		} catch (SocketException e) {
			System.err.println("Vous n'etes plus connecté au serveur. Il ne fonctionne peut etre plus");
			closeEverything();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Mauvaise lecture");
		}
	}

	public int connect() {
		System.out.println("Entrez votre pseudo : ");
		String pseudo = inFromUser.nextLine();
		System.out.println("Entrez votre mdp");
		String mdp = inFromUser.nextLine();
		System.out.println("Entrez votre portUDP (entre 1 et 65000)");
		String portSend = inFromUser.nextLine();
		int portUDP = 0;
		try {
			// Ecriture et envoi du message
			//			System.out.println("DEBUG - Envoi de l@ IP : " + socket.getInetAddress().getHostAddress());
			String message = "CONNECT;"+pseudo+";"+mdp+";"+portSend+";"+socket.getInetAddress().getHostAddress();
			printWriter.println(message);
			printWriter.flush();
			//Lecture
			String reception = bufferedReader.readLine();
			String[] receptionSplit = reception.split(";");
			if(receptionSplit[0].equals("CONNECT")) {
				System.out.println("Connection réussi");
				setPseudoCourant(pseudo);
				portUDP = Integer.parseInt(portSend);
				createUDP(portUDP);	//Demarre l'ecoute en UDP
				this.isConnected = true;
				return portUDP;
			} else if(receptionSplit[0].equals("FAIL")) {
				System.out.println(receptionSplit[1]);
				return -1;
			}
			else {
				System.out.println("Message Inconnu : " + reception);
				return -1;
			}
		} catch (NullPointerException e) {
			System.err.println("Vous n'etes plus connecté au serveur");
			return -1;
		} catch (SocketException e) {
			System.err.println("Vous n'etes plus connecté au serveur. Il ne fonctionne peut etre plus");
			System.exit(1);
			return -1;
		}
		catch (IOException e) {
			e.printStackTrace();
			System.err.println("Mauvaise lecture");
			return -1;
		}

	}

	public boolean disconnect() {	
		try {
			String message = "DISCONN";
			printWriter.println(message);
			printWriter.flush();
			//Lecture
			String reception = bufferedReader.readLine();
			if(reception.equals("OK")) {
				System.out.println("Déconnection réussi");
				pseudoCourant = "invite";
				if(clientUDP!=null)	clientUDP.stop(); //Deconnection du thread d'ecoute et d'envoi UDP
				isConnected = false;
			}			
			return true;
		} catch (NullPointerException e) {
			System.err.println("Vous n'etes plus connecté au serveur");
			return false;
		} catch (SocketException e) {
			System.err.println("Vous n'etes plus connecté au serveur. Il ne fonctionne peut etre plus");
			System.exit(1);
			return false;
		}
		catch (IOException e) {
			e.printStackTrace();
			System.err.println("Deconnection impossible");
			return false;
		} 
	}

	public boolean addAnnonce() {
		System.out.println("Entrez le domaine : ");
		String domaine = inFromUser.nextLine();
		System.out.println("Entrez le prix : ");
		String prix = inFromUser.nextLine();
		System.out.println("Entrez la description : ");
		String description = inFromUser.nextLine();
		try {
			String message = "ADDANNS;"+domaine+";"+prix+";"+description;
			printWriter.println(message);
			printWriter.flush();
			//Lecture
			String reception = bufferedReader.readLine();
			String[] receptionSplit = reception.split(";");
			if(receptionSplit[0].equals("OK")) {
				System.out.println("Annonce ajouté");
			} else if(receptionSplit[0].equals("FAIL")){
				System.out.println(receptionSplit[1]);
			}
			return true;
		} catch (NullPointerException e) {
			System.err.println("Vous n'etes plus connecté au serveur");
		} catch (SocketException e) {
			System.err.println("Vous n'etes plus connecté au serveur. Il ne fonctionne peut etre plus");
			System.exit(1);
			return false;
		}
		finally {
			return false;
		}
	}

	public boolean allAnnonce() {
		try {
			String message = "ALLANNS";
			printWriter.println(message);
			printWriter.flush();
			//Lecture
			String reception = bufferedReader.readLine();
			String[] receptionSplit = reception.split(";");
			if(receptionSplit[0].equals("FAIL")){
				System.out.println(receptionSplit[1]);
			}else if(receptionSplit[0].equals(message)){
				if(receptionSplit.length == 1 ){
					System.out.println("Il n'y a pas encore d'annonce");
				}else if(receptionSplit.length > 1){
					System.out.println("All announces online :");
					String[] tmp = receptionSplit[1].split("###");
					for(int i = 0; i < tmp.length; i++){
						String [] src = tmp[i].split("\\*\\*\\*");

						System.out.println("+----------------------------------------------+");
						System.out.println("| Reference : " + src[2]);
						System.out.println("| Domaine : " + src[0]);
						System.out.println("| Prix: " + src[3]);
						System.out.println("| Utilisateur: " + src[4]);
						System.out.println("| Description : " + src[1]);
						System.out.println("+----------------------------------------------+");
					}
				}
			}else{
				System.out.println("Reponse inconnu : " + reception);
			}
			return true;
		} catch (NullPointerException e) {
			System.err.println("Vous n'etes plus connecté au serveur");
		} catch (SocketException e) {
			System.err.println("Vous n'etes plus connecté au serveur. Il ne fonctionne peut etre plus");
			closeEverything();
			System.exit(1);
			return false;
		}
		finally {
			return false;
		}

	}

	public boolean myAnnonce() {
		try {
			String message = "MYYANNS";
			printWriter.println(message);
			printWriter.flush();
			//Lecture
			String reception = bufferedReader.readLine();
			String[] receptionSplit = reception.split(";");
			if(receptionSplit[0].equals("FAIL")){
				System.out.println(receptionSplit[1]);
			}else if(receptionSplit[0].equals(message)){
				if(receptionSplit.length == 1 ){
					System.out.println("Vous n'avez pas encore d'annonce");
				}else if(receptionSplit.length > 1){
					System.out.println("Vos annonces:");
					String[] tmp = receptionSplit[1].split("###");
					for(int i = 0; i < tmp.length; i++){
						String [] src = tmp[i].split("\\*\\*\\*");

						System.out.println("+----------------------------------------------+");
						System.out.println("| Reference : " + src[2]);
						System.out.println("| Domaine : " + src[0]);
						System.out.println("| Prix: " + src[3]);
						System.out.println("| Utilisateur: " + src[4]);
						System.out.println("| Description : " + src[1]);
						System.out.println("+----------------------------------------------+");
					}
				}
			}else{
				System.out.println("Reponse inconnu : " + reception);
			}
			return true;
		} catch (NullPointerException e) {
			System.err.println("Vous n'etes plus connecté au serveur");
		} catch (SocketException e) {
			System.err.println("Vous n'etes plus connecté au serveur. Il ne fonctionne peut etre plus");
			closeEverything();
			System.exit(1);
			return false;
		}
		finally {
			return false;
		}

	}

	public boolean filterAnnonce() {
		try {
			String message = "ANNONCE";
			System.out.println("Entrez le type : ");
			String type = inFromUser.nextLine();			
			message +=";" + type;

			printWriter.println(message);
			printWriter.flush();
			//Lecture
			String reception = bufferedReader.readLine();
			String[] receptionSplit = reception.split(";");
			if(receptionSplit[0].equals("FAIL")){
				System.out.println(receptionSplit[1]);
			}else if(receptionSplit[0].equals("ANNONCE")){
				if(receptionSplit.length == 1 ){
					System.out.println("Il n'y a pas encore d'annonce de ce type");
				}else if(receptionSplit.length > 1){
					System.out.println("Les annonces:");
					String[] tmp = receptionSplit[1].split("###");
					for(int i = 0; i < tmp.length; i++){
						String [] src = tmp[i].split("\\*\\*\\*");

						System.out.println("+----------------------------------------------+");
						System.out.println("| Reference : " + src[2]);
						System.out.println("| Domaine : " + src[0]);
						System.out.println("| Prix: " + src[3]);
						System.out.println("| Utilisateur: " + src[4]);
						System.out.println("| Description : " + src[1]);
						System.out.println("+----------------------------------------------+");
					}
				}
			}else{
				System.out.println("Reponse inconnu : " + reception);
			}
			return true;
		} catch (NullPointerException e) {
			System.err.println("Vous n'etes plus connecté au serveur");
		} catch (SocketException e) {
			System.err.println("Vous n'etes plus connecté au serveur. Il ne fonctionne peut etre plus");
			closeEverything();
			System.exit(1);
			return false;
		}
		finally {
			return false;
		}
	}

	public boolean deleteAnnonce() {
		System.out.println("Entrez la ref : ");
		String ref = inFromUser.nextLine();
		try {
			String message = "DELANNS;"+ref;
			printWriter.println(message);
			printWriter.flush();
			//Lecture
			String reception = bufferedReader.readLine();
			String[] receptionSplit = reception.split(";");
			if(receptionSplit[0].equals("OK")) {
				System.out.println("Annonce supprimé");
			} else if(receptionSplit[0].equals("FAIL")){
				System.out.println(receptionSplit[1]);
			}
			return true;
		} catch (NullPointerException e) {
			System.err.println("Vous n'etes plus connecté au serveur");
		} catch (SocketException e) {
			System.err.println("Vous n'etes plus connecté au serveur. Il ne fonctionne peut etre plus");
			closeEverything();
			System.exit(1);
			return false;
		}
		finally {
			return false;
		}
	}

	public Adresse sendMessage() {
		System.out.println("Entrez la ref de l'annonce de l'utilisateur à contacter : ");
		String ref = inFromUser.nextLine();
		try {
			Adresse adresse = null;
			String adresseIP;
			int portUDP = -1;
			String message = "MESSAGE;"+ref;
			printWriter.println(message);
			printWriter.flush();
			//Lecture
			String reception = bufferedReader.readLine();
			String[] receptionSplit = reception.split(";");
			if(receptionSplit[0].equals("MESSAGE")) {
				portUDP = Integer.parseInt(receptionSplit[1]);
				adresseIP = receptionSplit[2];
				adresse = new Adresse(adresseIP, portUDP);
				//				System.out.println("DEBUG - Port UDP récupéré : " + receptionSplit[1]);
			} else if(receptionSplit[0].equals("FAIL")){
				System.out.println(receptionSplit[1]);
			}
			//			System.out.println("DEBUG - portUDP dans sendMessage() : " + portUDP);
			return adresse;
		} catch (NullPointerException e) {
			System.err.println("Vous n'etes plus connecté au serveur");
			return null;
		} catch (SocketException e) {
			System.err.println("Vous n'etes plus connecté au serveur. Il ne fonctionne peut etre plus");
			closeEverything();
			System.exit(1);
			return null;
		} catch (IOException e) {
			System.out.println("Erreur de readline");
			return null;
		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("Le Serveur n'a pas envoyé l'adresse IP ou le portUDP");
			return null;
		}
	}
	
	public void closeEverything() {
		try {
			printWriter.close();
			bufferedReader.close();
			socket.close();
			inFromUser.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//------------------------- PEER TO PEER CLIENT/CLIENT EN UDP -----------------------------------------------------------

	public void sendUDP(Adresse adresse) {
		try {
			System.out.println("Entrez votre message :");
			String msg = inFromUser.nextLine();
			String msgSend = "WRITETO;"+pseudoCourant+";"+clientUDP.getPortUDP()+";"
					+ this.socket.getInetAddress().getHostAddress()+";"+msg;
			clientUDP.sendTo(adresse, msgSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createUDP(int portUDP) {
		try {
			clientUDP = new ClientUDP(portUDP);
			clientUDP.start();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void repondre(Message m) {
		System.out.println("Voulez vous répondre ? (oui|non)");
		try {
			String reponse = inFromUser.nextLine();
			if(reponse.toLowerCase().equals("oui")) {
				sendUDP(new Adresse(m.getAdresseIP(), m.getPortUDP()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//---------------------------------- GETTERS ET SETTERS -----------------------------------------------------------------

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(SSLSocket socket) {
		this.socket = socket;
	}

	public BufferedReader getBufferedReader() {
		return bufferedReader;
	}

	public void setBufferedReader(BufferedReader bufferedReader) {
		this.bufferedReader = bufferedReader;
	}

	public int getPortTCPServer() {
		return portTCPServer;
	}

	public void setPortTCPServer(int portTCPServer) {
		this.portTCPServer = portTCPServer;
	}

	public PrintWriter getPrintWriter() {
		return printWriter;
	}

	public void setPrintWriter(PrintWriter printWriter) {
		this.printWriter = printWriter;
	}

	public String getPseudoCourant() {
		return pseudoCourant;
	}
	
	public void setPseudoCourant(String pseudoCourant) {
		this.pseudoCourant = pseudoCourant;
	}
}	

