package model;


import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import lombok.Data;

import java.util.List;

@Data
public class StandardJsonList<Generic> {

    @Expose
    private Long result_count;
    @Expose
    private int page;
    @Expose
    private int page_size;
    @Expose
    private int last_page;
    @Expose
    private List<Generic> data;

    public StandardJsonList(Long result_count, int page,int page_size, int last_page, List<Generic> data) {
        this.result_count = result_count;
        this.page = page;
        this.page_size = page_size;
        this.last_page = last_page;
        this.data = data;
    }
}
