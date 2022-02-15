package org.lance.network.http.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.lance.network.http.model.Dash;

import java.util.List;

@Data
public class PlayUrlM4SDataResp extends GenericResponse {
    private int quality;
    private String format;
    @SerializedName("accept_description")
    private List<String> acceptDescription;
    @SerializedName("accept_quality")
    private List<Integer> acceptQuality;
    private Dash dash;
}
