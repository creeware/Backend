package model;

import com.google.gson.annotations.Expose;
import lombok.Data;
import util.Path;

@Data
public class CanvasCourse {
    @Expose
    private String course_name;
    @Expose
    private Integer course_id;

    public CanvasCourse(String course_name, Integer course_id){
        this.course_name = course_name;
        this.course_id = course_id;
    }
}
