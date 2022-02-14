package org.lance.network.http.model;

import lombok.Data;

import java.util.List;

@Data
public class Dash {
    private List<Video> video;
    private List<Audio> audio;
}
