package model;

import com.google.gson.annotations.Expose;
import lombok.Data;

@Data
public class CanvasStudent {
    @Expose
    private String user_name;
    @Expose
    private Integer user_id;

    public CanvasStudent(String user_name, Integer user_ids){
        this.user_name = user_name;
        this.user_id = user_ids;
    }
}
