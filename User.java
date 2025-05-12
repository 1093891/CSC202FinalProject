
package marketplace;

public abstract class User {
    private String id;
    private String name;
    private String email;
    
    public User(String id, String name, String email){
        this.id=id;
        this.name=name;
        this.email=email;
    }
    
    //Getters
    public String getId() {return id;}
    public String getName() {return name;}
    public String getEmail() {return email;}
    
    public abstract boolean isInServiceArea(String location);
    
    @Override 
    public String toString() {
        return name + " (" + email + ")";
    }
}
