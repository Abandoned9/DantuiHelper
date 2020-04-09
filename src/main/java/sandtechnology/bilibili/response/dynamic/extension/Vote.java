package sandtechnology.bilibili.response.dynamic.extension;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public class Vote {
    @SerializedName("desc")
    String text;
    @SerializedName("endtime")
    long endTime;
    @SerializedName("vote_id")
    long voteID;
    @SerializedName("uid")
    long AuthorUID;
    @SerializedName("type")
    int type;
    //总投票数
    @SerializedName("join_num")
    private
    int count;
    //选择
    @SerializedName("options")
    private
    List<Option> optionList;
    private static ZoneId zoneId = ZoneId.of("UTC+8");
    //0=正常 4=未找到对应动态？
    @SerializedName("status")
    private
    int status;

    @Override
    public String toString() {
        return "\n投票信息：" +
                "\n状态：" + (endTime >= Instant.now().atZone(zoneId).toEpochSecond() ? "进行中" : "已结束")
                + "\n总投票人数：" + count
                + "\n投票选项：\n" +
                optionList.stream().map(Option::toString).collect(Collectors.joining("\n"));
    }

    public static class Option {
        @SerializedName("desc")
        String text;
        @SerializedName("idx")
        int index;

        @Override
        public String toString() {
            return index + "." + text;
        }
    }
}
