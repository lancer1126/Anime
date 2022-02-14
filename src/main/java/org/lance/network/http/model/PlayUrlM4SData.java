package org.lance.network.http.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class PlayUrlM4SData extends BaseData {
    private int quality;
    private String format;
    @SerializedName("accept_description")
    private List<String> acceptDescription;
    @SerializedName("accept_quality")
    private List<Integer> acceptQuality;
    private Dash dash;
}
