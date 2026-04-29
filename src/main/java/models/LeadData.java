package models;

public class LeadData {

	private final String firstName;
	private final String lastName;
	private String company;
	private String email;
	private final String phone;
	private final String leadStatus;
	
	
	//The private constructor is — receives the Builder object and copies all values from it.
	private LeadData(Builder builder) {
		this.firstName =  builder.firstName;
		this.lastName = builder.lastName;
		this.company =  builder.company;
		this.email = builder.email;
		this.phone = builder.phone;
		this.leadStatus =  builder.leadStatus;
	}
	
	//to read private fields from outside
	public String getFirstName()  { return firstName; }
    public String getLastName()   { return lastName; }
    public String getCompany()    { return company; }
    public String getEmail()      { return email; }
    public String getPhone()      { return phone; }
    public String getLeadStatus() { return leadStatus; }
    public String getFullName()   { return firstName + " " + lastName; }
    
    //to set the company and email fields with unique number
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
	
	
	public static class Builder{
		
		private String firstName;
		private String lastName;
		private String company;
        private String email;
        private String phone;
        private String leadStatus = "Open - Not Contacted"; // default value
        
        // Each setter sets one field and returns the Builder itself
        public Builder firstName(String val)  { this.firstName  = val; return this; }
        public Builder lastName(String val)   { this.lastName   = val; return this; }
        public Builder company(String val)    { this.company    = val; return this; }
        public Builder email(String val)      { this.email      = val; return this; }
        public Builder phone(String val)      { this.phone      = val; return this; }
        public Builder leadStatus(String val) { this.leadStatus = val; return this; }
        
     // build() validates required fields, then creates the final LeadData
        public LeadData build() {
        	
        	if(lastName == null || company == null) {
        		throw new IllegalStateException("LeadData: lastName and company are required");
        	}
        	return new LeadData(this); // calls the private constructor
        	
        }
        
        
	}

}
