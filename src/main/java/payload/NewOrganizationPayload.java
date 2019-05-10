package payload;

import com.google.gson.annotations.Expose;
import lombok.Data;
import util.Validable;

@Data
public class NewOrganizationPayload implements Validable {

    @Expose
    String organization_name;


    public boolean isValid() {
        return organization_name != null && !organization_name.isEmpty();
    }
}
