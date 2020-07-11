import javax.net.ssl.SSLServerSocketFactory;

import java.io.FileNotFoundException;
import java.net.SocketException;

import javax.net.ssl.*;

public class Serveur{

    
    public static void main(String[] args) {
        
		System.setProperty("javax.net.ssl.keyStore", "server.jsk");
		System.setProperty("javax.net.ssl.keyStorePassword" , "123456");
		SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        try{
			SSLServerSocket server = (SSLServerSocket)factory.createServerSocket(1027);
            System.out.println("..... Bienvenue sur notre Serveur ....");   
            System.out.println("Adresse IP du serveur : " + server.getInetAddress().getLocalHost().getHostAddress());
            while(true) {
                SSLSocket socket = (SSLSocket)server.accept();
                Fonctions serv = new Fonctions(socket);
                Thread t = new Thread(serv);
                t.start();  
                
            }
        }catch (FileNotFoundException e) {
        	System.out.println("le fichier " + e.getMessage() + " est introuvable");
        } catch(SocketException e) {
        	e.printStackTrace();
        }
        catch(Exception e){
        	e.printStackTrace();
            System.out.println("Serveur ne peut pas connecter");
        }       
    } 
     
}