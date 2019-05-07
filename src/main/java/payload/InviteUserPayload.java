package payload;

import lombok.Data;
import util.Validable;
import java.util.UUID;

@Data
public class InviteUserPayload implements Validable {
    String email;

    public boolean isValid() {
        return email != null && !email.isEmpty();
    }
}
