package org.lance.network.http.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lance.network.http.model.Dash;
import org.lance.network.http.model.PlayUrlM4SData;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlayUrlM4SDataResp extends GenericResponse {
    private PlayUrlM4SData data;
}
