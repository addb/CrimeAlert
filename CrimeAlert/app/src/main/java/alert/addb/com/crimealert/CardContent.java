package alert.addb.com.crimealert;

/**
 * Created by ADDB Inc on 13-03-2016.
 */
public class CardContent {
    String crime_type;

    public String getCrime_id() {
        return crime_id;
    }

    public void setCrime_id(String crime_id) {
        this.crime_id = crime_id;
    }

    String crime_id;
    String crime_description;
    String email;
    String crime_location;
    String crime_time;

    public String getCrime_type() {
        return crime_type;
    }

    public void setCrime_type(String crime_type) {
        this.crime_type = crime_type;
    }

    public String getCrime_description() {
        return crime_description;
    }

    public void setCrime_description(String crime_description) {
        this.crime_description = crime_description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public String getCrime_location() {
        return crime_location;
    }

    public void setCrime_location(String crime_location) {
        this.crime_location = crime_location;
    }

    public String getCrime_time() {
        return crime_time;
    }

    public void setCrime_time(String crime_time) {
        this.crime_time = crime_time;
    }



    /*public CardContent(String img_url,String caption){
        this.img_url =img_url;
        this.caption=caption;
    }*/


}
