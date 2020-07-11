public class Message {
	private int portUDP;
	private String pseudo, message, adresseIP;
	
	public Message(String _pseudo, int _portUDP,  String _adresseIP, String _message) {
		this.pseudo = _pseudo;
		this.portUDP = _portUDP;
		this.message = _message;
		this.adresseIP = _adresseIP;
	}
	
	public String getPseudo() {
		return pseudo;
	}
	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}
	public int getPortUDP() {
		return portUDP;
	}
	public void setPortUDP(int portUDP) {
		this.portUDP = portUDP;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	public String getAdresseIP() {
		return adresseIP;
	}

	public void setAdresseIP(String adresseIP) {
		this.adresseIP = adresseIP;
	}
}
