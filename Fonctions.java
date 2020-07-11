import java.io.*;
import java.net.Socket;
import java.util.*;

public class Fonctions implements Runnable{ 
    
    private Socket socket;
    private PrintWriter out ;
    private BufferedReader in ;
    private String protocole;
    private static int ref = 0;      
    private static ArrayList<User> listUser = new ArrayList<User>() ;
    private static ArrayList<Annonce> annoncesAll = new ArrayList<Annonce>();

    
    public Fonctions(Socket _socket) {
        this.socket = _socket;
    }

    @Override
    public void run() {
    	User u = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));             
            String str = "";
            String [] token = null;
            while ((str = in.readLine()) != null) {
                System.out.println("Message: "+ str);
                if(!str.contains(";")){
                    protocole = str; 
                    token = null;
                }else{
                    token = str.split(";");
                    protocole = token[0];
                }   
                switch (protocole) {
                    case "CONNECT":
                        u = connect(token);
                        break;
                    case "ALLANNS":
                        afficherAllAnnonces();
                        break;
                    case "DISCONN":
                        u = disconnection(u);
                        break;
                    case "ADDANNS":
                        addAnnonce(u,token);
                        break;
                    case "MYYANNS":
                        afficherMesAnnoneces(u);
                        break;
                    case "ANNONCE":
                        afficherAnnonceParType(token);
                        break;
                    case "DELANNS":
                        deleteAnnonceParRef(u,token);
                        break;
                    case "MESSAGE":
                        envoiCoordonneeUDP(token,u);
                        break;
                    case "QUIT":
                    	quit(u);
                        break;
                    default:
                        out.println("FAIL;Veuillez respecter le protocole");
                        out.flush();
                }
            }

        } catch (IOException e) {
        	String nameClient;
        	if(u==null)	nameClient = "invite";
        	else	nameClient = u.getPseudo();
        	System.out.println("Le Client " + nameClient +  " s'est déconnecté de manière brutale");
            quit(u);
        }
    }
    
    private void quit(User u) {
        try{
        	for(User user : listUser) {
        		if(user.equals(u)) {
        			user.setConnect(false);
        		}
        	}
//        	u.setConnect(false);
            out.close();
            in.close();
            socket.close();
            return;
        }catch(IOException e){
            out.println("FAIL;vous n'êtes pas déconnectés");
            out.flush();
            System.exit(1);
        }
	}

	private User connect(String msg[]) {
        String pseudo="",mdp="",ip = "";
        int portUDP = 0;
        try{
            pseudo = msg[1].trim();
            if(pseudo.equals("")) {
        		out.println("FAIL;Vous n'avez pas tapé votre pseudo");
        		out.flush();
        		return null;
            }
            mdp = msg[2].trim();
            if(mdp.equals("")) {
        		out.println("FAIL;Vous n'avez pas tapé votre mot de passe");
        		out.flush();
        		return null;
            }
            portUDP = Integer.parseInt(msg[3].trim());
            if(portUDP<1 || portUDP > 65000) {
        		out.println("FAIL;Le port UDP doit etre entre 1 et 65000");
        		out.flush();
        		return null;            	
            }
            ip = msg[4].trim();
        } catch(ArrayIndexOutOfBoundsException e){
        	if(e.getMessage().equals("1")) {
        		out.println("FAIL;Vous n'avez pas tapé votre pseudo");
        	} else if(e.getMessage().equals("2")) {
        		out.println("FAIL;Vous n'avez pas tapé votre mot de passe");
        	} else if(e.getMessage().equals("3")) {
        		out.println("FAIL;Vous n'avez pas tapé votre portUDP");        		
        	} else if(e.getMessage().equals("4")) {
        		out.println("FAIL;Vous n'avez pas tapé votre IP");
        	} else {
        		out.println("FAIL;Il n'y a pas assez d'arguments");
        	}
        		out.flush();
        		return null;            	
        } catch (NumberFormatException e) {
        	out.println("FAIL; Veuillez envoyer un entier comme port UDP");
        	out.flush();
        	return null;
        } catch(NullPointerException e) {
        	out.println("FAIL;vous avez CONNECT utilisé sans arguments");
        	out.flush();
        	return null;
        }
        User user = null;
        for(User u : listUser) {
            if( pseudo.equals(u.getPseudo())) {
            	if(u.getConnect()) {
            		out.println("FAIL;Vous êtes déjà connecté");
            		out.flush();
            		user = u;
            		break;
            	}
            	else if (mdp.equals(u.getMdp())) {
            		u.setPortUDP(portUDP);
            		u.setConnect(true);
            		u.setIP(ip);
                    user = u;
                    break;
                }else{
                	out.println("FAIL;mot de passe incorrect");
                	out.flush();
                    return null;
                }
            }
        }
        if(user == null){
        	user = new User(pseudo, mdp,portUDP,ip);
        	listUser.add(user);        	
        }
        out.println("CONNECT");
        out.flush();
        return user;
    }
    
	private synchronized void addAnnonce(User user,String[]token){
		String domaine="", desc = "";
		int prix = 0;
		try {
			if(user == null) {
				out.println("FAIL;vous n'êtes pas connectés");
			} else/* if(token[2].trim().matches("\\d+"))*/{
				domaine = token[1].trim().toLowerCase();
				if(domaine.equals(""))	out.println("FAIL;Le domaine est vide");

				prix = Integer.parseInt(token[2].trim());
				if(prix == 0)	out.println("FAIL;le prix ne peut etre nul");

				desc = token[3].trim();
				if(desc.equals(""))	out.println("FAIL;La description est vide");

				ref++;
				Annonce a = new Annonce(ref,user.getPseudo().trim(),domaine, Integer.parseInt(token[2].trim()),desc);
				annoncesAll.add(a);
				out.println("OK");
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			if(e.getMessage().equals("1")) {
				out.println("FAIL;vous n'avez pas renseigné de type");
			}
			if(e.getMessage().equals("2")) {
				out.println("FAIL;vous n'avez pas renseigné de prix");
			}
			if(e.getMessage().equals("3")) {
				out.println("FAIL;vous n'avez pas renseigné de description");
			}
		} catch (NumberFormatException e) {
			out.println("FAIL;Veuillez envoyer un entier comme prix");
		} catch(NullPointerException e) {
			out.println("FAIL;Mauvais usage du protocole ADDANNS");
		}
		out.flush();
	}
	
    private synchronized void deleteAnnonceParRef(User u,String[] token){
        try{
        	boolean trouve = false;
        	int ref = Integer.parseInt(token[1]);
            if(u == null){
            out.println("FAIL;vous êtes déconnectés.");
            }else if((annoncesAll.size() > 0)){
                for (int i = 0; i < annoncesAll.size() ; i++) {
                    if(annoncesAll.get(i).getRef() == ref){
                        if(annoncesAll.get(i).getLogin().equals(u.getPseudo())) {
                            annoncesAll.remove(i);
                            out.println("OK");
                        }else{
                            out.println("FAIL;vous n'êtes pas le propriétaire de l'annonce.");
                        }
                        trouve = true;
                        break;
                    }
                }
            } if(!trouve)	out.println("FAIL;Il n'y a pas d'annonces.");     
        } catch (ArrayIndexOutOfBoundsException e) {
        	out.println("FAIL;Mauvais usage du protocole DELANNS");
        } catch(NumberFormatException e) {
        	out.println("FAIL;La reference n'est pas un nombre");
        } catch(NullPointerException e) {
        	out.println("FAIL;Mauvais usage du protocole DELANNS");
        }
        out.flush();
    }
    
    private synchronized void afficherAllAnnonces(){
    	//TODO; Verifier eventuellement si on peut écrire des arguments (ALLANNS;xxx)
        if( annoncesAll.size() > 0 ){
            String message ="ALLANNS;";
            for(int i = 0;i < annoncesAll.size();i++){
                message += annoncesAll.get(i).getDomaine()+"***"+ annoncesAll.get(i).getContenu()+
                "***"+annoncesAll.get(i).getRef()+"***"+annoncesAll.get(i).getPrix() + "***"+annoncesAll.get(i).getLogin()+"###";
            }  
            out.println(message);
            out.flush();
        }else{
            out.println("ALLANNS;");
            out.flush();
        }     
    }
    
    private synchronized void afficherMesAnnoneces(User usr){
        if(usr != null){
            String message ="MYYANNS;";
            for(int i = 0;i < annoncesAll.size();i++){
                if(annoncesAll.get(i).getLogin().equals(usr.getPseudo())){
                    message += annoncesAll.get(i).getDomaine()+"***"+ annoncesAll.get(i).getContenu()+
                    "***"+annoncesAll.get(i).getRef()+"***"+annoncesAll.get(i).getPrix() + "***"+annoncesAll.get(i).getLogin()+"###";    
                }
            }    
            out.println(message);
            out.flush();
        }else{
            out.println("FAIL;vous n'êtes pas connectés");
            out.flush();
        }
    }   
    
    private synchronized void afficherAnnonceParType(String[] token){
        try{
        	String type = token[1];
            String message = "ANNONCE;";
            for(int i = 0;i < annoncesAll.size();i++){                
                if(annoncesAll.get(i).getDomaine().equals(type.toLowerCase())){
                    message += annoncesAll.get(i).getDomaine()+"***"+ annoncesAll.get(i).getContenu()+
                    "***"+annoncesAll.get(i).getRef()+"***"+annoncesAll.get(i).getPrix() + "***"+annoncesAll.get(i).getLogin()+"###";        
                }
            }
            out.println(message);
        }catch(ArrayIndexOutOfBoundsException e){
        	out.println("FAIL;Mauvais usage du protocole ANNONCE)");
        } catch(NullPointerException e) {
        	out.println("FAIL;Mauvais usage du protocole ANNONCE");
        }
        out.flush();
           
    }

    private synchronized void envoiCoordonneeUDP(String[] token, User u){
        try{
        	int id_annonce = Integer.parseInt(token[1]);
            String envoi = "MESSAGE;";
            int portUDP = 0;
            String ip = "";
            if(u==null) {
            	out.println("FAIL;Vous devez vous connecter pour contacter un utilisateur");
            } else {
            	for(int i = 0; i < annoncesAll.size(); i++){
            		if(annoncesAll.get(i).getRef( ) == id_annonce){
            			String login = annoncesAll.get(i).getLogin();
            			for(int j = 0; j < listUser.size(); j++){
            				if(listUser.get(j).getPseudo().equals(login)){
            					portUDP=listUser.get(j).getPortUDP();
            					ip = listUser.get(j).getIP();
            					break;
            				}
            			}
            			break;
            		}
            	}
            	if(portUDP==0) {
            		out.println("FAIL;L'annonce n'existe pas");
            	} else {
            		out.println(envoi+portUDP+";"+ip);
            	}            	
            }
        }catch(NullPointerException e){
        	out.println("FAIL;L'ID d'annonce est vide");
        } catch(ArrayIndexOutOfBoundsException e) {
        	out.println("FAIL;L'ID d'annonce est vide");
        } catch(NumberFormatException e) {
        	out.println("FAIL;La reference doit etre un entier");
        }
        out.flush();    
    }
    private synchronized User disconnection(User usr){
        if(usr != null) {
            usr.setConnect(false);
            usr = null;
            out.println("OK");
        }else {
            out.println("FAIL;vous êtes déjà déconnectés");
        }
        out.flush();
        return usr;
    }
   
}