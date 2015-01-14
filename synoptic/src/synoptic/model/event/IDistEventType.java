package synoptic.model.event;

import synoptic.model.channelid.ChannelId;

/**
 * Skeleton interface for an event in a distributed system. The event is:
 * 
 * <pre>
 * 1. associated with some process pid
 * 2. associated with a channel id
 * 3. can be a receive/send/local event
 * 4. can be a synthetic event, used by the McScM model checker
 * 5. has a variety of string representations
 * </pre>
 */
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

    public String getEType();
}
