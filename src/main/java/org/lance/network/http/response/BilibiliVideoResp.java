package org.lance.network.http.response;

import lombok.Data;
import org.lance.network.http.model.VideoData;

@Data
public class BilibiliVideoResp extends GenericResponse{
    private VideoData data;
}
