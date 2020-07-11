public class Annonce{
   
    private String domaine,contenu,login;
    private int ref,prix;
    private boolean disponibilite;


    public Annonce(int _id ,String _login,String _domaine,int _prix,String _contenu){
        this.ref = _id;
        this.login = _login;
        this.domaine = _domaine;
        this.prix = _prix;
        this.contenu = _contenu;
    }

    public String getDomaine(){
        return domaine;
    }
    public int getPrix(){
        return prix;
    }
    public String getContenu(){
        return contenu;
    }
    public String getLogin(){
        return login;
    }
    public int getRef(){
        return ref;
    }
    public void setRef(int id){
        id = id;
    }
    public void setDisponibilite(boolean dispo){
        disponibilite = dispo;
    }
    public boolean getDisponibilite(){
        return disponibilite;
    }
 
}