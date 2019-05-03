package model;


import com.google.gson.JsonElement;
import lombok.Data;

import java.util.List;

@Data
public class StandardJsonList<Generic> {

    private Long result_count;
    private int page;
    private int last_page_number;
    private List<Generic> data;

    public StandardJsonList(Long result_count, int page, int lastPageNumber, List<Generic> data) {
        this.result_count = result_count;
        this.page = page;
        this.last_page_number = lastPageNumber;
        this.data = data;
    }
}
