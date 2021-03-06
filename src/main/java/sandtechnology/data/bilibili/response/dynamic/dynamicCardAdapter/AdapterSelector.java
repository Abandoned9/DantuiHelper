package sandtechnology.data.bilibili.response.dynamic.dynamicCardAdapter;

import com.google.gson.reflect.TypeToken;
import sandtechnology.data.bilibili.NormalResponse;
import sandtechnology.data.bilibili.response.dynamic.DynamicData;
import sandtechnology.data.bilibili.response.dynamic.dynamicCardAdapter.post.*;
import sandtechnology.data.bilibili.response.dynamic.dynamicCardAdapter.repost.RepostAdapter;
import sandtechnology.data.bilibili.response.dynamic.extension.VoteInfo;
import sandtechnology.data.bilibili.response.dynamic.lottery.LotteryData;
import sandtechnology.data.bilibili.response.dynamic.rich.DisputeInfo;
import sandtechnology.holder.WriteOnlyMessage;
import sandtechnology.utils.http.BiliBiliHTTPHelper;
import sandtechnology.utils.http.DataGetter;

import static sandtechnology.utils.JsonHelper.getGsonInstance;

public class AdapterSelector {


    private final static DataGetter<NormalResponse, LotteryData> lotteryInfoGetter;

    static {
        BiliBiliHTTPHelper biliBiliHTTPHelper = new BiliBiliHTTPHelper("https://api.vc.bilibili.com/lottery_svr/v1/lottery_svr/lottery_notice", null);
        biliBiliHTTPHelper.setOriginURL("https://t.bilibili.com");
        biliBiliHTTPHelper.setReferer("https://t.bilibili.com/lottery/h5/index/");
        lotteryInfoGetter = new DataGetter<>(biliBiliHTTPHelper, new TypeToken<LotteryData>() {
        }, "dynamic_id");

    }

    public static WriteOnlyMessage getString(DynamicData data) {
        return getString(data, true);
    }

    public static WriteOnlyMessage getString(DynamicData data, boolean withLink, String... additionWord) {
        WriteOnlyMessage message = new WriteOnlyMessage();
        if (withLink) {
            message.add("动态链接：\n").add("https://t.bilibili.com/").add(data.getDesc().getDynamicID()).newLine();
        }
        //获取动态解析器
        IAdapter adapter = getGsonInstance().fromJson(data.getCard(), getAdapter(data.getDesc().getType()));
        //添加用户名
        message.add(data.getDesc().getUserProfile().getInfo().getUserName());
        //添加操作的文本
        String actionText = data.getDisplayContent().getActionText();
        message.add(actionText == null || actionText.isEmpty() ? adapter.getActionText() : actionText).add("：").newLine().add(String.join("", additionWord));

        //解析提醒信息
        DisputeInfo disputeInfo = data.getRichMessageInfo().getDisputeInfo();
        if (disputeInfo != null && !disputeInfo.getContent().isEmpty()) {
            message.add("⚠").add(disputeInfo.getContent()).newLine();
        }

        //解析详细内容
        if (adapter instanceof IRepostAdapter) {
            message = ((IRepostAdapter) adapter).getContent(message, data);
        } else {
            message = adapter.getContent(message);
        }
        //解析表情
        message = data.getDisplayContent().getEmojiInfo().format(message);

        if (data.getRichMessageInfo().getLotteryInfo() != null) {
            //解析阿B的特殊字符
            message.replace("\u200B互动抽奖", "\uD83C\uDF81互动抽奖");
            lotteryInfoGetter.query(data.getDesc().getDynamicID());
            message.add(lotteryInfoGetter.getData().toWriteOnlyMessage());
        }

        //解析投票信息
        VoteInfo voteInfo = data.getExtension().getVoteInfo();
        if (voteInfo != null) {
            //解析阿B的特殊字符
            message.replace("\u200B" + voteInfo.getTitle(), "\ud83d\udcca" + voteInfo.getTitle());
            //转发的动态会重复解析，因此需要避免
            if (!data.getDesc().isRepost()) {
                message.add(voteInfo.toString());
            }
        }
        //移除多余的特殊字符
        message.replace("\u200B", "").replace("\u200D", "");
        return message;
    }

    public static Class<? extends IAdapter> getAdapter(int type) {
        Class<? extends IAdapter> adapterClass = UnknownAdapter.class;
        switch (type) {
            case 1:
                adapterClass = RepostAdapter.class;
                break;
            case 2:
                adapterClass = TextWithPicAdapter.class;
                break;
            case 4:
                adapterClass = PlainTextAdapter.class;
                break;
            case 8:
                adapterClass = VideoAdapter.class;
                break;
            case 16:
                adapterClass = MiniVideoAdapter.class;
                break;
            case 64:
                adapterClass = ArticleAdapter.class;
                break;
            case 256:
                adapterClass = MusicAdapter.class;
                break;
            case 2048:
                adapterClass = ShareAdapter.class;
                break;
            case 4308:
                adapterClass = StreamingAdapter.class;
                break;
            case 4098:
            case 4099:
            case 512:
                adapterClass = FilmAdapter.class;
                break;
            case 4200:
                adapterClass = LiveRoomAdapter.class;
                break;
        }
        return adapterClass;
    }
}
