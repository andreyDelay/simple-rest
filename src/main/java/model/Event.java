package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "events")
@JsonInclude(JsonInclude.Include.NON_NULL)
@SqlResultSetMapping(name = "getEventsByUserId", entities = {
        @EntityResult(entityClass = Event.class, fields = {
                @FieldResult(name = "id", column = "event_id"),
                @FieldResult(name = "name", column = "event_name"),
                @FieldResult(name = "eventDate", column = "event_date"),
                @FieldResult(name = "user", column = "user_id")
        })
},classes = {
        @ConstructorResult(
                targetClass = User.class,
                columns = {
                        @ColumnResult(name = "user_id", type = Long.class),
                        @ColumnResult(name = "name", type = String.class),
                        @ColumnResult(name = "surname", type = String.class),})
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;
    @Column(name = "event_name")
    private String name;
    @Column(name = "event_date")
    private Date eventDate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Event() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
