package com.code;

/**
 * MovieClip下的对话项.
 * 原始JSON:
 * <p>
 * {
 * * dialog_id : 922045
 * role_id : 646
 * content_en : Mom, what happened on the plane. I'm sorry.
 * content_cn : 妈妈，飞机上的事。我很抱歉。
 * time_begin : 700
 * time_end : 6200
 * fea : http://www.mofunenglish.com/storage/pool0/text/26/53/20150130195314734526001421.fea
 * fea_v2 : http://www.mofunenglish.com/storage/pool0/text/114/9/201503251350216963090011373528.fea
 * fea_content : Mom, what happened on the plane. I'm sorry.
 * fea_byte : 9009
 * fea_v2_byte : 9025
 * expl_count : 0
 * }
 * </p>
 * Created by Administrator on 13-8-17.
 */
public class DialogItem {


    public long dialog_id;
    public int role_id;
    public String content_en;
    public String content_cn;
    public int time_begin;
    public int time_end;
    public String fea;
    public String fea_v2;
    public String fea_content;
    public int fea_byte;
    public int fea_v2_byte;
    public int expl_count;

    /**
     * 获取对应文件路径
     *
     * @return
     */
    public String getMp3FilePath() {
        return "/storage/emulated/0/.mofunshow/records" + "/" + this.dialog_id + ".mp3";
    }

    public String getRecordFilePath() {
        return "/storage/emulated/0/.mofunshow/records" + "/" + this.dialog_id + ".wav";
    }

    /**
     * 判断是否属于某角色，若传入0则认为是全角色，返回true
     *
     * @param roleId
     * @return
     */
    public boolean isBelongToRole(long roleId) {
        return (roleId <= 0) || (roleId > 0
                && roleId == role_id);
    }
}
