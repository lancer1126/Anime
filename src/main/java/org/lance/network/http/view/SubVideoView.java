package org.lance.network.http.view;

public class SubVideoView {
    // bilibili 需要大会员才能看的番剧，每一集的 bvId 都不一样
    public String bvId;
    public int cid;
    public String name;
    public String url;

    @Override
    public String toString() {
        return "SubVideoView{" +
                "bvId='" + bvId + '\'' +
                ", cid=" + cid +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
