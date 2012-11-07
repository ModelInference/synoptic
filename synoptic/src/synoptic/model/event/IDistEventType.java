package synoptic.model.event;

import synoptic.model.channelid.ChannelId;

public interface IDistEventType {

    public int getPid();

    public ChannelId getChannelId();

    public boolean isCommEvent();

    public boolean isRecvEvent();

    public boolean isSendEvent();

    public boolean isSynthSendEvent();

    public boolean isLocalEvent();

    public String getScmEventString();

    public String getScmEventFullString();

    public String toDotString();

    public String toString(String cidString, char separator);

    // ///////////////////////////////////////////////////////////////////////

    public String getEType();
}
