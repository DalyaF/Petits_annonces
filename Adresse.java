
public class Adresse {
	private String adresse;
	private int portUDP;
	
	public Adresse(String _adresse, int _portUDP) {
		this.adresse = _adresse;
		this.portUDP = _portUDP;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public int getPortUDP() {
		return portUDP;
	}

	public void setPortUDP(int portUDP) {
		this.portUDP = portUDP;
	}
}
