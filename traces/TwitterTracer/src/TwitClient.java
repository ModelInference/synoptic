import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map.Entry;

import com.google.protobuf.Descriptors.FieldDescriptor;

import Tracer;
import MessageTrace.FullTrace;
import MessageTrace.TraceSet;
import MessageTrace.WrappedMessage;
import net.unto.twitter.Api;
import net.unto.twitter.TwitterProtos.RateLimitStatus;
import net.unto.twitter.TwitterProtos.Relationship;
import net.unto.twitter.TwitterProtos.Results;
import net.unto.twitter.TwitterProtos.Status;
import net.unto.twitter.TwitterProtos.Trends;
import net.unto.twitter.TwitterProtos.User;
import net.unto.twitter.TwitterProtos.Results.Result;
import net.unto.twitter.TwitterProtos.Trends.Trend;
import net.unto.twitter.UtilProtos.Url;

public class TwitClient {
    public static final String FILE_NAME = "TwitTrace2";

    public static void main(String[] args) throws Exception {
        Date current = new Date();
        User user;
        Tracer.create();

        Api api = Api.builder().username("test503p").password("503ptest")
                .build();
        for (Status status : api.publicTimeline().build().get()) {
            System.out.println(String.format("%s wrote '%s'", status.getUser()
                    .getName(), status.getText()));
            // Date current = new Date();
            Tracer
                    .log(1, 0, current.getTime(), "Status", status
                            .toByteString());
        }

        Tracer.newTrace();
        for (Status status : api.replies().build().get()) {
            // Date current = new Date();
            Tracer
                    .log(1, 0, current.getTime(), "Status", status
                            .toByteString());
        }

        Tracer.newTrace();
        Relationship relationship = api.showFriendships().targetScreenName(
                "wired").build().get();
        // Date current = new Date();
        Tracer.log(1, 0, current.getTime(), "Relationship", relationship
                .toByteString());

        Tracer.newTrace();
        Status status = api.updateStatus("Hello Twitter").build().post();
        Tracer.log(1, 0, current.getTime(), "Status", status.toByteString());

        Tracer.newTrace();
        for (Status s : api.userTimeline().build().get()) {
            Tracer.log(1, 0, current.getTime(), "Status", s.toByteString());
        }

        Tracer.newTrace();
        user = api.showUser().id("test503p").build().get();
        Tracer.log(1, 0, current.getTime(), "User", user.toByteString());

        Tracer.newTrace();
        Trends trends = api.trends().build().get();
        for (Trend trend : trends.getTrendsList()) {
            Tracer.log(1, 0, current.getTime(), "Trend", trend.toByteString());
        }

        Tracer.newTrace();
        Results results = api.search("obama").build().get();
        for (Result result : results.getResultsList()) {
            System.out.println(result.getText());
            Tracer
                    .log(1, 0, current.getTime(), "Result", result
                            .toByteString());
        }

        Tracer.newTrace();
        RateLimitStatus rateLimitStatus = api.rateLimitStatus().build().get();
        System.out.println(rateLimitStatus.getRemainingHits());
        Tracer.log(1, 0, current.getTime(), "RateLimitStatus", rateLimitStatus
                .toByteString());

        Tracer.newTrace();
        user = api.updateProfile().name("New Name").build().post();
        Tracer.log(1, 0, current.getTime(), "User", user.toByteString());

        Tracer.newTrace();
        User leaving = api.leave("wired").build().post();
        Tracer.log(1, 0, current.getTime(), "User", leaving.toByteString());

        Tracer.newTrace();
        User following = api.follow("wired").build().post();
        Tracer.log(1, 0, current.getTime(), "User", following.toByteString());

        Tracer.newTrace();
        for (Status s : api.friendsTimeline().build().get()) {
            Tracer.log(1, 0, current.getTime(), "Status", s.toByteString());
        }

        Tracer.newTrace();
        TraceSet result = Tracer.completeSet();

        FileOutputStream output = new FileOutputStream(FILE_NAME + ".trace");
        result.writeTo(output);
        output.close();

        PrintWriter txtOut = new PrintWriter(new FileOutputStream(FILE_NAME
                + ".txt"));
        for (FullTrace t : result.getFullTraceList()) {
            for (WrappedMessage m : t.getWrappedMessageList()) {

                boolean first = true;
                for (Entry<FieldDescriptor, Object> field : m.getAllFields()
                        .entrySet()) {
                    if (first)
                        first = false;
                    else
                        txtOut.print(", ");

                    if (field.getValue().equals("Url get")
                            || field.getValue().equals("Url post")) {
                        Url p = Url.parseFrom(m.getTheMessage());
                        txtOut.print(field.getKey().getName() + " : "
                                + p.getPath());
                    } else {
                        txtOut.print(field.getKey().getName() + " : "
                                + field.getValue());
                    }
                }
                txtOut.println();

                /*
                 * If accessing message contents is necessary (need more than
                 * meta info) then use code like the following: String msgType =
                 * m.getType(); if(msgType.equals("Prepare")){ Prepare p =
                 * Prepare.parseFrom(m.getTheMessage()); p.getWhateverField()...
                 * }
                 */
            }
            txtOut.println();
            txtOut.println();
        }
        txtOut.close();
    }
}
