package io.github.serfom256.benchmark.model.om;

import java.util.ArrayList;
import java.util.List;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Joined;
import io.github.serfom256.annotations.entity.Model;

@Model(primaryKey = "id")
public class TicketOM {

    @Field(name = "id")
    private String id;

    @Field(name = "title")
    private String title;

    @Joined(joinBy = "id", to = "ticketId", saveProcedure = "insertTicketComment")
    private List<TicketCommentOM> comments = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TicketCommentOM> getComments() {
        return comments;
    }

    public void setComments(List<TicketCommentOM> comments) {
        this.comments = comments;
    }
}
